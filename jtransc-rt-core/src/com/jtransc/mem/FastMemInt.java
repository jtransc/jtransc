package com.jtransc.mem;

import com.jtransc.annotation.JTranscAddHeader;
import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscSync;

@SuppressWarnings("JavacQuirks")
@JTranscAddHeader(target = "cpp", value = {
	"static int32_t *__JTRANSC_int_memSRC = NULL;",
	"static int32_t *__JTRANSC_int_memDST = NULL;",
	"static int32_t *__JTRANSC_int_memTMP = NULL;",
})
public class FastMemInt {
	static private int[] memSRC;
	static private int[] memDST;
	static private int[] memTMP;

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "__JTRANSC_int_memSRC = (int32_t *)GET_OBJECT(JA_I, p0)->getOffsetPtr(0); {% SFIELD com.jtransc.mem.FastMemInt:memSRC %} = p0;")
	@JTranscSync
	static public void selectSRC(int[] mem) {
		FastMemInt.memSRC = mem;
	}

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "__JTRANSC_int_memDST = (int32_t *)GET_OBJECT(JA_I, p0)->getOffsetPtr(0); {% SFIELD com.jtransc.mem.FastMemInt:memDST %} = p0;")
	@JTranscSync
	static public void selectDST(int[] mem) {
		FastMemInt.memDST = mem;
	}

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "__JTRANSC_int_memTMP = (int32_t *)GET_OBJECT(JA_I, p0)->getOffsetPtr(0); {% SFIELD com.jtransc.mem.FastMemInt:memTMP %} = p0;")
	@JTranscSync
	static public void selectTMP(int[] mem) {
		FastMemInt.memTMP = mem;
	}

	///////////////////////////////////////////////////////////////////////

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "__JTRANSC_int_memSRC[p0] = p1;")
	@JTranscSync
	static public void setSRC(int index, int value) {
		FastMemInt.memSRC[index] = value;
	}

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "__JTRANSC_int_memDST[p0] = p1;")
	@JTranscSync
	static public void setDST(int index, int value) {
		FastMemInt.memDST[index] = value;
	}

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "__JTRANSC_int_memTMP[p0] = p1;")
	@JTranscSync
	static public void setTMP(int index, int value) {
		FastMemInt.memTMP[index] = value;
	}

	/////////////////////////////////////////////////////////////////////////

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "return __JTRANSC_int_memSRC[p0];")
	@JTranscSync
	static public int getSRC(int index) {
		return FastMemInt.memSRC[index];
	}

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "return __JTRANSC_int_memDST[p0];")
	@JTranscSync
	static public int getDST(int index) {
		return FastMemInt.memDST[index];
	}

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "return __JTRANSC_int_memTMP[p0];")
	@JTranscSync
	static public int getTMP(int index) {
		return FastMemInt.memTMP[index];
	}
}
