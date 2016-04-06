package java.security;

public interface Principal {
	boolean equals(Object another);

	String toString();

	int hashCode();

	String getName();
}
