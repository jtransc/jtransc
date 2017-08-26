//	//#define GC_DEBUG
//	//#include <gc_cpp.h>
//	//#include "gc_cpp.h"
#include "gc.h"

void GC_ADD_ROOT_SINGLE(void* ptr) {
	GC_add_roots(ptr, (void *)((uintptr_t)ptr + sizeof(uintptr_t)));
}

void GC_init_main_thread() {
	GC_init();
	//GC_allow_register_threads();
}

void GC_init_pre_thread() {
	//GC_init();
}

void GC_init_thread() {
	//int a = 0; GC_register_my_thread(&a);
	GC_init();
}

void GC_finish_thread() {
}