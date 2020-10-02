#include <stdio.h>
#include <iostream>
#include <string>
#include <vector>
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

struct __GCVisitor;
struct __GCHeap;

struct __GC {
    __GC *next = nullptr;
    unsigned short markVersion = 0;

	virtual std::wstring __GC_Name() { return L"__GC"; }
    virtual int __GC_Size() { return sizeof(*this); }
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

struct __GCHeap {
    int allocatedObjectSize = 0;
    int allocatedArraySize = 0;
    int allocatedCount = 0;
    std::unordered_set<__GC*> allocated;
    std::unordered_set<__GCRootInfo*> roots;
    std::unordered_map<std::thread::id, __GCStack*> threads_to_stacks;
    __GC* head = nullptr;
    __GCVisitor visitor;
    std::atomic<bool> sweepingStop;
    //int gcCountThresold = 100000;
    int gcCountThresold = 10000;
    //int gcCountThresold = 1000;
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

    void Sweep() {
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

        int version = visitor.version;
        bool reset = version >= 10000;
        __GC* prev = nullptr;
        __GC* current = head;
        __GC* todelete = nullptr;
        while (current != nullptr) {
            if (current->markVersion != version) {
                todelete = current;
                allocated.erase(todelete);
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
                allocatedObjectSize -= todelete->__GC_Size();
                allocatedCount--;
                todelete->__GC_Dispose(this);
                delete todelete;
            } else {
                if (reset) {
                   current->markVersion = 0;
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
        todelete = nullptr;

        // Resume all threads
        {
            sweepingStop = false;
            for (auto tstack : threads_to_stacks) {
                if (tstack.first != currentThreadId) {
                    tstack.second->cv2.notify_all();
                }
            }
        }
    }

    void CheckStack(__GCStack *stack) {
        auto start = stack->start;
        void *value = nullptr;

        for (void **ptr = &value; ptr <= start; ptr++) {
            void *value = *ptr;
            if (value > (void *)0x10000) {
                if (allocated.find((__GC*)value) != allocated.end()) {
                    visitor.Trace((__GC*)value);
                }
                #if __TRACE_GC
                std::cout << ptr << ": " << value << "\n";
                #endif
            }
        }
    }

    void GC() {
        Mark();
        Sweep();
    }

    void Enable() {
    	enabled = true;
    }

    void Disable() {
    	enabled = false;
    }

    template <typename T, typename... Args>
    T* Alloc(Args&&... args) {
    	if (enabled) {
			if (this->allocatedCount >= gcCountThresold) GC();
			if (this->allocatedCount >= gcCountThresold) gcCountThresold *= 2;

			if (this->allocatedObjectSize >= gcSizeThresold) GC();
			if (this->allocatedObjectSize >= gcSizeThresold) gcSizeThresold *= 2;
		}

		const int size = sizeof(T);
        void *memory = malloc(size);
        T *newobj = ::new (memory) T(std::forward<Args>(args)...);
        newobj->__GC_Init(this);
        allocated.insert(newobj);
        newobj->next = (__GC*)this->head;
        this->allocatedObjectSize += size;
        this->allocatedCount++;
        this->head = newobj;
        return newobj;
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
