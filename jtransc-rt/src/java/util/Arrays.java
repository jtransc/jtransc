/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.util;

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.mem.FastMemByte;

import java.io.Serializable;
import java.lang.reflect.Array;

/**
 * {@code Arrays} contains static methods which operate on arrays.
 *
 * @since 1.2
 */
public class Arrays {
    private static class ArrayList<E> extends AbstractList<E> implements
            List<E>, Serializable, RandomAccess {

        private final E[] a;

        ArrayList(E[] storage) {
            if (storage == null) {
                throw new NullPointerException("storage == null");
            }
            a = storage;
        }

        @Override
        public boolean contains(Object object) {
            if (object != null) {
                for (E element : a) {
                    if (object.equals(element)) {
                        return true;
                    }
                }
            } else {
                for (E element : a) {
                    if (element == null) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public E get(int location) {
            try {
                return a[location];
            } catch (ArrayIndexOutOfBoundsException e) {
                //throw java.util.ArrayList.throwIndexOutOfBoundsException(location, a.length);
				throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public int indexOf(Object object) {
            if (object != null) {
                for (int i = 0; i < a.length; i++) {
                    if (object.equals(a[i])) {
                        return i;
                    }
                }
            } else {
                for (int i = 0; i < a.length; i++) {
                    if (a[i] == null) {
                        return i;
                    }
                }
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object object) {
            if (object != null) {
                for (int i = a.length - 1; i >= 0; i--) {
                    if (object.equals(a[i])) {
                        return i;
                    }
                }
            } else {
                for (int i = a.length - 1; i >= 0; i--) {
                    if (a[i] == null) {
                        return i;
                    }
                }
            }
            return -1;
        }

        @Override
        public E set(int location, E object) {
            E result = a[location];
            a[location] = object;
            return result;
        }

        @Override
        public int size() {
            return a.length;
        }

        @Override
        public Object[] toArray() {
            return a.clone();
        }

        @Override
        @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
        public <T> T[] toArray(T[] contents) {
            int size = size();
            if (size > contents.length) {
                contents = newInstance(contents, size);
            }
            System.arraycopy(a, 0, contents, 0, size);
            if (size < contents.length) contents[size] = null;
            return contents;
        }
    }

    private Arrays() {
        /* empty */
    }

    /**
     * Returns a {@code List} of the objects in the specified array. The size of the
     * {@code List} cannot be modified, i.e. adding and removing are unsupported, but
     * the elements can be set. Setting an element modifies the underlying
     * array.
     *
     * @param array
     *            the array.
     * @return a {@code List} of the elements of the specified array.
     */
    @SafeVarargs
    public static <T> List<T> asList(T... array) {
        return new ArrayList<T>(array);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     */
    public static int binarySearch(byte[] array, byte value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(byte[] array, int startIndex, int endIndex, byte value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            byte midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     */
    public static int binarySearch(char[] array, char value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(char[] array, int startIndex, int endIndex, char value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            char midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     */
    public static int binarySearch(double[] array, double value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(double[] array, int startIndex, int endIndex, double value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            double midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else if (midVal != 0 && midVal == value) {
                return mid;  // value found
            } else { // Either midVal and value are == 0 or at least one is NaN
                long midValBits = Double.doubleToLongBits(midVal);
                long valueBits  = Double.doubleToLongBits(value);

                if (midValBits < valueBits) {
                    lo = mid + 1; // (-0.0, 0.0) or (not NaN, NaN); midVal < val
                } else if (midValBits > valueBits) {
                    hi = mid - 1; // (0.0, -0.0) or (NaN, not NaN); midVal > val
                } else {
                    return mid; // bit patterns are equal; value found
                }
            }
        }
        return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     */
    public static int binarySearch(float[] array, float value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(float[] array, int startIndex, int endIndex, float value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            float midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else if (midVal != 0 && midVal == value) {
                return mid;  // value found
            } else { // Either midVal and value are == 0 or at least one is NaN
                int midValBits = Float.floatToIntBits(midVal);
                int valueBits  = Float.floatToIntBits(value);

                if (midValBits < valueBits) {
                    lo = mid + 1; // (-0.0, 0.0) or (not NaN, NaN); midVal < val
                } else if (midValBits > valueBits) {
                    hi = mid - 1; // (0.0, -0.0) or (NaN, not NaN); midVal > val
                } else {
                    return mid; // bit patterns are equal; value found
                }
            }
        }
        return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     */
    public static int binarySearch(int[] array, int value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(int[] array, int startIndex, int endIndex, int value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     */
    public static int binarySearch(long[] array, long value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(long[] array, int startIndex, int endIndex, long value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            long midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
         }
         return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws ClassCastException
     *         if an element in the array or the search element does not
     *         implement {@code Comparable}, or cannot be compared to each other.
     */
    public static int binarySearch(Object[] array, Object value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws ClassCastException
     *         if an element in the array or the search element does not
     *         implement {@code Comparable}, or cannot be compared to each other.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(Object[] array, int startIndex, int endIndex, Object value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            @SuppressWarnings("unchecked")
            int midValCmp = ((Comparable) array[mid]).compareTo(value);

            if (midValCmp < 0) {
                lo = mid + 1;
            } else if (midValCmp > 0) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * using {@code comparator} to compare elements.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @param comparator the {@code Comparator} used to compare the elements.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws ClassCastException
     *         if an element in the array or the search element does not
     *         implement {@code Comparable}, or cannot be compared to each other.
     */
    public static <T> int binarySearch(T[] array, T value, Comparator<? super T> comparator) {
        return binarySearch(array, 0, array.length, value, comparator);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive),
     * using {@code comparator} to compare elements.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @param comparator the {@code Comparator} used to compare the elements.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws ClassCastException
     *         if an element in the array or the search element does not
     *         implement {@code Comparable}, or cannot be compared to each other.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static <T> int binarySearch(T[] array, int startIndex, int endIndex, T value,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return binarySearch(array, startIndex, endIndex, value);
        }

        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int midValCmp = comparator.compare(array[mid], value);

            if (midValCmp < 0) {
                lo = mid + 1;
            } else if (midValCmp > 0) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     */
    public static int binarySearch(short[] array, short value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(short[] array, int startIndex, int endIndex, short value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            short midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }

    private static void checkBinarySearchBounds(int startIndex, int endIndex, int length) {
        if (startIndex > endIndex) {
            throw new IllegalArgumentException();
        }
        if (startIndex < 0 || endIndex > length) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public static void fill(byte[] array, byte value) {
		fill(array, 0, array.length, value);
    }

    public static void fill(short[] array, short value) {
		fill(array, 0, array.length, value);
    }

    public static void fill(char[] array, char value) {
		fill(array, 0, array.length, value);
    }

    public static void fill(int[] array, int value) {
		fill(array, 0, array.length, value);
    }

    public static void fill(long[] array, long value) {
		fill(array, 0, array.length, value);
    }

    public static void fill(float[] array, float value) {
		fill(array, 0, array.length, value);
	}

	public static void fill(double[] array, double value) {
		fill(array, 0, array.length, value);
	}

	public static void fill(boolean[] array, boolean value) {
		fill(array, 0, array.length, value);
	}

	public static void fill(Object[] array, Object value) {
		fill(array, 0, array.length, value);
	}

	@HaxeMethodBody(value = "p0.fill(p1, p2, p3);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT(JA_B, p0)->fill(p1, p2, p3);")
	public static void fill(byte[] array, int start, int end, byte value) {
		Arrays.checkStartAndEnd(array.length, start, end);
		for (int i = start; i < end; i++) {
			array[i] = value;
		}
	}

	@HaxeMethodBody(value = "p0.fill(p1, p2, p3);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT(JA_S, p0)->fill(p1, p2, p3);")
	public static void fill(short[] array, int start, int end, short value) {
		Arrays.checkStartAndEnd(array.length, start, end);
		for (int i = start; i < end; i++) {
			array[i] = value;
		}
	}

	@HaxeMethodBody(value = "p0.fill(p1, p2, p3);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT(JA_C, p0)->fill(p1, p2, p3);")
	public static void fill(char[] array, int start, int end, char value) {
		Arrays.checkStartAndEnd(array.length, start, end);
		for (int i = start; i < end; i++) {
			array[i] = value;
		}
	}

	@HaxeMethodBody(value = "p0.fill(p1, p2, p3);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT(JA_I, p0)->fill(p1, p2, p3);")
	public static void fill(int[] array, int start, int end, int value) {
		Arrays.checkStartAndEnd(array.length, start, end);
		for (int i = start; i < end; i++) {
			array[i] = value;
		}
	}

	@HaxeMethodBody(value = "p0.fill(p1, p2, p3);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT(JA_J, p0)->fill(p1, p2, p3);")
    public static void fill(long[] array, int start, int end, long value) {
        Arrays.checkStartAndEnd(array.length, start, end);
        for (int i = start; i < end; i++) {
            array[i] = value;
        }
    }

	@HaxeMethodBody(value = "p0.fill(p1, p2, p3);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT(JA_F, p0)->fill(p1, p2, p3);")
    public static void fill(float[] array, int start, int end, float value) {
        Arrays.checkStartAndEnd(array.length, start, end);
        for (int i = start; i < end; i++) {
            array[i] = value;
        }
    }

	@HaxeMethodBody(value = "p0.fill(p1, p2, p3);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT(JA_D, p0)->fill(p1, p2, p3);")
    public static void fill(double[] array, int start, int end, double value) {
        Arrays.checkStartAndEnd(array.length, start, end);
        for (int i = start; i < end; i++) {
            array[i] = value;
        }
    }

	@HaxeMethodBody(value = "p0.fill(p1, p2, p3 ? 1 : 0);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT(JA_Z, p0)->fill(p1, p2, p3);")
    public static void fill(boolean[] array, int start, int end, boolean value) {
        Arrays.checkStartAndEnd(array.length, start, end);
        for (int i = start; i < end; i++) {
            array[i] = value;
        }
    }

	@HaxeMethodBody(value = "p0.fill(p1, p2, p3);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT(JA_L, p0)->fill(p1, p2, p3);")
    public static void fill(Object[] array, int start, int end, Object value) {
        Arrays.checkStartAndEnd(array.length, start, end);
        for (int i = start; i < end; i++) {
            array[i] = value;
        }
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * {@code boolean} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()} method which is invoked on a {@link List}
     * containing a sequence of {@link Boolean} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     *
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(boolean[] array) {
        if (array == null) return 0;
        int hashCode = 1;
        for (boolean element : array) {
            // 1231, 1237 are hash code values for boolean value
            hashCode = 31 * hashCode + (element ? 1231 : 1237);
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * not-null {@code int} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()} method which is invoked on a {@link List}
     * containing a sequence of {@link Integer} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     *
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(int[] array) {
        if (array == null) return 0;
        int hashCode = 1;
        for (int element : array) {
            // the hash code value for integer value is integer value itself
            hashCode = 31 * hashCode + element;
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * {@code short} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()} method which is invoked on a {@link List}
     * containing a sequence of {@link Short} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     *
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(short[] array) {
        if (array == null) return 0;
        int hashCode = 1;
        for (short element : array) {
            // the hash code value for short value is its integer value
            hashCode = 31 * hashCode + element;
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * {@code char} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()} method which is invoked on a {@link List}
     * containing a sequence of {@link Character} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     *
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(char[] array) {
        if (array == null) return 0;
        int hashCode = 1;
        for (char element : array) {
            // the hash code value for char value is its integer value
            hashCode = 31 * hashCode + element;
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * {@code byte} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()} method which is invoked on a {@link List}
     * containing a sequence of {@link Byte} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     *
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(byte[] array) {
        if (array == null) return 0;
        int hashCode = 1;
        for (byte element : array) {
            // the hash code value for byte value is its integer value
            hashCode = 31 * hashCode + element;
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * {@code long} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()} method which is invoked on a {@link List}
     * containing a sequence of {@link Long} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     *
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(long[] array) {
        if (array == null) return 0;
        int hashCode = 1;
        for (long elementValue : array) {
            /*
             * the hash code value for long value is (int) (value ^ (value >>>
             * 32))
             */
            hashCode = 31 * hashCode + (int) (elementValue ^ (elementValue >>> 32));
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * {@code float} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()} method which is invoked on a {@link List}
     * containing a sequence of {@link Float} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     *
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(float[] array) {
        if (array == null) return 0;
        int hashCode = 1;
        for (float element : array) {
            /*
             * the hash code value for float value is
             * Float.floatToIntBits(value)
             */
            hashCode = 31 * hashCode + Float.floatToIntBits(element);
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * {@code double} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()} method which is invoked on a {@link List}
     * containing a sequence of {@link Double} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     *
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(double[] array) {
        if (array == null) return 0;
        int hashCode = 1;

        for (double element : array) {
            long v = Double.doubleToLongBits(element);
            /*
             * the hash code value for double value is (int) (v ^ (v >>> 32))
             * where v = Double.doubleToLongBits(value)
             */
            hashCode = 31 * hashCode + (int) (v ^ (v >>> 32));
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. If the
     * array contains other arrays as its elements, the hash code is based on
     * their identities not their contents. So it is acceptable to invoke this
     * method on an array that contains itself as an element, either directly or
     * indirectly.
     * <p>
     * For any two arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals
     * {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the method
     * Arrays.asList(array).hashCode(). If the array is {@code null}, the return value
     * is 0.
     *
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(Object[] array) {
        if (array == null) return 0;
        int hashCode = 1;
        for (Object element : array) {
            hashCode = 31 * hashCode + ((element == null) ? 0 : (element).hashCode());
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the "deep contents" of the given array. If
     * the array contains other arrays as its elements, the hash code is based
     * on their contents not their identities. So it is not acceptable to invoke
     * this method on an array that contains itself as an element, either
     * directly or indirectly.
     * <p>
     * For any two arrays {@code a} and {@code b}, if
     * {@code Arrays.deepEquals(a, b)} returns {@code true}, it
     * means that the return value of {@code Arrays.deepHashCode(a)} equals
     * {@code Arrays.deepHashCode(b)}.
     * <p>
     * The computation of the value returned by this method is similar to that
     * of the value returned by {@link List#hashCode()} invoked on a
     * {@link List} containing a sequence of instances representing the
     * elements of array in the same order. The difference is: If an element e
     * of array is itself an array, its hash code is computed by calling the
     * appropriate overloading of {@code Arrays.hashCode(e)} if e is an array of a
     * primitive type, or by calling {@code Arrays.deepHashCode(e)} recursively if e is
     * an array of a reference type. The value returned by this method is the
     * same value as the method {@code Arrays.asList(array).hashCode()}. If the array is
     * {@code null}, the return value is 0.
     *
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int deepHashCode(Object[] array) {
        if (array == null) return 0;
        int hashCode = 1;
        for (Object element : array) {
            int elementHashCode = deepHashCodeElement(element);
            hashCode = 31 * hashCode + elementHashCode;
        }
        return hashCode;
    }

    private static int deepHashCodeElement(Object element) {
        Class<?> cl;
        if (element == null) return 0;
        cl = element.getClass().getComponentType();
        if (cl == null) return element.hashCode();
        if (!cl.isPrimitive()) return deepHashCode((Object[]) element);
        if (cl.equals(int.class)) return hashCode((int[]) element);
        if (cl.equals(char.class)) return hashCode((char[]) element);
        if (cl.equals(boolean.class)) return hashCode((boolean[]) element);
        if (cl.equals(byte.class)) return hashCode((byte[]) element);
        if (cl.equals(long.class)) return hashCode((long[]) element);
        if (cl.equals(float.class)) return hashCode((float[]) element);
        if (cl.equals(double.class)) return hashCode((double[]) element);
        return hashCode((short[]) element);
    }

    /**
     * Compares the two arrays.
     *
     * @param array1
     *            the first {@code byte} array.
     * @param array2
     *            the second {@code byte} array.
     * @return {@code true} if both arrays are {@code null} or if the arrays have the
     *         same length and the elements at each index in the two arrays are
     *         equal, {@code false} otherwise.
     */
    public static boolean equals(byte[] array1, byte[] array2) {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length != array2.length) return false;
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) return false;
        }
        return true;
    }

    public static boolean equals(short[] array1, short[] array2) {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length != array2.length) return false;
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) return false;
        }
        return true;
    }

    public static boolean equals(char[] array1, char[] array2) {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length != array2.length) return false;
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) return false;
        }
        return true;
    }

    public static boolean equals(int[] array1, int[] array2) {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length != array2.length) return false;
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) return false;
        }
        return true;
    }

    public static boolean equals(long[] array1, long[] array2) {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length != array2.length) return false;
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) return false;
        }
        return true;
    }

    public static boolean equals(float[] array1, float[] array2) {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length != array2.length) return false;
        for (int i = 0; i < array1.length; i++) {
            if (Float.floatToIntBits(array1[i]) != Float.floatToIntBits(array2[i])) return false;
        }
        return true;
    }

    public static boolean equals(double[] array1, double[] array2) {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length != array2.length) return false;
        for (int i = 0; i < array1.length; i++) {
            if (Double.doubleToLongBits(array1[i]) != Double.doubleToLongBits(array2[i])) return false;
        }
        return true;
    }

    public static boolean equals(boolean[] array1, boolean[] array2) {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length != array2.length) return false;
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) return false;
        }
        return true;
    }

    public static boolean equals(Object[] array1, Object[] array2) {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length != array2.length) return false;
        for (int i = 0; i < array1.length; i++) {
            Object e1 = array1[i], e2 = array2[i];
            if (!(e1 == null ? e2 == null : e1.equals(e2))) return false;
        }
        return true;
    }

    public static boolean deepEquals(Object[] array1, Object[] array2) {
        if (array1 == array2) return true;
        if (array1 == null || array2 == null || array1.length != array2.length) return false;
        for (int i = 0; i < array1.length; i++) {
            Object e1 = array1[i], e2 = array2[i];

            if (!deepEqualsElements(e1, e2)) return false;
        }
        return true;
    }

    private static boolean deepEqualsElements(Object e1, Object e2) {
        Class<?> cl1, cl2;

        if (e1 == e2) {
            return true;
        }

        if (e1 == null || e2 == null) {
            return false;
        }

        cl1 = e1.getClass().getComponentType();
        cl2 = e2.getClass().getComponentType();

        if (cl1 != cl2) {
            return false;
        }

        if (cl1 == null) {
            return e1.equals(e2);
        }

        /*
         * compare as arrays
         */
        if (!cl1.isPrimitive()) {
            return deepEquals((Object[]) e1, (Object[]) e2);
        }

        if (cl1.equals(int.class)) {
            return equals((int[]) e1, (int[]) e2);
        }
        if (cl1.equals(char.class)) {
            return equals((char[]) e1, (char[]) e2);
        }
        if (cl1.equals(boolean.class)) {
            return equals((boolean[]) e1, (boolean[]) e2);
        }
        if (cl1.equals(byte.class)) {
            return equals((byte[]) e1, (byte[]) e2);
        }
        if (cl1.equals(long.class)) {
            return equals((long[]) e1, (long[]) e2);
        }
        if (cl1.equals(float.class)) {
            return equals((float[]) e1, (float[]) e2);
        }
        if (cl1.equals(double.class)) {
            return equals((double[]) e1, (double[]) e2);
        }
        return equals((short[]) e1, (short[]) e2);
    }

    /**
     * Sorts the specified array in ascending numerical order.
     *
     * @param array
     *            the {@code byte} array to be sorted.
     */
    public static void sort(byte[] array) {
        DualPivotQuicksort.sort(array);
    }

    /**
     * Sorts the specified range in the array in ascending numerical order.
     *
     * @param array
     *            the {@code byte} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void sort(byte[] array, int start, int end) {
        DualPivotQuicksort.sort(array, start, end);
    }

    /**
     * Checks that the range described by {@code offset} and {@code count} doesn't exceed
     * {@code arrayLength}.
     *
     * @hide
     */
    public static void checkOffsetAndCount(int arrayLength, int offset, int count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Checks that the range described by {@code start} and {@code end} doesn't exceed
     * {@code len}.
     *
     * @hide
     */
    public static void checkStartAndEnd(int len, int start, int end) {
        if (start < 0 || end > len)
			throw new ArrayIndexOutOfBoundsException("start < 0 || end > len. start=" + start + ", end=" + end + ", len=" + len);
        if (start > end) throw new IllegalArgumentException("start > end: " + start + " > " + end);
    }

    /**
     * Sorts the specified array in ascending numerical order.
     *
     * @param array
     *            the {@code char} array to be sorted.
     */
    public static void sort(char[] array) {
        DualPivotQuicksort.sort(array);
    }

    /**
     * Sorts the specified range in the array in ascending numerical order.
     *
     * @param array
     *            the {@code char} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void sort(char[] array, int start, int end) {
        DualPivotQuicksort.sort(array, start, end);
    }

    /**
     * Sorts the specified array in ascending numerical order.
     *
     * @param array
     *            the {@code double} array to be sorted.
     * @see #sort(double[], int, int)
     */
    public static void sort(double[] array) {
        DualPivotQuicksort.sort(array);
    }

    /**
     * Sorts the specified range in the array in ascending numerical order. The
     * values are sorted according to the order imposed by {@code Double.compareTo()}.
     *
     * @param array
     *            the {@code double} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     * @see Double#compareTo(Double)
     */
    public static void sort(double[] array, int start, int end) {
        DualPivotQuicksort.sort(array, start, end);
    }

    /**
     * Sorts the specified array in ascending numerical order.
     *
     * @param array
     *            the {@code float} array to be sorted.
     * @see #sort(float[], int, int)
     */
    public static void sort(float[] array) {
        DualPivotQuicksort.sort(array);
    }

    /**
     * Sorts the specified range in the array in ascending numerical order. The
     * values are sorted according to the order imposed by {@code Float.compareTo()}.
     *
     * @param array
     *            the {@code float} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     * @see Float#compareTo(Float)
     */
    public static void sort(float[] array, int start, int end) {
        DualPivotQuicksort.sort(array, start, end);
    }

    /**
     * Sorts the specified array in ascending numerical order.
     *
     * @param array
     *            the {@code int} array to be sorted.
     */
    public static void sort(int[] array) {
        DualPivotQuicksort.sort(array);
    }

    /**
     * Sorts the specified range in the array in ascending numerical order.
     *
     * @param array
     *            the {@code int} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void sort(int[] array, int start, int end) {
        DualPivotQuicksort.sort(array, start, end);
    }

    /**
     * Sorts the specified array in ascending numerical order.
     *
     * @param array
     *            the {@code long} array to be sorted.
     */
    public static void sort(long[] array) {
        DualPivotQuicksort.sort(array);
    }

    /**
     * Sorts the specified range in the array in ascending numerical order.
     *
     * @param array
     *            the {@code long} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void sort(long[] array, int start, int end) {
        DualPivotQuicksort.sort(array, start, end);
    }

    /**
     * Sorts the specified array in ascending numerical order.
     *
     * @param array
     *            the {@code short} array to be sorted.
     */
    public static void sort(short[] array) {
        DualPivotQuicksort.sort(array);
    }

    /**
     * Sorts the specified range in the array in ascending numerical order.
     *
     * @param array
     *            the {@code short} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void sort(short[] array, int start, int end) {
        DualPivotQuicksort.sort(array, start, end);
    }

// BEGIN android-note

    /*
     * <p>If this platform has an optimizing VM, check whether ComparableTimSort
     * offers any performance benefit over TimSort in conjunction with a
     * comparator that returns:
     *    {@code ((Comparable)first).compareTo(Second)}.
     * If not, you are better off deleting ComparableTimSort to eliminate the
     * code duplication.  In other words, the commented out code below
     * is the preferable implementation for sorting arrays of Comparables if it
     * offers sufficient performance.
     */

//    /**
//     * A comparator that implements the natural order of a group of
//     * mutually comparable elements.  Using this comparator saves us
//     * from duplicating most of the code in this file (one version for
//     * Comparables, one for explicit comparators).
//     */
//    private static final Comparator<Object> NATURAL_ORDER = new Comparator<Object>() {
//        @SuppressWarnings("unchecked")
//        public int compare(Object first, Object second) {
//            return ((Comparable<Object>)first).compareTo(second);
//        }
//    };
//
//    public static void sort(Object[] a) {
//        sort(a, 0, a.length, NATURAL_ORDER);
//    }
//
//    public static void sort(Object[] a, int fromIndex, int toIndex) {
//        sort(a, fromIndex, toIndex, NATURAL_ORDER);
//    }

// END android-note

    /**
     * Sorts the specified array in ascending natural order.
     *
     * @throws ClassCastException if any element does not implement {@code Comparable},
     *     or if {@code compareTo} throws for any pair of elements.
     */
    public static void sort(Object[] array) {
        ComparableTimSort.sort(array);
    }

    /**
     * Sorts the specified range in the array in ascending natural order.
     *
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws ClassCastException if any element does not implement {@code Comparable},
     *     or if {@code compareTo} throws for any pair of elements.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void sort(Object[] array, int start, int end) {
        ComparableTimSort.sort(array, start, end);
    }

    /**
     * Sorts the specified range in the array using the specified {@code Comparator}.
     * All elements must be comparable to each other without a
     * {@code ClassCastException} being thrown.
     *
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @param comparator
     *            the {@code Comparator}.
     * @throws ClassCastException
     *                if elements in the array cannot be compared to each other
     *                using the given {@code Comparator}.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static <T> void sort(T[] array, int start, int end, Comparator<? super T> comparator) {
        TimSort.sort(array, start, end, comparator);
    }

    /**
     * Sorts the specified array using the specified {@code Comparator}. All elements
     * must be comparable to each other without a {@code ClassCastException} being thrown.
     *
     * @throws ClassCastException
     *                if elements in the array cannot be compared to each other
     *                using the {@code Comparator}.
     */
    public static <T> void sort(T[] array, Comparator<? super T> comparator) {
        TimSort.sort(array, comparator);
    }

    /**
     * Creates a {@code String} representation of the {@code boolean[]} passed.
     * The result is surrounded by brackets ({@code "[]"}), each
     * element is converted to a {@code String} via the
     * {@link String#valueOf(boolean)} and separated by {@code ", "}.
     * If the array is {@code null}, then {@code "null"} is returned.
     *
     * @param array
     *            the {@code boolean} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(boolean[] array) {
        if (array == null) return "null";
        if (array.length == 0) return "[]";
        StringBuilder sb = new StringBuilder(array.length * 7);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", ");
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code byte[]} passed. The
     * result is surrounded by brackets ({@code "[]"}), each element
     * is converted to a {@code String} via the {@link String#valueOf(int)} and
     * separated by {@code ", "}. If the array is {@code null}, then
     * {@code "null"} is returned.
     *
     * @param array
     *            the {@code byte} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(byte[] array) {
        if (array == null) return "null";
        if (array.length == 0) return "[]";
        StringBuilder sb = new StringBuilder(array.length * 6);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", ");
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code char[]} passed. The
     * result is surrounded by brackets ({@code "[]"}), each element
     * is converted to a {@code String} via the {@link String#valueOf(char)} and
     * separated by {@code ", "}. If the array is {@code null}, then
     * {@code "null"} is returned.
     *
     * @param array
     *            the {@code char} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(char[] array) {
        if (array == null) return "null";
        if (array.length == 0) return "[]";
        StringBuilder sb = new StringBuilder(array.length * 3);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", ");
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code double[]} passed.
     * The result is surrounded by brackets ({@code "[]"}), each
     * element is converted to a {@code String} via the
     * {@link String#valueOf(double)} and separated by {@code ", "}.
     * If the array is {@code null}, then {@code "null"} is returned.
     *
     * @param array
     *            the {@code double} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(double[] array) {
        if (array == null) return "null";
        if (array.length == 0) return "[]";
        StringBuilder sb = new StringBuilder(array.length * 7);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", ");
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code float[]} passed.
     * The result is surrounded by brackets ({@code "[]"}), each
     * element is converted to a {@code String} via the
     * {@link String#valueOf(float)} and separated by {@code ", "}.
     * If the array is {@code null}, then {@code "null"} is returned.
     *
     * @param array
     *            the {@code float} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(float[] array) {
        if (array == null) return "null";
        if (array.length == 0) return "[]";
        StringBuilder sb = new StringBuilder(array.length * 7);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", ");
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code int[]} passed. The
     * result is surrounded by brackets ({@code "[]"}), each element
     * is converted to a {@code String} via the {@link String#valueOf(int)} and
     * separated by {@code ", "}. If the array is {@code null}, then
     * {@code "null"} is returned.
     *
     * @param array
     *            the {@code int} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(int[] array) {
        if (array == null) return "null";
        if (array.length == 0) return "[]";
        StringBuilder sb = new StringBuilder(array.length * 6);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", ");
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code long[]} passed. The
     * result is surrounded by brackets ({@code "[]"}), each element
     * is converted to a {@code String} via the {@link String#valueOf(long)} and
     * separated by {@code ", "}. If the array is {@code null}, then
     * {@code "null"} is returned.
     *
     * @param array
     *            the {@code long} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(long[] array) {
        if (array == null) return "null";
        if (array.length == 0) return "[]";
        StringBuilder sb = new StringBuilder(array.length * 6);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", ");
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code short[]} passed.
     * The result is surrounded by brackets ({@code "[]"}), each
     * element is converted to a {@code String} via the
     * {@link String#valueOf(int)} and separated by {@code ", "}. If
     * the array is {@code null}, then {@code "null"} is returned.
     *
     * @param array
     *            the {@code short} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(short[] array) {
        if (array == null) return "null";
        if (array.length == 0) return "[]";
        StringBuilder sb = new StringBuilder(array.length * 6);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", ");
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code Object[]} passed.
     * The result is surrounded by brackets ({@code "[]"}), each
     * element is converted to a {@code String} via the
     * {@link String#valueOf(Object)} and separated by {@code ", "}.
     * If the array is {@code null}, then {@code "null"} is returned.
     *
     * @param array
     *            the {@code Object} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(Object[] array) {
        if (array == null) return "null";
        if (array.length == 0) return "[]";
        StringBuilder sb = new StringBuilder(array.length * 7);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", ");
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a <i>"deep"</i> {@code String} representation of the
     * {@code Object[]} passed, such that if the array contains other arrays,
     * the {@code String} representation of those arrays is generated as well.
     * <p>
     * If any of the elements are primitive arrays, the generation is delegated
     * to the other {@code toString} methods in this class. If any element
     * contains a reference to the original array, then it will be represented
     * as {@code "[...]"}. If an element is an {@code Object[]}, then its
     * representation is generated by a recursive call to this method. All other
     * elements are converted via the {@link String#valueOf(Object)} method.
     *
     * @param array
     *            the {@code Object} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String deepToString(Object[] array) {
        // Special case null to prevent NPE
        if (array == null) return "null";
        // delegate this to the recursive method
        StringBuilder buf = new StringBuilder(array.length * 9);
        deepToStringImpl(array, new Object[] { array }, buf);
        return buf.toString();
    }

    /**
     * Implementation method used by {@link #deepToString(Object[])}.
     *
     * @param array
     *            the {@code Object[]} to dive into.
     * @param origArrays
     *            the original {@code Object[]}; used to test for self
     *            references.
     * @param sb
     *            the {@code StringBuilder} instance to append to or
     *            {@code null} one hasn't been created yet.
     * @return the result.
     * @see #deepToString(Object[])
     */
    private static void deepToStringImpl(Object[] array, Object[] origArrays, StringBuilder sb) {
        if (array == null) {
            sb.append("null");
            return;
        }

        sb.append('[');

        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            // establish current element
            Object elem = array[i];
            if (elem == null) {
                // element is null
                sb.append("null");
            } else {
                // get the Class of the current element
                Class<?> elemClass = elem.getClass();
                if (elemClass.isArray()) {
                    // element is an array type

                    // get the declared Class of the array (element)
                    Class<?> elemElemClass = elemClass.getComponentType();
                    if (elemElemClass.isPrimitive()) {
                        // element is a primitive array
                        if (boolean.class.equals(elemElemClass)) {
                            sb.append(toString((boolean[]) elem));
                        } else if (byte.class.equals(elemElemClass)) {
                            sb.append(toString((byte[]) elem));
                        } else if (char.class.equals(elemElemClass)) {
                            sb.append(toString((char[]) elem));
                        } else if (double.class.equals(elemElemClass)) {
                            sb.append(toString((double[]) elem));
                        } else if (float.class.equals(elemElemClass)) {
                            sb.append(toString((float[]) elem));
                        } else if (int.class.equals(elemElemClass)) {
                            sb.append(toString((int[]) elem));
                        } else if (long.class.equals(elemElemClass)) {
                            sb.append(toString((long[]) elem));
                        } else if (short.class.equals(elemElemClass)) {
                            sb.append(toString((short[]) elem));
                        } else {
                            // no other possible primitives, so we assert that
                            throw new AssertionError();
                        }
                    } else {
                        // element is an Object[], so we assert that
                        // assert elem instanceof Object[];
                        if (deepToStringImplContains(origArrays, elem)) {
                            sb.append("[...]");
                        } else {
                            Object[] newArray = (Object[]) elem;
                            Object[] newOrigArrays = new Object[origArrays.length + 1];
                            System.arraycopy(origArrays, 0, newOrigArrays, 0,
                                    origArrays.length);
                            newOrigArrays[origArrays.length] = newArray;
                            // make the recursive call to this method
                            deepToStringImpl(newArray, newOrigArrays, sb);
                        }
                    }
                } else { // element is NOT an array, just an Object
                    sb.append(array[i]);
                }
            }
        }
        sb.append(']');
    }

    /**
     * Utility method used to assist the implementation of
     * {@link #deepToString(Object[])}.
     *
     * @param origArrays
     *            An array of Object[] references.
     * @param array
     *            An Object[] reference to look for in {@code origArrays}.
     * @return A value of {@code true} if {@code array} is an
     *         element in {@code origArrays}.
     */
    private static boolean deepToStringImplContains(Object[] origArrays,
            Object array) {
        if (origArrays == null || origArrays.length == 0) {
            return false;
        }
        for (Object element : origArrays) {
            if (element == array) {
                return true;
            }
        }
        return false;
    }

    public static boolean[] copyOf(boolean[] original, int newLength) {
        return copyOfRange(original, 0, newLength);
    }

    public static byte[] copyOf(byte[] original, int newLength) {
        return copyOfRange(original, 0, newLength);
    }

    public static char[] copyOf(char[] original, int newLength) {
        return copyOfRange(original, 0, newLength);
    }

    public static double[] copyOf(double[] original, int newLength) {
        return copyOfRange(original, 0, newLength);
    }

    public static float[] copyOf(float[] original, int newLength) {
        return copyOfRange(original, 0, newLength);
    }

    public static int[] copyOf(int[] original, int newLength) {
        return copyOfRange(original, 0, newLength);
    }

    public static long[] copyOf(long[] original, int newLength) {
        return copyOfRange(original, 0, newLength);
    }

    public static short[] copyOf(short[] original, int newLength) {
        return copyOfRange(original, 0, newLength);
    }

    public static <T> T[] copyOf(T[] original, int newLength) {
        return copyOfRange(original, 0, newLength);
    }

    public static <T, U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        return copyOfRange(original, 0, newLength, newType);
    }

    public static boolean[] copyOfRange(boolean[] original, int start, int end) {
		int originalLength = checkRange(start, end, original.length);
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        boolean[] result = new boolean[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    public static byte[] copyOfRange(byte[] original, int start, int end) {
        int originalLength = checkRange(start, end, original.length);
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        byte[] result = new byte[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    public static char[] copyOfRange(char[] original, int start, int end) {
		int originalLength = checkRange(start, end, original.length);
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        char[] result = new char[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    public static double[] copyOfRange(double[] original, int start, int end) {
		int originalLength = checkRange(start, end, original.length);
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        double[] result = new double[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    public static float[] copyOfRange(float[] original, int start, int end) {
		int originalLength = checkRange(start, end, original.length);
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        float[] result = new float[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    public static int[] copyOfRange(int[] original, int start, int end) {
		int originalLength = checkRange(start, end, original.length);
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        int[] result = new int[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    public static long[] copyOfRange(long[] original, int start, int end) {
		int originalLength = checkRange(start, end, original.length);
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        long[] result = new long[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    public static short[] copyOfRange(short[] original, int start, int end) {
		int originalLength = checkRange(start, end, original.length);
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        short[] result = new short[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    @SuppressWarnings("unchecked")
	@JTranscMethodBody(target = "js", value = "return JA_L.copyOfRange(p0, p1, p2);")
    public static <T> T[] copyOfRange(T[] original, int start, int end) {
		int originalLength = checkRange(start, end, original.length);
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        T[] result = (T[]) newInstance(original, resultLength);
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    @SuppressWarnings("unchecked")
	@JTranscMethodBody(target = "js", value = "return JA_L.copyOfRange(p0, p1, p2, p3.name);")
    public static <T, U> T[] copyOfRange(U[] original, int start, int end, Class<? extends T[]> newType) {
		int originalLength = checkRange(start, end, original.length);
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        T[] result = (T[]) Array.newInstance(newType.getComponentType(), resultLength);
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

	static private int checkRange(int start, int end, int originalLength) {
		//if (end < 0) throw new NegativeArraySizeException(Integer.toString(end));
		if (start > end) throw new IllegalArgumentException();
		if (start < 0 || start > originalLength) throw new ArrayIndexOutOfBoundsException();
		return originalLength;
	}

	static public void parallelSort(byte[] v) {
    	sort(v);
	}

	static public void parallelSort(byte[] v, int start, int end) {
		sort(v, start, end);
	}

	static public void parallelSort(char[] v) {
		sort(v);
	}

	static public void parallelSort(char[] v, int start, int end) {
		sort(v, start, end);
	}

	static public void parallelSort(short[] v) {
		sort(v);
	}

	static public void parallelSort(short[] v, int start, int end) {
		sort(v, start, end);
	}

	static public void parallelSort(int[] v) {
		sort(v);
	}

	static public void parallelSort(int[] v, int start, int end) {
		sort(v, start, end);
	}

	static public void parallelSort(long[] v) {
		sort(v);
	}

	static public void parallelSort(long[] v, int start, int end) {
		sort(v, start, end);
	}

	static public void parallelSort(float[] v) {
		sort(v);
	}

	static public void parallelSort(float[] v, int start, int end) {
		sort(v, start, end);
	}

	static public void parallelSort(double[] v) {
		sort(v);
	}

	static public void parallelSort(double[] v, int start, int end) {
		sort(v, start, end);
	}

	static public <T extends Comparable<? super T>> void parallelSort(T[] v) {
		sort(v);
	}

	static public <T extends Comparable<? super T>> void parallelSort(T[] v, int start, int end) {
		sort(v, start, end);
	}

	static public <T extends Comparable<? super T>> void parallelSort(T[] v, Comparator<T> comparator) {
		sort(v, comparator);
	}

	static public <T extends Comparable<? super T>> void parallelSort(T[] v, int start, int end, Comparator<T> comparator) {
		sort(v, start, end, comparator);
	}

	// Custom bodies for all targets to prevent including java.lang.Class from here
	private static <T> T[] newInstance(T[] baseType, int size) {
		return (T[]) Array.newInstance(baseType.getClass().getComponentType(), size);
	}
}