package java.nio.file.attribute;

public interface BasicFileAttributes {
	FileTime lastModifiedTime();

	FileTime lastAccessTime();

	FileTime creationTime();

	boolean isRegularFile();

	boolean isDirectory();

	boolean isSymbolicLink();

	boolean isOther();

	long size();

	Object fileKey();
}
