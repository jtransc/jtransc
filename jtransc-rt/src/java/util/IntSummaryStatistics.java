package java.util;

import java.util.function.IntConsumer;

public class IntSummaryStatistics implements IntConsumer {
	public IntSummaryStatistics() {
	}

	@Override
	native public void accept(int value);

	native public void combine(IntSummaryStatistics other);

	native public final long getCount();

	native public final long getSum();

	native public final int getMin();

	native public final int getMax();

	native public final double getAverage();

	native public String toString();
}
