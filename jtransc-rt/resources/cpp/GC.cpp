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

#define __GC_ALIGNMENT_SIZE 32
#define MAX_FAST_FREE_BLOCKS_BY_SIZES 16

struct __GCVisitor;
struct __GCHeap;

template <typename T> T GC_roundUp(T numToRound, T multiple) {
	if (multiple == 0) return numToRound;
	int remainder = numToRound % multiple;
	if (remainder == 0) return numToRound;
	return numToRound + multiple - remainder;
}

const unsigned char GC_OBJECT_CONSTANT = 0xF1;

struct __GC {
    __GC *next = nullptr;
    unsigned short markVersion = 0;
    unsigned short __GC_objsize;
    unsigned char _gcallocated = GC_OBJECT_CONSTANT;
    unsigned char liveCount = 0;

	virtual std::wstring __GC_Name() { return L"__GC"; }
    virtual void __GC_Trace(__GCVisitor* visitor) {}

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
		this->__ptr = malloc(size);
		this->start = (void *)GC_roundUp((uintptr_t)this->__ptr, (uintptr_t)__GC_ALIGNMENT_SIZE);
		this->sizeTotal = size;
		this->end = ((char *)this->start) + size;
		this->sizeUsed = 0;
	}
	~__GCMemoryBlock() {
		free(this->__ptr);
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

struct __GCArrayMemory {
	std::vector<__GCMemoryBlock*> blocks;
	__GCMemoryBlock* lastBlock = nullptr;
	std::vector<void*> freeBlocksBySizes[MAX_FAST_FREE_BLOCKS_BY_SIZES];
	int allocCount = 0;
	int allocMemory = 0;
	int totalMemory = 0;

	std::vector<void*> *getFreeBlocksBySizeArray(int allocSize) {
		int index = allocSize / __GC_ALIGNMENT_SIZE;
		if (index < MAX_FAST_FREE_BLOCKS_BY_SIZES) {
			return &freeBlocksBySizes[index];
		}
		return nullptr;
	}

	void *Alloc(int size) {
		int allocSize = GC_roundUp(size, __GC_ALIGNMENT_SIZE);
		auto fblocks = getFreeBlocksBySizeArray(allocSize);
		if (fblocks != nullptr) {
			allocMemory += allocSize;
			allocCount++;
			if (!fblocks->empty()) {
				auto v = fblocks->back();
				fblocks->pop_back();
				return v;
			}
			if (lastBlock != nullptr) {
				auto ptr = lastBlock->Alloc(allocSize);
				if (ptr != nullptr) return ptr;
			}
			for (auto block : blocks) {
				auto ptr = block->Alloc(allocSize);
				if (ptr != nullptr) return ptr;
			}
			int blockSize = std::max((int)allocSize, (int)((1 + blocks.size()) * 1024 * 1024));
			auto block = new __GCMemoryBlock(blockSize);
			totalMemory += blockSize;
			lastBlock = block;
			blocks.push_back(block);
			return block->Alloc(allocSize);
		} else {
			return malloc(allocSize);
		}
	}
	void Free(void *ptr, int size) {
		int allocSize = GC_roundUp(size, __GC_ALIGNMENT_SIZE);
		int index = allocSize / __GC_ALIGNMENT_SIZE;
		if (index < MAX_FAST_FREE_BLOCKS_BY_SIZES) {
			freeBlocksBySizes[index].push_back(ptr);
		} else {
			free(ptr);
		}
	}
};

thread_local __GCArrayMemory __gcArrayMemory;

struct __GCMemoryBlocks {
	void *minptr = nullptr;
	void *maxptr = nullptr;
	__GCMemoryBlock* lastBlock = nullptr;
	std::vector<__GCMemoryBlock*> blocks;
	std::vector<__GC*> freeBlocksBySizes[MAX_FAST_FREE_BLOCKS_BY_SIZES];
	int allocCount = 0;
	int allocMemory = 0;
	int totalMemory = 0;

	__GCMemoryBlocks() {
	}

	std::vector<__GC*> *getFreeBlocksBySizeArray(int allocSize) {
		int index = allocSize / __GC_ALIGNMENT_SIZE;
		if (index < MAX_FAST_FREE_BLOCKS_BY_SIZES) {
			return &freeBlocksBySizes[index];
		}
		//std::cout << allocSize << "\n";
		return nullptr;
	}

	__GCAllocResult Alloc(int size) {
		int allocSize = GC_roundUp(size, __GC_ALIGNMENT_SIZE);
		allocMemory += allocSize;
		allocCount++;
		auto fblocks = getFreeBlocksBySizeArray(allocSize);
		if (fblocks == nullptr) return { malloc(allocSize), allocSize };
		if (!fblocks->empty()) {
			auto v = fblocks->back();
			fblocks->pop_back();
			return { v, allocSize };
		}
		if (lastBlock != nullptr) {
			auto ptr = lastBlock->Alloc(allocSize);
			if (ptr != nullptr) return { ptr, allocSize };
		}
		for (auto block : blocks) {
			auto ptr = block->Alloc(allocSize);
			if (ptr != nullptr) return { ptr, allocSize };
		}
		int blockSize = std::max((int)allocSize, (int)((1 + blocks.size()) * 1024 * 1024));
		auto block = new __GCMemoryBlock(blockSize);
		minptr = (minptr != nullptr) ? std::min(minptr, block->start) : block->start;
		maxptr = (maxptr != nullptr) ? std::max(maxptr, block->end) : block->end;
		totalMemory += blockSize;
		lastBlock = block;
		blocks.push_back(block);
		return { block->Alloc(allocSize), allocSize };
	}

	void Free(__GC *ptr) {
		int allocSize = ptr->__GC_objsize * __GC_ALIGNMENT_SIZE;
		allocCount--;
		allocMemory -= allocSize;
		auto fblocks = getFreeBlocksBySizeArray(allocSize);
		if (fblocks != nullptr) {
			fblocks->push_back(ptr);
		} else {
			free(ptr);
		}
	}

	inline bool PreContainsPointerFast(void *ptr) {
		return (ptr > (void *)0x10000) && (((uintptr_t)ptr % __GC_ALIGNMENT_SIZE) == 0);
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
	__GCArrayMemory arrays;
    //std::list<__GC*> allocated_gen1;
    std::unordered_set<__GCRootInfo*> roots;
    std::unordered_map<std::thread::id, __GCStack*> threads_to_stacks;
    __GC* head_gen1 = nullptr;
    __GC* head_gen2 = nullptr;
    __GC* head_delete = nullptr;
    __GCVisitor visitor;
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

    __GCSweepResult SweepList(__GC* &head, __GC** next_head) {
    	int exploreCount = 0;
		int deleteCount = 0;
		int version = visitor.version;
		bool reset = version >= 1000000000;
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
		auto results = SweepList(head_gen1, &head_gen2);
        return results;
    }

    __GCSweepResult SweepFull() {
		auto results1 = SweepList(head_gen1, &head_gen2);
		auto results2 = SweepList(head_gen2, nullptr);
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
    	newobj->__GC_objsize = objsize / __GC_ALIGNMENT_SIZE;
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

thread_local __GCHeap __gcHeap;

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