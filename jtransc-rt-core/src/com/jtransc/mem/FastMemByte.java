package com.jtransc.mem;

import com.jtransc.annotation.JTranscAddHeader;
import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscSync;

@SuppressWarnings("JavacQuirks")
@JTranscAddHeader(target = "cpp", value = {
	"static int8_t *__JTRANSC_byte_memSRC = NULL;",
	"static int8_t *__JTRANSC_byte_memDST = NULL;",
	"static int8_t *__JTRANSC_byte_memTMP = NULL;",
})
public class FastMemByte {
	static private byte[] memSRC;
	static private byte[] memDST;
	static private byte[] memTMP;

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "__JTRANSC_byte_memSRC = (int8_t *)GET_OBJECT(JA_B, p0)->getOffsetPtr(0); {% SFIELD com.jtransc.mem.FastMemByte:memSRC %} = p0;")
	@JTranscSync
	static public void selectSRC(byte[] mem) {
		memSRC = mem;
	}

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "__JTRANSC_byte_memDST = (int8_t *)GET_OBJECT(JA_B, p0)->getOffsetPtr(0); {% SFIELD com.jtransc.mem.FastMemByte:memDST %} = p0;")
	@JTranscSync
	static public void selectDST(byte[] mem) {
		memDST = mem;
	}

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "__JTRANSC_byte_memTMP = (int8_t *)GET_OBJECT(JA_B, p0)->getOffsetPtr(0); {% SFIELD com.jtransc.mem.FastMemByte:memTMP %} = p0;")
	@JTranscSync
	static public void selectTMP(byte[] mem) {
		memTMP = mem;
	}

	///////////////////////////////////////////////////////////////////////

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "__JTRANSC_byte_memSRC[p0] = p1;")
	@JTranscSync
	static public void setSRC(int index, byte value) {
		memSRC[index] = (byte) value;
	}

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "__JTRANSC_byte_memDST[p0] = p1;")
	@JTranscSync
	static public void setDST(int index, byte value) {
		memDST[index] = (byte) value;
	}

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "__JTRANSC_byte_memTMP[p0] = p1;")
	@JTranscSync
	static public void setTMP(int index, byte value) {
		memTMP[index] = (byte) value;
	}

	/////////////////////////////////////////////////////////////////////////

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "return __JTRANSC_byte_memSRC[p0];")
	@JTranscSync
	static public byte getSRC(int index) {
		return memSRC[index];
	}

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "return __JTRANSC_byte_memDST[p0];")
	@JTranscSync
	static public byte getDST(int index) {
		return memDST[index];
	}

	@JTranscInline
	@JTranscMethodBody(target = "cpp", value = "return __JTRANSC_byte_memTMP[p0];")
	@JTranscSync
	static public byte getTMP(int index) {
		return memTMP[index];
	}
}
