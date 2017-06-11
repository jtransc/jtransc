import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscStruct;
import com.jtransc.ffi.StdCall;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

// https://msdn.microsoft.com/en-us/library/windows/desktop/aa383751(v=vs.85).aspx
public class BeepExample {
	public interface Kernel32 extends Library {
		@StdCall
		boolean Beep(int FREQUENCY, int DURATION);

		@StdCall
		void Sleep(int DURATION);
	}

	public interface User32 extends Library {
		@StdCall
		boolean MessageBoxA(int a, String title, String text, int b);
	}

	public static class FILETIME extends Structure {
		public int dwLowDateTime;
		public int dwHighDateTime;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("dwLowDateTime", "dwHighDateTime");
		}
	}

	// http://www.willusher.io/sdl2%20tutorials/2013/08/17/lesson-1-hello-world/
	// http://fossies.org/linux/SDL2/include/SDL.h
	// http://fossies.org/linux/SDL2/include/SDL_video.h
	/*
    public interface SDL2 extends Library {
        int SDL_INIT_VIDEO = 0x00000020;
        int SDL_WINDOW_SHOWN = 0x00000004;

        int SDL_Init(int flags);

        int SDL_CreateWindow(String title, int x, int y, int width, int height, int flags);
    }
    */

	public static void main(String[] args) throws InterruptedException {
		//long lib = JTranscFFI.Loader.dlopen("kernel32.dll");
		//System.out.println(lib);
		//long SleepAddr = JTranscFFI.Loader.dlsym(lib, "Sleep");
		//System.out.println(SleepAddr);

		//SDL2 sdl2 = (SDL2) Native.loadLibrary("sdl2", SDL2.class);
		//sdl2.SDL_Init(SDL2.SDL_INIT_VIDEO);
		//sdl2.SDL_CreateWindow("Hello World!", 100, 100, 640, 480, SDL2.SDL_WINDOW_SHOWN);
		////Pointer.createConstant(10).getNativeLong()

		//Thread.sleep(1000L);

		if (JTranscSystem.isWindows()) {
			Kernel32 kernel32 = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);
			User32 user32 = (User32) Native.loadLibrary("user32", User32.class);
			for (int n = 0; n < 6; n++) {
				kernel32.Beep(698 * (n + 1), 300);
				kernel32.Sleep(100);
			}
			user32.MessageBoxA(0, "done!", "done!", 0);
		} else {
			System.out.println("This demo just works on windows!");
		}
	}
}