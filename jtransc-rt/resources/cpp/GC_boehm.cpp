//	//#define GC_DEBUG
//	//#include <gc_cpp.h>
//	//#include "gc_cpp.h"
#include "gc.h"

void GC_ADD_ROOT_SINGLE(void* ptr) {
	GC_add_roots(ptr, (void *)((uintptr_t)ptr + sizeof(uintptr_t)));
}
