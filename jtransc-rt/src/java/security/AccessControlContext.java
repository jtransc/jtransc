package java.security;

public final class AccessControlContext {
	public AccessControlContext(ProtectionDomain context[]) {
	}

	public AccessControlContext(AccessControlContext acc, DomainCombiner combiner) {
	}

	public DomainCombiner getDomainCombiner() {
		return null;
	}

	public void checkPermission(Permission perm) throws AccessControlException {
	}
}
