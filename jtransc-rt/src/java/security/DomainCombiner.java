package java.security;

public interface DomainCombiner {
	ProtectionDomain[] combine(ProtectionDomain[] currentDomains, ProtectionDomain[] assignedDomains);
}
