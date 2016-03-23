package java.security;

public class ProtectionDomain {
	private CodeSource codesource;
	private PermissionCollection permissions;
	private ClassLoader classloader;
	private Principal[] principals;

	public ProtectionDomain(CodeSource codesource, PermissionCollection permissions) {
		this(codesource, permissions, null, null);
	}

	public ProtectionDomain(CodeSource codesource, PermissionCollection permissions, ClassLoader classloader, Principal[] principals) {
		this.codesource = codesource;
		this.permissions = permissions;
		this.classloader = classloader;
		this.principals = principals;
	}

	public final CodeSource getCodeSource() {
		return this.codesource;
	}

	public final ClassLoader getClassLoader() {
		return this.classloader;
	}

	public final Principal[] getPrincipals() {
		return this.principals.clone();
	}

	public final PermissionCollection getPermissions() {
		return permissions;
	}

	public boolean implies(Permission permission) {
		return true;
	}

	@Override
	public String toString() {
		return "ProtectionDomain";
	}

}
