package java.util.logging;

import java.io.Serializable;
import java.security.BasicPermission;
import java.security.Guard;
import java.security.Permission;

public final class LoggingPermission extends BasicPermission implements Guard, Serializable {
	public LoggingPermission(String name, String actions) { super("", ""); }

	@Override public String getActions() { return null; }

	@Override public boolean implies(Permission permission) { return true; }
}