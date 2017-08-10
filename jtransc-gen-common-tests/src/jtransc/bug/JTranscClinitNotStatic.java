package jtransc.bug;

public class JTranscClinitNotStatic {
	static public void main(String[] args) {
		System.out.println("JTranscClinitNotStatic.main:");
		System.out.println(Filter.DEFAULT_FILTER.isAllowed(null));
		System.out.println(Filter.DEFAULT_FILTER.toString());
	}

	interface Filter {
		/** The default filter which always returns true */
		public static final Filter DEFAULT_FILTER = new Filter() {
			/* (non-Javadoc)
			 * @see org.dyn4j.collision.Filter#isAllowed(org.dyn4j.collision.Filter)
			 */
			@Override
			public boolean isAllowed(Filter filter) {
				// always return true
				return true;
			}

			/* (non-Javadoc)
			 * @see java.lang.Object#toString()
			 */
			public String toString() {
				return "DefaultFilter[]";
			}
		};

		/**
		 * Returns true if the given {@link Filter} and this {@link Filter}
		 * allow the objects to interact.
		 * <p>
		 * If the given {@link Filter} is not the same type as this {@link Filter}
		 * its up to the implementing class to specify the behavior.
		 * <p>
		 * In addition, if the given {@link Filter} is null its up to the implementing
		 * class to specify the behavior.
		 * @param filter the other {@link Filter}
		 * @return boolean
		 */
		public abstract boolean isAllowed(Filter filter);
	}
}
