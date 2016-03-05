package jtransc.internal;

import java.util.Comparator;
import java.util.List;

public class JTranscSorter {
	public interface ComparableArrayLike {
		void swap(int index1, int index2);
		int compare(int index1, int index2);
		int length();
	}

	public interface BinarySearchArrayLike {
		int compare(int index);
	}

	public interface ArrayLikeWrapper extends ComparableArrayLike, BinarySearchArrayLike {

	}

	static public <T> Comparator<T> getComparator(List<? extends Comparable<? super T>> list) {
		return new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return ((Comparable<T>)o1).compareTo(o2);
			}
		};
	}


	static public void sort(ComparableArrayLike a) {
	}

	static public int binarySearch(BinarySearchArrayLike bs, int fromIndex, int toIndex) {
		int min = fromIndex;
		int max = toIndex;
		int current = max / 2;
		while (min != max) {
			int result = bs.compare(current);
			if (result < 0) {
				max = current;
			} else if (result < 0) {
				min = current;
			} else {
				return current;
			}
			current = (max + min) / 2;
		}
		return -1;
	}

	static public class IntArrayWrapped implements ArrayLikeWrapper {
		private int[] data;
		private int compareValue;

		public IntArrayWrapped(int[] data, int compareValue) {
			this.data = data;
			this.compareValue = compareValue;
		}

		@Override
		public int compare(int index) {
			return Integer.compare(data[index], compareValue);
		}

		@Override
		public void swap(int index1, int index2) {
			int temp = data[index1];
			data[index1] = data[index2];
			data[index2] = temp;
		}

		@Override
		public int compare(int index1, int index2) {
			return Integer.compare(data[index1], data[index2]);
		}

		@Override
		public int length() {
			return data.length;
		}
	}

	static public class ListWrapper<T> implements ArrayLikeWrapper {
		private List<T> data;
		private Comparator<T> comparator;
		private T compareValue;

		public ListWrapper(List<T> data, Comparator<T> comparator, T compareValue) {
			this.data = data;
			this.comparator = comparator;
			this.compareValue = compareValue;
		}

		@Override
		public void swap(int index1, int index2) {
			T temp = data.get(index1);
			data.set(index1, data.get(index2));
			data.set(index2, temp);
		}

		@Override
		public int compare(int index1, int index2) {
			return comparator.compare(data.get(index1), data.get(index2));
		}

		@Override
		public int length() {
			return data.size();
		}

		@Override
		public int compare(int index) {
			return comparator.compare(data.get(index), compareValue);
		}
	}
}
