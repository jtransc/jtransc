@:headerNamespaceCode('namespace { extern "C" {
	#ifdef HX_WINDOWS
		void* __stdcall LoadLibraryA(char *name);
		void* __stdcall GetProcAddress(void *lib, char *name);
		int __stdcall FreeLibrary(void *lib);
	#else
		void* dlopen(const char *file, int mode);
		void *dlsym(void *handle, char *name);
		int dlclose(void *handle);
	#endif
} }')
class HaxeDynamicLoad {
	@:noStack
	static public function dlopen(name:String):haxe.Int64 {
		untyped __cpp__('
			#if HX_WINDOWS
				return (size_t)(void *)LoadLibraryA({0});
			#else
				return (size_t)(void *)dlopen({0}, 0);
			#endif
		', cpp.NativeString.c_str(name));
		return 0;
	}

	@:noStack
	static public function dlsym(handle:haxe.Int64, name:String):haxe.Int64 {
		untyped __cpp__('
			#if HX_WINDOWS
				return (size_t)(void *)GetProcAddress((void *)(size_t)({0}), {1});
			#else
				return (size_t)(void *)dlsym((void *)(size_t)({0}), {1});
			#endif
		', handle, cpp.NativeString.c_str(name));
		return 0;
	}

	@:noStack
	static public function dlclose(handle:haxe.Int64):Int {
		untyped __cpp__('
			#if HX_WINDOWS
				return (size_t)(void *)FreeLibrary((void *)(size_t){0});
			#else
				return (size_t)(void *)dlclose((void *)(size_t){0});
			#endif
    	', handle);
    	return 0;
	}
}