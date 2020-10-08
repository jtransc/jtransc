

void* DYN::openDynamicLib(const char* libraryName){
	#if _JTRANSC_USE_DYNAMIC_LIB
		#if _JTRANSC_WINDOWS_
		return LoadLibrary(libraryName);

		#elif _JTRANSC_UNIX_LIKE_
		return dlopen(libraryName, RTLD_LAZY | RTLD_LOCAL);

		#else
		#   error "Dynamic loading unsupported on this target"
		#endif
	#else
		std::cout << "WARNING not enabled DYN::openDynamicLib " << libraryName << "\n";
		return nullptr;
	#endif
}

void DYN::closeDynamicLib(void* handle){
	#if _JTRANSC_USE_DYNAMIC_LIB
		#if _JTRANSC_WINDOWS_
		FreeLibrary((HMODULE)handle);

		#elif _JTRANSC_UNIX_LIKE_
		dlclose(handle);

		#else
		#   error "Dynamic loading unsupported on this target"
		#endif
	#endif
}

void* DYN::findDynamicSymbol(void* handle, const char* symbolToSearch){
	#if _JTRANSC_USE_DYNAMIC_LIB
		#if _JTRANSC_WINDOWS_
		void* symbol = GetProcAddress((HMODULE)handle, symbolToSearch);

		//TODO error handling, etc.

		return symbol;

		#elif _JTRANSC_UNIX_LIKE_
		dlerror(); //Clear all old errors
		void* symbol = dlsym(handle, symbolToSearch);
		if(symbol){
			return symbol;
		} else {
			const char* error = dlerror();
			if(error){
				return NULL;
				//throw error;
			} else {
				return NULL;
				//throw (std::string("Unknown error while trying to resolve ") + std::string(symbolToSearch) + std::string(" or the symbol refers to a null pointer!")).c_str();
			}
		}

		#else
		#   error "Dynamic loading unsupported on this target"
		#endif
	#else
		return nullptr;
	#endif
}

void* DYN::jtvmResolveNative(JAVA_OBJECT clazz, const char* shortMangledName, const char* longMangledName, void** ptr){
	if(*ptr != NULL) {
		return *ptr;
	}
	else {
		auto classLoader = GET_OBJECT({% CLASS java.lang.Class %}, clazz)->{% METHOD java.lang.Class:getClassLoader %}();
		*ptr = jtvmResolveNativeMethodImpl(shortMangledName, longMangledName, classLoader, ptr);
	}
	if(*ptr == NULL){
		throw "Couldn't find native symbol";
	}
	return *ptr;
}

void* DYN::jtvmResolveNativeMethodImpl(const char* shortMangledName, const char* longMangledName, JAVA_OBJECT classLoader, void** ptr){
	JAVA_OBJECT nativeLibsRaw = GET_OBJECT({% CLASS java.lang.ClassLoader %}, classLoader)->{% METHOD java.lang.ClassLoader:getNativeLibs %}();
	auto nativeLibs = GET_OBJECT({% CLASS java.util.ArrayList %}, nativeLibsRaw);
	jint size = nativeLibs->{% METHOD java.util.ArrayList:size %}();
	for(jint i = 0; i < size; i++){
		auto nativeLib = GET_OBJECT({% CLASS java.lang.ClassLoader$NativeLib %}, nativeLibs->{% METHOD java.util.ArrayList:get %}(i));

		auto handle = jlong_to_ptr(nativeLib->{% FIELD java.lang.ClassLoader$NativeLib:handle %});

		void* symbol = DYN::findDynamicSymbol(handle, shortMangledName);
		if(symbol){
			return symbol;
		}
		else {
			symbol = DYN::findDynamicSymbol(handle, longMangledName);
			if(symbol) {
				return symbol;
			}
			else {
				return NULL;
			}
		}
	}
	return nullptr;
}
jmethodID JNICALL  FromReflectedMethod(JNIEnv *env, jobject method){
	return (jmethodID)method; // TODO check requirements and other stuff
}

jfieldID JNICALL  FromReflectedField(JNIEnv *env, jobject field){
	return (jfieldID)field; // TODO check requirements and other stuff
}

jobject JNICALL ToReflectedMethod(JNIEnv *env, jclass cls, jmethodID methodID, jboolean isStatic){
	return (jobject)methodID; // TODO check requirements and other stuff
}

jclass JNICALL GetSuperclass(JNIEnv *env, jclass clazz){
	return (jclass)GET_OBJECT({% CLASS java.lang.Class %}, (JAVA_OBJECT)clazz)->{% METHOD java.lang.Class:getSuperclass%}();
}

jboolean JNICALL IsAssignableFrom(JNIEnv *env, jclass clazz1, jclass clazz2){
	return GET_OBJECT({% CLASS java.lang.Class %}, (JAVA_OBJECT)clazz2)->{% METHOD java.lang.Class:isAssignableFrom %}(GET_OBJECT({% CLASS java.lang.Class %}, (JAVA_OBJECT)clazz1));
}

jobject JNICALL ToReflectedField(JNIEnv *env, jclass cls, jfieldID fieldID, jboolean isStatic){
	return (jobject)fieldID; // TODO check requirements and other stuff
}

jint JNICALL PushLocalFrame(JNIEnv* env, jint cap) {
    return 0;
}

jobject JNICALL PopLocalFrame(JNIEnv* env, jobject res) {
    return res;
}

jint JNICALL EnsureLocalCapacity(JNIEnv* env, jint capacity) {
    return 0;
}

static JA_L* jvalueToJavaArray(int32_t count, {% CLASS java.lang.reflect.MethodConstructor %}* ctor, const jvalue *args){
	JA_L* array = __GC_ALLOC_JA_L(count, L"[Ljava.lang.Object;");
	const char* s = N::istr3(ctor->{% FIELD java.lang.reflect.MethodConstructor:jniSignature%}).c_str();
	for( int32_t i = 0; i < count; i++){
		switch (s[i]) {
     		case 'B': array->fastSet(i, N::boxByte(args[i].b));
     		break;
     		case 'Z': array->fastSet(i, N::boxBool(args[i].z));
     		break;
			case 'S': array->fastSet(i, N::boxShort(args[i].s));
     		break;
        	case 'C': array->fastSet(i, N::boxChar(args[i].c));
     		break;
        	case 'I': array->fastSet(i, N::boxInt(args[i].i));
     		break;
        	case 'J': array->fastSet(i, N::boxLong(args[i].j));
     		break;
        	case 'F': array->fastSet(i, N::boxFloat(args[i].f));
     		break;
       		case 'D': array->fastSet(i, N::boxDouble(args[i].d));
     		break;
	        case '[': array->fastSet(i, (JAVA_OBJECT)args[i].l);
     		break;
	        case 'L': array->fastSet(i, (JAVA_OBJECT)args[i].l);
     		break;
    	}
    }
    return array;
}

static JA_L* va_listToJavaArray(int32_t count, {% CLASS java.lang.reflect.MethodConstructor %}* ctor, va_list args){
	JA_L* array = __GC_ALLOC_JA_L(count, L"[Ljava.lang.Object;");
	const char* s = N::istr3(ctor->{% FIELD java.lang.reflect.MethodConstructor:jniSignature%}).c_str();
	for( int32_t i = 0; i < count; i++){
		switch (s[i]) {
     		case 'B': array->fastSet(i, N::boxByte((jbyte)va_arg(args, jint)));
     		break;
     		case 'Z': array->fastSet(i, N::boxBool((jboolean)va_arg(args, jint)));
     		break;
			case 'S': array->fastSet(i, N::boxShort((jshort)va_arg(args, jint)));
			break;
        	case 'C': array->fastSet(i, N::boxChar((jchar)va_arg(args, jint)));
        	break;
        	case 'I': array->fastSet(i, N::boxInt(va_arg(args, jint)));
        	break;
        	case 'J': array->fastSet(i, N::boxLong(va_arg(args, jlong)));
        	break;
        	case 'F': array->fastSet(i, N::boxFloat((float)va_arg(args, jdouble)));
        	break;
       		case 'D': array->fastSet(i, N::boxDouble(va_arg(args, jdouble)));
       		break;
	        case '[': array->fastSet(i, (JAVA_OBJECT)va_arg(args, jobject));
     		break;
	        case 'L': array->fastSet(i, (JAVA_OBJECT)va_arg(args, jobject));
	        break;
    	}
    }
    return array;
}

JAVA_OBJECT jtvmNewObjectA(JAVA_OBJECT clazz, JAVA_OBJECT method, const jvalue *args) {
    {% CLASS java.lang.reflect.Constructor %}* ctor = GET_OBJECT({% CLASS java.lang.reflect.Constructor %}, method);
    int32_t parameterCount = ctor->{% METHOD java.lang.reflect.Constructor:getParameterCount %}();
    return ctor->{% METHOD java.lang.reflect.Constructor:newInstance %}(jvalueToJavaArray(parameterCount, ctor, args));
}

JAVA_OBJECT jtvmNewObjectV(JAVA_OBJECT clazz, JAVA_OBJECT method, va_list args) {
    {% CLASS java.lang.reflect.Constructor %}* ctor = GET_OBJECT({% CLASS java.lang.reflect.Constructor %}, method);
    int32_t parameterCount = ctor->{% METHOD java.lang.reflect.Constructor:getParameterCount %}();
    return ctor->{% METHOD java.lang.reflect.Constructor:newInstance %}(va_listToJavaArray(parameterCount, ctor, args));
}

jobject JNICALL NewObjectV(JNIEnv* env, jclass clazz, jmethodID methodID, va_list args) {
    return (jobject) jtvmNewObjectV((JAVA_OBJECT) clazz, (JAVA_OBJECT) methodID, args);
}

jobject JNICALL NewObjectA(JNIEnv* env, jclass clazz, jmethodID methodID, const jvalue*  args) {
    return (jobject) jtvmNewObjectA((JAVA_OBJECT) clazz, (JAVA_OBJECT) methodID, args);
}

jobject JNICALL NewObject(JNIEnv* env, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jobject o = NewObjectV(env, clazz, methodID, args);
    va_end(args);
    return o;
}

jobject JNICALL AllocObject(JNIEnv *env, jclass clazz){
	int32_t classId = GET_OBJECT({% CLASS java.lang.Class %}, (JAVA_OBJECT)clazz)->{% FIELD java.lang.Class:id %};
	return (jobject)CTOR_TABLE[classId]();
}


inline static jboolean isMethodStatic(jmethodID method_){
	JAVA_OBJECT method = (JAVA_OBJECT)method_;
	jboolean result = GET_OBJECT({% CLASS java.lang.reflect.MethodConstructor %}, method)->{% METHOD java.lang.reflect.MethodConstructor:isStatic %}();
	return result;
}

JAVA_OBJECT jtvmGetMethodForClass(JNIEnv *env, JAVA_OBJECT clazz, const char *name, const char *sig){
	if(strcmp(sig, "<init>") == 0){
		// for ctors
        // TODO check correctness etc.
        JAVA_OBJECT ctor_ = GET_OBJECT({% CLASS java.lang.Class %}, clazz)->{% METHOD java.lang.Class:getDeclaredConstructorBySig %}(N::str(sig));
        auto ctor = GET_OBJECT({% CLASS java.lang.reflect.Method %}, ctor_);

        if(ctor){
           	return ctor;
        }
	} else {
		// for methods
    	// TODO check correctness etc.
    	JAVA_OBJECT method_ = GET_OBJECT({% CLASS java.lang.Class %}, clazz)->{% METHOD java.lang.Class:getDeclaredMethodBySig %}(N::str(name), N::str(sig));
        auto method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, method_);

        if(method){
           	return method;
        }
	}

    return nullptr;
}

jmethodID jtvmGetInstanceMethod(JNIEnv *env, JAVA_OBJECT clazz_, const char *name, const char *sig){
	auto clazz = GET_OBJECT({% CLASS java.lang.Class %}, clazz_);
	if(strcmp(sig, "<init>") == 0 || strcmp(sig, "<clinit>") == 0 ){
		// ctors and static initializers are not inherited, so only check this class and not the superclass
		jmethodID method = (jmethodID)jtvmGetMethodForClass(env, reinterpret_cast<JAVA_OBJECT>(clazz), name, sig);
        if(method && !isMethodStatic(method)) return method; //TODO throw exception?
	}

	java_lang_Class* c = NULL;
	for (c = clazz; c != NULL; c = GET_OBJECT({% CLASS java.lang.Class %}, GET_OBJECT({% CLASS java.lang.Class %}, c)->{% METHOD java.lang.Class:getSuperclass %}())) {
    	jmethodID method = (jmethodID)jtvmGetMethodForClass(env, clazz, name, sig);
        if(method && !isMethodStatic(method)) return method; //TODO throw exception?
    }
    return NULL;
}

jmethodID JNICALL  GetMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig){
	// TODO force init of clazz if not already initialised
	return jtvmGetInstanceMethod(env, reinterpret_cast<JAVA_OBJECT>(clazz), name, sig);
}

/**
* Returns a methddID based on the obj. That's necessary because the methodID may not always be the exact right.
* This is needed for the virtual dispatch routines.
**/
static inline jmethodID getRealMethod(JNIEnv* env, jobject obj, jmethodID methodID){

	JAVA_OBJECT clazz = ((JAVA_OBJECT)obj)->{% METHOD java.lang.Object:getClass %}();
	auto method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT) methodID);
	JAVA_OBJECT containingClazz = method->{% FIELD java.lang.reflect.MethodConstructor:clazz %};
	if(method->{% METHOD java.lang.reflect.MethodConstructor:isPrivate %}() || clazz->{% METHOD java.lang.Class:equals %}(containingClazz)){
		return methodID;
	} else {
		const char* name = N::istr3(method->{% FIELD java.lang.reflect.MethodConstructor:name %}).c_str();
		const char* sig = N::istr3(method->{% FIELD java.lang.reflect.MethodConstructor:signature %}).c_str();
		return GetMethodID(env, GetSuperclass(env, (jclass)clazz), name, sig);
	}
}

jobject jtvmCallObjectInstanceMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args){
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return (jobject)method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, va_listToJavaArray(parameterCount, method, args));
}

jobject jtvmCallObjectInstanceMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return (jobject)method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, jvalueToJavaArray(parameterCount, method, args));
}

jobject JNICALL CallObjectMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args) {
    return jtvmCallObjectInstanceMethodV(env, obj, getRealMethod(env, obj, methodID), args);
}

jobject JNICALL CallObjectMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
    return jtvmCallObjectInstanceMethodA(env, obj, getRealMethod(env, obj, methodID), args);
}

jobject JNICALL CallObjectMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jobject o = CallObjectMethodV(env, obj, methodID, args);
    va_end(args);
    return o;
}

jboolean jtvmCallBooleanInstanceMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args){
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxBool(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, va_listToJavaArray(parameterCount, method, args)));
}

jboolean jtvmCallBooleanInstanceMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxBool(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, jvalueToJavaArray(parameterCount, method, args)));
}

jboolean JNICALL CallBooleanMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args) {
    return jtvmCallBooleanInstanceMethodV(env, obj, getRealMethod(env, obj, methodID), args);
}

jboolean JNICALL CallBooleanMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue*  args) {
    return jtvmCallBooleanInstanceMethodA(env, obj, getRealMethod(env, obj, methodID), args);
}

jboolean JNICALL CallBooleanMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jboolean b = CallBooleanMethodV(env, obj, methodID, args);
    va_end(args);
    return b;
}

jbyte jtvmCallByteInstanceMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args){
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxByte(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, va_listToJavaArray(parameterCount, method, args)));
}

jbyte jtvmCallByteInstanceMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxByte(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, jvalueToJavaArray(parameterCount, method, args)));
}

jbyte JNICALL CallByteMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args) {
    return jtvmCallByteInstanceMethodV(env, obj, getRealMethod(env, obj, methodID), args);
}

jbyte JNICALL CallByteMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
    return jtvmCallByteInstanceMethodA(env, obj, getRealMethod(env, obj, methodID), args);
}

jbyte JNICALL CallByteMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jbyte b = CallByteMethodV(env, obj, methodID, args);
    va_end(args);
    return b;
}

jchar jtvmCallCharInstanceMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args){
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxChar(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, va_listToJavaArray(parameterCount, method, args)));
}

jchar jtvmCallCharInstanceMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxChar(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, jvalueToJavaArray(parameterCount, method, args)));
}

jchar JNICALL CallCharMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args) {
    return jtvmCallCharInstanceMethodV(env, obj, getRealMethod(env, obj, methodID), args);
}

jchar JNICALL CallCharMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
    return jtvmCallCharInstanceMethodA(env, obj, getRealMethod(env, obj, methodID), args);
}

jchar JNICALL CallCharMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jchar c = CallCharMethodV(env, obj, methodID, args);
    va_end(args);
    return c;
}

jshort jtvmCallShortInstanceMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args){
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxShort(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, va_listToJavaArray(parameterCount, method, args)));
}

jshort jtvmCallShortInstanceMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxShort(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, jvalueToJavaArray(parameterCount, method, args)));
}

jshort JNICALL CallShortMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args) {
    return jtvmCallShortInstanceMethodV(env, obj, getRealMethod(env, obj, methodID), args);
}

jshort JNICALL CallShortMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
    return jtvmCallShortInstanceMethodA(env, obj, getRealMethod(env, obj, methodID), args);
}

jshort JNICALL CallShortMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jshort s = CallShortMethodV(env, obj, methodID, args);
    va_end(args);
    return s;
}

jint jtvmCallIntInstanceMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args){
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxInt(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, va_listToJavaArray(parameterCount, method, args)));
}

jint jtvmCallIntInstanceMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxInt(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, jvalueToJavaArray(parameterCount, method, args)));
}

jint JNICALL CallIntMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args) {
    return jtvmCallIntInstanceMethodV(env, obj, getRealMethod(env, obj, methodID), args);
}

jint JNICALL CallIntMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
    return jtvmCallIntInstanceMethodA(env, obj, getRealMethod(env, obj, methodID), args);
}

jint JNICALL CallIntMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jint i = CallIntMethodV(env, obj, methodID, args);
    va_end(args);
    return i;
}

jlong jtvmCallLongInstanceMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args){
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxLong(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, va_listToJavaArray(parameterCount, method, args)));
}

jlong jtvmCallLongInstanceMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxLong(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, jvalueToJavaArray(parameterCount, method, args)));
}

jlong JNICALL CallLongMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args) {
    return jtvmCallLongInstanceMethodV(env, obj, getRealMethod(env, obj, methodID), args);
}

jlong JNICALL CallLongMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
    return jtvmCallLongInstanceMethodA(env, obj, getRealMethod(env, obj, methodID), args);
}

jlong JNICALL CallLongMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jlong l = CallLongMethodV(env, obj, methodID, args);
    va_end(args);
    return l;
}

jfloat jtvmCallFloatInstanceMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args){
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxFloat(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, va_listToJavaArray(parameterCount, method, args)));
}

jfloat jtvmCallFloatInstanceMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxFloat(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, jvalueToJavaArray(parameterCount, method, args)));
}

jfloat JNICALL CallFloatMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args) {
    return jtvmCallFloatInstanceMethodV(env, obj, getRealMethod(env, obj, methodID), args);
}

jfloat JNICALL CallFloatMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
    return jtvmCallFloatInstanceMethodA(env, obj, getRealMethod(env, obj, methodID), args);
}

jfloat JNICALL CallFloatMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jfloat f = CallFloatMethodV(env, obj, methodID, args);
    va_end(args);
    return f;
}

jdouble jtvmCallDoubleInstanceMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args){
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxDouble(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, va_listToJavaArray(parameterCount, method, args)));
}

jdouble jtvmCallDoubleInstanceMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxDouble(method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, jvalueToJavaArray(parameterCount, method, args)));
}

jdouble JNICALL CallDoubleMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args) {
    return jtvmCallDoubleInstanceMethodV(env, obj, getRealMethod(env, obj, methodID), args);
}

jdouble JNICALL CallDoubleMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
    return jtvmCallDoubleInstanceMethodA(env, obj, getRealMethod(env, obj, methodID), args);
}

jdouble JNICALL CallDoubleMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jdouble d = CallDoubleMethodV(env, obj, methodID, args);
    va_end(args);
    return d;
}

void jtvmCallVoidInstanceMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args){
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, va_listToJavaArray(parameterCount, method, args));
}

void jtvmCallVoidInstanceMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue* args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    method->{% METHOD java.lang.reflect.Method:invoke %}((JAVA_OBJECT)obj, jvalueToJavaArray(parameterCount, method, args));
}

void JNICALL  CallVoidMethodV(JNIEnv* env, jobject obj, jmethodID methodID, va_list args) {
    jtvmCallVoidInstanceMethodV(env, obj, getRealMethod(env, obj, methodID), args);
}

void JNICALL  CallVoidMethodA(JNIEnv* env, jobject obj, jmethodID methodID, const jvalue*  args) {
    jtvmCallVoidInstanceMethodA(env, obj, getRealMethod(env, obj, methodID), args);
}

void JNICALL  CallVoidMethod(JNIEnv* env, jobject obj, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    CallVoidMethodV(env, obj, methodID, args);
    va_end(args);
}


//

jobject JNICALL CallNonvirtualObjectMethodV(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    return jtvmCallObjectInstanceMethodV(env, obj, methodID, args);
}

jobject JNICALL CallNonvirtualObjectMethodA(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, const jvalue* args) {
    return jtvmCallObjectInstanceMethodA(env, obj, methodID, args);
}

jobject JNICALL CallNonvirtualObjectMethod(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jobject o = CallNonvirtualObjectMethodV(env, obj, clazz, methodID, args);
    va_end(args);
    return o;
}

jboolean JNICALL CallNonvirtualBooleanMethodV(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    return jtvmCallBooleanInstanceMethodV(env, obj, methodID, args);
}

jboolean JNICALL CallNonvirtualBooleanMethodA(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, const jvalue*  args) {
    return jtvmCallBooleanInstanceMethodA(env, obj, methodID, args);
}

jboolean JNICALL CallNonvirtualBooleanMethod(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jboolean b = CallNonvirtualBooleanMethodV(env, obj, clazz, methodID, args);
    va_end(args);
    return b;
}

jbyte JNICALL CallNonvirtualByteMethodV(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    return jtvmCallByteInstanceMethodV(env, obj, methodID, args);
}

jbyte JNICALL CallNonvirtualByteMethodA(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, const jvalue* args) {
    return jtvmCallByteInstanceMethodA(env, obj, methodID, args);
}

jbyte JNICALL CallNonvirtualByteMethod(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jbyte b = CallNonvirtualByteMethodV(env, obj, clazz, methodID, args);
    va_end(args);
    return b;
}

jchar JNICALL CallNonvirtualCharMethodV(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    return jtvmCallCharInstanceMethodV(env, obj, methodID, args);
}

jchar JNICALL CallNonvirtualCharMethodA(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, const jvalue* args) {
    return jtvmCallCharInstanceMethodA(env, obj, methodID, args);
}

jchar JNICALL CallNonvirtualCharMethod(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jchar c = CallNonvirtualCharMethodV(env, obj, clazz, methodID, args);
    va_end(args);
    return c;
}

jshort JNICALL CallNonvirtualShortMethodV(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    return jtvmCallShortInstanceMethodV(env, obj, methodID, args);
}

jshort JNICALL CallNonvirtualShortMethodA(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, const jvalue* args) {
    return jtvmCallShortInstanceMethodA(env, obj, methodID, args);
}

jshort JNICALL CallNonvirtualShortMethod(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jshort s = CallNonvirtualShortMethodV(env, obj, clazz, methodID, args);
    va_end(args);
    return s;
}

jint JNICALL CallNonvirtualIntMethodV(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    return jtvmCallIntInstanceMethodV(env, obj, methodID, args);
}

jint JNICALL CallNonvirtualIntMethodA(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, const jvalue* args) {
    return jtvmCallIntInstanceMethodA(env, obj, methodID, args);
}

jint JNICALL CallNonvirtualIntMethod(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jint i = CallNonvirtualIntMethodV(env, obj, clazz, methodID, args);
    va_end(args);
    return i;
}

jlong JNICALL CallNonvirtualLongMethodV(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    return jtvmCallLongInstanceMethodV(env, obj, methodID, args);
}

jlong JNICALL CallNonvirtualLongMethodA(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, const jvalue* args) {
    return jtvmCallLongInstanceMethodA(env, obj, methodID, args);
}

jlong JNICALL CallNonvirtualLongMethod(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jlong l = CallNonvirtualLongMethodV(env, obj, clazz, methodID, args);
    va_end(args);
    return l;
}

jfloat JNICALL CallNonvirtualFloatMethodV(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    return jtvmCallFloatInstanceMethodV(env, obj, methodID, args);
}

jfloat JNICALL CallNonvirtualFloatMethodA(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, const jvalue* args) {
    return jtvmCallFloatInstanceMethodA(env, obj, methodID, args);
}

jfloat JNICALL CallNonvirtualFloatMethod(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jfloat f = CallNonvirtualFloatMethodV(env, obj, clazz, methodID, args);
    va_end(args);
    return f;
}

jdouble JNICALL CallNonvirtualDoubleMethodV(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    return jtvmCallDoubleInstanceMethodV(env, obj, methodID, args);
}

jdouble JNICALL CallNonvirtualDoubleMethodA(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, const jvalue* args) {
    return jtvmCallDoubleInstanceMethodA(env, obj, methodID, args);
}

jdouble JNICALL CallNonvirtualDoubleMethod(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jdouble d = CallNonvirtualDoubleMethodV(env, obj, clazz, methodID, args);
    va_end(args);
    return d;
}

void JNICALL  CallNonvirtualVoidMethodV(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, va_list args) {
    jtvmCallVoidInstanceMethodV(env, obj, methodID, args);
}

void JNICALL  CallNonvirtualVoidMethodA(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, const jvalue*  args) {
    jtvmCallVoidInstanceMethodA(env, obj, methodID, args);
}

void JNICALL  CallNonvirtualVoidMethod(JNIEnv* env, jobject obj, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    CallNonvirtualVoidMethodV(env, obj, clazz, methodID, args);
    va_end(args);
}


jfieldID jtvmGetFieldID({% CLASS java.lang.Class %}* clazz, JAVA_OBJECT name, JAVA_OBJECT sig){
	auto field_ = clazz->{% METHOD java.lang.Class:getField %}(name);
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, field_);
	if(field){
		JAVA_OBJECT signature = field->{% FIELD java.lang.reflect.Field:signature %};
		if(signature->{% METHOD java.lang.String:equals %}(sig)){
			if(field->{% METHOD java.lang.reflect.Field:isStatic %}()){
				return (jfieldID)field;
			} else {
				return NULL;
			}
		}
	}
	return (jfieldID)field;
}

jfieldID JNICALL  GetFieldID(JNIEnv *env, jclass clazz, const char *name, const char *sig){
	return jtvmGetFieldID(GET_OBJECT({% CLASS java.lang.Class %}, (JAVA_OBJECT)clazz), N::str(name), N::str(sig));
}

jobject JNICALL GetObjectField(JNIEnv *env, jobject obj, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return (jobject) field->{% METHOD java.lang.reflect.Field:get %}(reinterpret_cast<JAVA_OBJECT>(obj));
}

jboolean JNICALL GetBooleanField(JNIEnv *env, jobject obj, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getBoolean %}(reinterpret_cast<JAVA_OBJECT>(obj));
}

jbyte JNICALL GetByteField(JNIEnv *env, jobject obj, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getByte %}(reinterpret_cast<JAVA_OBJECT>(obj));
}

jchar JNICALL GetCharField(JNIEnv *env, jobject obj, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getChar %}(reinterpret_cast<JAVA_OBJECT>(obj));
}

jshort JNICALL GetShortField(JNIEnv *env, jobject obj, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getShort %}(reinterpret_cast<JAVA_OBJECT>(obj));
}

jint JNICALL GetIntField(JNIEnv *env, jobject obj, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getInt %}(reinterpret_cast<JAVA_OBJECT>(obj));
}

jlong JNICALL GetLongField(JNIEnv *env, jobject obj, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getLong %}(reinterpret_cast<JAVA_OBJECT>(obj));
}

jfloat JNICALL GetFloatField(JNIEnv *env, jobject obj, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getFloat %}(reinterpret_cast<JAVA_OBJECT>(obj));
}

jdouble JNICALL GetDoubleField(JNIEnv *env, jobject obj, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getDouble %}(reinterpret_cast<JAVA_OBJECT>(obj));
}

void JNICALL  SetObjectField(JNIEnv *env, jobject obj, jfieldID fieldID, jobject value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:set %}(reinterpret_cast<JAVA_OBJECT>(obj), reinterpret_cast<JAVA_OBJECT>(value));
}
void JNICALL  SetBooleanField(JNIEnv *env, jobject obj, jfieldID fieldID, jboolean value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setBoolean %}(reinterpret_cast<JAVA_OBJECT>(obj), value);
}
void JNICALL  SetByteField(JNIEnv *env, jobject obj, jfieldID fieldID, jbyte value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setByte %}(reinterpret_cast<JAVA_OBJECT>(obj), value);
}

void JNICALL  SetCharField(JNIEnv *env, jobject obj, jfieldID fieldID, jchar value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setChar %}(reinterpret_cast<JAVA_OBJECT>(obj), value);
}

void JNICALL  SetShortField(JNIEnv *env, jobject obj, jfieldID fieldID, jshort value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setShort %}(reinterpret_cast<JAVA_OBJECT>(obj), value);
}

void JNICALL  SetIntField(JNIEnv *env, jobject obj, jfieldID fieldID, jint value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setInt %}(reinterpret_cast<JAVA_OBJECT>(obj), value);
}

void JNICALL  SetLongField(JNIEnv *env, jobject obj, jfieldID fieldID, jlong value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setLong %}(reinterpret_cast<JAVA_OBJECT>(obj), value);
}

void JNICALL  SetFloatField(JNIEnv *env, jobject obj, jfieldID fieldID, jfloat value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setFloat %}(reinterpret_cast<JAVA_OBJECT>(obj), value);
}

void JNICALL  SetDoubleField(JNIEnv *env, jobject obj, jfieldID fieldID, jdouble value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setDouble %}(reinterpret_cast<JAVA_OBJECT>(obj), value);
}

jobject JNICALL CallStaticObjectMethodV(JNIEnv* env, jclass clazz, jmethodID methodID, va_list args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return (jobject)method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, va_listToJavaArray(parameterCount, method, args));
}

jmethodID JNICALL  GetStaticMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig){
	return (jmethodID)jtvmGetMethodForClass(env, (JAVA_OBJECT)clazz, name, sig); // TODO verify jni specs
}

jobject JNICALL CallStaticObjectMethodA(JNIEnv* env, jclass clazz, jmethodID methodID, const jvalue*  args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return (jobject)method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, jvalueToJavaArray(parameterCount, method, args));
}

jobject JNICALL CallStaticObjectMethod(JNIEnv* env, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jobject o = CallStaticObjectMethodV(env, clazz, methodID, args);
    va_end(args);
    return o;
}

jboolean JNICALL CallStaticBooleanMethodV(JNIEnv* env, jclass clazz, jmethodID methodID, va_list args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxBool(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, va_listToJavaArray(parameterCount, method, args)));
}

jboolean JNICALL CallStaticBooleanMethodA(JNIEnv* env, jclass clazz, jmethodID methodID, const jvalue*  args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxBool(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, jvalueToJavaArray(parameterCount, method, args)));
}

jboolean JNICALL CallStaticBooleanMethod(JNIEnv* env, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jboolean b = CallStaticBooleanMethodV(env, clazz, methodID, args);
    va_end(args);
    return b;
}

jbyte JNICALL CallStaticByteMethodV(JNIEnv* env, jclass clazz, jmethodID methodID, va_list args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxByte(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, va_listToJavaArray(parameterCount, method, args)));
}

jbyte JNICALL CallStaticByteMethodA(JNIEnv* env, jclass clazz, jmethodID methodID, const jvalue*  args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxByte(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, jvalueToJavaArray(parameterCount, method, args)));
}

jbyte JNICALL CallStaticByteMethod(JNIEnv* env, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jbyte b = CallStaticByteMethodV(env, clazz, methodID, args);
    va_end(args);
    return b;
}

jchar JNICALL CallStaticCharMethodV(JNIEnv* env, jclass clazz, jmethodID methodID, va_list args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxChar(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, va_listToJavaArray(parameterCount, method, args)));
}

jchar JNICALL CallStaticCharMethodA(JNIEnv* env, jclass clazz, jmethodID methodID, const jvalue*  args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxChar(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, jvalueToJavaArray(parameterCount, method, args)));
}

jchar JNICALL CallStaticCharMethod(JNIEnv* env, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jchar c = CallStaticCharMethodV(env, clazz, methodID, args);
    va_end(args);
    return c;
}

jshort JNICALL CallStaticShortMethodV(JNIEnv* env, jclass clazz, jmethodID methodID, va_list args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxShort(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, va_listToJavaArray(parameterCount, method, args)));
}

jshort JNICALL CallStaticShortMethodA(JNIEnv* env, jclass clazz, jmethodID methodID, const jvalue*  args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxShort(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, jvalueToJavaArray(parameterCount, method, args)));
}

jshort JNICALL CallStaticShortMethod(JNIEnv* env, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jshort s = CallStaticShortMethodV(env, clazz, methodID, args);
    va_end(args);
    return s;
}

jint JNICALL CallStaticIntMethodV(JNIEnv* env, jclass clazz, jmethodID methodID, va_list args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxInt(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, va_listToJavaArray(parameterCount, method, args)));
}

jint JNICALL CallStaticIntMethodA(JNIEnv* env, jclass clazz, jmethodID methodID, const jvalue*  args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxInt(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, jvalueToJavaArray(parameterCount, method, args)));
}

jint JNICALL CallStaticIntMethod(JNIEnv* env, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jint i = CallStaticIntMethodV(env, clazz, methodID, args);
    va_end(args);
    return i;
}

jlong JNICALL CallStaticLongMethodV(JNIEnv* env, jclass clazz, jmethodID methodID, va_list args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxLong(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, va_listToJavaArray(parameterCount, method, args)));
}

jlong JNICALL CallStaticLongMethodA(JNIEnv* env, jclass clazz, jmethodID methodID, const jvalue*  args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxLong(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, jvalueToJavaArray(parameterCount, method, args)));
}

jlong JNICALL CallStaticLongMethod(JNIEnv* env, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jlong l = CallStaticLongMethodV(env, clazz, methodID, args);
    va_end(args);
    return l;
}

jfloat JNICALL CallStaticFloatMethodV(JNIEnv* env, jclass clazz, jmethodID methodID, va_list args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxFloat(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, va_listToJavaArray(parameterCount, method, args)));
}

jfloat JNICALL CallStaticFloatMethodA(JNIEnv* env, jclass clazz, jmethodID methodID, const jvalue*  args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxFloat(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, jvalueToJavaArray(parameterCount, method, args)));
}

jfloat JNICALL CallStaticFloatMethod(JNIEnv* env, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jfloat f = CallStaticFloatMethodV(env, clazz, methodID, args);
    va_end(args);
    return f;
}

jdouble JNICALL CallStaticDoubleMethodV(JNIEnv* env, jclass clazz, jmethodID methodID, va_list args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxDouble(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, va_listToJavaArray(parameterCount, method, args)));
}

jdouble JNICALL CallStaticDoubleMethodA(JNIEnv* env, jclass clazz, jmethodID methodID, const jvalue*  args) {
    {% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
    int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    return N::unboxDouble(method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, jvalueToJavaArray(parameterCount, method, args)));
}

jdouble JNICALL CallStaticDoubleMethod(JNIEnv* env, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    jdouble d = CallStaticDoubleMethodV(env, clazz, methodID, args);
    va_end(args);
    return d;
}

void JNICALL  CallStaticVoidMethodV(JNIEnv* env, jclass clazz, jmethodID methodID, va_list args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, va_listToJavaArray(parameterCount, method, args));
}

void JNICALL  CallStaticVoidMethodA(JNIEnv* env, jclass clazz, jmethodID methodID, const jvalue* args) {
	{% CLASS java.lang.reflect.Method %}* method = GET_OBJECT({% CLASS java.lang.reflect.Method %}, (JAVA_OBJECT)methodID);
	int32_t parameterCount = method->{% METHOD java.lang.reflect.MethodConstructor:getParameterCount %}();
    method->{% METHOD java.lang.reflect.Method:invoke %}(NULL, jvalueToJavaArray(parameterCount, method, args));
}

void JNICALL  CallStaticVoidMethod(JNIEnv* env, jclass clazz, jmethodID methodID, ...) {
    va_list args;
    va_start(args, methodID);
    CallStaticVoidMethodV(env, clazz, methodID, args);
    va_end(args);
}


jfieldID jtvmGetStaticFieldID(JNIEnv *env, JAVA_OBJECT clazz, const char *name, const char *sig){
	JAVA_OBJECT field_ = GET_OBJECT({% CLASS java.lang.Class %}, clazz)->{% METHOD java.lang.Class:getDeclaredField %}(N::str(name));
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, field_);

    if(field){
    	JAVA_OBJECT signature = field->{% FIELD java.lang.reflect.Field:signature %};
    	if(strcmp(N::istr3(signature).c_str(), sig) == 0){
    		if(field->{% METHOD java.lang.reflect.Field:isStatic %}()){
    			return (jfieldID)field;
    		}
    	}
    }
    return nullptr;
}

jfieldID JNICALL  GetStaticFieldID(JNIEnv *env, jclass clazz, const char *name, const char *sig){
	// TODO force init of clazz if not already initialised
	return jtvmGetStaticFieldID(env, reinterpret_cast<JAVA_OBJECT>(clazz), name, sig);
}



jobject JNICALL GetStaticObjectField(JNIEnv *env, jclass clazz, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return (jobject) field->{% METHOD java.lang.reflect.Field:get %}(reinterpret_cast<JAVA_OBJECT>(clazz));
}

jboolean JNICALL GetStaticBooleanField(JNIEnv *env, jclass clazz, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getBoolean %}(reinterpret_cast<JAVA_OBJECT>(clazz));
}

jbyte JNICALL GetStaticByteField(JNIEnv *env, jclass clazz, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getByte %}(reinterpret_cast<JAVA_OBJECT>(clazz));
}

jchar JNICALL GetStaticCharField(JNIEnv *env, jclass clazz, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getChar %}(reinterpret_cast<JAVA_OBJECT>(clazz));
}

jshort JNICALL GetStaticShortField(JNIEnv *env, jclass clazz, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getShort %}(reinterpret_cast<JAVA_OBJECT>(clazz));
}

jint JNICALL GetStaticIntField(JNIEnv *env, jclass clazz, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getInt %}(reinterpret_cast<JAVA_OBJECT>(clazz));
}

jlong JNICALL GetStaticLongField(JNIEnv *env, jclass clazz, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getLong %}(reinterpret_cast<JAVA_OBJECT>(clazz));
}

jfloat JNICALL GetStaticFloatField(JNIEnv *env, jclass clazz, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getFloat %}(reinterpret_cast<JAVA_OBJECT>(clazz));
}

jdouble JNICALL GetStaticDoubleField(JNIEnv *env, jclass clazz, jfieldID fieldID){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (JAVA_OBJECT)fieldID);
	return field->{% METHOD java.lang.reflect.Field:getDouble %}(reinterpret_cast<JAVA_OBJECT>(clazz));
}

void JNICALL  SetStaticObjectField(JNIEnv *env, jclass clazz, jfieldID fieldID, jobject value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:set %}(reinterpret_cast<JAVA_OBJECT>(clazz), reinterpret_cast<JAVA_OBJECT>(value));
}

void JNICALL  SetStaticBooleanField(JNIEnv *env, jclass clazz, jfieldID fieldID, jboolean value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setBoolean %}(reinterpret_cast<JAVA_OBJECT>(clazz), value);
}

void JNICALL  SetStaticByteField(JNIEnv *env, jclass clazz, jfieldID fieldID, jbyte value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setByte %}(reinterpret_cast<JAVA_OBJECT>(clazz), value);
}

void JNICALL  SetStaticCharField(JNIEnv *env, jclass clazz, jfieldID fieldID, jchar value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setChar %}(reinterpret_cast<JAVA_OBJECT>(clazz), value);
}

void JNICALL  SetStaticShortField(JNIEnv *env, jclass clazz, jfieldID fieldID, jshort value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setShort %}(reinterpret_cast<JAVA_OBJECT>(clazz), value);
}

void JNICALL  SetStaticIntField(JNIEnv *env, jclass clazz, jfieldID fieldID, jint value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setInt %}(reinterpret_cast<JAVA_OBJECT>(clazz), value);
}

void JNICALL  SetStaticLongField(JNIEnv *env, jclass clazz, jfieldID fieldID, jlong value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setLong %}(reinterpret_cast<JAVA_OBJECT>(clazz), value);
}

void JNICALL  SetStaticFloatField(JNIEnv *env, jclass clazz, jfieldID fieldID, jfloat value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setFloat %}(reinterpret_cast<JAVA_OBJECT>(clazz), value);
}

void JNICALL  SetStaticDoubleField(JNIEnv *env, jclass clazz, jfieldID fieldID, jdouble value){
	auto field = GET_OBJECT({% CLASS java.lang.reflect.Field %}, (reinterpret_cast<JAVA_OBJECT>(fieldID)));
	field->{% METHOD java.lang.reflect.Field:setDouble %}(reinterpret_cast<JAVA_OBJECT>(clazz), value);
}






JAVA_OBJECT jtvmNewDirectByteBuffer(JNIEnv* env, void* address, jlong capacity){
	#ifdef INLINE_ARRAYS
	std::cout << "jtvmNewDirectByteBuffer not implemented with inline arrays!\n";
	abort();
	JA_B* byteArray = nullptr;
	#else
	JA_B* byteArray = __GC_ALLOC<JA_B>(address, (jint)capacity);
	#endif
	return {% CONSTRUCTOR java.nio.ByteBuffer:([BZ)V %}(byteArray, (int8_t)true);
}

jobject JNICALL NewDirectByteBuffer(JNIEnv* env, void* address, jlong capacity){
	return (jobject) jtvmNewDirectByteBuffer(env, address, capacity);
}

void* jtvmGetDirectBufferAddress(JNIEnv* env, JAVA_OBJECT buf){
	auto buffer = GET_OBJECT({% CLASS java.nio.ByteBuffer %}, buf);
	//TODO check that this is a direct buffer
	return GET_OBJECT(JA_B, buffer->{% FIELD java.nio.ByteBuffer:backingArray %})->getStartPtr();
}

void* JNICALL GetDirectBufferAddress(JNIEnv* env, jobject buf){
	return jtvmGetDirectBufferAddress(env, (JAVA_OBJECT) buf);
}

jlong jtvmGetDirectBufferCapacity(JNIEnv* env, JAVA_OBJECT buf){
	auto buffer = GET_OBJECT({% CLASS java.nio.ByteBuffer %}, buf);
    return GET_OBJECT(JA_B, buffer->{% FIELD java.nio.ByteBuffer:backingArray %})->length;
}

jlong JNICALL GetDirectBufferCapacity(JNIEnv* env, jobject buf){
	return jtvmGetDirectBufferCapacity(env, (JAVA_OBJECT) buf);
}

jsize jtvmGetArrayLength(JNIEnv* env, jarray array){
	return ((JA_0*)array)->length;
}

jsize JNICALL GetArrayLength(JNIEnv* env, jarray array){
	return jtvmGetArrayLength(env, array);
}

jobjectArray JNICALL NewObjectArray(JNIEnv *env, jsize length, jclass elementClass, jobject initialElement){
	JA_L* array = __GC_ALLOC_JA_L(length, L"[Ljava/lang/Object"); // TODO fix me!
	for(int32_t i = 0; i < length; i++){
		array->fastSet(i, (JAVA_OBJECT)initialElement);
	}
	return (jobjectArray)array;
}

jobject JNICALL GetObjectArrayElement(JNIEnv *env, jobjectArray array, jsize index){
	JA_L* arr = (JA_L*)array;
	return (jobject)arr->fastGet(index); // TODO indexoutofbounds check
}

void JNICALL  SetObjectArrayElement(JNIEnv *env, jobjectArray array, jsize index, jobject value){
	JA_L* arr = (JA_L*)array;
	arr->fastSet(index, (JAVA_OBJECT)value); // TODO indexoutofbounds check
}

void* jtvmGetUniversalArrayElements(JNIEnv *env, JA_0* array, jboolean *isCopy){
	if(isCopy) *isCopy = false;
	return array->getStartPtrRaw();
}

jboolean* JNICALL GetBooleanArrayElements(JNIEnv *env, jbooleanArray array, jboolean *isCopy){
	return (jboolean*) jtvmGetUniversalArrayElements(env, (JA_Z*) array, isCopy);
}

jbyte* JNICALL GetByteArrayElements(JNIEnv *env, jbyteArray array, jboolean *isCopy){
	return (jbyte*) jtvmGetUniversalArrayElements(env, (JA_0*) array, isCopy);
}

jchar* JNICALL GetCharArrayElements(JNIEnv *env, jcharArray array, jboolean *isCopy){
	return (jchar*) jtvmGetUniversalArrayElements(env, (JA_0*) array, isCopy);
}

jshort* JNICALL GetShortArrayElements(JNIEnv *env, jshortArray array, jboolean *isCopy){
	return (jshort*) jtvmGetUniversalArrayElements(env, (JA_0*) array, isCopy);
}

jint* JNICALL GetIntArrayElements(JNIEnv *env, jintArray array, jboolean *isCopy){
	return (jint*) jtvmGetUniversalArrayElements(env, (JA_0*) array, isCopy);
}

jlong* JNICALL GetLongArrayElements(JNIEnv *env, jlongArray array, jboolean *isCopy){
	return (jlong*) jtvmGetUniversalArrayElements(env, (JA_0*) array, isCopy);
}

jfloat* JNICALL GetFloatArrayElements(JNIEnv *env, jfloatArray array, jboolean *isCopy){
	return (jfloat*) jtvmGetUniversalArrayElements(env, (JA_0*) array, isCopy);
}

jdouble* JNICALL GetDoubleArrayElements(JNIEnv *env, jdoubleArray array, jboolean *isCopy){
	return (jdouble*) jtvmGetUniversalArrayElements(env, (JA_0*) array, isCopy);
}









void JNICALL  ReleaseBooleanArrayElements(JNIEnv *env, jbooleanArray array, jboolean *elems, jint mode){
}

void JNICALL  ReleaseByteArrayElements(JNIEnv *env, jbyteArray array, jbyte *elems, jint mode){
}

void JNICALL  ReleaseCharArrayElements(JNIEnv *env, jcharArray array, jchar *elems, jint mode){
}

void JNICALL  ReleaseShortArrayElements(JNIEnv *env, jshortArray array, jshort *elems, jint mode){
}

void JNICALL  ReleaseIntArrayElements(JNIEnv *env, jintArray array, jint *elems, jint mode){
}

void JNICALL  ReleaseLongArrayElements(JNIEnv *env, jlongArray array, jlong *elems, jint mode){
}

void JNICALL  ReleaseFloatArrayElements(JNIEnv *env, jfloatArray array, jfloat *elems, jint mode){
}

void JNICALL  ReleaseDoubleArrayElements(JNIEnv *env, jdoubleArray array, jdouble *elems, jint mode){
}


static jboolean checkBounds(JNIEnv* env, JA_0* array, jint start, jint len){
	jsize arrayLength = array->length;
	jsize end = start + arrayLength;
	if(start < 0 || len < 0 || end > arrayLength){
		//jtvmThrowException();
		return false;
	}
	return true;
}

void JNICALL  GetBooleanArrayRegion(JNIEnv *env, jbooleanArray array, jsize start, jsize len, jboolean *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jboolean* )((JA_Z*) array)->getStartPtr()) + start, sizeof(jboolean) * len);
}

void JNICALL  GetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len, jbyte *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jbyte* )((JA_B*) array)->getStartPtr()) + start, sizeof(jbyte) * len);
}

void JNICALL  GetCharArrayRegion(JNIEnv *env, jcharArray array, jsize start, jsize len, jchar *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jchar* )((JA_C*) array)->getStartPtr()) + start, sizeof(jchar) * len);
}

void JNICALL  GetShortArrayRegion(JNIEnv *env, jshortArray array, jsize start, jsize len, jshort *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jshort* )((JA_S*) array)->getStartPtr()) + start, sizeof(jshort) * len);
}

void JNICALL  GetIntArrayRegion(JNIEnv *env, jintArray array, jsize start, jsize len, jint *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jint* )((JA_I*) array)->getStartPtr()) + start, sizeof(jint) * len);
}

void JNICALL  GetLongArrayRegion(JNIEnv *env, jlongArray array, jsize start, jsize len, jlong *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jlong* )((JA_J*) array)->getStartPtr()) + start, sizeof(jlong) * len);
}

void JNICALL  GetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize start, jsize len, jfloat *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jfloat* )((JA_F*) array)->getStartPtr()) + start, sizeof(jfloat) * len);
}

void JNICALL  GetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize start, jsize len, jdouble *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jdouble* )((JA_D*) array)->getStartPtr()) + start, sizeof(jdouble) * len);
}

void JNICALL  SetBooleanArrayRegion(JNIEnv *env, jbooleanArray array, jsize start, jsize len, const jboolean *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jboolean* )((JA_Z*) array)->getStartPtr()) + start, buf, sizeof(jboolean) * len);
}

void JNICALL  SetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len, const jbyte *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jbyte* )((JA_B*) array)->getStartPtr()) + start, buf, sizeof(jbyte) * len);
}

void JNICALL  SetCharArrayRegion(JNIEnv *env, jcharArray array, jsize start, jsize len, const jchar *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jchar* )((JA_C*) array)->getStartPtr()) + start, buf, sizeof(jchar) * len);
}

void JNICALL  SetShortArrayRegion(JNIEnv *env, jshortArray array, jsize start, jsize len, const jshort *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jshort* )((JA_S*) array)->getStartPtr()) + start, buf, sizeof(jshort) * len);
}

void JNICALL  SetIntArrayRegion(JNIEnv *env, jintArray array, jsize start, jsize len, const jint *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jint* )((JA_I*) array)->getStartPtr()) + start, buf, sizeof(jint) * len);
}

void JNICALL  SetLongArrayRegion(JNIEnv *env, jlongArray array, jsize start, jsize len, const jlong *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jlong* )((JA_J*) array)->getStartPtr()) + start, buf, sizeof(jlong) * len);
}

void JNICALL  SetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize start, jsize len, const jfloat *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jfloat* )((JA_F*) array)->getStartPtr()) + start, buf, sizeof(jfloat) * len);
}

void JNICALL  SetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize start, jsize len, const jdouble *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jdouble* )((JA_D*) array)->getStartPtr()) + start, buf, sizeof(jdouble) * len);
}


void* JNICALL GetPrimitiveArrayCritical(JNIEnv *env, jarray array, jboolean *isCopy){
	if(isCopy) *isCopy = false;
	return ((JA_0*) array)->getStartPtrRaw();
}

void JNICALL ReleasePrimitiveArrayCritical(JNIEnv *env, jarray array, void *carray, jint mode){

}

JA_Z* jtvmNewBooleanArray(JNIEnv* env, jsize length){
	JA_Z* out = __GC_ALLOC_JA_Z(length);
    return out;
}

jbooleanArray JNICALL NewBooleanArray(JNIEnv* env, jsize length){
	return (jbooleanArray) jtvmNewBooleanArray(env, length);
}


JA_B* jtvmNewByteArray(JNIEnv* env, jsize length){
	JA_B* out = __GC_ALLOC_JA_B(length);
    return out;
}

jbyteArray JNICALL NewByteArray(JNIEnv* env, jsize length){
	return (jbyteArray) jtvmNewByteArray(env, length);
}


JA_C* jtvmNewCharArray(JNIEnv* env, jsize length){
	JA_C* out = __GC_ALLOC_JA_C(length);
    return out;
}

jcharArray JNICALL NewCharArray(JNIEnv* env, jsize length){
	return (jcharArray) jtvmNewCharArray(env, length);
}


JA_S* jtvmNewShortArray(JNIEnv* env, jsize length){
	JA_S* out = __GC_ALLOC_JA_S(length);
    return out;
}

jshortArray JNICALL NewShortArray(JNIEnv* env, jsize length){
	return (jshortArray) jtvmNewShortArray(env, length);
}

JA_I* jtvmNewIntArray(JNIEnv* env, jsize length){
	JA_I* out = __GC_ALLOC_JA_I(length);
    return out;
}

jintArray JNICALL NewIntArray(JNIEnv* env, jsize length){
	return (jintArray) jtvmNewIntArray(env, length);
}

JA_J* jtvmNewLongArray(JNIEnv* env, jsize length){
	JA_J* out = __GC_ALLOC_JA_J(length);
    return out;
}

jlongArray JNICALL NewLongArray(JNIEnv* env, jsize length){
	return (jlongArray) jtvmNewLongArray(env, length);
}

JA_F* jtvmNewFloatArray(JNIEnv* env, jsize length){
	JA_F* out = __GC_ALLOC_JA_F(length);
    return out;
}

jfloatArray JNICALL NewFloatArray(JNIEnv* env, jsize length){
	return (jfloatArray) jtvmNewFloatArray(env, length);
}

JA_D* jtvmNewDoubleArray(JNIEnv* env, jsize length){
	JA_D* out = __GC_ALLOC_JA_D(length);
    return out;
}

jdoubleArray JNICALL NewDoubleArray(JNIEnv* env, jsize length){
	return (jdoubleArray) jtvmNewDoubleArray(env, length);
}

JNIEnv* N::getJniEnv(){
	return &N::env.jni;
}

jint JNICALL GetVersion(JNIEnv* env){
	return JNI_VERSION_1_6;
}

jclass JNICALL DefineClass(JNIEnv *env, const char *name, jobject loader, const jbyte *buf, jsize bufLen){
	throw "Unsupported Operation"; // TODO add proper exception
	return NULL;
}

JAVA_OBJECT jtvmFindClass(JNIEnv* env, const char *name){
	return N::resolveClass(N::istr2(N::str(name)));
	// FIXME horribly inefficient
	// FIXME semantics are probably wrong
}

jclass JNICALL FindClass(JNIEnv* env, const char *name){
	return (jclass) jtvmFindClass(env, name);
}

JAVA_OBJECT jtvmGetObjectClass(JNIEnv *env, JAVA_OBJECT obj){
	return obj->{% METHOD java.lang.Object:getClass %}();
}

jclass JNICALL GetObjectClass(JNIEnv *env, jobject obj){
	return (jclass) jtvmGetObjectClass(env, (JAVA_OBJECT) obj);
}

bool jtvmIsInstanceOf(JNIEnv *env, JAVA_OBJECT obj, JAVA_OBJECT clazz){
	return N::is(obj, GET_OBJECT_NPE({% CLASS java.lang.Class %}, clazz)->{% FIELD java.lang.Class:id %}); // FIXME verification and stuff...
}

jboolean JNICALL IsInstanceOf(JNIEnv *env, jobject obj, jclass clazz){
	return jtvmIsInstanceOf(env, (JAVA_OBJECT) obj, (JAVA_OBJECT) clazz);
}

const struct JNINativeInterface_ jni = {

	nullptr,
	nullptr,
	nullptr,
	nullptr,
	&GetVersion,

	&DefineClass,
	&FindClass,

	&FromReflectedMethod,
	&FromReflectedField,
	&ToReflectedMethod,

	&GetSuperclass,
	&IsAssignableFrom,

	&ToReflectedField,

	nullptr,
	nullptr,
	nullptr,
	nullptr,
	nullptr,
	nullptr,

	&PushLocalFrame,
	&PopLocalFrame,

	nullptr,
	nullptr,
	nullptr,
	nullptr,
	nullptr,
	&EnsureLocalCapacity,

	&AllocObject,
	&NewObject,
	&NewObjectV,
	&NewObjectA,

	&GetObjectClass,
	&IsInstanceOf,

	&GetMethodID,

	&CallObjectMethod,
    &CallObjectMethodV,
    &CallObjectMethodA,
    &CallBooleanMethod,
    &CallBooleanMethodV,
    &CallBooleanMethodA,
    &CallByteMethod,
    &CallByteMethodV,
    &CallByteMethodA,
    &CallCharMethod,
    &CallCharMethodV,
    &CallCharMethodA,
    &CallShortMethod,
    &CallShortMethodV,
    &CallShortMethodA,
    &CallIntMethod,
    &CallIntMethodV,
    &CallIntMethodA,
    &CallLongMethod,
    &CallLongMethodV,
    &CallLongMethodA,
    &CallFloatMethod,
    &CallFloatMethodV,
    &CallFloatMethodA,
    &CallDoubleMethod,
    &CallDoubleMethodV,
    &CallDoubleMethodA,
    &CallVoidMethod,
    &CallVoidMethodV,
    &CallVoidMethodA,

	&CallNonvirtualObjectMethod,
    &CallNonvirtualObjectMethodV,
    &CallNonvirtualObjectMethodA,
    &CallNonvirtualBooleanMethod,
    &CallNonvirtualBooleanMethodV,
    &CallNonvirtualBooleanMethodA,
    &CallNonvirtualByteMethod,
    &CallNonvirtualByteMethodV,
    &CallNonvirtualByteMethodA,
    &CallNonvirtualCharMethod,
    &CallNonvirtualCharMethodV,
    &CallNonvirtualCharMethodA,
    &CallNonvirtualShortMethod,
    &CallNonvirtualShortMethodV,
    &CallNonvirtualShortMethodA,
    &CallNonvirtualIntMethod,
    &CallNonvirtualIntMethodV,
    &CallNonvirtualIntMethodA,
    &CallNonvirtualLongMethod,
    &CallNonvirtualLongMethodV,
    &CallNonvirtualLongMethodA,
    &CallNonvirtualFloatMethod,
    &CallNonvirtualFloatMethodV,
    &CallNonvirtualFloatMethodA,
    &CallNonvirtualDoubleMethod,
    &CallNonvirtualDoubleMethodV,
    &CallNonvirtualDoubleMethodA,
    &CallNonvirtualVoidMethod,
    &CallNonvirtualVoidMethodV,
    &CallNonvirtualVoidMethodA,

	&GetFieldID,

	&GetObjectField,
    &GetBooleanField,
    &GetByteField,
    &GetCharField,
    &GetShortField,
    &GetIntField,
    &GetLongField,
    &GetFloatField,
    &GetDoubleField,
    &SetObjectField,
    &SetBooleanField,
    &SetByteField,
    &SetCharField,
    &SetShortField,
    &SetIntField,
    &SetLongField,
    &SetFloatField,
    &SetDoubleField,

	&GetStaticMethodID,

    &CallStaticObjectMethod,
    &CallStaticObjectMethodV,
    &CallStaticObjectMethodA,
    &CallStaticBooleanMethod,
    &CallStaticBooleanMethodV,
    &CallStaticBooleanMethodA,
    &CallStaticByteMethod,
    &CallStaticByteMethodV,
    &CallStaticByteMethodA,
    &CallStaticCharMethod,
    &CallStaticCharMethodV,
    &CallStaticCharMethodA,
    &CallStaticShortMethod,
    &CallStaticShortMethodV,
    &CallStaticShortMethodA,
    &CallStaticIntMethod,
    &CallStaticIntMethodV,
    &CallStaticIntMethodA,
    &CallStaticLongMethod,
    &CallStaticLongMethodV,
    &CallStaticLongMethodA,
    &CallStaticFloatMethod,
    &CallStaticFloatMethodV,
    &CallStaticFloatMethodA,
    &CallStaticDoubleMethod,
    &CallStaticDoubleMethodV,
    &CallStaticDoubleMethodA,
    &CallStaticVoidMethod,
    &CallStaticVoidMethodV,
    &CallStaticVoidMethodA,

	&GetStaticFieldID,

	&GetStaticObjectField,
	&GetStaticBooleanField,
	&GetStaticByteField,
	&GetStaticCharField,
	&GetStaticShortField,
	&GetStaticIntField,
	&GetStaticLongField,
	&GetStaticFloatField,
	&GetStaticDoubleField,

	&SetStaticObjectField,
    &SetStaticBooleanField,
    &SetStaticByteField,
    &SetStaticCharField,
    &SetStaticShortField,
    &SetStaticIntField,
    &SetStaticLongField,
    &SetStaticFloatField,
    &SetStaticDoubleField,

	nullptr,

	nullptr,
	nullptr,
	nullptr,

	nullptr,
	nullptr,
	nullptr,
	nullptr,

	&GetArrayLength,

	&NewObjectArray,
    &GetObjectArrayElement,
    &SetObjectArrayElement,

	&NewBooleanArray,
    &NewByteArray,
    &NewCharArray,
    &NewShortArray,
    &NewIntArray,
    &NewLongArray,
    &NewFloatArray,
    &NewDoubleArray,

	&GetBooleanArrayElements,
	&GetByteArrayElements,
	&GetCharArrayElements,
	&GetShortArrayElements,
	&GetIntArrayElements,
	&GetLongArrayElements,
	&GetFloatArrayElements,
	&GetDoubleArrayElements,

	&ReleaseBooleanArrayElements,
	&ReleaseByteArrayElements,
	&ReleaseCharArrayElements,
	&ReleaseShortArrayElements,
	&ReleaseIntArrayElements,
	&ReleaseLongArrayElements,
	&ReleaseFloatArrayElements,
	&ReleaseDoubleArrayElements,

	&GetBooleanArrayRegion,
    &GetByteArrayRegion,
    &GetCharArrayRegion,
    &GetShortArrayRegion,
    &GetIntArrayRegion,
    &GetLongArrayRegion,
    &GetFloatArrayRegion,
    &GetDoubleArrayRegion,
    &SetBooleanArrayRegion,
    &SetByteArrayRegion,
    &SetCharArrayRegion,
    &SetShortArrayRegion,
    &SetIntArrayRegion,
    &SetLongArrayRegion,
    &SetFloatArrayRegion,
    &SetDoubleArrayRegion,

	nullptr,
	nullptr,

	nullptr,
	nullptr,

	nullptr,

	nullptr,
    nullptr,

	&GetPrimitiveArrayCritical,
    &ReleasePrimitiveArrayCritical,

	nullptr,
	nullptr,

	nullptr,
	nullptr,

	nullptr,

	&NewDirectByteBuffer,
	&GetDirectBufferAddress,
	&GetDirectBufferCapacity,

	nullptr
};
