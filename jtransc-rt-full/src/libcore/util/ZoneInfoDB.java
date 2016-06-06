/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.util;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

public final class ZoneInfoDB {
	private static final TzData DATA;

	static {
		DATA = new TzData();
	}

	public static class TzData {

		private String[] ids;

		public TzData() {
			ids = new String[]{"GMT"};
		}

		public String[] getAvailableIDs() {
			return ids.clone();
		}

		public String[] getAvailableIDs(int rawOffset) {
			return getAvailableIDs();
		}

		public String getZoneTab() {
			return "# Emergency fallback data.\n";
		}

		public TimeZone makeTimeZone(String id) throws IOException {
			return new TimeZone() {
				private static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
				private static final long MILLISECONDS_PER_400_YEARS = MILLISECONDS_PER_DAY * (400 * 365 + 100 - 3);

				private static final long UNIX_OFFSET = 62167219200000L;

				private final int[] NORMAL = new int[] {
					0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334,
				};

				private final int[] LEAP = new int[] {
					0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335,
				};

				int mRawOffset = 0;


				@Override
				public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
					// XXX This assumes Gregorian always; Calendar switches from
					// Julian to Gregorian in 1582.  What calendar system are the
					// arguments supposed to come from?

					long calc = (year / 400) * MILLISECONDS_PER_400_YEARS;
					year %= 400;

					calc += year * (365 * MILLISECONDS_PER_DAY);
					calc += ((year + 3) / 4) * MILLISECONDS_PER_DAY;

					if (year > 0) {
						calc -= ((year - 1) / 100) * MILLISECONDS_PER_DAY;
					}

					boolean isLeap = (year == 0 || (year % 4 == 0 && year % 100 != 0));
					int[] mlen = isLeap ? LEAP : NORMAL;

					calc += mlen[month] * MILLISECONDS_PER_DAY;
					calc += (day - 1) * MILLISECONDS_PER_DAY;
					calc += millis;

					calc -= mRawOffset;
					calc -= UNIX_OFFSET;

					return getOffset(calc);
				}

				@Override
				public void setRawOffset(int offsetMillis) {
					mRawOffset = offsetMillis;
				}

				@Override
				public int getRawOffset() {
					return mRawOffset;
				}

				@Override
				public boolean useDaylightTime() {
					return false;
				}

				@Override
				public boolean inDaylightTime(Date date) {
					return false;
				}
			};
		}

		public String getDefaultID() {
			return null;
		}
	}

	private ZoneInfoDB() {
	}

	public static TzData getInstance() {
		return DATA;
	}
}
