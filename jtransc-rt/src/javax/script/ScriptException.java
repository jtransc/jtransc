/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package javax.script;

public class ScriptException extends Exception {
	private String fileName;
	private int lineNumber;
	private int columnNumber;

	public ScriptException(String s) {
		super(s);
		fileName = null;
		lineNumber = -1;
		columnNumber = -1;
	}

	public ScriptException(Exception e) {
		super(e);
		fileName = null;
		lineNumber = -1;
		columnNumber = -1;
	}

	public ScriptException(String message, String fileName, int lineNumber) {
		super(message);
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.columnNumber = -1;
	}

	public ScriptException(String message, String fileName, int lineNumber, int columnNumber) {
		super(message);
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}

	public String getMessage() {
		return super.getMessage() + (" in " + fileName) + " at line number " + lineNumber + " at column number " + columnNumber;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	public String getFileName() {
		return fileName;
	}
}
