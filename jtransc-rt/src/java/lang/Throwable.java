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

import jtransc.annotation.haxe.HaxeMethodBody;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;

public class Throwable implements Serializable {
	//private String detailMessage;
	//private Throwable cause;

	private String message;
	private Throwable cause;
	private boolean enableSuppression = false;
	private boolean writableStackTrace = false;

	public Throwable() {
		this("Throwable", null, false, false);
	}

	public Throwable(String message) {
		this(message, null, false, false);
	}

	public Throwable(String message, Throwable cause) {
		this(message, null, false, false);
	}

	public Throwable(Throwable cause) {
		this("Throwable", null, false, false);
	}

	protected Throwable(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		fillInStackTrace();
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

	public String toString() {
		return getClass().getName() + ":" + message;
	}

	public void printStackTrace() {
		printStackTrace(System.err);
	}

	public void printStackTrace(PrintStream s) {
		// Print our stack trace
		s.println(this);
		StackTraceElement[] trace = this.stackTrace;
		for (StackTraceElement traceElement : trace)
			s.println("\tat " + traceElement);

		/*
		// Print suppressed exceptions, if any
		for (Throwable se : getSuppressed())
			se.printEnclosedStackTrace(s, trace, SUPPRESSED_CAPTION, "\t", dejaVu);

		// Print cause, if any
		Throwable ourCause = getCause();
		if (ourCause != null)
			ourCause.printEnclosedStackTrace(s, trace, CAUSE_CAPTION, "", dejaVu);
		*/
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

	@HaxeMethodBody("return HaxeNatives.getStackTrace(1);")
	native private StackTraceElement[] getStackTraceInternal();

	//private native Throwable fillInStackTrace(int dummy);
	public StackTraceElement[] getStackTrace() {
		return this.stackTrace.clone();
	}

	public void setStackTrace(StackTraceElement[] stackTrace) {
		this.stackTrace = stackTrace;
	}

	//native int getStackTraceDepth();
	//native StackTraceElement getStackTraceElement(int index);
	native public final synchronized void addSuppressed(Throwable exception);

	native public final synchronized Throwable[] getSuppressed();
}