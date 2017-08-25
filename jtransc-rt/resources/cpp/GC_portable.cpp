#include <map>
#include <set>
#include <unordered_map>
#include <algorithm>
#include <cstring>
#include <iostream>

enum PointersType { NONE, ALL, MAP };

#undef max
#undef min

//#define GC_TRACE 1

struct HeapEntry {
	uintptr_t start;
	uintptr_t end;
	PointersType type;
	uintptr_t pointersCount;
	uintptr_t* pointers;

	HeapEntry(
		uintptr_t start,
		uintptr_t end,
		PointersType type,
		uintptr_t pointersCount,
		uintptr_t* pointers
	) : start(start), end(end), type(type), pointersCount(pointersCount), pointers(pointers) {
	}

	void *ptr() { return (void *)start; }
	int size() { return end - start; }
	bool contains(uintptr_t ptr) { return ptr >= start && ptr < end; }
};

//template<TKey, TValue>
//struct RangeMap {
//};

struct GC {
	std::map<uintptr_t, HeapEntry*> roots;
	std::map<uintptr_t, HeapEntry*> entriesYoung; // Generational GC
	std::map<uintptr_t, HeapEntry*> entriesOld;
	std::map<uintptr_t, HeapEntry*> entries;
	//thread_local uintptr_t stackHigh;
	uintptr_t stackHigh;
	uintptr_t heapLow;
	uintptr_t heapHigh;
	uintptr_t allocatedBytes;
	uintptr_t allocatedBytesSinceGC;

	GC() {
		heapLow = UINTPTR_MAX;
		heapHigh = 0;
		allocatedBytes = 0;
		allocatedBytesSinceGC = 0;
	}

	void clearRoots() {
		roots.clear();
	}

	void _addRange(uintptr_t low, uintptr_t high) {
		this->heapLow = std::min(this->heapLow, low);
		this->heapHigh = std::max(this->heapHigh, high);
	}

	bool isPointer(uintptr_t ptr) {
		if (ptr == 0) return false;
		return (ptr >= this->heapLow && ptr < this->heapHigh);
	}

	void addRoot(uintptr_t low, uintptr_t high) {
		roots[low] = new HeapEntry(low, high, PointersType::ALL, 0, nullptr);
		_addRange(low, high);
	}

	void addRootSingle(void** ptrPtr) {
		addRoot((uintptr_t)ptrPtr, (uintptr_t)ptrPtr + sizeof(uintptr_t));
	}

	HeapEntry* allocateEntry(int sizeInBytes, PointersType pointersType, uintptr_t pointersCount, uintptr_t* pointers) {
		// 16 MB
		if (allocatedBytesSinceGC >= 16 * 1024 * 1024) collectSmall();
		//collectSmall();
		uintptr_t ptr = 0;
		int retries = 0;
	retry:;
		ptr = (uintptr_t)malloc(sizeInBytes);
		::memset((void*)ptr, 0, sizeInBytes);
		if (ptr == 0) {
			if (retries >= 1) {
				outOfMemory();
			}
			collect();
			retries++;
			goto retry;
		}
		auto e = new HeapEntry(
			(uintptr_t)ptr,
			(uintptr_t)ptr + sizeInBytes,
			pointersType,
			pointersCount,
			pointers
		);
#ifdef GC_TRACE
		printf("ALLOCATED: %p(%d)\n", e->ptr(), e->size());
#endif
		_addRange(e->start, e->end);
		entriesYoung[ptr] = e;
		entries[ptr] = e;
		allocatedBytes += sizeInBytes;
		allocatedBytesSinceGC += sizeInBytes;
		return e;
	}

	void freeEntry(HeapEntry* entry) {
		if (entry != nullptr) {
#ifdef GC_TRACE
			printf("FREE: %p\n", entry->ptr());
#endif

			this->entriesYoung.erase(entry->start);
			this->entriesOld.erase(entry->start);
			this->entries.erase(entry->start);
			allocatedBytes -= entry->size();
			::memset((void*)(entry->ptr()), 0, entry->size());
			::free(entry->ptr());
			::free(entry);
		}
	}

	void free(void* ptr) {
		freeEntry(this->get(ptr));
	}

	void outOfMemory() {
		abort();
	}

	HeapEntry* allocateWithoutPointers(int sizeInBytes) {
		return allocateEntry(sizeInBytes, PointersType::NONE, 0, nullptr);
	}

	HeapEntry* allocatePointerArray(int sizeInBytes) {
		return allocateEntry(sizeInBytes, PointersType::ALL, 0, nullptr);
	}

	HeapEntry* get(uintptr_t ptr) {
		auto entry = entries[(uintptr_t)ptr];
		if (entry == nullptr) {
			// SLOW for inner pointers
			for (auto z : entries) {
				auto e2 = z.second;
				if (e2 != nullptr) {
					if (e2->contains(ptr)) return e2;
				}
			}

			auto bb = entries.lower_bound((uintptr_t)ptr);
			//auto bb = entries.upper_bound((uintptr_t)ptr);
			//auto bb = entries.lower_bound((uintptr_t)ptr);
			//bb++;
			if (bb == entries.end()) return nullptr;
			//printf("[A] %p\n", bb); fflush(stdout);
			auto c = *bb;
			//printf("[B]\n"); fflush(stdout);
			auto e2 = c.second;
			if (e2 != nullptr) {
				//printf("%p-%p :: %p\n", e2->start, e2->end, ptr);
				if (e2->contains(ptr)) return e2;
			}
		}
		return entry;
	}

	HeapEntry* get(void* ptr) {
		return get((uintptr_t)ptr);
	}

	uintptr_t getCurrentStack() {
		uintptr_t stackTop = 0;
		return (uintptr_t)(void *)&stackTop;
	}

	void init() {
		this->stackHigh = getCurrentStack() + sizeof(uintptr_t);
	}

	void init(void *stackHigh) {
		this->stackHigh = (uintptr_t)stackHigh;
	}

	std::set<HeapEntry*> marked;

	bool markContains(HeapEntry* entry) {
		return marked.find(entry) != marked.end();
	}

	void markReset() {
		marked.clear();
	}

	void markEntry(HeapEntry* entry) {
		if (entry == nullptr || markContains(entry)) return;
#ifdef GC_TRACE
		printf("MARKED: %p\n", entry->ptr());
#endif
		marked.insert(entry);
		switch (entry->type) {
		case PointersType::NONE: {
			break;
		}
		case PointersType::ALL: {
			markRange(entry->start, entry->end);
			break;
		}
		case PointersType::MAP: {
			int pointersCount = entry->pointersCount;
			for (int n = 0; n < pointersCount; n++) {
				uintptr_t ptrPtr = entry->start + entry->pointers[n];
				uintptr_t ptr = *(uintptr_t *)ptrPtr;

#ifdef GC_TRACE
				printf("MARKED_MAP: %p offset:%d - %p\n", entry->ptr(), entry->pointers[n], ptr);
#endif
				markPtr(ptr);
			}
			break;
		}
		}
	}

	void markPtr(uintptr_t ptr) {
		markEntry(this->get(ptr));
	}

	void markRange(uintptr_t low, uintptr_t high) {
		for (uintptr_t s = low; s < high; s += sizeof(uintptr_t)) {
			uintptr_t ptr = *(uintptr_t *)s;
			bool isptr = isPointer(ptr);
			//printf("%p: %p %p, %p : %d\n", s, ptr, heapLow, heapHigh, isptr ? 1 : 0);
			if (isptr) markPtr(ptr);
		}
	}

	void markStack() {
		auto stackLow = getCurrentStack();
#ifdef GC_TRACE
		printf("STACK(%p-%p)\n", (void *)stackLow, (void *)stackHigh);
#endif
		markRange(stackLow, stackHigh);
	}

	void markRoots() {
		for (auto ee : this->roots) {
			auto e = ee.second;
			markRange(e->start, e->end);
		}
	}

	void sweep(bool collectOld) {
		std::set<HeapEntry*> collect;
		std::set<HeapEntry*> promote;

		for (auto ee : this->entriesYoung) {
			auto e = ee.second;
			if (!markContains(e)) {
				collect.insert(e);
			}
			else {
				promote.insert(e);
			}
		}

		if (collectOld) {
			for (auto ee : this->entriesOld) {
				auto e = ee.second;
				if (!markContains(e)) {
					collect.insert(e);
				}
			}
		}

		// Entry to old
		this->entriesYoung.clear();
		for (auto ee : promote) {
			this->entriesOld[ee->start] = ee;
		}

		for (auto ee : collect) {
			freeEntry(ee);
		}

		allocatedBytesSinceGC = 0;
	}

	void collectBase(bool collectOld) {
		markReset();
		markStack();
		markRoots();
		sweep(collectOld);
		//for (auto root : roots) root.second.
		//printf("%p, %p\n", stackHigh, stackBottom);
	}

	void collectSmall() {
		collectBase(false);
	}

	void collect() {
		collectBase(true);
	}
};

static GC __gc;

void GC_gcollect() {
	__gc.collect();
}

#define GC_INIT() { int v; __gc.init(&v); }
#define GC_MALLOC(size) __gc.allocatePointerArray(size)->ptr()
#define GC_MALLOC_ATOMIC(size) __gc.allocateWithoutPointers(size)->ptr()
#define GC_MALLOC_PRECISE(size, pointersCount, pointers) __gc.allocateEntry(size, PointersType::MAP, pointersCount, pointers)->ptr()
#define GC_COLLECT_SMALL() __gc.collectSmall();
#define GC_COLLECT() __gc.collect();
#define GC_ADD_ROOT(low, high) __gc.addRoot(low, high)
#define GC_ADD_ROOT_SINGLE(ptr) __gc.addRootSingle((void **)(void *)ptr)

void GC_set_no_dls(int v) {
}
void GC_set_dont_precollect(int v) {
}

void GC_clear_roots() {
	__gc.clearRoots();
}

void GC_add_roots(void *start, void *end) {
	__gc.addRoot((uintptr_t)start, (uintptr_t)end);
}

uintptr_t GC_get_free_bytes() {
	return 0;
}

uintptr_t GC_get_total_bytes() {
	return 0;
}

void GC_init_main_thread() {
}

void GC_init_pre_thread() {
}

void GC_init_thread() {
}

void GC_finish_thread() {
}