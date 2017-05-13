package com.jtransc.time;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscMethodBodyList;
import com.jtransc.annotation.haxe.HaxeMethodBody;

public class JTranscClock {
	static public Impl impl = new Impl(null) {
	};

	static public class Impl {
		public Impl parent;

		public Impl(Impl parent) {
			this.parent = parent;
		}

		@HaxeMethodBody("return N.getTime();")
		@JTranscMethodBodyList({
			@JTranscMethodBody(target = "js", value = "return N.getTime();"),
			@JTranscMethodBody(target = "cpp", value = "return N::getTime();"),
			@JTranscMethodBody(target = "cs", value = "return N.getTime();"),
			@JTranscMethodBody(target = "as3", value = "return new Date().time;"), // Optimize this to avoid allocations (using just one new Date().time + getTimer())!
		})
		public double fastTime() {
			if (parent != null) {
				return parent.fastTime();
			}

			if (JTranscSystem.isJTransc()) {
				throw new RuntimeException("Not implemented JTranscSystem.fastTime()");
			} else {
				return System.currentTimeMillis();
			}
		}

		//performance.now()
		//process.hrtime()[1] / 1000000000.0
		@JTranscMethodBody(target = "js", value = "return N.hrtime();")
		public long nanoTime() {
			if (JTranscSystem.isJTransc()) {
				//return (long) hrtime();
				return System.currentTimeMillis() * 1000000L;
			} else {
				return System.nanoTime();
			}
		}

		@HaxeMethodBody(target = "sys", value = "Sys.sleep(p0 / 1000.0);")
		@JTranscMethodBody(target = "cs", value = {
			"System.Threading.Thread.Sleep((int)p0);"
		})
		public void sleep(double ms) {
			if (parent != null) {
				parent.sleep(ms);
				return;
			}
			if (JTranscSystem.isJTransc()) {
				double start = JTranscSystem.fastTime();
				// Spinlock/Busywait!
				while (true) {
					double current = JTranscSystem.fastTime();
					if ((current - start) >= ms) break;
				}
			} else {
				_sleep(ms);
			}
		}

		static private void _sleep(double ms) {
			try {
				Thread.sleep((long) ms);
			} catch (Throwable t) {
			}
		}
	}
}
