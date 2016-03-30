package java.security;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public abstract class PermissionCollection implements java.io.Serializable {
	private volatile boolean readOnly;

	public abstract void add(Permission permission);

	public abstract boolean implies(Permission permission);

	public abstract Enumeration<Permission> elements();

	public void setReadOnly() {
		readOnly = true;
	}

	public boolean isReadOnly() {
		return readOnly;
	}
}
