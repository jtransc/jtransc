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

import com.jtransc.annotation.JTranscSync;

import java.util.Objects;

@SuppressWarnings("unused")
public final class StackTraceElement implements java.io.Serializable {
	private String declaringClass;
	private String methodName;
	private String fileName;
	private int lineNumber;

	@JTranscSync
	public StackTraceElement(String declaringClass, String methodName, String fileName, int lineNumber) {
		this.declaringClass = (declaringClass != null) ? declaringClass : "UNKNOWN";
		this.methodName = (methodName != null) ? methodName : "UNKNOWN";
		this.fileName = (fileName != null) ? fileName : "UNKNOWN";
		this.lineNumber = lineNumber;
	}

	@JTranscSync
	public String getFileName() {
		return fileName;
	}

	@JTranscSync
	public int getLineNumber() {
		return lineNumber;
	}

	@JTranscSync
	public String getClassName() {
		return declaringClass;
	}

	@JTranscSync
	public String getMethodName() {
		return methodName;
	}

	@JTranscSync
	public boolean isNativeMethod() {
		return lineNumber == -2;
	}

	public String toString() {
		return getClassName() + "." + methodName +
			(isNativeMethod() ? "(Native Method)" :
				(fileName != null && lineNumber >= 0 ?
					"(" + fileName + ":" + lineNumber + ")" :
					(fileName != null ? "(" + fileName + ")" : "(Unknown Source)")));
	}

	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof StackTraceElement)) return false;
		StackTraceElement e = (StackTraceElement) obj;
		return e.declaringClass.equals(declaringClass) &&
			e.lineNumber == lineNumber &&
			Objects.equals(methodName, e.methodName) &&
			Objects.equals(fileName, e.fileName);
	}

	public int hashCode() {
		int result = 31 * declaringClass.hashCode() + methodName.hashCode();
		result = 31 * result + Objects.hashCode(fileName);
		result = 31 * result + lineNumber;
		return result;
	}
}
