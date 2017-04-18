#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#define _CRT_SECURE_NO_DEPRECATE
#include <windows.h>
#endif

#include <memory>
#include <vector>
#include <string>
#include <sstream>
#include <iostream>
#include <algorithm>
#include <cmath>
#include <csignal>
//#include <chrono>
//#include <thread>
#include "jni.h"


#include "gc_cpp.h"

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

// HEADERS + INCLUDES
{{ HEADER }}

//#define null ((void*)0)
//#define Lnull ((wchar_t*)0)

#define null 0
#define Lnull ((wchar_t*)0)

int TRACE_INDENT = 0;

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

struct N { public:
	static Env env;


	static const int32_t MIN_INT32 = (int32_t)0x80000000;
	static const int32_t MAX_INT32 = (int32_t)0x7FFFFFFF;

	static const int64_t MIN_INT64 = (int64_t)0x8000000000000000;
	static const int64_t MAX_INT64 = (int64_t)0x7FFFFFFFFFFFFFFF;

	//static const int64_t MIN_INT64 = (int64_t)0x8000000000000000;
	//static const int64_t MAX_INT64 = (int64_t)0x7FFFFFFFFFFFFFFF;
	static JAVA_OBJECT resolveClass(std::wstring str);
	inline static int64_t lnew(int high, int low);
	static bool is(JAVA_OBJECT obj, int type);
	static bool isArray(JAVA_OBJECT obj);
	static bool isArray(JAVA_OBJECT obj, std::wstring desc);
	static bool isUnknown(std::shared_ptr<{% CLASS java.lang.Object %}> obj, const char *error);
	static int cmp(double a, double b);
	static int cmpl(double a, double b);
	static int cmpg(double a, double b);
	inline static int32_t iushr(int32_t a, int32_t b);
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
	inline static int32_t z2i(int32_t v);
	inline static float   l2f(int64_t v);
	inline static double  l2d(int64_t v);
	inline static int64_t i2j(int32_t v);
	inline static int32_t l2i(int64_t v);
	inline static int64_t f2j(float v);
	inline static int64_t d2j(double v);
	static void log(std::wstring str);
	static void log(JAVA_OBJECT str);
	static JAVA_OBJECT str(char *str);
	static JAVA_OBJECT str(const wchar_t *str, int len);
	static JAVA_OBJECT str(std::wstring str);
	static JAVA_OBJECT str(std::string str);
	static JAVA_OBJECT strArray(int count, wchar_t **strs);
	static JAVA_OBJECT strArray(std::vector<std::wstring> strs);
	static JAVA_OBJECT strArray(std::vector<std::string> strs);
	static JAVA_OBJECT strEmptyArray();
	static std::wstring istr2(JAVA_OBJECT obj);
	static std::string istr3(JAVA_OBJECT obj);
	static JAVA_OBJECT dummyMethodClass();
	static void throwNpe(const wchar_t *position);
	static JAVA_OBJECT ensureNpe(JAVA_OBJECT obj, const wchar_t *position);
	static void throwNpe();
	static JAVA_OBJECT ensureNpe(JAVA_OBJECT obj);
	static std::vector<JAVA_OBJECT> getVectorOrEmpty(JAVA_OBJECT array);

	static int strLen(JAVA_OBJECT obj);
	static int strCharAt(JAVA_OBJECT obj, int n);

	static int identityHashCode(JAVA_OBJECT obj);

	static void writeChars(JAVA_OBJECT str, char *out, int len);

	static JAVA_OBJECT    unboxVoid(JAVA_OBJECT obj);
	static int32_t unboxBool(JAVA_OBJECT obj);
	static int32_t unboxByte(JAVA_OBJECT obj);
	static int32_t unboxShort(JAVA_OBJECT obj);
	static int32_t unboxChar(JAVA_OBJECT obj);
	static int32_t unboxInt(JAVA_OBJECT obj);
	static int64_t unboxLong(JAVA_OBJECT obj);
	static float   unboxFloat(JAVA_OBJECT obj);
	static double  unboxDouble(JAVA_OBJECT obj);

	static JAVA_OBJECT  boxVoid(void);
	static JAVA_OBJECT  boxVoid(JAVA_OBJECT v);
	static JAVA_OBJECT  boxBool(bool v);
	static JAVA_OBJECT  boxByte(int32_t v);
	static JAVA_OBJECT  boxShort(int32_t v);
	static JAVA_OBJECT  boxChar(int32_t v);
	static JAVA_OBJECT  boxInt(int32_t v);
	static JAVA_OBJECT  boxLong(int64_t v);
	static JAVA_OBJECT  boxFloat(float v);
	static JAVA_OBJECT  boxDouble(double v);

	static double getTime();
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
};


// Strings
{{ STRINGS }}


/// ARRAY_HEADERS

{{ ARRAY_HEADERS_PRE }}

struct JA_0 : public java_lang_Object { public:
	void *_data;
	int length;
	int elementSize;
	std::wstring desc;
	JA_0(void* data, int len, int esize, std::wstring d) : length(len), elementSize(esize), desc(d) {
		this->__INSTANCE_CLASS_ID = 1;
		this->_data = data;
	}

	JA_0(int len, int esize, std::wstring d) : JA_0((void*)GC_MALLOC(esize * (len + 1)), len, esize, d) {
		::memset(this->_data, 0, (len + 1) * esize);
	}
	~JA_0() { /*::free(_data);*/ }
	void *getOffsetPtr(int offset) { return (void*)&(((int8_t *)_data)[offset * elementSize]); }
	void *getStartPtr() { return getOffsetPtr(0); }
	int bytesLength() { return length * elementSize; }
	static void copy(JA_0* src, int srcpos, JA_0* dst, int dstpos, int len) {
		::memmove(dst->getOffsetPtr(dstpos), src->getOffsetPtr(srcpos), len * src->elementSize);
	}
	JAVA_OBJECT toBoolArray();
	JAVA_OBJECT toByteArray();
	JAVA_OBJECT toCharArray();
	JAVA_OBJECT toShortArray();
	JAVA_OBJECT toIntArray();
	JAVA_OBJECT toLongArray();
	JAVA_OBJECT toFloatArray();
	JAVA_OBJECT toDoubleArray();
};

template <class T>
struct JA_Base : JA_0 {
	JA_Base(int size, std::wstring desc) : JA_0(size, sizeof(T), desc) {
	};
	JA_Base(void* data, int size, std::wstring desc) : JA_0(data, size, sizeof(T), desc) {
	};
	inline void checkBounds(int offset) {
		if (offset < 0 || offset >= length) {
			std::wstringstream os;
			os << L"Out of bounds " << offset << L" " << length;
			throw os.str();
		}
	};
	T *getStartPtr() { return (T *)_data; }

	#ifdef CHECK_ARRAYS
		inline void fastSet(int offset, T v) { checkBounds(offset); ((T*)(this->_data))[offset] = v; };
		inline T fastGet(int offset) { checkBounds(offset); return ((T*)(this->_data))[offset]; }
	#else
		inline void fastSet(int offset, T v) { ((T*)(this->_data))[offset] = v; };
		inline T fastGet(int offset) { return ((T*)(this->_data))[offset]; }
	#endif

	inline JA_Base<T> *init(int offset, T v) { ((T*)(this->_data))[offset] = v; return this; };

	void set(int offset, T v) { checkBounds(offset); fastSet(offset, v); };
	T get(int offset) { checkBounds(offset); return fastGet(offset); };

	void fill(int from, int to, T v) { checkBounds(from); checkBounds(to - 1); T* data = (T*)this->_data; for (int n = from; n < to; n++) data[n] = v; };

	JA_Base<T> *setArray(int start, int size, const T *arrays) {
		for (int n = 0; n < size; n++) this->set(start + n, arrays[n]);
		return this;
	};
};

struct JA_B : JA_Base<int8_t> {
	JA_B(int size, std::wstring desc = L"[B") : JA_Base(size, desc) { };
	JA_B(void* data, int size, std::wstring desc = L"[B") : JA_Base(data, size, desc) { };
};
struct JA_Z : public JA_B {
	JA_Z(int size, std::wstring desc = L"[Z") : JA_B(size, desc) { };
	JA_Z(void* data, int size, std::wstring desc = L"[Z") : JA_B(data, size, desc) { };
};
struct JA_S : JA_Base<int16_t> {
	JA_S(int size, std::wstring desc = L"[S") : JA_Base(size, desc) { };
	JA_S(void* data, int size, std::wstring desc = L"[S") : JA_Base(data, size, desc) { };
};
struct JA_C : JA_Base<uint16_t> {
	JA_C(int size, std::wstring desc = L"[C") : JA_Base(size, desc) { };
	JA_C(void* data, int size, std::wstring desc = L"[C") : JA_Base(data, size, desc) { };
};
struct JA_I : JA_Base<int32_t> {
	JA_I(int size, std::wstring desc = L"[I") : JA_Base(size, desc) { };
	JA_I(void* data, int size, std::wstring desc = L"[I") : JA_Base(data, size, desc) { };

	// @TODO: Try to move to JA_Base
	static JA_I *fromVector(int *data, int count) {
		return (JA_I * )(new JA_I(count))->setArray(0, count, (const int *)data);
	};

	static JA_I *fromArgValues() { return (JA_I * )(new JA_I(0)); };
	static JA_I *fromArgValues(int a0) { return (JA_I * )(new JA_I(1))->init(0, a0); };
	static JA_I *fromArgValues(int a0, int a1) { return (JA_I * )(new JA_I(2))->init(0, a0)->init(1, a1); };
	static JA_I *fromArgValues(int a0, int a1, int a2) { return (JA_I * )(new JA_I(3))->init(0, a0)->init(1, a1)->init(2, a2); };
	static JA_I *fromArgValues(int a0, int a1, int a2, int a3) { return (JA_I * )(new JA_I(4))->init(0, a0)->init(1, a1)->init(2, a2)->init(3, a3); };

};
struct JA_J : JA_Base<int64_t> {
	JA_J(int size, std::wstring desc = L"[J") : JA_Base(size, desc) { };
	JA_J(void* data, int size, std::wstring desc = L"[J") : JA_Base(data, size, desc) { };
};
struct JA_F : JA_Base<float> {
	JA_F(int size, std::wstring desc = L"[F") : JA_Base(size, desc) { };
	JA_F(void* data, int size, std::wstring desc = L"[F") : JA_Base(data, size, desc) { };
};
struct JA_D : JA_Base<double> {
	JA_D(int size, std::wstring desc = L"[D") : JA_Base(size, desc) { };
	JA_D(void* data, int size, std::wstring desc = L"[D") : JA_Base(data, size, desc) { };
};
struct JA_L : JA_Base<JAVA_OBJECT> {
	JA_L(int size, std::wstring desc) : JA_Base(size, desc) { };
	JA_L(void* data, int size, std::wstring desc) : JA_Base(data, size, desc) { };

	std::vector<JAVA_OBJECT> getVector() {
		int len = this->length;
		std::vector<JAVA_OBJECT> out(len);
		for (int n = 0; n < len; n++) out[n] = this->fastGet(n);
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
		for (int n = 0; n < size; n++) {
			out->set(n, createMultiSure(subdesc, subsizes));
		}
		return out;
	}
};

JAVA_OBJECT JA_0::toBoolArray  () { return new JA_Z((void *)getStartPtr(), bytesLength() / 1); };
JAVA_OBJECT JA_0::toByteArray  () { return new JA_B((void *)getStartPtr(), bytesLength() / 1); };
JAVA_OBJECT JA_0::toCharArray  () { return new JA_C((void *)getStartPtr(), bytesLength() / 2); };
JAVA_OBJECT JA_0::toShortArray () { return new JA_S((void *)getStartPtr(), bytesLength() / 2); };
JAVA_OBJECT JA_0::toIntArray   () { return new JA_I((void *)getStartPtr(), bytesLength() / 4); };
JAVA_OBJECT JA_0::toLongArray  () { return new JA_J((void *)getStartPtr(), bytesLength() / 8); };
JAVA_OBJECT JA_0::toFloatArray () { return new JA_F((void *)getStartPtr(), bytesLength() / 4); };
JAVA_OBJECT JA_0::toDoubleArray() { return new JA_D((void *)getStartPtr(), bytesLength() / 8); };



#include "jni_impl.cpp"


{{ ARRAY_HEADERS_POST }}


// Classes IMPLS
{{ CLASSES_IMPL }}


// N IMPLS

JAVA_OBJECT N::resolveClass(std::wstring str) {
	return {% SMETHOD java.lang.Class:forName0 %}(N::str(str));
};

int64_t N::lnew(int high, int low) {
	return (((int64_t)high) << 32) | (((int64_t)low) << 0);
};

bool N::is(JAVA_OBJECT obj, int type) {
	if (obj == NULL) return false;
	const TYPE_INFO type_info = TYPE_TABLE::TABLE[obj->__INSTANCE_CLASS_ID];
	const size_t size = type_info.size;
	const int* subtypes = type_info.subtypes;
    for(int i = 0; i < size; i++){
    	if(subtypes[i] == type) return true;
    }
	return false;
};

bool N::isArray(JAVA_OBJECT obj) {
	return GET_OBJECT(JA_0, obj) != NULL;
};

bool N::isArray(JAVA_OBJECT obj, std::wstring desc) {
	JA_0* ptr = GET_OBJECT(JA_0, obj);
	return (ptr != null) && (ptr->desc == desc);
};

bool N::isUnknown(std::shared_ptr<{% CLASS java.lang.Object %}> obj, const char * error) {
	throw error;
};

int N::cmp(double a, double b) {
	return (a < b) ? (-1) : ((a > b) ? (+1) : 0);
};

int N::cmpl(double a, double b) {
	return (std::isnan(a) || std::isnan(b)) ? (-1) : N::cmp(a, b);
};

int N::cmpg(double a, double b) {
	return (std::isnan(a) || std::isnan(b)) ? (+1) : N::cmp(a, b);
};

int32_t N::iushr(int32_t a, int32_t b) { return (int32_t)(((uint32_t)a) >> b); }
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
int64_t N::lshl (int64_t a, int b) { return a << b; }
int64_t N::lshr (int64_t a, int b) { return a >> b; }
int64_t N::lushr(int64_t a, int b) { return (int64_t)(((uint64_t)a) >> b); }

int32_t N::z2i(int32_t v) { return (v != 0) ? 1 : 0; }
float   N::l2f(int64_t v) { return (float)v; }
double  N::l2d(int64_t v) { return (double)v; }
int64_t N::i2j(int32_t v) { return (int64_t)v; }
int32_t N::l2i(int64_t v) { return (int32_t)v; }
int64_t N::f2j(float v) { return (int64_t)v; }
int64_t N::d2j(double v) { return (int64_t)v; }

//SOBJ N::strLiteral(wchar_t *ptr, int len) {
//	SOBJ out(new {% CLASS java.lang.String %}());
//	return out.get()->sptr();
//}

JAVA_OBJECT N::str(const wchar_t *str, int len) {
	JAVA_OBJECT out = new {% CLASS java.lang.String %}();
	JA_C *array = new JA_C(len);
	JAVA_OBJECT arrayobj = array;
	uint16_t *ptr = (uint16_t *)array->getStartPtr();
	if (sizeof(wchar_t) == sizeof(uint16_t)) {
		::memcpy((void *)ptr, (void *)str, len * sizeof(uint16_t));
	} else {
		for (int n = 0; n < len; n++) ptr[n] = (uint16_t)str[n];
	}
	GET_OBJECT({% CLASS java.lang.String %}, out)->{% FIELD java.lang.String:value %} = arrayobj;
	//GET_OBJECT({% CLASS java.lang.String %}, out)->M_java_lang_String__init____CII_V(array, 0, len);
	return out;
};

JAVA_OBJECT N::str(std::wstring str) {
	int len = str.length();
	JAVA_OBJECT out(new {% CLASS java.lang.String %}());
	JA_C *array = new JA_C(len);
	JAVA_OBJECT arrayobj = array;
	uint16_t *ptr = (uint16_t *)array->getStartPtr();
	for (int n = 0; n < len; n++) ptr[n] = (uint16_t)str[n];
	GET_OBJECT({% CLASS java.lang.String %}, out)->{% FIELD java.lang.String:value %} = arrayobj;
	//GET_OBJECT({% CLASS java.lang.String %}, out)->M_java_lang_String__init____CII_V(array, 0, len);
	return out;
};

JAVA_OBJECT N::str(std::string s) {
	//if (s == NULL) return SOBJ(NULL);
	std::wstring ws(s.begin(), s.end());
	return N::str(ws);
};

JAVA_OBJECT N::str(char *s) {
	if (s == NULL) return NULL;
	int len = strlen(s);
	JAVA_OBJECT out(new {% CLASS java.lang.String %}());
	JA_C *array = new JA_C(len);
	JAVA_OBJECT arrayobj = array;
	uint16_t *ptr = (uint16_t *)array->getStartPtr();
	//::memcpy((void *)ptr, (void *)str, len * sizeof(uint16_t));
	for (int n = 0; n < len; n++) ptr[n] = (uint16_t)s[n];
	GET_OBJECT({% CLASS java.lang.String %}, out)->{% FIELD java.lang.String:value %} = arrayobj;
	//GET_OBJECT({% CLASS java.lang.String %}, out)->M_java_lang_String__init____CII_V(array, 0, len);
	return out;
};

JAVA_OBJECT N::strArray(int count, wchar_t **strs) {
	JA_L* out = new JA_L(count, L"[java/lang/String;");
	for (int n = 0; n < count; n++) out->set(n, N::str(std::wstring(strs[n])));
	return out;
}

JAVA_OBJECT N::strArray(std::vector<std::wstring> strs) {
	int len = strs.size();
	JA_L* out = new JA_L(len, L"[java/lang/String;");
	for (int n = 0; n < len; n++) out->set(n, N::str(strs[n]));
	return out;
}

JAVA_OBJECT N::strArray(std::vector<std::string> strs) {
	int len = strs.size();
	JA_L* out = new JA_L(len, L"[Ljava/lang/String;");
	for (int n = 0; n < len; n++) out->set(n, N::str(strs[n]));
	return out;
}

JAVA_OBJECT N::strEmptyArray() {
	JA_L* out = new JA_L(0, L"Ljava/lang/String;");
	return out;
}

std::wstring N::istr2(JAVA_OBJECT obj) {
	int len = N::strLen(obj);
	std::wstring s;
	s.reserve(len);
	for (int n = 0; n < len; n++) s.push_back(N::strCharAt(obj, n));
	return s;
}

std::string N::istr3(JAVA_OBJECT obj) {
	int len = N::strLen(obj);
	std::string s;
	s.reserve(len);
	for (int n = 0; n < len; n++) s.push_back(N::strCharAt(obj, n));
	return s;
}

int N::strLen(JAVA_OBJECT obj) {
	auto str = GET_OBJECT({% CLASS java.lang.String %}, obj);
	return str->{% METHOD java.lang.String:length %}();
}

int N::strCharAt(JAVA_OBJECT obj, int n) {
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
	return NULL;
}

void N::throwNpe(const wchar_t* position) {
	TRACE_REGISTER("N::throwNpe()");
	std::wcout << L"N::throwNpe():" << std::wstring(position) << L"\n";
	throw {% CONSTRUCTOR java.lang.NullPointerException:()V %}();
}

JAVA_OBJECT N::ensureNpe(JAVA_OBJECT obj, const wchar_t* position) {
	#ifdef CHECK_NPE
	if (obj == NULL) N::throwNpe(position);
	#endif
	return obj;
}

void N::throwNpe() {
	N::throwNpe(L"unknown");
}

JAVA_OBJECT N::ensureNpe(JAVA_OBJECT obj) {
	#ifdef CHECK_NPE
	if (obj == NULL) N::throwNpe();
	#endif
	return obj;
}

int N::identityHashCode(JAVA_OBJECT obj) {
	return (int32_t)(size_t)(void *)(obj);
}


void N::writeChars(JAVA_OBJECT str, char *out, int maxlen) {
	int len = std::min(N::strLen(str), maxlen - 1);
	for (int n = 0; n < len; n++) {
		out[n] = N::strCharAt(str, n);
	}
	out[len] = 0;
}

JAVA_OBJECT    N::unboxVoid(JAVA_OBJECT obj) { return NULL; }
int32_t N::unboxBool(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Boolean %}, obj)->{% SMETHOD java.lang.Boolean:booleanValue %}(); }
int32_t N::unboxByte(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Byte %}, obj)->{% SMETHOD java.lang.Byte:byteValue %}(); }
int32_t N::unboxShort(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Short %}, obj)->{% SMETHOD java.lang.Short:shortValue %}(); }
int32_t N::unboxChar(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Character %}, obj)->{% SMETHOD java.lang.Character:charValue %}(); }
int32_t N::unboxInt(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Integer %}, obj)->{% SMETHOD java.lang.Integer:intValue %}(); }
int64_t N::unboxLong(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Long %}, obj)->{% SMETHOD java.lang.Long:longValue %}(); }
float   N::unboxFloat(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Float %}, obj)->{% SMETHOD java.lang.Float:floatValue %}(); }
double  N::unboxDouble(JAVA_OBJECT obj) { return GET_OBJECT({% CLASS java.lang.Double %}, obj)->{% SMETHOD java.lang.Double:doubleValue %}(); }

JAVA_OBJECT N::boxVoid(void)       { return null; }
JAVA_OBJECT N::boxVoid(JAVA_OBJECT v)     { return null; }
JAVA_OBJECT N::boxBool(bool v)     { return {% SMETHOD java.lang.Boolean:valueOf:(Z)Ljava/lang/Boolean; %}(v); }
JAVA_OBJECT N::boxByte(int32_t v)  { return {% SMETHOD java.lang.Byte:valueOf:(B)Ljava/lang/Byte; %}(v); }
JAVA_OBJECT N::boxShort(int32_t v) { return {% SMETHOD java.lang.Short:valueOf:(S)Ljava/lang/Short; %}(v); }
JAVA_OBJECT N::boxChar(int32_t v)  { return {% SMETHOD java.lang.Character:valueOf:(C)Ljava/lang/Character; %}(v); }
JAVA_OBJECT N::boxInt(int32_t v)   { return {% SMETHOD java.lang.Integer:valueOf:(I)Ljava/lang/Integer; %}(v); }
JAVA_OBJECT N::boxLong(int64_t v)  { return {% SMETHOD java.lang.Long:valueOf:(J)Ljava/lang/Long; %}(v); }
JAVA_OBJECT N::boxFloat(float v)   { return {% SMETHOD java.lang.Float:valueOf:(F)Ljava/lang/Float; %}(v); }
JAVA_OBJECT N::boxDouble(double v) { return {% SMETHOD java.lang.Double:valueOf:(D)Ljava/lang/Double; %}(v); }

//SOBJ JA_0::{% METHOD java.lang.Object:getClass %}() { return {% SMETHOD java.lang.Class:forName0 %}(N::str(desc)); }

std::vector<JAVA_OBJECT> N::getVectorOrEmpty(JAVA_OBJECT obj) {
	auto array = GET_OBJECT(JA_L, obj);
	if (array == NULL)  return std::vector<JAVA_OBJECT>(0);
	return array->getVector();
};

#include <chrono>

double N::getTime() {
	using namespace std::chrono;
    milliseconds ms = duration_cast< milliseconds >(
        system_clock::now().time_since_epoch()
    );

	return (double)(int64_t)ms.count();
};

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









JAVA_OBJECT jtvmNewDirectByteBuffer(JNIEnv* env, void* address, jlong capacity){
	JA_B* byteArray = new JA_B(address, capacity);
	return {% CONSTRUCTOR java.nio.ByteBuffer:([BZ)V %}(byteArray, (int8_t)true);

	/*auto byteArray = SOBJ(new JA_B(address, capacity));
	std::cerr << "N::jtvmNewDirectByteBuffer after byte array";
	//std::shared_ptr<JA_L> out(new JA_L(count, L"[java/lang/String;"));
    //for (int n = 0; n < count; n++) out->set(n, N::str(std::wstring(strs[n])));
    //return out.get()->sptr();
	auto buffer = std::make_shared<{% CLASS java.nio.ByteBuffer %}>({% CONSTRUCTOR java.nio.ByteBuffer:([BZ)V %}(byteArray, (int8_t)true)).get()->sptr();
	std::cerr << "N::jtvmNewDirectByteBuffer after alloc";
	return buffer.get();*/
	//return null;
}

jobject NewDirectByteBuffer(JNIEnv* env, void* address, jlong capacity){
	return (jobject) jtvmNewDirectByteBuffer(env, address, capacity);
}

void* jtvmGetDirectBufferAddress(JNIEnv* env, JAVA_OBJECT buf){
	auto buffer = GET_OBJECT({% CLASS java.nio.ByteBuffer %}, buf);
	//TODO check that this is a direct buffer
	return GET_OBJECT(JA_B, buffer->{% FIELD java.nio.ByteBuffer:backingArray %})->_data;
}

void* GetDirectBufferAddress(JNIEnv* env, jobject buf){
	return jtvmGetDirectBufferAddress(env, (JAVA_OBJECT) buf);
}

jlong jtvmGetDirectBufferCapacity(JNIEnv* env, JAVA_OBJECT buf){
	auto buffer = GET_OBJECT({% CLASS java.nio.ByteBuffer %}, buf);
    return GET_OBJECT(JA_B, buffer->{% FIELD java.nio.ByteBuffer:backingArray %})->length;
}

jlong GetDirectBufferCapacity(JNIEnv* env, jobject buf){
	return jtvmGetDirectBufferCapacity(env, (JAVA_OBJECT) buf);
}

jsize jtvmGetArrayLength(JNIEnv* env, jarray array){
	return ((JA_0*)array)->length;
}

jsize GetArrayLength(JNIEnv* env, jarray array){
	return jtvmGetArrayLength(env, array);
}

void* jtvmGetUniversalArrayElements(JNIEnv *env, JA_0* array, jboolean *isCopy){
	if(isCopy) *isCopy = false;
	return array->_data;
}

jboolean* GetBooleanArrayElements(JNIEnv *env, jbooleanArray array, jboolean *isCopy){
	return (jboolean*) jtvmGetUniversalArrayElements(env, (JA_Z*) array, isCopy);
}

jbyte* GetByteArrayElements(JNIEnv *env, jbyteArray array, jboolean *isCopy){
	return (jbyte*) jtvmGetUniversalArrayElements(env, (JA_0*) array, isCopy);
}

jchar* GetCharArrayElements(JNIEnv *env, jcharArray array, jboolean *isCopy){
	return (jchar*) jtvmGetUniversalArrayElements(env, (JA_0*) array, isCopy);
}

jshort* GetShortArrayElements(JNIEnv *env, jshortArray array, jboolean *isCopy){
	return (jshort*) jtvmGetUniversalArrayElements(env, (JA_0*) array, isCopy);
}

jint* GetIntArrayElements(JNIEnv *env, jintArray array, jboolean *isCopy){
	return (jint*) jtvmGetUniversalArrayElements(env, (JA_0*) array, isCopy);
}

jlong* GetLongArrayElements(JNIEnv *env, jlongArray array, jboolean *isCopy){
	return (jlong*) jtvmGetUniversalArrayElements(env, (JA_0*) array, isCopy);
}

jfloat* GetFloatArrayElements(JNIEnv *env, jfloatArray array, jboolean *isCopy){
	return (jfloat*) jtvmGetUniversalArrayElements(env, (JA_0*) array, isCopy);
}

jdouble* GetDoubleArrayElements(JNIEnv *env, jdoubleArray array, jboolean *isCopy){
	return (jdouble*) jtvmGetUniversalArrayElements(env, (JA_0*) array, isCopy);
}









void ReleaseBooleanArrayElements(JNIEnv *env, jbooleanArray array, jboolean *elems, jint mode){
}

void ReleaseByteArrayElements(JNIEnv *env, jbyteArray array, jbyte *elems, jint mode){
}

void ReleaseCharArrayElements(JNIEnv *env, jcharArray array, jchar *elems, jint mode){
}

void ReleaseShortArrayElements(JNIEnv *env, jshortArray array, jshort *elems, jint mode){
}

void ReleaseIntArrayElements(JNIEnv *env, jintArray array, jint *elems, jint mode){
}

void ReleaseLongArrayElements(JNIEnv *env, jlongArray array, jlong *elems, jint mode){
}

void ReleaseFloatArrayElements(JNIEnv *env, jfloatArray array, jfloat *elems, jint mode){
}

void ReleaseDoubleArrayElements(JNIEnv *env, jdoubleArray array, jdouble *elems, jint mode){
}


static jboolean checkBounds(JNIEnv* env, JA_0* array, jint start, jint len){
	jsize arrayLength = array->length;
	jsize end = start + arrayLength;
	if(len < 0 || len < 0 || end > arrayLength){
		//jtvmThrowException();
		return false;
	}
	return true;
}

void GetBooleanArrayRegion(JNIEnv *env, jbooleanArray array, jsize start, jsize len, jboolean *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jboolean* )((JA_Z*) array)->_data) + start, sizeof(jboolean) * len);
}

void GetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len, jbyte *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jbyte* )((JA_B*) array)->_data) + start, sizeof(jbyte) * len);
}

void GetCharArrayRegion(JNIEnv *env, jcharArray array, jsize start, jsize len, jchar *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jchar* )((JA_C*) array)->_data) + start, sizeof(jchar) * len);
}

void GetShortArrayRegion(JNIEnv *env, jshortArray array, jsize start, jsize len, jshort *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jshort* )((JA_S*) array)->_data) + start, sizeof(jshort) * len);
}

void GetIntArrayRegion(JNIEnv *env, jintArray array, jsize start, jsize len, jint *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jint* )((JA_I*) array)->_data) + start, sizeof(jint) * len);
}

void GetLongArrayRegion(JNIEnv *env, jlongArray array, jsize start, jsize len, jlong *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jlong* )((JA_J*) array)->_data) + start, sizeof(jlong) * len);
}

void GetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize start, jsize len, jfloat *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jfloat* )((JA_F*) array)->_data) + start, sizeof(jfloat) * len);
}

void GetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize start, jsize len, jdouble *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(buf, ((jdouble* )((JA_D*) array)->_data) + start, sizeof(jdouble) * len);
}

void SetBooleanArrayRegion(JNIEnv *env, jbooleanArray array, jsize start, jsize len, const jboolean *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jboolean* )((JA_Z*) array)->_data) + start, buf, sizeof(jboolean) * len);
}

void SetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len, const jbyte *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jbyte* )((JA_B*) array)->_data) + start, buf, sizeof(jbyte) * len);
}

void SetCharArrayRegion(JNIEnv *env, jcharArray array, jsize start, jsize len, const jchar *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jchar* )((JA_C*) array)->_data) + start, buf, sizeof(jchar) * len);
}

void SetShortArrayRegion(JNIEnv *env, jshortArray array, jsize start, jsize len, const jshort *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jshort* )((JA_S*) array)->_data) + start, buf, sizeof(jshort) * len);
}

void SetIntArrayRegion(JNIEnv *env, jintArray array, jsize start, jsize len, const jint *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jint* )((JA_I*) array)->_data) + start, buf, sizeof(jint) * len);
}

void SetLongArrayRegion(JNIEnv *env, jlongArray array, jsize start, jsize len, const jlong *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jlong* )((JA_J*) array)->_data) + start, buf, sizeof(jlong) * len);
}

void SetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize start, jsize len, const jfloat *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jfloat* )((JA_F*) array)->_data) + start, buf, sizeof(jfloat) * len);
}

void SetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize start, jsize len, const jdouble *buf){
	if(!checkBounds(env, (JA_0*) array, start, len)) return;
	memcpy(((jdouble* )((JA_D*) array)->_data) + start, buf, sizeof(jdouble) * len);
}


void* GetPrimitiveArrayCritical(JNIEnv *env, jarray array, jboolean *isCopy){
	if(isCopy) *isCopy = false;
	return ((JA_0*) array)->_data;
}

void ReleasePrimitiveArrayCritical(JNIEnv *env, jarray array, void *carray, jint mode){

}

JA_Z* jtvmNewBooleanArray(JNIEnv* env, jsize length){
	JA_Z* out = new JA_Z(length);
    return out;
}

jbooleanArray NewBooleanArray(JNIEnv* env, jsize length){
	return (jbooleanArray) jtvmNewBooleanArray(env, length);
}


JA_B* jtvmNewByteArray(JNIEnv* env, jsize length){
	JA_B* out = new JA_B(length);
    return out;
}

jbyteArray NewByteArray(JNIEnv* env, jsize length){
	return (jbyteArray) jtvmNewByteArray(env, length);
}


JA_C* jtvmNewCharArray(JNIEnv* env, jsize length){
	JA_C* out = new JA_C(length);
    return out;
}

jcharArray NewCharArray(JNIEnv* env, jsize length){
	return (jcharArray) jtvmNewCharArray(env, length);
}


JA_S* jtvmNewShortArray(JNIEnv* env, jsize length){
	JA_S* out = new JA_S(length);
    return out;
}

jshortArray NewShortArray(JNIEnv* env, jsize length){
	return (jshortArray) jtvmNewShortArray(env, length);
}

JA_I* jtvmNewIntArray(JNIEnv* env, jsize length){
	JA_I* out = new JA_I(length);
    return out;
}

jintArray NewIntArray(JNIEnv* env, jsize length){
	return (jintArray) jtvmNewIntArray(env, length);
}

JA_J* jtvmNewLongArray(JNIEnv* env, jsize length){
	JA_J* out = new JA_J(length);
    return out;
}

jlongArray NewLongArray(JNIEnv* env, jsize length){
	return (jlongArray) jtvmNewLongArray(env, length);
}

JA_F* jtvmNewFloatArray(JNIEnv* env, jsize length){
	JA_F* out = new JA_F(length);
    return out;
}

jfloatArray NewFloatArray(JNIEnv* env, jsize length){
	return (jfloatArray) jtvmNewFloatArray(env, length);
}

JA_D* jtvmNewDoubleArray(JNIEnv* env, jsize length){
	JA_D* out = new JA_D(length);
    return out;
}

jdoubleArray NewDoubleArray(JNIEnv* env, jsize length){
	return (jdoubleArray) jtvmNewDoubleArray(env, length);
}

JNIEnv* getJniEnv(){
	return &N::env.jni;
}

jint GetVersion(JNIEnv* env){
	return JNI_VERSION_1_6;
}

JAVA_OBJECT jtvmFindClass(JNIEnv* env, const char *name){
	return N::resolveClass(N::istr2(N::str(name)));
	// FIXME horribly inefficient
	// FIXME semantics are probably wrong
}

jclass FindClass(JNIEnv* env, const char *name){
	return (jclass) jtvmFindClass(env, name);
}

JAVA_OBJECT jtvmGetObjectClass(JNIEnv *env, JAVA_OBJECT obj){
	return obj->{% METHOD java.lang.Object:getClass %}();
}

jclass GetObjectClass(JNIEnv *env, jobject obj){
	return (jclass) jtvmGetObjectClass(env, (JAVA_OBJECT) obj);
}

bool jtvmIsInstanceOf(JNIEnv *env, JAVA_OBJECT obj, JAVA_OBJECT clazz){
	return N::is(obj, GET_OBJECT_NPE({% CLASS java.lang.Class %}, clazz)->{% FIELD java.lang.Class:id %}); // FIXME verification and stuff...
}

jboolean IsInstanceOf(JNIEnv *env, jobject obj, jclass clazz){
	return jtvmIsInstanceOf(env, (JAVA_OBJECT) obj, (JAVA_OBJECT) clazz);
}



const struct JNINativeInterface_ jni = {

	NULL,
	NULL,
	NULL,
	NULL,
	&GetVersion,

	NULL,
	&FindClass,

	NULL,
	NULL,
	NULL,

	NULL,
	NULL,

	NULL,

	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,

	NULL,
	NULL,

	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,

	NULL,
	NULL,//&NewObject,
	NULL,//&NewObjectV,
	NULL,//&NewObjectA,

	&GetObjectClass,
	&IsInstanceOf,

	NULL,

	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,

	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,

	NULL,

	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,

	NULL,

	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,

	NULL,

	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,

	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,

	NULL,

	NULL,
	NULL,
	NULL,

	NULL,
	NULL,
	NULL,
	NULL,

	&GetArrayLength/*NULL*/,

	NULL,
	NULL,
	NULL,

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

	NULL,
	NULL,

	NULL,
	NULL,

	NULL,

	NULL,
    NULL,

	&GetPrimitiveArrayCritical,
    &ReleasePrimitiveArrayCritical,

	NULL,
	NULL,

	NULL,
	NULL,

	NULL,

	&NewDirectByteBuffer,
	&GetDirectBufferAddress,
	&GetDirectBufferCapacity,

	NULL
};







Env N::env;
void N::startup() {
	N::env.jni.functions = &jni;
	setvbuf(stdout, NULL, _IONBF, 0);
	setvbuf(stderr, NULL, _IONBF, 0);
	std::signal(SIGSEGV, SIGSEGV_handler);
	std::signal(SIGFPE, SIGFPE_handler);

	N::initStringPool();
};

// Type Table Footer
{{ TYPE_TABLE_FOOTER }}

// Main
{{ MAIN }}