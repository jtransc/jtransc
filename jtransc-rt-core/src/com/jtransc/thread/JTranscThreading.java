package com.jtransc.thread;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscMethodBodyList;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

public class JTranscThreading {
	static public Impl impl = new Impl(null);

	@HaxeAddMembers("private static var threadsMap = new haxe.ds.ObjectMap<Dynamic, {% CLASS java.lang.Thread %}>();")
	static public class Impl {
		Impl parent;

		public Impl(Impl parent) {
			this.parent = parent;
		}

		public boolean isSupported() {
			if (parent != null) return parent.isSupported();
			return _isSupported();
		}
		@HaxeMethodBody(target = "cpp", value = "return threadsMap.get(cpp.vm.Thread.current().handle);")
		public Thread getCurrent() {
			return null;
		}

		@HaxeMethodBody(target = "cpp", value =
			"cpp.vm.Thread.create(function():Void{" +
				"var h = cpp.vm.Thread.current().handle;" +
				"threadsMap.set(h, p0);" +
				"p0{% IMETHOD java.lang.Runnable:run %}();" +
				"threadsMap.remove(h);" +
			"});")
		public void start(Thread thread) {
			if (parent != null) {
				parent.start(thread);
				return;
			}
			System.err.println("WARNING: Threads not supported! Executing thread code in the parent's thread!");
			thread.run();
		}
		
		@JTranscMethodBodyList({
			@JTranscMethodBody(target = "js", value = "return false;"),
			@JTranscMethodBody(target = "d", value = "return true;"),
		})
		private boolean _isSupported() {
			return !JTranscSystem.isJTransc();
		}

		public boolean isAlive(Thread thread) {
			if (parent != null) return parent.isAlive(thread);
			//return thread._isAlive;
			return false;
		}
	}
}
