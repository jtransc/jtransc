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

	public String toString() {
		Enumeration<Permission> enum_ = elements();
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString() + " (\n");
		while (enum_.hasMoreElements()) {
			try {
				sb.append(" ");
				sb.append(enum_.nextElement().toString());
				sb.append("\n");
			} catch (NoSuchElementException e) {
				// ignore
			}
		}
		sb.append(")\n");
		return sb.toString();
	}
}
