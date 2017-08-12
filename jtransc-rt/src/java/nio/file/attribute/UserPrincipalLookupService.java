package java.nio.file.attribute;

import java.io.IOException;

public abstract class UserPrincipalLookupService {

	protected UserPrincipalLookupService() {
	}

	public abstract UserPrincipal lookupPrincipalByName(String name) throws IOException;

	public abstract GroupPrincipal lookupPrincipalByGroupName(String group) throws IOException;
}
