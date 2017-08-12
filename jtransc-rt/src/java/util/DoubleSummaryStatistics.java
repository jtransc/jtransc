package java.util;

import java.util.function.DoubleConsumer;

public class DoubleSummaryStatistics implements DoubleConsumer {
	public DoubleSummaryStatistics() {
	}

	@Override
	native public void accept(double value);

	native public void combine(DoubleSummaryStatistics other);

	native public final long getCount();

	native public final double getSum();

	native public final double getMin();

	native public final double getMax();

	native public final double getAverage();

	@Override
	native public String toString();
}
