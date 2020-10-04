#include <stdio.h>
#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <string>
#include <vector>
#include <list>
#include <forward_list>
#include <unordered_set>
#include <unordered_map>
#include <memory>
#include <atomic>
#include <future>
#include <mutex>
#include <condition_variable>
#include <cassert>
#include <thread>
#include <chrono>

//#define __TRACE_GC 1

#define ENABLE_GC 1
//#define DUMMY_ALLOCATOR 1

#define __GC_ALIGNMENT_SIZE 64
#define __GC_ALIGNMENT_MASK (__GC_ALIGNMENT_SIZE - 1)

struct __GCVisitor;
struct __GCHeap;

template <typename T> T GC_roundUp(T numToRound, T multiple) {
	if (multiple == 0) return numToRound;
	int remainder = numToRound % multiple;
	if (remainder == 0) return numToRound;
	return numToRound + multiple - remainder;
}

#ifdef DUMMY_ALLOCATOR
	int64_t BIG_HEAP_SIZE = (3LL * 1024LL * 1024LL * 1024LL) - 16;
	char *BIG_HEAP = (char *)malloc(BIG_HEAP_SIZE + 1024);
	char *BIG_HEAP_END = BIG_HEAP + BIG_HEAP_SIZE;

	void *jtalloc(size_t size) {
		//return malloc(size);
		auto out = BIG_HEAP;
		if (out + size >= BIG_HEAP_END) {
			std::cout << "ERROR ALLOCATING" << out << " > " << BIG_HEAP_END << "\n";
			abort();
		}
		BIG_HEAP += GC_roundUp(size, (size_t)128);
		memset(out, 0, size);
		return out;
	}
	void jtfree(void *ptr) {
		//free(ptr);
	}
#else
	void *jtalloc(size_t size) { return malloc(size); }
	void jtfree(void *ptr) { free(ptr); }
#endif

#define GC_OBJECT_CONSTANT 0x139912F0

struct __GC {
    int _gcallocated = GC_OBJECT_CONSTANT;
    __GC *next = nullptr;
    unsigned short markVersion = 0;
    unsigned short liveCount = 0;
    int __GC_objsize;

	virtual std::wstring __GC_Name() { return L"__GC"; }
    virtual void __GC_Trace(__GCVisitor* visitor) {}

    __GC() {
    }

    virtual ~__GC() {
       //__GC_Delete();
    }

    virtual void __GC_Init(__GCHeap *heap) {
		#if __TRACE_GC
		std::wcout << L"Created " << __GC_Name() << " - " << this << L"\n";
		#endif
    }

    virtual void __GC_Dispose(__GCHeap *heap) {
		#if __TRACE_GC
		std::wcout << L"Deleted " << __GC_Name() << L" - " << this << L"\n";
		#endif
    }
};

template <typename T>
struct __GCBaseMember {
    __GCBaseMember() { this->raw_ = nullptr; }
    __GCBaseMember(const T* x) { this->raw_ = (T *)x; }
    __GCBaseMember& operator= (const T* x) { return *this; }
    operator T*() const { return raw_; }
    mutable T* raw_ = nullptr;
};

template <typename T>
struct __GCMember : public __GCBaseMember<T> {
    __GCMember() : __GCBaseMember<T>() {}
    __GCMember(const T* x) : __GCBaseMember<T>(x) {}
};

template <typename T>
struct __GCWeakMember : public __GCBaseMember<T> {
    __GCWeakMember() : __GCBaseMember<T>() {}
    __GCWeakMember(const T* x) : __GCBaseMember<T>(x) {}
};

struct __GCVisitor {
    int version = 1;

    template <typename T>
    void Trace(const __GCMember<T>& member) {
        Trace((__GC *)(T *)member);
    }

    virtual void Trace(__GC *obj) {
        if (obj == nullptr) return;
        if (obj->markVersion == version) return;
        obj->markVersion = version;
        obj->__GC_Trace(this);
        //std::cout << "Visitor.Trace: " << obj;
    }
};

struct __GCStack {
    void **start = nullptr;
    std::mutex mutex;
    std::mutex mutex2;
    std::condition_variable cv;
    std::condition_variable cv2;
    std::promise<int> locked;
    __GCStack(void **start) : start(start) { }
};

struct __GCRootInfo {
	std::string name;
	__GC** root;
};

struct __GCSweepResult {
	int explored;
	int deleted;
};

struct __GCMemoryChunk {
	void *start;
	int size;
};

struct __GCMemoryBlock {
	void *__ptr;
	void *start;
	void *end;
	int sizeTotal;
	int sizeUsed;
	__GCMemoryBlock(int size) {
		this->__ptr = jtalloc(size);
		this->start = (void *)GC_roundUp((uintptr_t)this->__ptr, (uintptr_t)64);
		this->sizeTotal = size;
		this->end = ((char *)this->start) + size;
		this->sizeUsed = 0;
	}
	~__GCMemoryBlock() {
		jtfree(this->__ptr);
		this->__ptr = nullptr;
		this->start = nullptr;
		this->end = nullptr;
		this->sizeTotal = 0;
		this->sizeUsed = 0;
	}
	void *Alloc(int size) {
		if (sizeUsed + size >= sizeTotal) return nullptr;
		auto output = (((char *)start) + sizeUsed);
		sizeUsed += size;
		return output;
	}
	int sizeFree() {
		return sizeTotal - sizeUsed;
	}
	bool ContainsPointer(void *ptr) {
		return ptr >= start && ptr < end;
	}
};

struct __GCAllocResult {
	void *ptr;
	int size;
};

struct __GCMemoryBlocks {
	void *minptr = nullptr;
	void *maxptr = nullptr;
	std::vector<__GCMemoryBlock*> blocks;
	std::vector<__GC*> freeBlocksBySize128;
	std::vector<__GC*> freeBlocksBySize64;
	std::unordered_map<int, std::vector<__GC*>> freeBlocksBySize;
	int allocCount = 0;
	int allocMemory = 0;
	int totalMemory = 0;

	__GCMemoryBlocks() {
		freeBlocksBySize128.reserve(1024);
		freeBlocksBySize64.reserve(1024);
	}

	std::vector<__GC*> *getFreeBlocksBySizeArray(int allocSize) {
		if (allocSize == 128) return &freeBlocksBySize128;
		if (allocSize == 64) return &freeBlocksBySize64;
		//std::cout << allocSize << "\n";
		return &freeBlocksBySize[allocSize];
	}

	__GCAllocResult Alloc(int size) {
		int allocSize = GC_roundUp(size, __GC_ALIGNMENT_SIZE);
		allocMemory += allocSize;
		allocCount++;
		auto fblocks = getFreeBlocksBySizeArray(allocSize);
		if (!fblocks->empty()) {
			auto v = fblocks->back();
			fblocks->pop_back();
			return { v, allocSize };
		}
		for (auto block : blocks) {
			auto ptr = block->Alloc(allocSize);
			if (ptr != nullptr) return { ptr, allocSize };
		}
		int blockSize = 16 * 1024 * 1024;
		auto block = new __GCMemoryBlock(blockSize);
		minptr = (minptr != nullptr) ? std::min(minptr, block->start) : block->start;
		maxptr = (maxptr != nullptr) ? std::max(maxptr, block->end) : block->end;
		totalMemory += blockSize;
		blocks.push_back(block);
		return { block->Alloc(allocSize), allocSize };
	}

	void Free(__GC *ptr) {
		int allocSize = ptr->__GC_objsize;
		allocCount--;
		allocMemory -= allocSize;
		getFreeBlocksBySizeArray(allocSize)->push_back(ptr);
	}

	inline bool PreContainsPointerFast(void *ptr) {
		return (ptr > (void *)0x10000) && (((uintptr_t)ptr & __GC_ALIGNMENT_MASK) == 0);
	}

	bool ContainsPointer(void *ptr) {
		if (ptr < minptr || ptr > maxptr) return false;

		for (auto block : blocks) {
			if (block->ContainsPointer(ptr)) return true;
		}
		//std::cout << "Can't find " << ptr << " in blocks " << blocks.size() << ", range=" << minptr << "," << maxptr << "\n";
		return false;
	}
};

struct __GCHeap {
    int allocatedArraySize = 0;
    __GCMemoryBlocks memory;
    //std::list<__GC*> allocated_gen1;
    std::unordered_set<__GCRootInfo*> roots;
    std::unordered_map<std::thread::id, __GCStack*> threads_to_stacks;
    __GC* head_gen1 = nullptr;
    __GC* head_gen2 = nullptr;
    __GC* head_delete = nullptr;
    __GCVisitor visitor;
    std::atomic<bool> sweepingStop;
    //int gcCountThresold = 100000;
    //int gcCountThresold = 10000;
    int gcCountThresold = 1000;
    //int gcCountThresold = 10;
    int gcSizeThresold = 4 * 1024 * 1024;
    bool enabled = true;

    int GetTotalBytes() {
    	return memory.allocMemory + allocatedArraySize;
    }

    // @TODO
    int GetFreeBytes() {
    	return 16 * 1024 * 1024;
    }

    void ShowStats() {
        int totalSize = GetTotalBytes();
        std::wcout << L"Heap Stats. "
        	<< L"Object Count: " << memory.allocCount
        	<< L", TotalMemory: " << memory.totalMemory
        	<< L", TotalSize: " << totalSize
        	<< L", ObjectSize: " << memory.allocMemory
        	<< L", ArraySize: " << allocatedArraySize << L"\n";
		fflush(stdout);
    }

    __GCRootInfo * AddRoot(std::string name, __GC** root) {
		__GCRootInfo *info = new __GCRootInfo { name, root };
		roots.insert(info);
		return info;
    }

    __GCRootInfo * AddRoot(__GC** root) {
    	return AddRoot("unknown", root);
    }

    void RemoveRoot(__GCRootInfo * root) {
        roots.erase(root);
    }

    void RegisterCurrentThread() {
        void *ptr = nullptr;
        RegisterThreadInternal(new __GCStack(&ptr));
    }

    void RegisterCurrentThread(void **ptr) {
        RegisterThreadInternal(new __GCStack(ptr));
    }

    void UnregisterCurrentThread() {
        auto current_thread_id = std::this_thread::get_id();
        #if __TRACE_GC
        std::cout << "UnregisterCurrentThread:" << current_thread_id << "\n";
        #endif
        threads_to_stacks.erase(current_thread_id);
    }

    // Call at some points to perform a stop-the-world
    void CheckCurrentThread() {
        if (sweepingStop) {
            auto stack = threads_to_stacks[std::this_thread::get_id()];
            stack->cv.notify_all();
            std::unique_lock<std::mutex> lk(stack->mutex2);
            stack->cv2.wait(lk);
            //stack->cv.wait(stack->mutex);
        }
    }

    void RegisterThreadInternal(__GCStack *stack) {
        auto current_thread_id = std::this_thread::get_id();
        #if __TRACE_GC
        std::cout << "RegisterThreadInternal:" << current_thread_id << "\n";
        #endif
        threads_to_stacks[current_thread_id] = stack;
    }
    

    void Mark() {
		#if __TRACE_GC
		std::cout << "__GCHeap.Mark(): roots=" << roots.size() << ", stacks=" << threads_to_stacks.size() << "\n";
		#endif
        visitor.version++;
        for (auto rootInfo : roots)  {
            auto rootPtr = rootInfo->root;
        	auto root = *rootPtr;
			#if __TRACE_GC
			std::cout << "rootName=" << rootInfo->name << ", rootPtr=" << rootPtr << ", root=" << root << "\n";
			#endif
            visitor.Trace(root);
        }
        #if __TRACE_GC
        std::cout << "threads_to_stacks.size(): " << threads_to_stacks.size() << "\n";
        #endif
        for (auto tstack : threads_to_stacks) {
            CheckStack(tstack.second);
        }
    }

    void SweepStart() {
		sweepingStop = true;
		auto currentThreadId = std::this_thread::get_id();
		for (auto tstack : threads_to_stacks) {
			if (tstack.first != currentThreadId) {
				std::unique_lock<std::mutex> lk(tstack.second->mutex);
				if (tstack.second->cv.wait_for(lk, std::chrono::seconds(10)) == std::cv_status::timeout) {
					std::cout << "Thread was locked for too much time. Aborting.\n";
					abort();
				}
			}
		}
    }

    void SweepEnd() {
		auto currentThreadId = std::this_thread::get_id();
		sweepingStop = false;
		for (auto tstack : threads_to_stacks) {
			if (tstack.first != currentThreadId) {
				tstack.second->cv2.notify_all();
			}
		}
    }

    __GCSweepResult SweepList(__GC* &head, __GC** next_head) {
    	int exploreCount = 0;
		int deleteCount = 0;
		int version = visitor.version;
		bool reset = version >= 10000;
		__GC* prev = nullptr;
		__GC* current = head;
		while (current != nullptr) {
			exploreCount++;
			if (current->markVersion != version) {
				deleteCount++;
				auto todelete = current;

				//std::cout << "delete unreferenced object!\n";
				if (prev != nullptr) {
					prev->next = current->next;
					current = prev;
				} else {
					//std::cout << "head=" << head << ", current=" << current << "\n";
					assert(head == current);
					head = current->next;
				}
				current = current->next;

				todelete->_gcallocated = 0;
				todelete->__GC_Dispose(this);
				memory.Free(todelete);

				//todelete->next = head_delete;
				//head_delete = todelete;

				//delete todelete;
			} else {
				if (reset) {
				   current->markVersion = 0;
				}
				if (current->liveCount < 10) {
					current->liveCount++;
					if (current->liveCount >= 10) {
						if (next_head != nullptr) {
							auto tomove = current;
							if (prev != nullptr) {
								prev->next = current->next;
								current = prev;
							} else {
								assert(head == current);
								head = current->next;
							}
							current = current->next;

							tomove->next = *next_head;
							*next_head = tomove;
							continue;
						}
					}
				}

				prev = current;
				current = current->next;
			}
		}

		if (reset) {
			version = 0;
		}
		prev = nullptr;
		current = nullptr;
		return { exploreCount, deleteCount };
    }

    __GCSweepResult SweepPartial() {
		SweepStart();
		auto results = SweepList(head_gen1, &head_gen2);
        SweepEnd();
        return results;
    }

    __GCSweepResult SweepFull() {
		SweepStart();
		auto results1 = SweepList(head_gen1, &head_gen2);
		auto results2 = SweepList(head_gen2, nullptr);
		SweepEnd();
		return { results1.explored + results2.explored, results1.deleted + results2.deleted };
	}

    void CheckStack(__GCStack *stack) {
        auto start = stack->start;
        void *value = nullptr;
        void **end = &value;
        #if __TRACE_GC
        std::cout << "Checking stack: " << (start - end) << "\n";
        std::cout << "  - Memory: minptr=" << memory.minptr << ", maxptr=" << memory.maxptr << "\n";
        #endif

        for (void **ptr = end; ptr <= start; ptr++) {
            void *value = *ptr;
            if (!memory.PreContainsPointerFast(value)) continue;

			auto v = (__GC*)value;
			bool isptr = memory.ContainsPointer(v);
			bool isobj = isptr && v->_gcallocated == GC_OBJECT_CONSTANT;
			if (isobj) {
				visitor.Trace(v);
			}
			#if __TRACE_GC
			std::cout << "  - " << ptr << ": " << value << ": isptr=" << isptr << ", isobj=" << isobj << "\n";
			#endif
        }
    }

    int DeleteOldObjects() {
    	int count = 0;
    	/*
		while (head_delete != nullptr) {
			auto todelete = head_delete;
			head_delete = head_delete->next;
			todelete->__GC_Dispose(this);
			memory.Free(todelete);
			count++;
		}
		*/
		return count;
    }

    void GC(bool full = true) {
    	#ifdef ENABLE_GC
    		int roots_size = roots.size();
    		int threads_size = threads_to_stacks.size();
    		int allocatedCount = memory.allocCount;
    		int allocatedObjectSize = memory.allocMemory;
			auto t0 = std::chrono::steady_clock::now();
			Mark();
			auto t1 = std::chrono::steady_clock::now();
			auto sweepResult = full ? SweepFull() : SweepPartial();
			auto t2 = std::chrono::steady_clock::now();
			//auto deleteResult = full ? DeleteOldObjects() : 0;
			auto deleteResult = DeleteOldObjects();
			auto t3 = std::chrono::steady_clock::now();

			auto e1 = std::chrono::duration_cast<std::chrono::milliseconds>(t1 - t0).count();
			auto e2 = std::chrono::duration_cast<std::chrono::milliseconds>(t2 - t1).count();
			auto e3 = std::chrono::duration_cast<std::chrono::milliseconds>(t3 - t2).count();
    		#ifdef __TRACE_GC
				std::cout << "GC: full=" << full << ", "
					<< "mark=" << e1 << "ms, "
					<< "sweep=" << e2 << "ms/" << sweepResult.deleted << "/" << sweepResult.explored << ", "
					<< "delete=" << e3 << "ms/" << deleteResult << ", "
					<< "roots=" << roots_size << ", threads=" << threads_size
					<< ", totalObjects=" << allocatedCount << "/" << gcCountThresold
					<< ", heapSize=" << allocatedObjectSize << "/" << gcSizeThresold
					<< "\n";
			#endif
        #endif
    }

    void Enable() {
    	enabled = true;
    }

    void Disable() {
    	enabled = false;
    }

	template <typename T, typename... Args>
	T* AllocCustomSize(int size, Args&&... args) {
    	#ifdef ENABLE_GC
		if (enabled) {
			if (memory.allocCount >= gcCountThresold) {
				GC(false);
				if (memory.allocCount >= gcCountThresold * 0.5) GC(true);
				if (memory.allocCount >= gcCountThresold * 0.5) gcCountThresold *= 2;
			}

			if (memory.allocMemory >= gcSizeThresold) {
				GC(false);
				if (memory.allocMemory >= gcSizeThresold * 0.5) GC(true);
				if (memory.allocMemory >= gcSizeThresold * 0.5) gcSizeThresold *= 2;
			}
		}
		#endif

		auto res = memory.Alloc(size);
		int objsize = res.size;
		T *newobj = ::new (res.ptr) T(std::forward<Args>(args)...);
		//T *newobj = ::new T(std::forward<Args>(args)...);
    	#ifdef ENABLE_GC
		newobj->__GC_Init(this);
    	newobj->__GC_objsize = objsize;
    	newobj->_gcallocated = GC_OBJECT_CONSTANT;
        //allocated_gen1.push_back(newobj);
		newobj->next = (__GC*)this->head_gen1;
		this->head_gen1 = newobj;
		#endif
		return newobj;
	};

    template <typename T, typename... Args>
    T* Alloc(Args&&... args) {
    	return AllocCustomSize<T>(sizeof(T), std::forward<Args>(args)...);
    };
};

__GCHeap __gcHeap;

struct __GCThread {
    __GCThread(void **ptr) {
        __gcHeap.RegisterCurrentThread(ptr);
        #if __TRACE_GC
        std::cout << "GcThread\n";
        #endif
    }
    ~__GCThread() {
        __gcHeap.UnregisterCurrentThread();
        #if __TRACE_GC
        std::cout << "~GcThread\n";
        #endif
    }
};

#define __GC_REGISTER_THREAD() void *__current_gc_thread_base = nullptr; __GCThread __current_gc_thread(&__current_gc_thread_base);
#define __GC_GC __gcHeap.GC
#define __GC_SHOW_STATS __gcHeap.ShowStats
#define __GC_ALLOC __gcHeap.Alloc
#define __GC_ADD_ROOT(v) __gcHeap.AddRoot((__GC**)v);
//#define __GC_ADD_ROOT_NAMED(name, v) __gcHeap.AddRoot(name, (__GC**)v);
//#define __GC_ADD_ROOT_CONSTANT(name, v) __gcHeap.AddRoot(name, (__GC**)v);
#define __GC_ADD_ROOT_NAMED(name, v) __gcHeap.AddRoot((__GC**)v);
#define __GC_ADD_ROOT_CONSTANT(name, v) __gcHeap.AddRoot((__GC**)v);
#define __GC_ENABLE __gcHeap.Enable
#define __GC_DISABLE __gcHeap.Disable
#define GC_gcollect __GC_GC
#define GC_get_free_bytes __gcHeap.GetFreeBytes
#define GC_get_total_bytes __gcHeap.GetTotalBytes
#define GC_init_pre_thread __GC_REGISTER_THREAD
#define GC_init_thread() { }
#define GC_finish_thread() { }