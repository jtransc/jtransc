#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#define _CRT_SECURE_NO_DEPRECATE
#include <windows.h>
#endif



#ifdef _WIN32
	#define _JTRANSC_UNIX_LIKE_ 0
    #define _JTRANSC_WINDOWS_ 1
#elif __APPLE__
    #define _JTRANSC_UNIX_LIKE_ 1
    #define _JTRANSC_WINDOWS_ 0
#elif __linux__
    #define _JTRANSC_UNIX_LIKE_ 1
    #define _JTRANSC_WINDOWS_ 0
#elif __unix__
    #define _JTRANSC_UNIX_LIKE_ 1
    #define _JTRANSC_WINDOWS_ 0
#elif defined(_POSIX_VERSION)
    #define _JTRANSC_UNIX_LIKE_ 1
    #define _JTRANSC_WINDOWS_ 0
#else
#   error "Unknown compiler"
#endif

#define jlong_to_ptr(a) ((void *)(uintptr_t)(a))
#define ptr_to_jlong(a) ((jlong)(uintptr_t)(a))



#include <memory>
#include <vector>
#include <string>
#include <sstream>
#include <iostream>
#include <algorithm>
#include <cmath>
//#include <clocale>
#include <csignal>
//#include <chrono>
#include <thread>
#include <mutex>
#include "jni.h"


#if _JTRANSC_UNIX_LIKE_
	#include <dlfcn.h>
#endif

{% for include in CPP_INCLUDES %}
#include <{{ include }}>
{% end %}

//#ifndef USE_PORTABLE_GC
//#define USE_PORTABLE_GC
//#endif

#ifdef USE_PORTABLE_GC
#	include "GC_portable.cpp"
#else
#	include "GC_boehm.cpp"
#endif

//#define DO_NOT_USE_GC // For debugging purposes (to detect crashes related to GC)
#ifdef DO_NOT_USE_GC
	void* JT_MALLOC(std::size_t sz) { void* out = malloc(sz); memset(out, 0, sz); return out; }
	void* JT_MALLOC_ATOMIC(std::size_t sz) { return malloc(sz); }
#else
	void* JT_MALLOC(std::size_t sz) { return GC_MALLOC(sz); }
	void* JT_MALLOC_ATOMIC(std::size_t sz) { return GC_MALLOC_ATOMIC(sz); }
#endif

struct gc {
	void* operator new(std::size_t sz) {
		//std::printf("global op new called, size = %zu\n",sz);
		return JT_MALLOC(sz);
	}
};

#ifdef _WIN32
	#ifdef _WIN64
 		const char *JT_OS = "Windows 64";
 	#else
 		const char *JT_OS = "Windows 32";
 	#endif
 #elif __APPLE__
     #include "TargetConditionals.h"
     #if TARGET_IPHONE_SIMULATOR
 		const char *JT_OS = "iOS Simulator";
     #elif TARGET_OS_IPHONE
 		const char *JT_OS = "iOS Device";
     #elif TARGET_OS_MAC
 		const char *JT_OS = "MacOSX";
     #else
 	 const char *JT_OS = "Apple Unknown";
     #endif
 #elif __linux__
 	const char *JT_OS = "Linux";
 #elif __unix__
 	const char *JT_OS = "Unix";
 #elif defined(_POSIX_VERSION)
 	const char *JT_OS = "Posix";
 #else
 	const char *JT_OS = "Unknown";
 #endif

extern "C" {
	#include <stdio.h>
	#include <wchar.h>
	#include <string.h>
	#ifndef _WIN32
		#include <sys/stat.h>
		#include <unistd.h>
	#endif
	#include <math.h>
	#include <stdlib.h>
	#include <stdint.h>
}

#undef min
#undef max

#if defined(S_IFREG) && !defined(_S_IFREG)
	#define _S_IFREG S_IFREG
	#define _S_IFDIR S_IFDIR
#endif

typedef float float32_t;
typedef double float64_t;
typedef int8_t JT_BOOL;

// HEADERS + INCLUDES
{{ HEADER }}

int TRACE_INDENT = 0;

typedef struct { int32_t x, y; } Int32x2;
typedef struct { float32_t x, y, z, w; } Float32x4;
typedef struct { Float32x4 x, y, z, w; } Float32x4x4;

inline Int32x2 Int32x2_i(int32_t x, int32_t y) { return {x, y}; }
inline Float32x4 Float32x4_i() { return {0, 0, 0, 0}; }
inline Float32x4 Float32x4_i(float x, float y, float z, float w) { return {x, y, z, w}; }

inline Float32x4x4 Float32x4x4_i() { return { Float32x4_i(), Float32x4_i(), Float32x4_i(), Float32x4_i() }; }
inline Float32x4x4 Float32x4x4_i(Float32x4 x, Float32x4 y, Float32x4 z, Float32x4 w) { return { x, y, z, w }; }

inline Float32x4 operator-(const Float32x4& l) { return {-l.x, -l.y, -l.z, -l.w}; };
inline Float32x4 operator+(const Float32x4& l, const Float32x4& r) { return {l.x+r.x, l.y+r.y, l.z+r.z, l.w+r.w}; };
inline Float32x4 operator-(const Float32x4& l, const Float32x4& r) { return {l.x-r.x, l.y-r.y, l.z-r.z, l.w-r.w}; };
inline Float32x4 operator*(const Float32x4& l, const Float32x4& r) { return {l.x*r.x, l.y*r.y, l.z*r.z, l.w*r.w}; };
inline Float32x4 operator/(const Float32x4& l, const Float32x4& r) { return {l.x/r.x, l.y/r.y, l.z/r.z, l.w/r.w}; };
inline Float32x4 operator*(const Float32x4& l, float r) { return {l.x*r, l.y*r, l.z*r, l.w*r}; };

inline float     sum(const Float32x4& l) { return l.x + l.y + l.z + l.w; };
inline Float32x4 abs(const Float32x4& l) { return { std::fabs(l.x), std::fabs(l.y), std::fabs(l.z), std::fabs(l.w)}; };
inline Float32x4 min(const Float32x4& l, const Float32x4& r){ return { std::fmin(l.x, r.x), std::fmin(l.y, r.y), std::fmin(l.z, r.z), std::fmin(l.w, r.w)}; };
inline Float32x4 max(const Float32x4& l, const Float32x4& r){ return { std::fmax(l.x, r.x), std::fmax(l.y, r.y), std::fmax(l.z, r.z), std::fmax(l.w, r.w)}; };

struct CLASS_TRACE { public:
	const char* text;
	static void print_indent() { for (int n = 0; n < TRACE_INDENT; n++) putchar(' '); };
	CLASS_TRACE(const char* text) : text(text) { print_indent(); printf("Enter: %s\n", text); TRACE_INDENT++; };
	~CLASS_TRACE() {
		#ifdef TRACING_JUST_ENTER
			TRACE_INDENT--;
		#else
			TRACE_INDENT--; print_indent(); printf("Exit: %s\n", text);
		#endif
	};
};

#if TRACING
	#define TRACE_REGISTER(location) std::shared_ptr<CLASS_TRACE> __CLASS_TRACE(new CLASS_TRACE(location));
#else
	#define TRACE_REGISTER(location) ;
#endif

// For referencing pointers
{{ CLASS_REFERENCES }}

//typedef std::shared_ptr<java_lang_Object> SOBJ;
//typedef std::weak_ptr<java_lang_Object> WOBJ;
typedef java_lang_Object* JAVA_OBJECT;

// generateTypeTableHeader()
{{ TYPE_TABLE_HEADERS }}

#define GET_OBJECT(type, obj) (dynamic_cast<type*>(obj))
#define GET_OBJECT2(ptype, obj) (dynamic_cast<type>(obj))
#define GET_OBJECT_NPE(type, obj) GET_OBJECT(type, N::ensureNpe(obj))

#ifdef DEBUG
#define CHECK_NPE 1
#else
#define CHECK_NPE 0
#endif

{{ ARRAY_TYPES }}

struct N;

// Headers

struct Env {
 	JNIEnv jni;
 	//
};

//template<typename T> p_java_lang_Object CC_GET_OBJECT(T t) {
// 	if (t == nullptr) return nullptr;
// 	return t->__toObj();
//}

//template<typename T> void* CC_GET_VOID(T t) {
// 	if (t == nullptr) return nullptr;
// 	return t->__toVoid();
//}

/*
template <typename TTo> TTo CC_CHECK_CAST1(void* i, int typeId, const char *from, const char *to) {
	//printf("N::CHECK_CAST(%p, %s -> %s)\n", i, from, to);
	if (i == nullptr) return nullptr;
	if (!N::is(i, typeId)) return nullptr;
	TTo res = static_cast<TTo>(i);
	if (res == nullptr) {
		throw {% CONSTRUCTOR java.lang.ClassCastException:(Ljava/lang/String;)V %}(N::str("Class cast error"));
	}
	return res;
}
*/

//template <typename TTo> TTo CC_CHECK_CAST2(p_java_lang_Object i, const char *from, const char *to) {
//	//printf("N::CHECK_CAST(%p, %s -> %s)\n", i, from, to);
//	if (i == nullptr) return nullptr;
//	TTo res = dynamic_cast<TTo>(i);
//	if (res == nullptr) {
//		throw {% CONSTRUCTOR java.lang.ClassCastException:(Ljava/lang/String;)V %}(N::str("Class cast error"));
//	}
//	return res;
//}

struct N { public:
	static Env env;

	static int64_t NAN_LONG;
	static double NAN_DOUBLE;
	static double INFINITY_DOUBLE;

	static int32_t NAN_INT;
	static float NAN_FLOAT;
	static float INFINITY_FLOAT;

	static const int32_t MIN_INT32 = (int32_t)0x80000000;
	static const int32_t MAX_INT32 = (int32_t)0x7FFFFFFF;

	static const int64_t MIN_INT64 = (int64_t)0x8000000000000000;
	static const int64_t MAX_INT64 = (int64_t)0x7FFFFFFFFFFFFFFF;

	static JAVA_OBJECT resolveClass(std::wstring str);
	inline static int64_t lnew(int32_t high, int32_t low);
	static JT_BOOL is(JAVA_OBJECT obj, int32_t type);
	template<typename T> inline static bool is(T* obj, int32_t type) { return is((JAVA_OBJECT)obj, type); }
	static JT_BOOL isArray(JAVA_OBJECT obj);
	static JT_BOOL isArray(JAVA_OBJECT obj, std::wstring desc);
	static JT_BOOL isUnknown(std::shared_ptr<{% CLASS java.lang.Object %}> obj, const char *error);
	static int cmp(double a, double b);
	static int cmpl(double a, double b);
	static int cmpg(double a, double b);
	inline static int32_t ishl(int32_t a, int32_t b);
	inline static int32_t ishr(int32_t a, int32_t b);
	inline static int32_t iushr(int32_t a, int32_t b);

	inline static int64_t bswap64(int64_t a);
	inline static int32_t bswap32(int32_t a);
	inline static int16_t bswap16(int16_t a);

	inline static int32_t ishl_cst(int32_t a, int32_t b);
	inline static int32_t ishr_cst(int32_t a, int32_t b);
	inline static int32_t iushr_cst(int32_t a, int32_t b);

	inline static int32_t idiv (int32_t a, int32_t b);
	inline static int32_t irem (int32_t a, int32_t b);
	inline static int64_t lcmp (int64_t a, int64_t b);
	inline static int64_t ladd (int64_t a, int64_t b);
	inline static int64_t lsub (int64_t a, int64_t b);
	inline static int64_t lmul (int64_t a, int64_t b);
	inline static int64_t ldiv (int64_t a, int64_t b);
	inline static int64_t lrem (int64_t a, int64_t b);
	inline static int64_t land (int64_t a, int64_t b);
	inline static int64_t lor  (int64_t a, int64_t b);
	inline static int64_t lxor (int64_t a, int64_t b);

	inline static int64_t lshl (int64_t a, int b);
	inline static int64_t lshr (int64_t a, int b);
	inline static int64_t lushr(int64_t a, int b);

	inline static int64_t lshl_cst (int64_t a, int b);
	inline static int64_t lshr_cst (int64_t a, int b);
	inline static int64_t lushr_cst(int64_t a, int b);

	inline static int32_t z2i(int32_t v);
	inline static float   j2f(int64_t v);
	inline static double  j2d(int64_t v);
	inline static int64_t i2j(int32_t v);
	inline static int32_t j2i(int64_t v);
	inline static int64_t f2j(float v);
	inline static int64_t d2j(double v);
	inline static int32_t f2i(float v);
	inline static int32_t d2i(double v);
	static void log(std::wstring str);
	static void log(JAVA_OBJECT str);

	{% if ENABLE_TYPING %}
	static p_java_lang_String str(const char *str);
	static p_java_lang_String str(const wchar_t *str, int32_t len);
	static p_java_lang_String str(std::wstring str);
	static p_java_lang_String str(std::string str);
	static p_JA_L strArray(int32_t count, wchar_t **strs);
	static p_JA_L strArray(std::vector<std::wstring> strs);
	static p_JA_L strArray(std::vector<std::string> strs);
	static p_JA_L strEmptyArray();
	{% else %}
	static JAVA_OBJECT str(const char *str);
	static JAVA_OBJECT str(const wchar_t *str, int32_t len);
	static JAVA_OBJECT str(std::wstring str);
	static JAVA_OBJECT str(std::string str);
	static JAVA_OBJECT strArray(int32_t count, wchar_t **strs);
	static JAVA_OBJECT strArray(std::vector<std::wstring> strs);
	static JAVA_OBJECT strArray(std::vector<std::string> strs);
	static JAVA_OBJECT strEmptyArray();
	{% end %}

	static std::wstring istr2(JAVA_OBJECT obj);
	static std::string istr3(JAVA_OBJECT obj);
	static JAVA_OBJECT dummyMethodClass();
	static void throwNpe(const wchar_t *position);
	template<typename T> static T ensureNpe(T obj, const wchar_t *position);
	static void throwNpe();
	template<typename T> static T ensureNpe(T obj);
	static std::vector<JAVA_OBJECT> getVectorOrEmpty(JAVA_OBJECT array);

	template<typename T> static p_java_lang_Object CC_GET_OBJ(T t);
	template<typename TTo, typename TFrom> TTo static CC_CHECK_CLASS(TFrom i, int32_t typeId);
	template<typename T> T static CC_CHECK_UNTYPED(T i, int32_t typeId);
	template<typename TTo, typename TFrom> TTo static CC_CHECK_INTERFACE(TFrom i, int32_t typeId);
	template<typename TTo, typename TFrom> TTo static CC_CHECK_GENERIC(TFrom i);

	static int32_t strLen(JAVA_OBJECT obj);
	static uint16_t strCharAt(JAVA_OBJECT obj, int32_t n);

	static int32_t identityHashCode(JAVA_OBJECT obj);

	static void writeChars(JAVA_OBJECT str, char *out, int32_t len);

	static JAVA_OBJECT    unboxVoid(JAVA_OBJECT obj);
	static JT_BOOL unboxBool(JAVA_OBJECT obj);
	static int8_t unboxByte(JAVA_OBJECT obj);
	static int16_t unboxShort(JAVA_OBJECT obj);
	static uint16_t unboxChar(JAVA_OBJECT obj);
	static int32_t unboxInt(JAVA_OBJECT obj);
	static int64_t unboxLong(JAVA_OBJECT obj);
	static float   unboxFloat(JAVA_OBJECT obj);
	static double  unboxDouble(JAVA_OBJECT obj);

	static JAVA_OBJECT  boxVoid(void);
	static JAVA_OBJECT  boxVoid(JAVA_OBJECT v);
	static JAVA_OBJECT  boxBool(JT_BOOL v);
	static JAVA_OBJECT  boxByte(int8_t v);
	static JAVA_OBJECT  boxShort(int16_t v);
	static JAVA_OBJECT  boxChar(uint16_t v);
	static JAVA_OBJECT  boxInt(int32_t v);
	static JAVA_OBJECT  boxLong(int64_t v);
	static JAVA_OBJECT  boxFloat(float v);
	static JAVA_OBJECT  boxDouble(double v);

	static double getTime();
	static int64_t nanoTime();
	static void startup();

	static void initStringPool();


	static JAVA_OBJECT newBoolArray();
	static JAVA_OBJECT newByteArray();
	static JAVA_OBJECT newShortArray();
	static JAVA_OBJECT newCharArray();
	static JAVA_OBJECT newIntArray();
	static JAVA_OBJECT newLongArray();
	static JAVA_OBJECT newFloatArray();
	static JAVA_OBJECT newDoubleArray();

	static JNIEnv* getJniEnv();

	static void monitorEnter(JAVA_OBJECT obj);
	static void monitorExit(JAVA_OBJECT obj);
};

/* Used for synchronized methods.*/
class SynchronizedMethodLocker{
    JAVA_OBJECT obj;
public:
	SynchronizedMethodLocker(JAVA_OBJECT obj){
	    this->obj=obj;
		N::monitorEnter(obj);
	}
	~SynchronizedMethodLocker(){
		N::monitorExit(this->obj);
	}
};


struct DYN { public:
	static void* openDynamicLib(const char*);
	static void closeDynamicLib(void*);
	static void* findDynamicSymbol(void*, const char*);
	static void* jtvmResolveNative(JAVA_OBJECT, const char*, const char*, void**);
    static void* jtvmResolveNativeMethodImpl(const char*, const char*, JAVA_OBJECT, void**);
};


// Strings
{{ STRINGS }}


/// ARRAY_HEADERS

{{ ARRAY_HEADERS_PRE }}

struct JA_0 : public java_lang_Object { public:
	void *_data;
	int32_t length;
	int8_t elementSize;
	std::wstring desc;
	JA_0(JT_BOOL pointers, void* data, int32_t len, int8_t esize, std::wstring d) : length(len), elementSize(esize), desc(d) {
		this->__JT__CLASS_ID = 1;
		this->_data = data;
	}

	JA_0(JT_BOOL pointers, int32_t len, int8_t esize, std::wstring d) : JA_0(pointers, alloc(pointers, len, esize), len, esize, d) {
	}

	static void* alloc(JT_BOOL pointers, int32_t len, int8_t esize) {
		void * result = nullptr;
		int64_t bytesSize = esize * (len + 1);
		if (pointers) {
			result = (void*)JT_MALLOC(bytesSize);
			// this malloc already clears memory, so it is pointless doing this again
		} else {
			result = (void*)JT_MALLOC_ATOMIC(bytesSize);
			::memset(result, 0, bytesSize);
		}
		return result;
	}

	~JA_0() { /*::free(_data);*/ }
	void *getOffsetPtr(int32_t offset) { return (void*)&(((int8_t *)_data)[offset * elementSize]); }
	void *getStartPtr() { return getOffsetPtr(0); }
	int64_t bytesLength() { return length * elementSize; }
	static void copy(JA_0* src, int32_t srcpos, JA_0* dst, int32_t dstpos, int32_t len) {
		::memmove(dst->getOffsetPtr(dstpos), src->getOffsetPtr(srcpos), len * src->elementSize);
	}
	//JAVA_OBJECT toBoolArray();
	//JAVA_OBJECT toByteArray();
	//JAVA_OBJECT toCharArray();
	//JAVA_OBJECT toShortArray();
	//JAVA_OBJECT toIntArray();
	//JAVA_OBJECT toLongArray();
	//JAVA_OBJECT toFloatArray();
	//JAVA_OBJECT toDoubleArray();
};

template <class T>
struct JA_Base : JA_0 {
	JA_Base(JT_BOOL pointers, int32_t size, std::wstring desc) : JA_0(pointers, size, sizeof(T), desc) {
	};
	JA_Base(JT_BOOL pointers, void* data, int32_t size, std::wstring desc) : JA_0(pointers, data, size, sizeof(T), desc) {
	};

	/*
	* Checks that the offset is in the bounds.
	* Returns true when offset is in bounds, false otherwise.
	*/
	static inline JT_BOOL isInBounds(int32_t offset, int32_t length) {
		return ((uint32_t)offset <= (uint32_t)length);
	};

	/*
	* Checks that the offset is in the bounds.
	* Throws exception on failure.
	*/
	static void checkBoundsThrowing(int32_t offset, int32_t length) {
    	if(((uint32_t)offset <= (uint32_t)length)){
    		// In bounds
    	} else {
    		__throwArrayOutOfBounds(offset, length);
    	}
    };

	static void __throwArrayOutOfBounds(int32_t offset, int32_t length){
		std::wstringstream os;
        os << L"Out of bounds offset: " << offset << L" length: " << length;
        throw os.str();
	}

	T *getStartPtr() { return (T *)_data; }

	#ifdef CHECK_ARRAYS
		void fastSet(int32_t offset, T v) {
			if(isInBounds(offset, length)) ((T*)(this->_data))[offset] = v;
			else __throwArrayOutOfBounds(offset, length);
		};

		T fastGet(int32_t offset) {
			if(isInBounds(offset, length)) return ((T*)(this->_data))[offset];
			else __throwArrayOutOfBounds(offset, length);
		}
	#else
		void fastSet(int32_t offset, T v) { ((T*)(this->_data))[offset] = v; };
		T fastGet(int32_t offset) { return ((T*)(this->_data))[offset]; }
	#endif

	JA_Base<T> *init(int32_t offset, T v) { ((T*)(this->_data))[offset] = v; return this; };

	void set(int32_t offset, T v) { checkBoundsThrowing(offset, length); fastSet(offset, v); };
	T get(int32_t offset) { checkBoundsThrowing(offset, length); return fastGet(offset); };

	void fill(int32_t from, int32_t to, T v) {
		constexpr int32_t typesize = sizeof(T);
		checkBoundsThrowing(from, length);
		checkBoundsThrowing(to - 1, length);
		if ((typesize == 8) && (sizeof(void*) == 4)) { // constexpr (we are on 32-bits but this is a 64-bit size). Let's optimize this since some compilers don't do this for us.
			int32_t* data = (int32_t*)this->_data;
			int32_t from32 = from * 2;
			int32_t to32 = to * 2;
			int32_t* src = (int32_t *)&v;
			int32_t v1 = src[0];
			int32_t v2 = src[1];
			int32_t n = from32;
			while (n < to32) {
				data[n++] = v1;
				data[n++] = v2;
			}
		} else {
			T* data = (T*)this->_data;
			for (int32_t n = from; n < to; n++) data[n] = v;
		}
	};

	JA_Base<T> *setArray(int32_t start, int32_t size, const T *arrays) {
		for (int32_t n = 0; n < size; n++) this->set(start + n, arrays[n]);
		return this;
	};
};

struct JA_B : JA_Base<int8_t> {
	JA_B(int32_t size, std::wstring desc = L"[B") : JA_Base(false, size, desc) { };
	JA_B(void* data, int32_t size, std::wstring desc = L"[B") : JA_Base(false, data, size, desc) { };

	void fill(int32_t from, int32_t to, int8_t v) {
		checkBoundsThrowing(from, length);
        checkBoundsThrowing(to - 1, length);
		::memset((void *)(&((int8_t *)this->_data)[from]), v, (to - from));
	}

	static JA_B* fromArray(std::wstring desc, std::vector<int8_t> array) {
		auto len = (int32_t)array.size();
		auto out = new JA_B(len);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_Z : public JA_B {
	JA_Z(int32_t size, std::wstring desc = L"[Z") : JA_B(size, desc) { };
	JA_Z(void* data, int32_t size, std::wstring desc = L"[Z") : JA_B(data, size, desc) { };

	static JA_Z* fromArray(std::wstring desc, std::vector<int8_t> array) {
		auto len = (int32_t)array.size();
		auto out = new JA_Z(len);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_S : JA_Base<int16_t> {
	JA_S(int32_t size, std::wstring desc = L"[S") : JA_Base(false, size, desc) { };
	JA_S(void* data, int32_t size, std::wstring desc = L"[S") : JA_Base(false, data, size, desc) { };

	static JA_S* fromArray(std::wstring desc, std::vector<int16_t> array) {
		auto len = (int32_t)array.size();
		auto out = new JA_S(len);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_C : JA_Base<uint16_t> {
	JA_C(int32_t size, std::wstring desc = L"[C") : JA_Base(false, size, desc) { };
	JA_C(void* data, int32_t size, std::wstring desc = L"[C") : JA_Base(false, data, size, desc) { };

	static JA_C* fromArray(std::wstring desc, std::vector<uint16_t> array) {
		auto len = (int32_t)array.size();
		auto out = new JA_C(len);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_I : JA_Base<int32_t> {
	JA_I(int32_t size, std::wstring desc = L"[I") : JA_Base(false, size, desc) { };
	JA_I(void* data, int32_t size, std::wstring desc = L"[I") : JA_Base(false, data, size, desc) { };

	// @TODO: Try to move to JA_Base
	static JA_I *fromVector(int32_t *data, int32_t count) {
		return (JA_I * )(new JA_I(count))->setArray(0, count, (const int32_t *)data);
	};

	static JA_I *fromArgValues() { return (JA_I * )(new JA_I(0)); };
	static JA_I *fromArgValues(int32_t a0) { return (JA_I * )(new JA_I(1))->init(0, a0); };
	static JA_I *fromArgValues(int32_t a0, int32_t a1) { return (JA_I * )(new JA_I(2))->init(0, a0)->init(1, a1); };
	static JA_I *fromArgValues(int32_t a0, int32_t a1, int32_t a2) { return (JA_I * )(new JA_I(3))->init(0, a0)->init(1, a1)->init(2, a2); };
	static JA_I *fromArgValues(int32_t a0, int32_t a1, int32_t a2, int32_t a3) { return (JA_I * )(new JA_I(4))->init(0, a0)->init(1, a1)->init(2, a2)->init(3, a3); };

	static JA_I* fromArray(std::wstring desc, std::vector<int32_t> array) {
		auto len = (int32_t)array.size();
		auto out = new JA_I(len);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_J : JA_Base<int64_t> {
	JA_J(int32_t size, std::wstring desc = L"[J") : JA_Base(false, size, desc) { };
	JA_J(void* data, int32_t size, std::wstring desc = L"[J") : JA_Base(false, data, size, desc) { };

	static JA_J* fromArray(std::wstring desc, std::vector<int64_t> array) {
		auto len = (int32_t)array.size();
		auto out = new JA_J(len);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_F : JA_Base<float> {
	JA_F(int32_t size, std::wstring desc = L"[F") : JA_Base(false, size, desc) { };
	JA_F(void* data, int32_t size, std::wstring desc = L"[F") : JA_Base(false, data, size, desc) { };

	static JA_F* fromArray(std::wstring desc, std::vector<float> array) {
		auto len = (int32_t)array.size();
		auto out = new JA_F(len);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_D : JA_Base<double> {
	JA_D(int32_t size, std::wstring desc = L"[D") : JA_Base(false, size, desc) { };
	JA_D(void* data, int32_t size, std::wstring desc = L"[D") : JA_Base(false, data, size, desc) { };

	static JA_D* fromArray(std::wstring desc, std::vector<double> array) {
		auto len = (int32_t)array.size();
		auto out = new JA_D(len);
		for (int n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_L : JA_Base<JAVA_OBJECT> {
	JA_L(int32_t size, std::wstring desc) : JA_Base(true, size, desc) { };
	JA_L(void* data, int32_t size, std::wstring desc) : JA_Base(true, data, size, desc) { };

	std::vector<JAVA_OBJECT> getVector() {
		int32_t len = this->length;
		std::vector<JAVA_OBJECT> out(len);
		for (int32_t n = 0; n < len; n++) out[n] = this->fastGet(n);
		return out;
	}

	static JA_0* createMultiSure(std::wstring desc, std::vector<int32_t> sizes) {
		if (sizes.size() == 0) throw L"Multiarray with zero sizes";

		int32_t size = sizes[0];

		if (sizes.size() == 1) {
			if (desc == std::wstring(L"[Z")) return new JA_Z(size);
			if (desc == std::wstring(L"[B")) return new JA_B(size);
			if (desc == std::wstring(L"[S")) return new JA_S(size);
			if (desc == std::wstring(L"[C")) return new JA_C(size);
			if (desc == std::wstring(L"[I")) return new JA_I(size);
			if (desc == std::wstring(L"[J")) return new JA_J(size);
			if (desc == std::wstring(L"[F")) return new JA_F(size);
			if (desc == std::wstring(L"[D")) return new JA_D(size);
			throw L"Invalid multiarray";
		}

		// std::vector<decltype(myvector)::value_type>(myvector.begin()+N, myvector.end()).swap(myvector);


		auto out = new JA_L(size, desc);
		auto subdesc = desc.substr(1);
		auto subsizes = std::vector<int32_t>(sizes.begin() + 1, sizes.end());
		for (int32_t n = 0; n < size; n++) {
			out->set(n, createMultiSure(subdesc, subsizes));
		}
		return out;
	}

	static JA_L* fromArray(std::wstring desc, std::vector<JAVA_OBJECT> array) {
		auto len = (int32_t)array.size();
		auto out = new JA_L(len, desc);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};

//JAVA_OBJECT JA_0::toBoolArray  () { return new JA_Z((void *)getStartPtr(), bytesLength() / 1); };
//JAVA_OBJECT JA_0::toByteArray  () { return new JA_B((void *)getStartPtr(), bytesLength() / 1); };
//JAVA_OBJECT JA_0::toCharArray  () { return new JA_C((void *)getStartPtr(), bytesLength() / 2); };
//JAVA_OBJECT JA_0::toShortArray () { return new JA_S((void *)getStartPtr(), bytesLength() / 2); };
//JAVA_OBJECT JA_0::toIntArray   () { return new JA_I((void *)getStartPtr(), bytesLength() / 4); };
//JAVA_OBJECT JA_0::toLongArray  () { return new JA_J((void *)getStartPtr(), bytesLength() / 8); };
//JAVA_OBJECT JA_0::toFloatArray () { return new JA_F((void *)getStartPtr(), bytesLength() / 4); };
//JAVA_OBJECT JA_0::toDoubleArray() { return new JA_D((void *)getStartPtr(), bytesLength() / 8); };


{{ ARRAY_HEADERS_POST }}


// Classes IMPLS
{{ CLASSES_IMPL }}

// Map of c++ constructors
// Used for contructing an object without calling the *java constructor*
{{ CPP_CTOR_MAP }}


// N IMPLS

JAVA_OBJECT N::resolveClass(std::wstring str) {
	return {% SMETHOD java.lang.Class:forName0 %}(N::str(str));
};

int64_t N::lnew(int32_t high, int32_t low) {
	return (((int64_t)high) << 32) | (((int64_t)low) << 0);
};

JT_BOOL N::is(JAVA_OBJECT obj, int32_t type) {
	if (obj == nullptr) return false;
	const TYPE_INFO type_info = TYPE_TABLE::TABLE[obj->__JT__CLASS_ID];
	const size_t size = type_info.size;
	const int32_t* subtypes = type_info.subtypes;
    for (size_t i = 0; i < size; i++){
    	if (subtypes[i] == type) return true;
    }
	return false;
};

JT_BOOL N::isArray(JAVA_OBJECT obj) { return GET_OBJECT(JA_0, obj) != nullptr; };
JT_BOOL N::isArray(JAVA_OBJECT obj, std::wstring desc) {
	JA_0* ptr = GET_OBJECT(JA_0, obj);
	JT_BOOL result = (ptr != nullptr) && (ptr->desc == desc);
	if (!result) {
		if (desc.substr(0, 2) == L"[L") {
			return GET_OBJECT(JA_L, obj) != nullptr;
		}
	}
	return result;
};
JT_BOOL N::isUnknown(std::shared_ptr<{% CLASS java.lang.Object %}> obj, const char * error) { throw error; };
int N::cmp(double a, double b) { return (a < b) ? (-1) : ((a > b) ? (+1) : 0); };
int N::cmpl(double a, double b) { return (std::isnan(a) || std::isnan(b)) ? (-1) : N::cmp(a, b); };
int N::cmpg(double a, double b) { return (std::isnan(a) || std::isnan(b)) ? (+1) : N::cmp(a, b); };

int64_t N::NAN_LONG = 0x7FF8000000000000L;
double N::NAN_DOUBLE = *(double*)&(N::NAN_LONG);

int32_t N::NAN_INT = 0x7FC00000;
float N::NAN_FLOAT = *(float*)&(N::NAN_INT);

double N::INFINITY_DOUBLE = (double)INFINITY;
float N::INFINITY_FLOAT = (float)INFINITY;

int FIXSHIFT(int r) {
	if (r < 0) {
		return (32 - ((-r) & 0x1F)) & 0x1F;
	} else {
		return r & 0x1F;
	}
}

int32_t N::ishl(int32_t a, int32_t b) { return (a << FIXSHIFT(b)); }
int32_t N::ishr(int32_t a, int32_t b) { return (a >> FIXSHIFT(b)); }
int32_t N::iushr(int32_t a, int32_t b) { return (int32_t)(((uint32_t)a) >> FIXSHIFT(b)); }

int32_t N::ishl_cst(int32_t a, int32_t b) { return (a << b); }
int32_t N::ishr_cst(int32_t a, int32_t b) { return (a >> b); }
int32_t N::iushr_cst(int32_t a, int32_t b) { return (int32_t)(((uint32_t)a) >> b); }

int32_t N::idiv(int32_t a, int32_t b) {
	if (a == 0) return 0;
	if (b == 0) return 0; // CRASH
	if (a == N::MIN_INT32 && b == -1) { // CRASH TOO
		//printf("aaaaaaaaaaaaaaa\n"); fflush(stdout);
		return N::MIN_INT32; // CRASH TOO?
	}
	return a / b;
}

int32_t N::irem(int32_t a, int32_t b) {
	if (a == 0) return 0;
	if (b == 0) return 0; // CRASH
	if (a == N::MIN_INT32 && b == -1) { // CRASH TOO
		return 0; // CRASH TOO?
	}
	return a % b;
}

//INT_ADD_RANGE_OVERFLOW (a, b, min, max)
//Yield 1 if a + b would overflow in [min,max] integer arithmetic. See above for restrictions.

//INT_SUBTRACT_RANGE_OVERFLOW (a, b, min, max)
//Yield 1 if a - b would overflow in [min,max] integer arithmetic. See above for restrictions.

//INT_NEGATE_RANGE_OVERFLOW (a, min, max)
//Yield 1 if -a would overflow in [min,max] integer arithmetic. See above for restrictions.

//INT_MULTIPLY_RANGE_OVERFLOW (a, b, min, max)
//Yield 1 if a * b would overflow in [min,max] integer arithmetic. See above for restrictions.

//INT_DIVIDE_RANGE_OVERFLOW (a, b, min, max)
//Yield 1 if a / b would overflow in [min,max] integer arithmetic. See above for restrictions. Division overflow can happen on two’s complement hosts when dividing the most negative integer by -1. This macro does not check for division by zero.

//INT_REMAINDER_RANGE_OVERFLOW (a, b, min, max)
//Yield 1 if a % b would overflow in [min,max] integer arithmetic. See above for restrictions. Remainder overflow can happen on two’s complement hosts when dividing the most negative integer by -1; although the mathematical result is always 0, in practice some implementations trap, so this counts as an overflow. This macro does not check for division by zero.

//INT_LEFT_SHIFT_RANGE_OVERFLOW (a, b, min, max)
//Yield 1 if a << b would overflow in [min,max] integer arithmetic. See above for restrictions. Here, min and max are for a only, and b need not be of the same type as the other arguments. The C standard says that behavior is undefined for shifts unless 0≤b<w where w is a’s word width, and that when a is negative then a << b has undefined behavior, but this macro does not check these other restrictions.

int64_t N::lcmp (int64_t a, int64_t b) { return (a < b) ? -1 : ((a > b) ? +1 : 0); }
int64_t N::ladd (int64_t a, int64_t b) { return a + b; }
int64_t N::lsub (int64_t a, int64_t b) { return a - b; }
int64_t N::lmul (int64_t a, int64_t b) { return a * b; }
int64_t N::ldiv (int64_t a, int64_t b) {
	if (a == 0) return 0;
	if (b == 0) return 0;
	//printf("ldiv::: %lld %lld\n", a, N::MIN_INT64);
	if (a == N::MIN_INT64 && b == -1) {
		//printf("aaaaaaaaaaaaaaaaaa\n");
		//-9223372036854775808/-1
		return N::MIN_INT64;
	}
	return a / b;
}
int64_t N::lrem (int64_t a, int64_t b) {
	if (a == 0) return 0;
	if (b == 0) return 0;
	if (a == N::MIN_INT64 && b == -1) return 0;
	return a % b;
}
int64_t N::land (int64_t a, int64_t b) { return a & b; }
int64_t N::lor  (int64_t a, int64_t b) { return a | b; }
int64_t N::lxor (int64_t a, int64_t b) { return a ^ b; }

int LFIXSHIFT(int r) {
	if (r < 0) {
		return (64 - ((-r) & 0x3F)) & 0x3F;
	} else {
		return r & 0x3F;
	}
}

int64_t N::lshl(int64_t a, int b) { return (a << LFIXSHIFT(b)); }
int64_t N::lshr(int64_t a, int b) { return (a >> LFIXSHIFT(b)); }
int64_t N::lushr(int64_t a, int b) { return (int64_t)(((uint64_t)a) >> LFIXSHIFT(b)); }

int64_t N::lshl_cst(int64_t a, int b) { return (a << b); }
int64_t N::lshr_cst(int64_t a, int b) { return (a >> b); }
int64_t N::lushr_cst(int64_t a, int b) { return (int64_t)(((uint64_t)a) >> b); }

int32_t N::z2i(int32_t v) { return (v != 0) ? 1 : 0; }
int64_t N::i2j(int32_t v) { return (int64_t)v; }

float   N::j2f(int64_t v) { return (float)v; }
double  N::j2d(int64_t v) { return (double)v; }
int32_t N::j2i(int64_t v) { return (int32_t)v; }

int64_t N::f2j(float v) {
	if (std::isfinite(v)) {
		return (int64_t)v;
	} else {
		if (std::isnan(v)) {
			return 0;
		} else if (v >= 0) {
			return MAX_INT64;
		} else {
			return MIN_INT64;
		}
	}
}
int64_t N::d2j(double v) {
	if (std::isfinite(v)) {
		return (int64_t)v;
	} else {
		if (std::isnan(v)) {
			return 0;
		} else if (v >= 0) {
			return MAX_INT64;
		} else {
			return MIN_INT64;
		}
	}
}

// TODO: templatize d2i and f2i to write just once
int32_t N::d2i(double v) {
	if (std::isfinite(v)) {
		return (int32_t)v;
	} else {
		if (std::isnan(v)) {
			return 0;
		} else if (v >= 0) {
			return MAX_INT32;
		} else {
			return MIN_INT32;
		}
	}
}

int32_t N::f2i(float v) {
	if (std::isfinite(v)) {
		return (int32_t)v;
	} else {
		if (std::isnan(v)) {
			return 0;
		} else if (v >= 0) {
			return MAX_INT32;
		} else {
			return MIN_INT32;
		}
	}
}

//SOBJ N::strLiteral(wchar_t *ptr, int len) {
//	SOBJ out(new {% CLASS java.lang.String %}());
//	return out.get()->sptr();
//}

{% if ENABLE_TYPING %}p_java_lang_String{% else %}JAVA_OBJECT{% end %}
N::str(const wchar_t *str, int32_t len) {
	p_java_lang_String out = new {% CLASS java.lang.String %}();
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	p_JA_C array = new JA_C(len);
	p_JA_C arrayobj = array;
	uint16_t *ptr = (uint16_t *)array->getStartPtr();
	if (sizeof(wchar_t) == sizeof(uint16_t)) {
		::memcpy((void *)ptr, (void *)str, len * sizeof(uint16_t));
	} else {
		for (int32_t n = 0; n < len; n++) ptr[n] = (uint16_t)str[n];
	}
	out->{% FIELD java.lang.String:value %} = arrayobj;
	//GET_OBJECT({% CLASS java.lang.String %}, out)->M_java_lang_String__init____CII_V(array, 0, len);
	return {% if ENABLE_TYPING %}out{% else %}_out{% end %};
};

{% if ENABLE_TYPING %}p_java_lang_String{% else %}JAVA_OBJECT{% end %}
N::str(std::wstring str) {
	int32_t len = str.length();
	p_java_lang_String out(new {% CLASS java.lang.String %}());
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	p_JA_C array = new JA_C(len);
	p_JA_C arrayobj = array;
	uint16_t *ptr = (uint16_t *)array->getStartPtr();
	for (int32_t n = 0; n < len; n++) ptr[n] = (uint16_t)str[n];
	out->{% FIELD java.lang.String:value %} = arrayobj;
	//GET_OBJECT({% CLASS java.lang.String %}, out)->M_java_lang_String__init____CII_V(array, 0, len);
	return {% if ENABLE_TYPING %}out{% else %}_out{% end %};
};

{% if ENABLE_TYPING %}p_java_lang_String{% else %}JAVA_OBJECT{% end %}
N::str(std::string s) {
	//if (s == nullptr) return SOBJ(nullptr);
	std::wstring ws(s.begin(), s.end());
	return N::str(ws);
};

{% if ENABLE_TYPING %}p_java_lang_String{% else %}JAVA_OBJECT{% end %}
N::str(const char *s) {
	if (s == nullptr) return nullptr;
	int32_t len = strlen(s);
	p_java_lang_String out(new {% CLASS java.lang.String %}());
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	p_JA_C array = new JA_C(len);
	p_JA_C arrayobj = array;
	uint16_t *ptr = (uint16_t *)array->getStartPtr();
	//::memcpy((void *)ptr, (void *)str, len * sizeof(uint16_t));
	for (int32_t n = 0; n < len; n++) ptr[n] = (uint16_t)s[n];
	out->{% FIELD java.lang.String:value %} = arrayobj;
	//GET_OBJECT({% CLASS java.lang.String %}, out)->M_java_lang_String__init____CII_V(array, 0, len);
	return {% if ENABLE_TYPING %}out{% else %}_out{% end %};
};

{% if ENABLE_TYPING %}p_JA_L{% else %}JAVA_OBJECT{% end %}
N::strArray(int32_t count, wchar_t **strs) {
	p_JA_L out = new JA_L(count, L"[java/lang/String;");
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	for (int32_t n = 0; n < count; n++) out->set(n, N::str(std::wstring(strs[n])));
	return {% if ENABLE_TYPING %}out{% else %}_out{% end %};
}

{% if ENABLE_TYPING %}p_JA_L{% else %}JAVA_OBJECT{% end %}
N::strArray(std::vector<std::wstring> strs) {
	int32_t len = strs.size();
	p_JA_L out = new JA_L(len, L"[java/lang/String;");
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	for (int32_t n = 0; n < len; n++) out->set(n, N::str(strs[n]));
	return {% if ENABLE_TYPING %}out{% else %}_out{% end %};
}

{% if ENABLE_TYPING %}p_JA_L{% else %}JAVA_OBJECT{% end %}
N::strArray(std::vector<std::string> strs) {
	int32_t len = strs.size();
	p_JA_L out = new JA_L(len, L"[Ljava/lang/String;");
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	for (int32_t n = 0; n < len; n++) out->set(n, N::str(strs[n]));
	return {% if ENABLE_TYPING %}out{% else %}_out{% end %};
}

{% if ENABLE_TYPING %}p_JA_L{% else %}JAVA_OBJECT{% end %}
N::strEmptyArray() {
	p_JA_L out = new JA_L(0, L"Ljava/lang/String;");
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	return {% if ENABLE_TYPING %}out{% else %}_out{% end %};
}

std::wstring N::istr2(JAVA_OBJECT obj) {
	int32_t len = N::strLen(obj);
	std::wstring s;
	s.reserve(len);
	for (int32_t n = 0; n < len; n++) s.push_back(N::strCharAt(obj, n));
	return s;
}

std::string N::istr3(JAVA_OBJECT obj) {
	int32_t len = N::strLen(obj);
	std::string s;
	s.reserve(len);
	for (int32_t n = 0; n < len; n++) s.push_back(N::strCharAt(obj, n));
	return s;
}

int32_t N::strLen(JAVA_OBJECT obj) {
	auto str = GET_OBJECT({% CLASS java.lang.String %}, obj);
	return str->{% METHOD java.lang.String:length %}();
}

uint16_t N::strCharAt(JAVA_OBJECT obj, int32_t n) {
	auto str = GET_OBJECT({% CLASS java.lang.String %}, obj);
	return str->{% METHOD java.lang.String:charAt %}(n);
}

void N::log(std::wstring str) {
	std::wcout << str << L"\n";
	fflush(stdout);
}

void N::log(JAVA_OBJECT obj) {
	N::log(N::istr2(obj));
}

JAVA_OBJECT N::dummyMethodClass() {
	throw "Not supported java8 method references";
	return nullptr;
}

void N::throwNpe(const wchar_t* position) {
	TRACE_REGISTER("N::throwNpe()");
	std::wcout << L"N::throwNpe():" << std::wstring(position) << L"\n";
	throw {% CONSTRUCTOR java.lang.NullPointerException:()V %}();
}

template<typename T>
T N::ensureNpe(T obj, const wchar_t* position) {
	#ifdef CHECK_NPE
	if (obj == nullptr) N::throwNpe(position);
	#endif
	return obj;
}

void N::throwNpe() {
	N::throwNpe(L"unknown");
}

template<typename T>
T N::ensureNpe(T obj) {
	#ifdef CHECK_NPE
	if (obj == nullptr) N::throwNpe();
	#endif
	return obj;
}


int32_t N::identityHashCode(JAVA_OBJECT obj) {
	return (int32_t)(size_t)(void *)(obj);
}

//TODO signature of *out?
void N::writeChars(JAVA_OBJECT str, char *out, int32_t maxlen) {
	int32_t len = std::min(N::strLen(str), maxlen - 1);
	for (int32_t n = 0; n < len; n++) {
		out[n] = N::strCharAt(str, n);
	}
	out[len] = 0;
}

void __throwCLASSCAST() {
	throw {% CONSTRUCTOR java.lang.ClassCastException:(Ljava/lang/String;)V %}(N::str(L"Class cast error"));
}

template<typename T> p_java_lang_Object N::CC_GET_OBJ(T t) {
 	if (t == nullptr) return nullptr;
 	return t->__getObj();
}

template<typename TTo, typename TFrom> TTo N::CC_CHECK_CLASS(TFrom i, int32_t typeId) {
 	if (i == nullptr) return nullptr;
	if (!N::is(i, typeId)) __throwCLASSCAST();
 	TTo result = dynamic_cast<TTo>(i);
 	if (result == nullptr) __throwCLASSCAST();
 	return result;
}

template<typename T> T N::CC_CHECK_UNTYPED(T i, int32_t typeId) {
 	if (i == nullptr) return nullptr;
	if (!N::is(i, typeId)) __throwCLASSCAST();
 	return i;
}

template<typename TTo, typename TFrom> TTo N::CC_CHECK_INTERFACE(TFrom i, int32_t typeId) {
 	if (i == nullptr) return nullptr;
	if (!N::is(i, typeId)) __throwCLASSCAST();
 	TTo result = static_cast<TTo>(i->__getInterface(typeId));
 	if (result == nullptr) __throwCLASSCAST();
 	return result;
}

template<typename TTo, typename TFrom> TTo N::CC_CHECK_GENERIC(TFrom i) {
 	if (i == nullptr) return nullptr;
 	TTo result = dynamic_cast<TTo>(i);
 	if (result == nullptr) __throwCLASSCAST();
 	return result;
}


#ifndef __has_builtin
  #define __has_builtin(x) 0
#endif

// https://stackoverflow.com/questions/105252/how-do-i-convert-between-big-endian-and-little-endian-values-in-c
// https://msdn.microsoft.com/es-es/library/b0084kay.aspx
#ifdef _MSC_VER
	#include <intrin.h>

	#define JT_HAS_INTRINSIC_BSWAP64
	#define JT_HAS_INTRINSIC_BSWAP32
	#define JT_HAS_INTRINSIC_BSWAP16

	// Unification
	#define __builtin_bswap16 _byteswap_ushort
	#define __builtin_bswap32 _byteswap_ulong
	#define __builtin_bswap64 _byteswap_uint64

#else

	#if defined(__GNUC__) || __has_builtin(__builtin_bswap64)
		#define JT_HAS_INTRINSIC_BSWAP64
	#endif

	#if defined(__GNUC__) || __has_builtin(__builtin_bswap32)
		#define JT_HAS_INTRINSIC_BSWAP32
	#endif

	#if defined(__GNUC__) || __has_builtin(__builtin_bswap16)
		#define JT_HAS_INTRINSIC_BSWAP16
	#endif

#endif

int16_t N::bswap16(int16_t a) {
	#ifdef JT_HAS_INTRINSIC_BSWAP16
		return __builtin_bswap16(a);
	#else
		return ((a & 0xff) << 8) | ((a & 0xff00) >> 8);
	#endif
}

int32_t N::bswap32(int32_t a) {
	#ifdef JT_HAS_INTRINSIC_BSWAP32
		return __builtin_bswap32(a);
	#else
		return (a & 0x000000ff) << 24 | (a & 0x0000ff00) << 8 | (a & 0x00ff0000) >> 8 | (a & 0xff000000) >> 24;
	#endif
}

// https://linux.die.net/man/3/htobe64
int64_t N::bswap64(int64_t a) {
	#ifdef JT_HAS_INTRINSIC_BSWAP64
		return __builtin_bswap64(a);
	#else
		return
			((a << 56) & 0xff00000000000000UL) |
			((a << 40) & 0x00ff000000000000UL) |
			((a << 24) & 0x0000ff0000000000UL) |
			((a <<  8) & 0x000000ff00000000UL) |
			((a >>  8) & 0x00000000ff000000UL) |
			((a >> 24) & 0x0000000000ff0000UL) |
			((a >> 40) & 0x000000000000ff00UL) |
			((a >> 56) & 0x00000000000000ffUL)
		;
	#endif
}

JAVA_OBJECT    N::unboxVoid(JAVA_OBJECT obj) { return (JAVA_OBJECT)nullptr; }
JT_BOOL N::unboxBool(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Boolean %}, obj)->{% SMETHOD java.lang.Boolean:booleanValue %}(); }
int8_t N::unboxByte(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Byte %}, obj)->{% SMETHOD java.lang.Byte:byteValue %}(); }
int16_t N::unboxShort(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Short %}, obj)->{% SMETHOD java.lang.Short:shortValue %}(); }
uint16_t N::unboxChar(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Character %}, obj)->{% SMETHOD java.lang.Character:charValue %}(); }
int32_t N::unboxInt(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Integer %}, obj)->{% SMETHOD java.lang.Integer:intValue %}(); }
int64_t N::unboxLong(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Long %}, obj)->{% SMETHOD java.lang.Long:longValue %}(); }
float   N::unboxFloat(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Float %}, obj)->{% SMETHOD java.lang.Float:floatValue %}(); }
double  N::unboxDouble(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Double %}, obj)->{% SMETHOD java.lang.Double:doubleValue %}(); }

JAVA_OBJECT N::boxVoid(void)       { return (JAVA_OBJECT)nullptr; }
JAVA_OBJECT N::boxVoid(JAVA_OBJECT v)     { return (JAVA_OBJECT)nullptr; }
JAVA_OBJECT N::boxBool(JT_BOOL v)     { return {% SMETHOD java.lang.Boolean:valueOf:(Z)Ljava/lang/Boolean; %}(v); }
JAVA_OBJECT N::boxByte(int8_t v)  { return {% SMETHOD java.lang.Byte:valueOf:(B)Ljava/lang/Byte; %}(v); }
JAVA_OBJECT N::boxShort(int16_t v) { return {% SMETHOD java.lang.Short:valueOf:(S)Ljava/lang/Short; %}(v); }
JAVA_OBJECT N::boxChar(uint16_t v)  { return {% SMETHOD java.lang.Character:valueOf:(C)Ljava/lang/Character; %}(v); }
JAVA_OBJECT N::boxInt(int32_t v)   { return {% SMETHOD java.lang.Integer:valueOf:(I)Ljava/lang/Integer; %}(v); }
JAVA_OBJECT N::boxLong(int64_t v)  { return {% SMETHOD java.lang.Long:valueOf:(J)Ljava/lang/Long; %}(v); }
JAVA_OBJECT N::boxFloat(float v)   { return {% SMETHOD java.lang.Float:valueOf:(F)Ljava/lang/Float; %}(v); }
JAVA_OBJECT N::boxDouble(double v) { return {% SMETHOD java.lang.Double:valueOf:(D)Ljava/lang/Double; %}(v); }

//SOBJ JA_0::{% METHOD java.lang.Object:getClass %}() { return {% SMETHOD java.lang.Class:forName0 %}(N::str(desc)); }

std::vector<JAVA_OBJECT> N::getVectorOrEmpty(JAVA_OBJECT obj) {
	auto array = GET_OBJECT(JA_L, obj);
	if (array == nullptr)  return std::vector<JAVA_OBJECT>(0);
	return array->getVector();
};

#include <chrono>

double N::getTime() {
	using namespace std::chrono;
    milliseconds ms = duration_cast<milliseconds>( system_clock::now().time_since_epoch() );
	return (double)(int64_t)ms.count();
};

int64_t N::nanoTime() {
	using namespace std::chrono;
	auto time = duration_cast<nanoseconds>( high_resolution_clock::now().time_since_epoch() );
	return (int64_t)time.count();
};

void N::monitorEnter(JAVA_OBJECT obj) {
	if (obj == nullptr) return;
	//std::wcout << L"N::monitorEnter[1]\n";
	obj->mtx.lock();
	//std::wcout << L"N::monitorEnter[2]\n";
}

void N::monitorExit(JAVA_OBJECT obj){
	if (obj == nullptr) return;
	//std::wcout << L"N::monitorExit[1]\n";
	obj->mtx.unlock();
	//std::wcout << L"N::monitorExit[2]\n";
}

void SIGSEGV_handler(int signal) {
	std::wcout << L"invalid memory access (segmentation fault)\n";
	throw L"invalid memory access (segmentation fault)";
};

void SIGFPE_handler(int signal) {
	std::wcout << L"erroneous arithmetic operation such as divide by zero\n";
	throw L"erroneous arithmetic operation such as divide by zero";
};

//SIGTERM	termination request, sent to the program
//SIGSEGV	invalid memory access (segmentation fault)
//SIGINT	external interrupt, usually initiated by the user
//SIGILL	invalid program image, such as invalid instruction
//SIGABRT	abnormal termination condition, as is e.g. initiated by std::abort()
//SIGFPE	erroneous arithmetic operation such as divide by zero




	void* DYN::openDynamicLib(const char* libraryName){
    	#if _JTRANSC_WINDOWS_
    	return LoadLibrary(libraryName);

    	#elif _JTRANSC_UNIX_LIKE_
    	return dlopen(libraryName, RTLD_LAZY | RTLD_LOCAL);

    	#else
    	#   error "Dynamic loading unsupported on this target"
    	#endif
	}

	void DYN::closeDynamicLib(void* handle){
		#if _JTRANSC_WINDOWS_
        FreeLibrary((HMODULE)handle);

        #elif _JTRANSC_UNIX_LIKE_
        dlclose(handle);

        #else
        #   error "Dynamic loading unsupported on this target"
        #endif
	}

	void* DYN::findDynamicSymbol(void* handle, const char* symbolToSearch){
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
	JA_L* array = new JA_L(count, L"[Ljava.lang.Object;");
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
	JA_L* array = new JA_L(count, L"[Ljava.lang.Object;");
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
	JA_B* byteArray = new JA_B(address, (jint)capacity);
	return {% CONSTRUCTOR java.nio.ByteBuffer:([BZ)V %}(byteArray, (int8_t)true);
}

jobject JNICALL NewDirectByteBuffer(JNIEnv* env, void* address, jlong capacity){
	return (jobject) jtvmNewDirectByteBuffer(env, address, capacity);
}

void* jtvmGetDirectBufferAddress(JNIEnv* env, JAVA_OBJECT buf){
	auto buffer = GET_OBJECT({% CLASS java.nio.ByteBuffer %}, buf);
	//TODO check that this is a direct buffer
	return GET_OBJECT(JA_B, buffer->{% FIELD java.nio.ByteBuffer:backingArray %})->_data;
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
	JA_L* array = new JA_L(length, L"[Ljava/lang/Object"); // TODO fix me!
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
	return array->_data;
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
	memcpy(buf, ((jboolean* )((JA_Z*) array)->_data) + start, sizeof(jboolean) * len);
}

void JNICALL  GetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len, jbyte *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jbyte* )((JA_B*) array)->_data) + start, sizeof(jbyte) * len);
}

void JNICALL  GetCharArrayRegion(JNIEnv *env, jcharArray array, jsize start, jsize len, jchar *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jchar* )((JA_C*) array)->_data) + start, sizeof(jchar) * len);
}

void JNICALL  GetShortArrayRegion(JNIEnv *env, jshortArray array, jsize start, jsize len, jshort *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jshort* )((JA_S*) array)->_data) + start, sizeof(jshort) * len);
}

void JNICALL  GetIntArrayRegion(JNIEnv *env, jintArray array, jsize start, jsize len, jint *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jint* )((JA_I*) array)->_data) + start, sizeof(jint) * len);
}

void JNICALL  GetLongArrayRegion(JNIEnv *env, jlongArray array, jsize start, jsize len, jlong *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jlong* )((JA_J*) array)->_data) + start, sizeof(jlong) * len);
}

void JNICALL  GetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize start, jsize len, jfloat *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jfloat* )((JA_F*) array)->_data) + start, sizeof(jfloat) * len);
}

void JNICALL  GetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize start, jsize len, jdouble *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jdouble* )((JA_D*) array)->_data) + start, sizeof(jdouble) * len);
}

void JNICALL  SetBooleanArrayRegion(JNIEnv *env, jbooleanArray array, jsize start, jsize len, const jboolean *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jboolean* )((JA_Z*) array)->_data) + start, buf, sizeof(jboolean) * len);
}

void JNICALL  SetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len, const jbyte *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jbyte* )((JA_B*) array)->_data) + start, buf, sizeof(jbyte) * len);
}

void JNICALL  SetCharArrayRegion(JNIEnv *env, jcharArray array, jsize start, jsize len, const jchar *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jchar* )((JA_C*) array)->_data) + start, buf, sizeof(jchar) * len);
}

void JNICALL  SetShortArrayRegion(JNIEnv *env, jshortArray array, jsize start, jsize len, const jshort *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jshort* )((JA_S*) array)->_data) + start, buf, sizeof(jshort) * len);
}

void JNICALL  SetIntArrayRegion(JNIEnv *env, jintArray array, jsize start, jsize len, const jint *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jint* )((JA_I*) array)->_data) + start, buf, sizeof(jint) * len);
}

void JNICALL  SetLongArrayRegion(JNIEnv *env, jlongArray array, jsize start, jsize len, const jlong *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jlong* )((JA_J*) array)->_data) + start, buf, sizeof(jlong) * len);
}

void JNICALL  SetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize start, jsize len, const jfloat *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jfloat* )((JA_F*) array)->_data) + start, buf, sizeof(jfloat) * len);
}

void JNICALL  SetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize start, jsize len, const jdouble *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jdouble* )((JA_D*) array)->_data) + start, buf, sizeof(jdouble) * len);
}


void* JNICALL GetPrimitiveArrayCritical(JNIEnv *env, jarray array, jboolean *isCopy){
	if(isCopy) *isCopy = false;
	return ((JA_0*) array)->_data;
}

void JNICALL ReleasePrimitiveArrayCritical(JNIEnv *env, jarray array, void *carray, jint mode){

}

JA_Z* jtvmNewBooleanArray(JNIEnv* env, jsize length){
	JA_Z* out = new JA_Z(length);
    return out;
}

jbooleanArray JNICALL NewBooleanArray(JNIEnv* env, jsize length){
	return (jbooleanArray) jtvmNewBooleanArray(env, length);
}


JA_B* jtvmNewByteArray(JNIEnv* env, jsize length){
	JA_B* out = new JA_B(length);
    return out;
}

jbyteArray JNICALL NewByteArray(JNIEnv* env, jsize length){
	return (jbyteArray) jtvmNewByteArray(env, length);
}


JA_C* jtvmNewCharArray(JNIEnv* env, jsize length){
	JA_C* out = new JA_C(length);
    return out;
}

jcharArray JNICALL NewCharArray(JNIEnv* env, jsize length){
	return (jcharArray) jtvmNewCharArray(env, length);
}


JA_S* jtvmNewShortArray(JNIEnv* env, jsize length){
	JA_S* out = new JA_S(length);
    return out;
}

jshortArray JNICALL NewShortArray(JNIEnv* env, jsize length){
	return (jshortArray) jtvmNewShortArray(env, length);
}

JA_I* jtvmNewIntArray(JNIEnv* env, jsize length){
	JA_I* out = new JA_I(length);
    return out;
}

jintArray JNICALL NewIntArray(JNIEnv* env, jsize length){
	return (jintArray) jtvmNewIntArray(env, length);
}

JA_J* jtvmNewLongArray(JNIEnv* env, jsize length){
	JA_J* out = new JA_J(length);
    return out;
}

jlongArray JNICALL NewLongArray(JNIEnv* env, jsize length){
	return (jlongArray) jtvmNewLongArray(env, length);
}

JA_F* jtvmNewFloatArray(JNIEnv* env, jsize length){
	JA_F* out = new JA_F(length);
    return out;
}

jfloatArray JNICALL NewFloatArray(JNIEnv* env, jsize length){
	return (jfloatArray) jtvmNewFloatArray(env, length);
}

JA_D* jtvmNewDoubleArray(JNIEnv* env, jsize length){
	JA_D* out = new JA_D(length);
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







Env N::env;

void N::startup() {
	/*
	GC_set_no_dls(0);
	GC_set_dont_precollect(1);
	*/

	//GC_set_all_interior_pointers(0);
	GC_init_main_thread();

	/*
	GC_clear_roots();
	GC_add_roots(&STRINGS_START, &STRINGS_END);
	{% for ptr in CPP_GLOBAL_POINTERS %}
	GC_ADD_ROOT_SINGLE({{ ptr }});
	{% end %}
	*/

	//GC_set_finalize_on_demand(0);

	//std::setlocale(LC_COLLATE, "en_US.UTF-8");
	//std::setlocale(LC_CTYPE, "en_US.UTF-8");

	N::env.jni.functions = &jni;
	setvbuf(stdout, nullptr, _IONBF, 0);
	setvbuf(stderr, nullptr, _IONBF, 0);
	std::signal(SIGSEGV, SIGSEGV_handler);
	std::signal(SIGFPE, SIGFPE_handler);

	N::initStringPool();
};

// Type Table Footer
{{ TYPE_TABLE_FOOTER }}

// Main
{{ MAIN }}