package java.nio.file;

import java.io.IOException;

public class FileSystemException extends IOException {
	private final String file;
	private final String other;

	public FileSystemException(String file) {
		super((String)null);
		this.file = file;
		this.other = null;
	}

	public FileSystemException(String file, String other, String reason) {
		super(reason);
		this.file = file;
		this.other = other;
	}

	public String getFile() {
		return file;
	}
	public String getOtherFile() {
		return other;
	}
	public String getReason() {
		return super.getMessage();
	}

	@Override
	public String getMessage() {
		String out = "";
		if (file != null) out += file;
		if (other != null) {
			out += " -> ";
			out += other;
		}
		if (getReason() != null) {
			if (!out.isEmpty()) out += ": ";
			out += getReason();
		}
		return out;
	}
}
