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

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.io.JTranscConsole;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;

public class Throwable implements Serializable {
	//private String detailMessage;
	//private Throwable cause;

	private String message;
	private Throwable cause;
	private boolean enableSuppression = false;
	private boolean writableStackTrace = false;

	public Throwable() {
		fillInStackTrace();
		t_init(null, null, false, false);
	}

	public Throwable(String message) {
		fillInStackTrace();
		t_init(message, null, false, false);
	}

	public Throwable(String message, Throwable cause) {
		fillInStackTrace();
		t_init(message, cause, false, false);
	}

	public Throwable(Throwable cause) {
		fillInStackTrace();
		t_init(null, cause, false, false);
	}

	protected Throwable(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		fillInStackTrace();
		t_init(message, cause, enableSuppression, writableStackTrace);
	}

	private void t_init(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		this.message = message;
		this.cause = cause;
		this.enableSuppression = enableSuppression;
		this.writableStackTrace = writableStackTrace;
	}

	public String getMessage() {
		return message;
	}

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

	public void printStackTrace() {
		// Print our stack trace
		JTranscConsole.error(this);
		StackTraceElement[] trace = this.stackTrace;
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
		StackTraceElement[] trace = this.stackTrace;
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

	native public void printStackTrace(PrintWriter s);

	public synchronized Throwable fillInStackTrace() {
		fillInStackTrace(0);
		return this;
	}

	private StackTraceElement[] stackTrace;

	private void fillInStackTrace(int dummy) {
		setStackTrace(getStackTraceInternal());
	}

	@HaxeMethodBody("return N.getStackTrace(1);")
	@JTranscMethodBody(target = "js", value = "return N.getStackTrace(1);")
	@JTranscMethodBody(target = "cs", value = "return N.getStackTrace(1);")
	private StackTraceElement[] getStackTraceInternal() {
		return new StackTraceElement[] {
			new StackTraceElement("DummyClass", "dummyMethod", "DummyClass.java", 1),
			new StackTraceElement("DummyClass", "dummyMethod", "DummyClass.java", 1),
			new StackTraceElement("DummyClass", "dummyMethod", "DummyClass.java", 1)
		};
	}

	//private native Throwable fillInStackTrace(int dummy);
	public StackTraceElement[] getStackTrace() {
		return this.stackTrace.clone();
	}

	public void setStackTrace(StackTraceElement[] stackTrace) {
		this.stackTrace = stackTrace;
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