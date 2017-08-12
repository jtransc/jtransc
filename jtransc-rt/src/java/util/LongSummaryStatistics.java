package java.util;

import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public class LongSummaryStatistics implements LongConsumer, IntConsumer {
	public LongSummaryStatistics() {
	}

	@Override
	native public void accept(int value);

	@Override
	native public void accept(long value);

	native public void combine(LongSummaryStatistics other);

	native public final long getCount();

	native public final long getSum();

	native public final long getMin();

	native public final long getMax();

	native public final double getAverage();

	native public String toString();
}
