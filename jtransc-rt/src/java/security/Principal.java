package java.security;

public interface Principal {
	public boolean equals(Object another);

	public String toString();

	public int hashCode();

	public String getName();
}
