#include <stdio.h>
#include <iostream>
#include <string>
#include <vector>
#include <list>
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

struct __GCVisitor;
struct __GCHeap;

#ifdef DUMMY_ALLOCATOR
	int64_t BIG_HEAP_SIZE = (3LL * 1024LL * 1024LL * 1024LL) - 16;
	char *BIG_HEAP = (char *)malloc(BIG_HEAP_SIZE + 1024);
	char *BIG_HEAP_END = BIG_HEAP + BIG_HEAP_SIZE;

	template <typename T> T GC_roundUp(T numToRound, T multiple) {
		if (multiple == 0) return numToRound;
		int remainder = numToRound % multiple;
		if (remainder == 0) return numToRound;
		return numToRound + multiple - remainder;
	}

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

struct __GC {
    __GC *next = nullptr;
    unsigned short markVersion = 0;
    unsigned short liveCount = 0;
    int __GC_objsize = 0;
    int deleting = 0;

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

struct __GCHeap {
    int allocatedObjectSize = 0;
    int allocatedArraySize = 0;
    int allocatedCount = 0;
    std::unordered_set<__GC*> allocated;
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
    	return allocatedObjectSize + allocatedArraySize;
    }

    // @TODO
    int GetFreeBytes() {
    	return 16 * 1024 * 1024;
    }

    void ShowStats() {
        int totalSize = GetTotalBytes();
        std::wcout << L"Heap Stats. Object Count: " << allocatedCount << L", TotalSize: " << totalSize << L", ObjectSize: " << allocatedObjectSize << L", ArraySize: " << allocatedArraySize << L"\n";
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
	    auto allocated = &this->allocated;
	    auto allocatedObjectSize = this->allocatedObjectSize;
	    auto allocatedCount = this->allocatedCount;
		while (current != nullptr) {
			exploreCount++;
			if (current->markVersion != version) {
				deleteCount++;
				auto todelete = current;
				todelete->deleting = 1;
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
				allocatedObjectSize -= todelete->__GC_objsize;
				allocatedCount--;

				todelete->next = head_delete;
				head_delete = todelete;

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
	    this->allocatedObjectSize = allocatedObjectSize;
	    this->allocatedCount = allocatedCount;

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
		auto results = SweepList(head_gen2, nullptr);
		SweepEnd();
		return results;
	}

    void CheckStack(__GCStack *stack) {
        auto start = stack->start;
        void *value = nullptr;
        void **end = &value;
        #if __TRACE_GC
        std::cout << "Checking stack: " << (start - end) << "\n";
        #endif

        for (void **ptr = end; ptr <= start; ptr++) {
            void *value = *ptr;
            if (value > (void *)0x10000) {
            	auto v = (__GC*)value;
                if (allocated.find(v) != allocated.end()) {
                	if (!v->deleting) {
                    	visitor.Trace(v);
					}
                }
                #if __TRACE_GC
                std::cout << ptr << ": " << value << "\n";
                #endif
            }
        }
    }

    int DeleteOldObjects() {
    	int count = 0;
		while (head_delete != nullptr) {
			auto todelete = head_delete;
			head_delete = head_delete->next;
			allocated.erase(todelete);
			todelete->__GC_Dispose(this);
			jtfree(todelete);
			count++;
		}
		return count;
    }

    void GC(bool full = false) {
    	#ifdef ENABLE_GC
    		int roots_size = roots.size();
    		int threads_size = threads_to_stacks.size();
    		int allocated_count = this->allocatedCount;
    		int allocated_objsize = this->allocatedObjectSize;
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
					<< ", totalObjects=" << allocated_count << "/" << gcCountThresold
					<< ", heapSize=" << allocated_objsize << "/" << gcSizeThresold
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
			if (this->allocatedCount >= gcCountThresold * 0.75) {
				GC(this->allocatedCount >= gcCountThresold);
				if (this->allocatedCount >= gcCountThresold * 0.5) gcCountThresold *= 2;
			}

			if (this->allocatedObjectSize >= gcSizeThresold * 0.75) {
				GC(this->allocatedObjectSize >= gcSizeThresold);
				if (this->allocatedObjectSize >= gcSizeThresold * 0.5) gcSizeThresold *= 2;
			}
		}
		#endif

		void *memory = jtalloc(size);
		T *newobj = ::new (memory) T(std::forward<Args>(args)...);
		//T *newobj = ::new T(std::forward<Args>(args)...);
    	#ifdef ENABLE_GC
		newobj->__GC_Init(this);
        newobj->__GC_objsize = size;
		allocated.insert(newobj);
        //allocated_gen1.push_back(newobj);
		this->allocatedObjectSize += size;
		this->allocatedCount++;
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
