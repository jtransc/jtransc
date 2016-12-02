package big

import com.jtransc.annotation.JTranscAddHeader
import com.jtransc.annotation.JTranscInline
import com.jtransc.annotation.JTranscMethodBody
import com.jtransc.io.JTranscSyncIO

object HelloWorldKotlinTest {
	@JvmStatic fun main(args: Array<String>) {
		PSP2.main(args)
	}
}

//@JTranscAddHeader(target = "cpp", value = *arrayOf("extern \"C\" {", "#include <psp2/ctrl.h>", "#include <psp2/touch.h>", "#include <psp2/display.h>", "#include <psp2/gxm.h>", "#include <psp2/types.h>", "#include <psp2/moduleinfo.h>", "#include <psp2/kernel/processmgr.h>", "}"))
//@JTranscAddLibraries(target = "cpp", value = *arrayOf("c", "SceKernel_stub", "SceDisplay_stub", "SceGxm_stub", "SceCtrl_stub", "SceTouch_stub"))
//@JTranscAddFile(target = "cpp", prepend = "draw_include.c", process = true)
//@JTranscAddFlags(target = "cpp", value = {
//        "-Wl,-q"
//})
//-lc -lSceKernel_stub -lSceDisplay_stub -lSceGxm_stub -lSceCtrl_stub -lSceTouch_stub
@Suppress("unused", "UNUSED_PARAMETER")
@JTranscAddHeader(target = "cpp", value = """
	void input_read() { }
	int pad_buttons() { return 0; }
	void font_draw_string(int x, int y, int color, char *str) { printf("%s\n", str); }
	void sceKernelExitProcess(int code) {}
	void sceDisplayWaitVblankStart() { }
	void init_video() { }
	void end_video() { }
	void clear_screen() { }
	void swap_buffers() { }
	void draw_pixel(int x, int y, int color) {}
	void draw_rectangle(int x, int y, int width, int height, int color) { printf("draw_rectangle:%d,%d,%d,%d,%d\n", x, y, width, height, color); }
""")
object PSP2 {
	private val WHITE = 0xFFFFFFFF.toInt()
	private val RED = 0xFF0000FF.toInt()
	private val PINK = 0xFFFF00FF.toInt()
	private val YELLOW = 0xFF00FFFF.toInt()
	private val BLACK = 0xFF000000.toInt()

	@JvmStatic fun main(args: Array<String>) {
		val api = Api.create()

		println("Start!")
		init_video()

		println(api.demo())

		JTranscSyncIO.impl = object : JTranscSyncIO.Impl(JTranscSyncIO.impl) {
			@JTranscMethodBody(target = "cpp", value = """
				auto str = N::istr3(p0);
				std::vector<std::string> out;
				out.push_back(str);
				out.push_back(std::string("hello"));
				out.push_back(std::string("world"));
				return N::strArray(out);
			""")
			override external fun list(file: String): Array<String>
		}

		println("Start2!")
		var frame = 0
		while (true) {
			clear_screen()
			input_read()

			draw_rectangle(0, 0, 100, 100, RED)
			draw_rectangle(100, 100, 100, 100, PINK)

			val str = "PSVITA HELLO WORLD FROM KOTLIN WITH JTRANSC! %d".format(frame)
			//val str = "PSVITA HELLO WORLD FROM KOTLIN WITH JTRANSC! " + frame

			font_draw_string(0, 0, YELLOW, str)
			font_draw_string(1, 1, BLACK, str)

			if (pad_buttons() and PSP2_CTRL_START != 0) break

			//frame_end();
			swap_buffers()
			sceDisplayWaitVblankStart()
			frame++
			if (frame >= 10) {
				break
			}
		}

		end_video()

		sceKernelExitProcess(0)
	}

	@JTranscMethodBody(target = "cpp", value = "::init_video();")
	@JTranscInline
	fun init_video() {

	}

	@JTranscMethodBody(target = "cpp", value = "::end_video();")
	@JTranscInline
	fun end_video() {

	}

	@JTranscMethodBody(target = "cpp", value = "::clear_screen();")
	@JTranscInline
	fun clear_screen() {

	}

	@JTranscMethodBody(target = "cpp", value = "::swap_buffers();")
	@JTranscInline
	fun swap_buffers() {

	}

	@JTranscMethodBody(target = "cpp", value = "::draw_pixel(p0, p1, p2);")
	@JTranscInline
	fun draw_pixel(x: Int, y: Int, color: Int) {

	}

	@JTranscMethodBody(target = "cpp", value = "::draw_rectangle(p0, p1, p2, p3, p4);")
	@JTranscInline
	fun draw_rectangle(x: Int, y: Int, w: Int, h: Int, color: Int) {
		println("draw_rectangle:$x,$y,$w,$h,$color")
	}

	//static int strLen(SOBJ obj);
	//static int strCharAt(SOBJ obj, int n);

	@JTranscMethodBody(target = "cpp", value = "int len = N::strLen(p3); char *temp = (char*)malloc(len + 1); memset(temp, 0, len + 1); for (int n = 0; n < len; n++) temp[n] = N::strCharAt(p3, n); ::font_draw_string(p0, p1, p2, temp); free((void*)temp);")
	@JTranscInline
	fun font_draw_string(x: Int, y: Int, color: Int, str: String) {
		println(str)
	}

	@JTranscMethodBody(target = "cpp", value = "::input_read();")
	@JTranscInline
	fun input_read() {

	}

	@JTranscMethodBody(target = "cpp", value = "::frame_end();")
	@JTranscInline
	fun frame_end() {

	}

	@JTranscMethodBody(target = "cpp", value = "return ::pad_buttons();")
	@JTranscInline
	fun pad_buttons(): Int {
		return 0
	}

	@JTranscMethodBody(target = "cpp", value = "::sceDisplayWaitVblankStart();")
	@JTranscInline
	fun sceDisplayWaitVblankStart() {
		try {
			Thread.sleep(20L)
		} catch (e: InterruptedException) {
			e.printStackTrace()
		}

	}

	@JTranscMethodBody(target = "cpp", value = "::sceKernelExitProcess(p0);")
	@JTranscInline
	fun sceKernelExitProcess(value: Int) {
	}

	val PSP2_CTRL_SELECT = 0x000001
	val PSP2_CTRL_START = 0x000008
	val PSP2_CTRL_UP = 0x000010
	val PSP2_CTRL_RIGHT = 0x000020
	val PSP2_CTRL_DOWN = 0x000040
	val PSP2_CTRL_LEFT = 0x000080
	val PSP2_CTRL_LTRIGGER = 0x000100
	val PSP2_CTRL_RTRIGGER = 0x000200
	val PSP2_CTRL_TRIANGLE = 0x001000
	val PSP2_CTRL_CIRCLE = 0x002000
	val PSP2_CTRL_CROSS = 0x004000
	val PSP2_CTRL_SQUARE = 0x008000
	val PSP2_CTRL_ANY = 0x010000
}


