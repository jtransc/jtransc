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
