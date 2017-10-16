/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

import com.jtransc.annotation.JTranscAddHeader;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.io.JTranscConsole;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

@JTranscAddHeader(target = "as3", value = "import flash.system.System;")
public class Runtime {
	private static Runtime current;

	public static Runtime getRuntime() {
		if (current == null) {
			current = new Runtime();
		}
		return current;
	}

	//@JTranscMethodBody(target = "js", value = "this.os = require('os');")
	private Runtime() {
	}

	@HaxeMethodBody(target = "sys", value = "Sys.exit(p0);")
	@HaxeMethodBody(target = "js", value = "untyped __js__(\"if (typeof process != 'undefined') process.exit(p0);\");")
	@HaxeMethodBody("throw 'EXIT!';")
	@JTranscMethodBody(target = "js", value = "process.exit(p0);")
	@JTranscMethodBody(target = "cpp", value = "::exit(p0);")
	@JTranscMethodBody(target = "d", value = "core.stdc.stdlib.exit(p0);")
	@JTranscMethodBody(target = "dart", value = "exit(p0);")
	@JTranscMethodBody(target = "php", value = "exit($p0);")
	native public void exit(int status);

	private Set<Thread> shutdownThreads = null;

	private void _executeShutdownHooks() {
		if (shutdownThreads != null) {
			for (Thread shutdownThread : shutdownThreads) {
				shutdownThread.run();
			}
		}
	}

	@JTranscMethodBody(target = "js", value = "var that = this; process.on('exit', function() { that{% IMETHOD java.lang.Runtime:_executeShutdownHooks %}({{ JC }}); });")
	private void _registerShutdownHook() {
	}

	private void _registerShutdownHookOnce() {
		if (shutdownThreads != null) return;
		shutdownThreads = new HashSet<>();
		_registerShutdownHook();
	}

	public void addShutdownHook(Thread hook) {
		_registerShutdownHookOnce();
		if (shutdownThreads != null) {
			shutdownThreads.add(hook);
		}
	}

	public boolean removeShutdownHook(Thread hook) {
		return shutdownThreads != null && shutdownThreads.remove(hook);
	}

	public void halt(int status) {
		exit(status);
	}

	@Deprecated
	native public static void runFinalizersOnExit(boolean value);

	public Process exec(String command) throws IOException {
		return exec(command, null, null);
	}

	public Process exec(String command, String[] envp) throws IOException {
		return exec(command, envp, null);
	}

	public Process exec(String command, String[] envp, File dir) throws IOException {
		if (command.length() == 0) throw new IllegalArgumentException("Empty command");
		StringTokenizer st = new StringTokenizer(command);
		String[] cmdarray = new String[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); i++) cmdarray[i] = st.nextToken();
		return exec(cmdarray, envp, dir);
	}

	public Process exec(String cmdarray[]) throws IOException {
		return exec(cmdarray, null, null);
	}

	public Process exec(String[] cmdarray, String[] envp) throws IOException {
		return exec(cmdarray, envp, null);
	}

	public Process exec(String[] cmdarray, String[] envp, File dir) throws IOException {
		return new ProcessBuilder(cmdarray).environment(envp).directory(dir).start();
	}

	public int availableProcessors() {
		return 1;
	}

	@JTranscMethodBody(target = "cpp", value = "return GC_get_free_bytes();")
	@JTranscMethodBody(target = "php", value = "return N::d2j((float)0.0);")
	public long freeMemory() {
		return totalMemory() - _usedMemory();
	}

	@JTranscMethodBody(target = "cpp", value = "return GC_get_total_bytes();")
	@JTranscMethodBody(target = "php", value = "return N::d2j((float)memory_get_peak_usage());")
	@HaxeMethodBody(target = "cpp", value = "return cpp.vm.Gc.memInfo(1);")
	public long totalMemory() {
		return 8 * 1024 * 1024 * 1024L;
	}

	@JTranscMethodBody(target = "cpp", value = "return GC_get_total_bytes();")
	@JTranscMethodBody(target = "php", value = "return N::d2j((float)memory_get_usage());")
	@HaxeMethodBody(target = "cpp", value = "return cpp.vm.Gc.memInfo(1);")
	public long maxMemory() {
		return 8 * 1024 * 1024 * 1024L;
	}

	@HaxeMethodBody(target = "cpp", value = "return cpp.vm.Gc.memInfo(2);")
	private static long _usedMemory() {
		return 8 * 1024 * 1024 * 1024L;
	}

	@JTranscMethodBody(target = "as3", value = "System.gc();")
	@JTranscMethodBody(target = "cpp", value = "GC_gcollect();")
	@JTranscMethodBody(target = "php", value = "gc_collect_cycles();")
	public void gc() {
	}

	public void runFinalization() {
	}

	public void traceInstructions(boolean on) {
	}

	public void traceMethodCalls(boolean on) {

	}

	public void load(String filename) {

	}

	synchronized void load0(Class<?> fromClass, String filename) {

	}

	public void loadLibrary(String libname) {

	}

	synchronized void loadLibrary0(Class<?> fromClass, String libname) {

	}

	@Deprecated
	public InputStream getLocalizedInputStream(InputStream in) {
		return in;
	}

	@Deprecated
	public OutputStream getLocalizedOutputStream(OutputStream out) {
		return out;
	}
}
