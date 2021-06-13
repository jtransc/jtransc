#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#define _CRT_SECURE_NO_DEPRECATE
#include <windows.h>
#endif

//#define _JTRANSC_USE_DYNAMIC_LIB 1
//#define INLINE_ARRAYS 1
#define INLINE_ARRAYS_OFFSET 0
//#define TRACING 1

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
	#warning "Unknown compiler"
    #define _JTRANSC_UNIX_LIKE_ 1
    #define _JTRANSC_WINDOWS_ 0
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
#include <csignal>
#include "jni.h"

#if _JTRANSC_UNIX_LIKE_
#ifdef _JTRANSC_USE_DYNAMIC_LIB
	#include <dlfcn.h>
#endif
#endif

{% for include in CPP_INCLUDES %}
#include <{{ include }}>
{% end %}

//#ifndef USE_PORTABLE_GC
//#define USE_PORTABLE_GC
//#endif

#include "GC.cpp"

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

const wchar_t *FUNCTION_NAME = L"Unknown";

#if TRACING
	struct CLASS_TRACE {
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

	#define TRACE_REGISTER(location) std::shared_ptr<CLASS_TRACE> __CLASS_TRACE(__GC_ALLOC<CLASS_TRACE>(location));
	#define N_ENSURE_NPE(obj) N::ensureNpe(obj)
#else
	#define TRACE_REGISTER(location) ;
	#define N_ENSURE_NPE(obj) N::ensureNpe(obj)
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

struct N {
	__GC_thread_local static Env env;

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
	static JT_BOOL isFast(JAVA_OBJECT obj, int32_t t0);
	static JT_BOOL isFast(JAVA_OBJECT obj, int32_t t0, int32_t t1);
	static JT_BOOL isFast(JAVA_OBJECT obj, int32_t t0, int32_t t1, int32_t t2);
	static JT_BOOL isFast(JAVA_OBJECT obj, int32_t t0, int32_t t1, int32_t t2, int32_t t3);
	static JT_BOOL isFast(JAVA_OBJECT obj, int32_t t0, int32_t t1, int32_t t2, int32_t t3, int32_t t4);
	static JT_BOOL isFast(JAVA_OBJECT obj, int32_t t0, int32_t t1, int32_t t2, int32_t t3, int32_t t4, int32_t t5);
	static JT_BOOL isFast(JAVA_OBJECT obj, int32_t t0, int32_t t1, int32_t t2, int32_t t3, int32_t t4, int32_t t5, int32_t t6);

	template<typename T> inline static bool is(T* obj, int32_t type) { return is((JAVA_OBJECT)obj, type); }
	static JT_BOOL isArray(JAVA_OBJECT obj);
	static JT_BOOL isArray(JAVA_OBJECT obj, wchar_t *desc);
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

	static JAVA_OBJECT str(const char *str);
	static JAVA_OBJECT str(const wchar_t *str, int32_t len);
	static JAVA_OBJECT strLiteral(const wchar_t *str, int32_t len);
	static JAVA_OBJECT str(std::u16string str);
	static JAVA_OBJECT str(std::wstring str);
	static JAVA_OBJECT str(std::string str);
	static JAVA_OBJECT strArray(int32_t count, wchar_t **strs);
	static JAVA_OBJECT strArray(int32_t count, char **strs);
	static JAVA_OBJECT strArray(std::vector<std::wstring> strs);
	static JAVA_OBJECT strArray(std::vector<std::string> strs);
	static JAVA_OBJECT strEmptyArray();

	static const char16_t* getStrDataPtr(JAVA_OBJECT obj);
	static std::u16string istr(JAVA_OBJECT obj);
	static std::wstring istr2(JAVA_OBJECT obj);
	static std::string istr3(JAVA_OBJECT obj);
	static JAVA_OBJECT dummyMethodClass();
	static void throwNpe(const wchar_t *position);
	template<typename T> static T ensureNpe(T obj, const wchar_t *position);
	static void throwNpe();
	template<typename T> static T ensureNpe(T obj);
	static std::vector<JAVA_OBJECT> getVectorOrEmpty(JAVA_OBJECT array);

	template<typename TTo, typename TFrom> TTo static CC_CHECK_CLASS(TFrom i, int32_t typeId);
	template<typename T> T static CC_CHECK_UNTYPED(T i, int32_t typeId);
	template<typename TTo, typename TFrom> TTo static CC_CHECK_INTERFACE(TFrom i, int32_t typeId);
	template<typename TTo, typename TFrom> TTo static CC_CHECK_GENERIC(TFrom i);

	static int32_t strLen(JAVA_OBJECT obj);
	static uint16_t strCharAt(JAVA_OBJECT obj, int32_t n);
	static JA_C* strGetCharsFast(JAVA_OBJECT obj);

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
	static void startupThread();
	static void initStringPool();
	static void staticInit();

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

struct java_lang_ObjectBase : public __GC {
};

struct DYN {
	static void* openDynamicLib(const char*);
	static void closeDynamicLib(void*);
	static void* findDynamicSymbol(void*, const char*);
	static void* jtvmResolveNative(JAVA_OBJECT, const char*, const char*, void**);
    static void* jtvmResolveNativeMethodImpl(const char*, const char*, JAVA_OBJECT, void**);
};

// Strings
//#define N_ADD_STRING(VAR, STR, LEN) { __GC_DISABLE(); VAR = N::str(STR, LEN); __GC_ADD_ROOT_CONSTANT(#VAR, &STR); __GC_ENABLE(); }
#define N_ADD_STRING(VAR, STR, LEN) { VAR = N::str(STR, LEN); __GC_ADD_ROOT_CONSTANT(#VAR, &VAR); }
{{ STRINGS }}


/// ARRAY_HEADERS

{{ ARRAY_HEADERS_PRE }}

struct JA_Z;
struct JA_B;
struct JA_C;
struct JA_S;
struct JA_I;
struct JA_J;
struct JA_F;
struct JA_D;
struct JA_L;

JA_Z *__GC_ALLOC_JA_Z(int size);
JA_B *__GC_ALLOC_JA_B(int size);
JA_C *__GC_ALLOC_JA_C(int size);
JA_S *__GC_ALLOC_JA_S(int size);
JA_I *__GC_ALLOC_JA_I(int size);
JA_J *__GC_ALLOC_JA_J(int size);
JA_F *__GC_ALLOC_JA_F(int size);
JA_D *__GC_ALLOC_JA_D(int size);
JA_L *__GC_ALLOC_JA_L(int size, const wchar_t *desc);

struct JA_0 : public java_lang_Object {
	void *_data;
	int32_t length;
	int8_t elementSize;
	const wchar_t *desc;
	bool allocated;
	#ifdef INLINE_ARRAYS
		void *__inline_data;
	#endif

	JA_0(JT_BOOL pointers, int32_t len, int8_t esize, const wchar_t *d) {
		this->__JT__CLASS_ID = 1;
		this->length = len;
		this->elementSize = esize;
		this->desc = d;
		#ifdef INLINE_ARRAYS
			this->_data = getStartPtrRaw(); this->allocated = false;
			::memset(this->_data, 0, bytesLength());
		#else
			this->_data = alloc(pointers, len, esize); this->allocated = true;
		#endif
	}

	JA_0(JT_BOOL pointers, void* data, int32_t len, int8_t esize, const wchar_t *d) : length(len), elementSize(esize), desc(d) {
		this->__JT__CLASS_ID = 1;
		this->_data = data;
		this->allocated = false;
		#ifdef INLINE_ARRAYS
			std::cout << "ERROR: With INLINE_ARRAYS. Array from data won't work\n";
			abort();
		#endif
	}

    virtual void __GC_Init(__GCHeap *heap) {
    	java_lang_Object::__GC_Init(heap);
		heap->allocatedArraySize += length * elementSize;
    }

    virtual void __GC_Dispose(__GCHeap *heap) {
    	int blength = bytesLength();
    	if (allocated) __gcHeap->arrays.Free(_data, blength);
    	java_lang_Object::__GC_Dispose(heap);
		heap->allocatedArraySize -= blength;
	}

	static void* alloc(JT_BOOL pointers, int32_t len, int8_t esize) {
		void * result = nullptr;
		int64_t bytesSize = esize * (len + 1);
		result = (void*)__gcHeap->arrays.Alloc(bytesSize);
		::memset(result, 0, bytesSize);
		return result;
	}

	std::wstring __GC_Name() { return L"JA_0"; }

	#ifdef INLINE_ARRAYS
		inline void *getStartPtrRaw() { return ((char *)(void *)&__inline_data) + INLINE_ARRAYS_OFFSET; }
	#else
		inline void *getStartPtrRaw() { return _data; }
	#endif

	void *getOffsetPtr(int32_t offset) { return (void*)&(((char *)getStartPtrRaw())[offset * elementSize]); }
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
	JA_Base(JT_BOOL pointers, int32_t size, const wchar_t *desc) : JA_0(pointers, size, sizeof(T), desc) {
	};
	JA_Base(JT_BOOL pointers, void* data, int32_t size, const wchar_t *desc) : JA_0(pointers, data, size, sizeof(T), desc) {
	};

	std::wstring __GC_Name() { return L"JA_Base"; }
	

	inline T *getStartPtr() { return (T *)getStartPtrRaw(); }

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
    	if (((uint32_t)offset <= (uint32_t)length)){
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

	#ifdef CHECK_ARRAYS
		void fastSet(int32_t offset, T v) {
			if (isInBounds(offset, length)) (getStartPtr())[offset] = v;
			else __throwArrayOutOfBounds(offset, length);
		};

		T fastGet(int32_t offset) {
			if (isInBounds(offset, length)) return (getStartPtr())[offset];
			else __throwArrayOutOfBounds(offset, length);
		}
	#else
		void fastSet(int32_t offset, T v) { (getStartPtr())[offset] = v; };
		T fastGet(int32_t offset) { return (getStartPtr())[offset]; }
	#endif

	JA_Base<T> *init(int32_t offset, T v) { (getStartPtr())[offset] = v; return this; };

	void set(int32_t offset, T v) { checkBoundsThrowing(offset, length); fastSet(offset, v); };
	T get(int32_t offset) { checkBoundsThrowing(offset, length); return fastGet(offset); };

	//void set(int32_t offset, T v) { fastSet(offset, v); };
	//T get(int32_t offset) { return fastGet(offset); };

	void fill(int32_t from, int32_t to, T v) {
		constexpr int32_t typesize = sizeof(T);
		checkBoundsThrowing(from, length);
		checkBoundsThrowing(to - 1, length);
		if ((typesize == 8) && (sizeof(void*) == 4)) { // constexpr (we are on 32-bits but this is a 64-bit size). Let's optimize this since some compilers don't do this for us.
			int32_t* data = (int32_t*)getStartPtr();
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
			T* data = getStartPtr();
			for (int32_t n = from; n < to; n++) data[n] = v;
		}
	};

	JA_Base<T> *setArray(int32_t start, int32_t size, const T *arrays) {
		for (int32_t n = 0; n < size; n++) this->set(start + n, arrays[n]);
		return this;
	};
};

struct JA_B : JA_Base<int8_t> {
	std::wstring __GC_Name() { return L"JA_B"; }
	

	JA_B(int32_t size, const wchar_t *desc = L"[B") : JA_Base(false, size, desc) { };
	JA_B(void* data, int32_t size, const wchar_t *desc = L"[B") : JA_Base(false, data, size, desc) { };

	void fill(int32_t from, int32_t to, int8_t v) {
		checkBoundsThrowing(from, length);
        checkBoundsThrowing(to - 1, length);
		::memset((void *)(&((int8_t *)getStartPtr())[from]), v, (to - from));
	}

	static JA_B* fromArray(const wchar_t *desc, std::vector<int8_t> array) {
		auto len = (int32_t)array.size();
		auto out = __GC_ALLOC_JA_B(len);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_Z : public JA_B {
	std::wstring __GC_Name() { return L"JA_Z"; }
	

	JA_Z(int32_t size, const wchar_t *desc = L"[Z") : JA_B(size, desc) { };
	JA_Z(void* data, int32_t size, const wchar_t *desc = L"[Z") : JA_B(data, size, desc) { };

	static JA_Z* fromArray(const wchar_t *desc, std::vector<int8_t> array) {
		auto len = (int32_t)array.size();
		auto out = __GC_ALLOC_JA_Z(len);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_S : JA_Base<int16_t> {
	std::wstring __GC_Name() { return L"JA_S"; }
	

	JA_S(int32_t size, const wchar_t *desc = L"[S") : JA_Base(false, size, desc) { };
	JA_S(void* data, int32_t size, const wchar_t *desc = L"[S") : JA_Base(false, data, size, desc) { };

	static JA_S* fromArray(const wchar_t *desc, std::vector<int16_t> array) {
		auto len = (int32_t)array.size();
		auto out = __GC_ALLOC_JA_S(len);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_C : JA_Base<uint16_t> {
	std::wstring __GC_Name() { return L"JA_C"; }
	

	JA_C(int32_t size, const wchar_t *desc = L"[C") : JA_Base(false, size, desc) { };
	JA_C(void* data, int32_t size, const wchar_t *desc = L"[C") : JA_Base(false, data, size, desc) { };

	static JA_C* fromArray(const wchar_t *desc, std::vector<uint16_t> array) {
		auto len = (int32_t)array.size();
		auto out = __GC_ALLOC_JA_C(len);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_I : JA_Base<int32_t> {
	std::wstring __GC_Name() { return L"JA_I"; }
	

	JA_I(int32_t size, const wchar_t *desc = L"[I") : JA_Base(false, size, desc) { };
	JA_I(void* data, int32_t size, const wchar_t *desc = L"[I") : JA_Base(false, data, size, desc) { };

	// @TODO: Try to move to JA_Base
	static JA_I *fromVector(int32_t *data, int32_t count) {
		return (JA_I * )(__GC_ALLOC_JA_I(count))->setArray(0, count, (const int32_t *)data);
	};

	static JA_I *fromArgValues() { return (JA_I * )(__GC_ALLOC_JA_I(0)); };
	static JA_I *fromArgValues(int32_t a0) { return (JA_I * )(__GC_ALLOC_JA_I(1))->init(0, a0); };
	static JA_I *fromArgValues(int32_t a0, int32_t a1) { return (JA_I * )(__GC_ALLOC_JA_I(2))->init(0, a0)->init(1, a1); };
	static JA_I *fromArgValues(int32_t a0, int32_t a1, int32_t a2) { return (JA_I * )(__GC_ALLOC_JA_I(3))->init(0, a0)->init(1, a1)->init(2, a2); };
	static JA_I *fromArgValues(int32_t a0, int32_t a1, int32_t a2, int32_t a3) { return (JA_I * )(__GC_ALLOC_JA_I(4))->init(0, a0)->init(1, a1)->init(2, a2)->init(3, a3); };

	static JA_I* fromArray(const wchar_t *desc, std::vector<int32_t> array) {
		auto len = (int32_t)array.size();
		auto out = __GC_ALLOC_JA_I(len);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_J : JA_Base<int64_t> {
	std::wstring __GC_Name() { return L"JA_J"; }
	

	JA_J(int32_t size, const wchar_t *desc = L"[J") : JA_Base(false, size, desc) { };
	JA_J(void* data, int32_t size, const wchar_t *desc = L"[J") : JA_Base(false, data, size, desc) { };

	static JA_J* fromArray(const wchar_t *desc, std::vector<int64_t> array) {
		auto len = (int32_t)array.size();
		auto out = __GC_ALLOC_JA_J(len);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_F : JA_Base<float> {
	std::wstring __GC_Name() { return L"JA_F"; }
	

	JA_F(int32_t size, const wchar_t *desc = L"[F") : JA_Base(false, size, desc) { };
	JA_F(void* data, int32_t size, const wchar_t *desc = L"[F") : JA_Base(false, data, size, desc) { };

	static JA_F* fromArray(wchar_t *desc, std::vector<float> array) {
		auto len = (int32_t)array.size();
		auto out = __GC_ALLOC_JA_F(len);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_D : JA_Base<double> {
	std::wstring __GC_Name() { return L"JA_D"; }
	

	JA_D(int32_t size, const wchar_t *desc = L"[D") : JA_Base(false, size, desc) { };
	JA_D(void* data, int32_t size, const wchar_t *desc = L"[D") : JA_Base(false, data, size, desc) { };

	static JA_D* fromArray(wchar_t *desc, std::vector<double> array) {
		auto len = (int32_t)array.size();
		auto out = __GC_ALLOC_JA_D(len);
		for (int n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};
struct JA_L : JA_Base<JAVA_OBJECT> {
	JA_L(int32_t size, const wchar_t *desc) : JA_Base(true, size, desc) { };
	JA_L(void* data, int32_t size, const wchar_t *desc) : JA_Base(true, data, size, desc) { };

	std::vector<JAVA_OBJECT> getVector() {
		int32_t len = this->length;
		std::vector<JAVA_OBJECT> out(len);
		for (int32_t n = 0; n < len; n++) out[n] = this->fastGet(n);
		return out;
	}

	std::wstring __GC_Name() { return L"JA_L"; }
	
    void __GC_Trace(__GCVisitor* visitor) {
    	JA_Base::__GC_Trace(visitor);
    	int length = this->length;
    	for (int n = 0; n < length; n++) {
			visitor->Trace(this->fastGet(n));
    	}
    }

	static JA_0* createMultiSure(wchar_t *desc, std::vector<int32_t> sizes) {
		if (sizes.size() == 0) throw L"Multiarray with zero sizes";

		int32_t size = sizes[0];

		if (sizes.size() == 1) {
			if (desc == std::wstring(L"[Z")) return __GC_ALLOC_JA_Z(size);
			if (desc == std::wstring(L"[B")) return __GC_ALLOC_JA_B(size);
			if (desc == std::wstring(L"[S")) return __GC_ALLOC_JA_S(size);
			if (desc == std::wstring(L"[C")) return __GC_ALLOC_JA_C(size);
			if (desc == std::wstring(L"[I")) return __GC_ALLOC_JA_I(size);
			if (desc == std::wstring(L"[J")) return __GC_ALLOC_JA_J(size);
			if (desc == std::wstring(L"[F")) return __GC_ALLOC_JA_F(size);
			if (desc == std::wstring(L"[D")) return __GC_ALLOC_JA_D(size);
			throw L"Invalid multiarray";
		}

		// std::vector<decltype(myvector)::value_type>(myvector.begin()+N, myvector.end()).swap(myvector);


		auto out = __GC_ALLOC_JA_L(size, desc);
		auto subdesc = &desc[1];
		auto subsizes = std::vector<int32_t>(sizes.begin() + 1, sizes.end());
		for (int32_t n = 0; n < size; n++) {
			out->set(n, createMultiSure(subdesc, subsizes));
		}
		return out;
	}

	static JA_L* fromArray(wchar_t *desc, std::vector<JAVA_OBJECT> array) {
		auto len = (int32_t)array.size();
		auto out = __GC_ALLOC_JA_L(len, desc);
		for (int32_t n = 0; n < len; n++) out->fastSet(n, array[n]);
		return out;
	}
};

//JAVA_OBJECT JA_0::toBoolArray  () { return __GC_ALLOC<JA_Z>((void *)getStartPtr(), bytesLength() / 1); };
//JAVA_OBJECT JA_0::toByteArray  () { return __GC_ALLOC<JA_B>((void *)getStartPtr(), bytesLength() / 1); };
//JAVA_OBJECT JA_0::toCharArray  () { return __GC_ALLOC<JA_C>((void *)getStartPtr(), bytesLength() / 2); };
//JAVA_OBJECT JA_0::toShortArray () { return __GC_ALLOC<JA_S>((void *)getStartPtr(), bytesLength() / 2); };
//JAVA_OBJECT JA_0::toIntArray   () { return __GC_ALLOC<JA_I>((void *)getStartPtr(), bytesLength() / 4); };
//JAVA_OBJECT JA_0::toLongArray  () { return __GC_ALLOC<JA_J>((void *)getStartPtr(), bytesLength() / 8); };
//JAVA_OBJECT JA_0::toFloatArray () { return __GC_ALLOC<JA_F>((void *)getStartPtr(), bytesLength() / 4); };
//JAVA_OBJECT JA_0::toDoubleArray() { return __GC_ALLOC<JA_D>((void *)getStartPtr(), bytesLength() / 8); };

#ifdef INLINE_ARRAYS
	JA_Z *__GC_ALLOC_JA_Z(int size) { return __gcHeap->AllocCustomSize<JA_Z>(sizeof(JA_Z) + INLINE_ARRAYS_OFFSET + (size * sizeof(int8_t)), size); }
	JA_B *__GC_ALLOC_JA_B(int size) { return __gcHeap->AllocCustomSize<JA_B>(sizeof(JA_B) + INLINE_ARRAYS_OFFSET + (size * sizeof(int8_t)), size); }
	JA_C *__GC_ALLOC_JA_C(int size) { return __gcHeap->AllocCustomSize<JA_C>(sizeof(JA_C) + INLINE_ARRAYS_OFFSET + (size * sizeof(int16_t)), size); }
	JA_S *__GC_ALLOC_JA_S(int size) { return __gcHeap->AllocCustomSize<JA_S>(sizeof(JA_S) + INLINE_ARRAYS_OFFSET + (size * sizeof(int16_t)), size); }
	JA_I *__GC_ALLOC_JA_I(int size) { return __gcHeap->AllocCustomSize<JA_I>(sizeof(JA_I) + INLINE_ARRAYS_OFFSET + (size * sizeof(int32_t)), size); }
	JA_J *__GC_ALLOC_JA_J(int size) { return __gcHeap->AllocCustomSize<JA_J>(sizeof(JA_J) + INLINE_ARRAYS_OFFSET + (size * sizeof(int64_t)), size); }
	JA_F *__GC_ALLOC_JA_F(int size) { return __gcHeap->AllocCustomSize<JA_F>(sizeof(JA_F) + INLINE_ARRAYS_OFFSET + (size * sizeof(float32_t)), size); }
	JA_D *__GC_ALLOC_JA_D(int size) { return __gcHeap->AllocCustomSize<JA_D>(sizeof(JA_D) + INLINE_ARRAYS_OFFSET + (size * sizeof(float64_t)), size); }
	JA_L *__GC_ALLOC_JA_L(int size, const wchar_t *desc) { return __gcHeap->AllocCustomSize<JA_L>(sizeof(JA_L) + INLINE_ARRAYS_OFFSET + (size * sizeof(void *)), size,  (wchar_t *)desc); }
#else
	JA_Z *__GC_ALLOC_JA_Z(int size) { return __gcHeap->Alloc<JA_Z>(size); }
	JA_B *__GC_ALLOC_JA_B(int size) { return __gcHeap->Alloc<JA_B>(size); }
	JA_C *__GC_ALLOC_JA_C(int size) { return __gcHeap->Alloc<JA_C>(size); }
	JA_S *__GC_ALLOC_JA_S(int size) { return __gcHeap->Alloc<JA_S>(size); }
	JA_I *__GC_ALLOC_JA_I(int size) { return __gcHeap->Alloc<JA_I>(size); }
	JA_J *__GC_ALLOC_JA_J(int size) { return __gcHeap->Alloc<JA_J>(size); }
	JA_F *__GC_ALLOC_JA_F(int size) { return __gcHeap->Alloc<JA_F>(size); }
	JA_D *__GC_ALLOC_JA_D(int size) { return __gcHeap->Alloc<JA_D>(size); }
	JA_L *__GC_ALLOC_JA_L(int size, const wchar_t *desc) { return __gcHeap->Alloc<JA_L>(size, (wchar_t *)desc); }
#endif


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
	int obj_type = obj->__JT__CLASS_ID;
	if (obj_type == type) return true;
	const TYPE_INFO type_info = TYPE_TABLE::TABLE[obj_type];
	const size_t size = type_info.size;
	const int32_t* subtypes = type_info.subtypes;
    for (size_t i = 0; i < size; i++) {
    	if (subtypes[i] == type) return true;
    }
	return false;
};

JT_BOOL N::isFast(JAVA_OBJECT obj, int32_t t0) {
	if (obj == nullptr) return false;
	int obj_type = obj->__JT__CLASS_ID;
	if (obj_type == t0) return true;
	return false;
};
JT_BOOL N::isFast(JAVA_OBJECT obj, int32_t t0, int32_t t1) {
	if (obj == nullptr) return false;
	int obj_type = obj->__JT__CLASS_ID;
	if (obj_type == t0) return true;
	if (obj_type == t1) return true;
	return false;
};
JT_BOOL N::isFast(JAVA_OBJECT obj, int32_t t0, int32_t t1, int32_t t2) {
	if (obj == nullptr) return false;
	int obj_type = obj->__JT__CLASS_ID;
	if (obj_type == t0) return true;
	if (obj_type == t1) return true;
	if (obj_type == t2) return true;
	return false;
};
JT_BOOL N::isFast(JAVA_OBJECT obj, int32_t t0, int32_t t1, int32_t t2, int32_t t3) {
	if (obj == nullptr) return false;
	int obj_type = obj->__JT__CLASS_ID;
	if (obj_type == t0) return true;
	if (obj_type == t1) return true;
	if (obj_type == t2) return true;
	if (obj_type == t3) return true;
	return false;
};
JT_BOOL N::isFast(JAVA_OBJECT obj, int32_t t0, int32_t t1, int32_t t2, int32_t t3, int32_t t4) {
	if (obj == nullptr) return false;
	int obj_type = obj->__JT__CLASS_ID;
	if (obj_type == t0) return true;
	if (obj_type == t1) return true;
	if (obj_type == t2) return true;
	if (obj_type == t3) return true;
	if (obj_type == t4) return true;
	return false;
};
JT_BOOL N::isFast(JAVA_OBJECT obj, int32_t t0, int32_t t1, int32_t t2, int32_t t3, int32_t t4, int32_t t5) {
	if (obj == nullptr) return false;
	int obj_type = obj->__JT__CLASS_ID;
	if (obj_type == t0) return true;
	if (obj_type == t1) return true;
	if (obj_type == t2) return true;
	if (obj_type == t3) return true;
	if (obj_type == t4) return true;
	if (obj_type == t5) return true;
	return false;
};
JT_BOOL N::isFast(JAVA_OBJECT obj, int32_t t0, int32_t t1, int32_t t2, int32_t t3, int32_t t4, int32_t t5, int32_t t6) {
	if (obj == nullptr) return false;
	int obj_type = obj->__JT__CLASS_ID;
	if (obj_type == t0) return true;
	if (obj_type == t1) return true;
	if (obj_type == t2) return true;
	if (obj_type == t3) return true;
	if (obj_type == t4) return true;
	if (obj_type == t5) return true;
	if (obj_type == t6) return true;
	return false;
};

JT_BOOL N::isArray(JAVA_OBJECT obj) { return GET_OBJECT(JA_0, obj) != nullptr; };
JT_BOOL N::isArray(JAVA_OBJECT obj, wchar_t *desc) {
	JA_0* ptr = GET_OBJECT(JA_0, obj);
	JT_BOOL result = (ptr != nullptr) && (ptr->desc == desc);
	if (!result) {
		if (desc[0] == L'[' && desc[1] == L'L') {
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

inline int32_t N::ishl(int32_t a, int32_t b) { return (a << FIXSHIFT(b)); }
inline int32_t N::ishr(int32_t a, int32_t b) { return (a >> FIXSHIFT(b)); }
inline int32_t N::iushr(int32_t a, int32_t b) { return (int32_t)(((uint32_t)a) >> FIXSHIFT(b)); }

inline int32_t N::ishl_cst(int32_t a, int32_t b) { return (a << b); }
inline int32_t N::ishr_cst(int32_t a, int32_t b) { return (a >> b); }
inline int32_t N::iushr_cst(int32_t a, int32_t b) { return (int32_t)(((uint32_t)a) >> b); }

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

inline int64_t N::lcmp (int64_t a, int64_t b) { return (a < b) ? -1 : ((a > b) ? +1 : 0); }
inline int64_t N::ladd (int64_t a, int64_t b) { return a + b; }
inline int64_t N::lsub (int64_t a, int64_t b) { return a - b; }
inline int64_t N::lmul (int64_t a, int64_t b) { return a * b; }
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
int64_t N::lrem (int64_t a, int64_t b) { return (a == 0 || b == 0 || (a == N::MIN_INT64 && b == -1)) ? 0 : (a % b); }
inline int64_t N::land (int64_t a, int64_t b) { return a & b; }
inline int64_t N::lor  (int64_t a, int64_t b) { return a | b; }
inline int64_t N::lxor (int64_t a, int64_t b) { return a ^ b; }

int LFIXSHIFT(int r) {
	if (r < 0) {
		return (64 - ((-r) & 0x3F)) & 0x3F;
	} else {
		return r & 0x3F;
	}
}

inline int64_t N::lshl(int64_t a, int b) { return (a << LFIXSHIFT(b)); }
inline int64_t N::lshr(int64_t a, int b) { return (a >> LFIXSHIFT(b)); }
inline int64_t N::lushr(int64_t a, int b) { return (int64_t)(((uint64_t)a) >> LFIXSHIFT(b)); }

inline int64_t N::lshl_cst(int64_t a, int b) { return (a << b); }
inline int64_t N::lshr_cst(int64_t a, int b) { return (a >> b); }
inline int64_t N::lushr_cst(int64_t a, int b) { return (int64_t)(((uint64_t)a) >> b); }

inline int32_t N::z2i(int32_t v) { return (v != 0) ? 1 : 0; }
inline int64_t N::i2j(int32_t v) { return (int64_t)v; }

inline float   N::j2f(int64_t v) { return (float)v; }
inline double  N::j2d(int64_t v) { return (double)v; }
inline int32_t N::j2i(int64_t v) { return (int32_t)v; }

int64_t N::f2j(float v) {
	if (std::isfinite(v)) return (int64_t)v;
	if (std::isnan(v)) return 0;
	if (v >= 0) return MAX_INT64;
	return MIN_INT64;
}
int64_t N::d2j(double v) {
	if (std::isfinite(v)) return (int64_t)v;
	if (std::isnan(v)) return 0;
	if (v >= 0) return MAX_INT64;
	return MIN_INT64;
}

// TODO: templatize d2i and f2i to write just once
int32_t N::d2i(double v) {
	if (std::isfinite(v)) return (int32_t)v;
	if (std::isnan(v)) return 0;
	if (v >= 0) return MAX_INT32;
	return MIN_INT32;
}

int32_t N::f2i(float v) {
	if (std::isfinite(v)) return (int32_t)v;
	if (std::isnan(v)) return 0;
	if (v >= 0) return MAX_INT32;
	return MIN_INT32;
}

JAVA_OBJECT N::str(const wchar_t *str, int32_t len) {
	p_java_lang_String out = __GC_ALLOC<{% CLASS java.lang.String %}>();
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	p_JA_C array = __GC_ALLOC_JA_C(len);
	p_JA_C arrayobj = array;
	uint16_t *ptr = (uint16_t *)array->getStartPtr();
	if (sizeof(wchar_t) == sizeof(uint16_t)) {
		::memcpy((void *)ptr, (void *)str, len * sizeof(uint16_t));
	} else {
		for (int32_t n = 0; n < len; n++) ptr[n] = (uint16_t)str[n];
	}
	out->{% FIELD java.lang.String:value %} = arrayobj;
	//GET_OBJECT({% CLASS java.lang.String %}, out)->M_java_lang_String__init____CII_V(array, 0, len);
	return _out;
};

JAVA_OBJECT N::strLiteral(const wchar_t *ptr, int len) {
	return N::str(ptr, len);
}

JAVA_OBJECT N::str(std::u16string str) {
	int32_t len = str.length();
	p_java_lang_String out(__GC_ALLOC<{% CLASS java.lang.String %}>());
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	p_JA_C array = __GC_ALLOC_JA_C(len);
	p_JA_C arrayobj = array;
	uint16_t *ptr = (uint16_t *)array->getStartPtr();
	for (int32_t n = 0; n < len; n++) ptr[n] = (uint16_t)str[n];
	out->{% FIELD java.lang.String:value %} = arrayobj;
	//GET_OBJECT({% CLASS java.lang.String %}, out)->M_java_lang_String__init____CII_V(array, 0, len);
	return _out;
};

JAVA_OBJECT N::str(std::wstring str) {
	int32_t len = str.length();
	p_java_lang_String out(__GC_ALLOC<{% CLASS java.lang.String %}>());
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	p_JA_C array = __GC_ALLOC_JA_C(len);
	p_JA_C arrayobj = array;
	uint16_t *ptr = (uint16_t *)array->getStartPtr();
	for (int32_t n = 0; n < len; n++) ptr[n] = (uint16_t)str[n];
	out->{% FIELD java.lang.String:value %} = arrayobj;
	//GET_OBJECT({% CLASS java.lang.String %}, out)->M_java_lang_String__init____CII_V(array, 0, len);
	return _out;
};

JAVA_OBJECT N::str(std::string s) {
	//if (s == nullptr) return SOBJ(nullptr);
	std::wstring ws(s.begin(), s.end());
	return N::str(ws);
};

JAVA_OBJECT N::str(const char *s) {
	if (s == nullptr) return nullptr;
	int32_t len = strlen(s);
	p_java_lang_String out(__GC_ALLOC<{% CLASS java.lang.String %}>());
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	p_JA_C array = __GC_ALLOC_JA_C(len);
	p_JA_C arrayobj = array;
	uint16_t *ptr = (uint16_t *)array->getStartPtr();
	//::memcpy((void *)ptr, (void *)str, len * sizeof(uint16_t));
	for (int32_t n = 0; n < len; n++) ptr[n] = (uint16_t)s[n];
	out->{% FIELD java.lang.String:value %} = arrayobj;
	//GET_OBJECT({% CLASS java.lang.String %}, out)->M_java_lang_String__init____CII_V(array, 0, len);
	return _out;
};

JAVA_OBJECT N::strArray(int32_t count, wchar_t **strs) {
	p_JA_L out = __GC_ALLOC_JA_L(count, L"[java/lang/String;");
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	for (int32_t n = 0; n < count; n++) out->set(n, N::str(std::wstring(strs[n])));
	return _out;
}

JAVA_OBJECT N::strArray(std::vector<std::wstring> strs) {
	int32_t len = strs.size();
	p_JA_L out = __GC_ALLOC_JA_L(len, L"[java/lang/String;");
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	for (int32_t n = 0; n < len; n++) out->set(n, N::str(strs[n]));
	return _out;
}

JAVA_OBJECT N::strArray(std::vector<std::string> strs) {
	int32_t len = strs.size();
	p_JA_L out = __GC_ALLOC_JA_L(len, L"[Ljava/lang/String;");
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	for (int32_t n = 0; n < len; n++) out->set(n, N::str(strs[n]));
	return _out;
}

JAVA_OBJECT N::strEmptyArray() {
	p_JA_L out = __GC_ALLOC_JA_L(0, L"Ljava/lang/String;");
	JAVA_OBJECT _out = (JAVA_OBJECT)out;
	return _out;
}

JAVA_OBJECT N::strArray(int argc, char **argv) {
	std::vector<std::string> arguments(argv + 1, argv + argc);
	return strArray(arguments);
}

const char16_t* N::getStrDataPtr(JAVA_OBJECT obj) {
	if (obj == nullptr) return nullptr;
	auto chars = N::strGetCharsFast(obj);
	//auto len = chars->length;
	auto ptr = (const char16_t *)chars->getOffsetPtr(0);
	return ptr;
}

std::u16string N::istr(JAVA_OBJECT obj) {
	if (obj == nullptr) return u"null";
	auto str = obj->M__toString__Ljava_lang_String_();
	auto chars = N::strGetCharsFast(str);
	if (chars == nullptr) return u"null";
	std::u16string o;
	o.reserve((int)chars->length);
	o.append((const char16_t *)chars->getOffsetPtr(0), (int)chars->length);
	return o;
}

// @TODO: Can we make this faster?
std::wstring N::istr2(JAVA_OBJECT obj) {
	if (obj == nullptr) return L"null";
	auto str = obj->M__toString__Ljava_lang_String_();
	auto chars = N::strGetCharsFast(str);
	auto len = chars->length;
	auto ptr = (const uint16_t *)chars->getOffsetPtr(0);
	std::wstring o;
	o.reserve(len);
	for (int32_t n = 0; n < len; n++) o.push_back(ptr[n]);
	return o;
}

std::string N::istr3(JAVA_OBJECT obj) {
	if (obj == nullptr) return "null";
	auto str = obj->M__toString__Ljava_lang_String_();
	auto chars = N::strGetCharsFast(str);
	auto len = chars->length;
	auto ptr = (const uint16_t *)chars->getOffsetPtr(0);
	std::string s;
	s.reserve(len);
	for (int32_t n = 0; n < len; n++) s.push_back(ptr[n]);
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

JA_C *N::strGetCharsFast(JAVA_OBJECT obj) {
	auto str = GET_OBJECT({% CLASS java.lang.String %}, obj);
	return (JA_C *)str->{% FIELD java.lang.String:value %};
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
#include <ctime>

double N::getTime() {
	using namespace std::chrono;
	//auto now = system_clock::now();
	//auto now_n = std::chrono::time_point_cast<std::chrono::nanoseconds>(now);
	//return (double)now_n / (double)1000000.0;
	//return ((double)::clock() / (double)CLOCKS_PER_SEC) * 1000;
    nanoseconds ns = duration_cast<nanoseconds>(steady_clock::now().time_since_epoch() );
	return (double)(int64_t)ns.count() / (double)1000000;
};

int64_t N::nanoTime() {
	using namespace std::chrono;
	auto time = duration_cast<nanoseconds>(high_resolution_clock::now().time_since_epoch());
	return (int64_t)time.count();
};

void N::monitorEnter(JAVA_OBJECT obj) {
}

void N::monitorExit(JAVA_OBJECT obj){
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

#include "JNI.cpp"

__GC_thread_local Env N::env;

void N::startupThread() {
	N::env.jni.functions = &jni;
	__GC_DISABLE();
	N::initStringPool();
	__GC_ENABLE();
	N::staticInit();
};

void N::startup() {
	setvbuf(stdout, nullptr, _IONBF, 0);
	setvbuf(stderr, nullptr, _IONBF, 0);
	std::signal(SIGSEGV, SIGSEGV_handler);
	std::signal(SIGFPE, SIGFPE_handler);
	N::startupThread();
};

// Type Table Footer
{{ TYPE_TABLE_FOOTER }}

// Main
{{ MAIN }}