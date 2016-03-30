package java.security;

public abstract class Permission implements Guard, java.io.Serializable {
	private String name;

	public Permission(String name) {
		this.name = name;
	}

	public void checkGuard(Object object) throws SecurityException {
	}

	public abstract boolean implies(Permission permission);

	public abstract boolean equals(Object obj);

	public abstract int hashCode();

	public final String getName() {
		return name;
	}

	public abstract String getActions();

	public PermissionCollection newPermissionCollection() {
		return null;
	}
}
