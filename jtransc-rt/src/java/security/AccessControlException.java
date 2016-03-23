package java.security;

public class AccessControlException extends java.lang.SecurityException {
	private Permission perm;

	public AccessControlException(String s) {
		super(s);
	}

	public AccessControlException(String s, Permission p) {
		super(s);
		perm = p;
	}

	public Permission getPermission() {
		return perm;
	}
}
