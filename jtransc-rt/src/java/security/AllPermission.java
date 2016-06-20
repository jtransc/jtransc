package java.security;

public final class AllPermission extends Permission {
	public AllPermission(String name, String actions) {
		super("");
	}

	public AllPermission() {
		super("");
	}

	@Override
	public String getActions() {
		return null;
	}

	@Override
	public boolean implies(Permission permission) {
		return true;
	}
}
