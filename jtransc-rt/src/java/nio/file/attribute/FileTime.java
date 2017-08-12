package java.nio.file.attribute;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public final class FileTime implements Comparable<FileTime> {
	native public static FileTime from(long value, TimeUnit unit);

	native public static FileTime fromMillis(long value);

	native public static FileTime from(Instant instant);

	native public long to(TimeUnit unit);

	native public long toMillis();

	native public Instant toInstant();

	@Override
	native public boolean equals(Object obj);

	@Override
	native public int hashCode();

	@Override
	native public int compareTo(FileTime that);

	@Override
	native public String toString();
}
