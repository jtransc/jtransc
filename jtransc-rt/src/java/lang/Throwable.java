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

import com.jtransc.annotation.*;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.io.JTranscConsole;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;

@JTranscAddMembersList({
	@JTranscAddMembers(target = "cs", value = "public Exception csException; System.Diagnostics.StackTrace currentStackTrace;"),
	@JTranscAddMembers(target = "dart", value = "Error dartError; StackTrace currentStackTrace;"),
	//@JTranscAddMembers(target = "js", value = "Error error;"),
})
public class Throwable implements Serializable {
	//private String detailMessage;
	//private Throwable cause;

	private String message;
	private Throwable cause;
	private boolean enableSuppression = false;
	private boolean writableStackTrace = false;

	@JTranscSync
	public Throwable() {
		init(null, null, false, false);
	}

	@JTranscSync
	public Throwable(String message) {
		init(message, null, false, false);
	}

	@JTranscSync
	public Throwable(String message, Throwable cause) {
		init(message, cause, false, false);
	}

	@JTranscSync
	public Throwable(Throwable cause) {
		init(null, cause, false, false);
	}

	@JTranscSync
	protected Throwable(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		init(message, cause, enableSuppression, writableStackTrace);
	}

	@JTranscSync
	private void init(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		this.message = message;
		this.cause = cause;
		this.enableSuppression = enableSuppression;
		this.writableStackTrace = writableStackTrace;
	}

	@JTranscSync
	public String getMessage() {
		return message;
	}

	@JTranscSync
	public String getLocalizedMessage() {
		return message;
	}

	public synchronized Throwable getCause() {
		return cause;
	}

	public synchronized Throwable initCause(Throwable cause) {
		this.cause = cause;
		return this.cause;
	}

	//@JTranscMethodBody(target = "js", value = "return N.str('Object');")
	public String toString() {
		//return getClass().getName() + ":" + message;
		return "Exception:" + message;
	}

	private StackTraceElement[] getStackTraceLazy() {
		if (this.stackTrace == null) {
			fillInStackTrace(0);
		}
		return this.stackTrace;
	}

	public void printStackTrace() {
		// Print our stack trace
		JTranscConsole.error(this);
		StackTraceElement[] trace = this.getStackTraceLazy();
		for (StackTraceElement traceElement : trace)
			JTranscConsole.error("\tat " + traceElement);

		// Print suppressed exceptions, if any
		for (Throwable se : getSuppressed()) {
			JTranscConsole.error("Supressed:");
			se.printStackTrace();
		}

		// Print cause, if any
		Throwable ourCause = getCause();

		if (ourCause != null) {
			JTranscConsole.error("Cause:");
			ourCause.printStackTrace();
		}
	}

	public void printStackTrace(PrintStream s) {
		// Print our stack trace
		s.println(this);
		StackTraceElement[] trace = this.getStackTraceLazy();
		for (StackTraceElement traceElement : trace)
			s.println("\tat " + traceElement);

		// Print suppressed exceptions, if any
		for (Throwable se : getSuppressed()) {
			JTranscConsole.error("Supressed:");
			se.printStackTrace(s);
		}

		// Print cause, if any
		Throwable ourCause = getCause();

		if (ourCause != null) {
			JTranscConsole.error("Cause:");
			ourCause.printStackTrace(s);
		}
	}

	public void printStackTrace(PrintWriter s) {
		// Print our stack trace
		s.println(this);
		StackTraceElement[] trace = this.getStackTraceLazy();
		for (StackTraceElement traceElement : trace)
			s.println("\tat " + traceElement);

		// Print suppressed exceptions, if any
		for (Throwable se : getSuppressed()) {
			JTranscConsole.error("Supressed:");
			se.printStackTrace(s);
		}

		// Print cause, if any
		Throwable ourCause = getCause();

		if (ourCause != null) {
			JTranscConsole.error("Cause:");
			ourCause.printStackTrace(s);
		}
	}

	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	private StackTraceElement[] stackTrace;

	private void fillInStackTrace(int dummy) {
		if (thrown) {
			genStackTraceFromError();
		} else {
			genStackTrace();
		}
		setStackTrace(getStackTraceInternal());
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "dart", value = "this.currentStackTrace = this.dartError.stackTrace;"),
		@JTranscMethodBody(target = "cs", value = "this.currentStackTrace = new System.Diagnostics.StackTrace(this.csException);"),
	})
	public void genStackTraceFromError() {
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "this.error = new Error();"),
		@JTranscMethodBody(target = "dart", value = "this.currentStackTrace = StackTrace.current;"),
		@JTranscMethodBody(target = "cs", value = "this.currentStackTrace = new System.Diagnostics.StackTrace();"),
	})
	public void genStackTrace() {
	}

	private boolean thrown = false;

	@JTranscKeep
	public Throwable prepareThrow() {
		if (!thrown) {
			init_exception();
		}
		thrown = true;
		return this;
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "this.error = new Error();"),
		@JTranscMethodBody(target = "dart", value = "this.dartError = new WrappedThrowable(this);"),
		@JTranscMethodBody(target = "cs", value = "this.csException = new WrappedThrowable(this);"),
	})
	private void init_exception() {

	}

	@HaxeMethodBody("return N.getStackTrace(1);")
	@JTranscMethodBody(target = "js", value = "return N.getStackTrace(this.error, 0);", async = true)
	@JTranscMethodBody(target = "cs", value = "return N.getStackTrace(this.currentStackTrace, 1);")
	@JTranscMethodBody(target = "dart", value = "return N.getStackTrace(this.currentStackTrace, 0);")
	private StackTraceElement[] getStackTraceInternal() {
		return new StackTraceElement[]{
			new StackTraceElement("DummyClass", "dummyMethod", "DummyClass.java", 1),
			new StackTraceElement("DummyClass", "dummyMethod", "DummyClass.java", 1),
			new StackTraceElement("DummyClass", "dummyMethod", "DummyClass.java", 1)
		};
	}

	//private native Throwable fillInStackTrace(int dummy);
	public StackTraceElement[] getStackTrace() {
		genStackTrace();
		return this.getStackTraceLazy().clone();
	}

	public void setStackTrace(StackTraceElement[] stackTrace) {
		this.stackTrace = stackTrace.clone();
	}

	//native int getStackTraceDepth();
	//native StackTraceElement getStackTraceElement(int index);

	private ArrayList<Throwable> supressed;

	public final synchronized void addSuppressed(Throwable exception) {
		if (supressed == null) supressed = new ArrayList<Throwable>();
		supressed.add(exception);
	}

	public final synchronized Throwable[] getSuppressed() {
		return (supressed != null) ? supressed.toArray(EMPTY_ARRAY) : EMPTY_ARRAY;
	}

	static private Throwable[] EMPTY_ARRAY = new Throwable[0];
}