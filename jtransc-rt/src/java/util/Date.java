/*
 * Copyright 2016 Carlos Ballesteros Velasco
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

package java.util;

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

@HaxeAddMembers({ "var _date:Date;" })
public class Date implements java.io.Serializable, Cloneable, Comparable<Date> {
    @HaxeMethodBody("_date = Date.now();")
	public Date() {
		this(System.currentTimeMillis());
	}

    @HaxeMethodBody("_date = Date.fromTime(HaxeNatives.longToFloat(p0));")
	@JTranscMethodBody(target = "js", value = "this._date = new Date(Int64.toFloat(p0));")
	public Date(long date) {
	}

	@Deprecated
    @HaxeMethodBody("_date = new Date(p0, p1, p2, 0, 0, 0);")
	public Date(int year, int month, int date) {
		this(year, month, date, 0, 0, 0);
	}

	@Deprecated
	@HaxeMethodBody("_date = new Date(p0, p1, p2, p3, p4, 0);")
	public Date(int year, int month, int date, int hrs, int min) {
		this(year, month, date, hrs, min, 0);
	}

	@Deprecated
	@HaxeMethodBody("_date = new Date(p0, p1, p2, p3, p4, p5);")
	@JTranscMethodBody(target = "js", value = "this._date = new Date(p0, p1, p2, p3, p4, p5);")
	public Date(int year, int month, int date, int hrs, int min, int sec) {
	}

	@Deprecated
	@HaxeMethodBody("_date = Date.fromString(p0._str);")
	@JTranscMethodBody(target = "js", value = "this._date = new Date(Date.parse(N.istr(p0)));")
	public Date(String s) {
	}

	native public Object clone();

	@Deprecated
	@JTranscMethodBody(target = "js", value = "return Int64.ofFloat(Date.UTC(p0, p1, p2, p3, p4, p5));")
	native public static long UTC(int year, int month, int date, int hrs, int min, int sec);

	@Deprecated
    @HaxeMethodBody("return HaxeNatives.floatToLong(Date.fromString(p0._str).getTime());")
	@JTranscMethodBody(target = "js", value = "return Int64.ofFloat(Date.parse(N.istr(p0)));")
	native public static long parse(String s);

    @Deprecated
	@HaxeMethodBody("return _date.getFullYear() - 1900;")
	@JTranscMethodBody(target = "js", value = "return this._date.getYear();")
	native public int getYear();

	@Deprecated
	@JTranscMethodBody(target = "js", value = "this._date.setYear(p0);")
	native public void setYear(int year);

	@Deprecated
	@HaxeMethodBody("return _date.getMonth();")
	@JTranscMethodBody(target = "js", value = "return this._date.getMonth();")
	native public int getMonth();

	@Deprecated
	@JTranscMethodBody(target = "js", value = "this._date.setMonth(p0);")
	native public void setMonth(int month);

	@Deprecated
    @HaxeMethodBody("return _date.getDate();")
	@JTranscMethodBody(target = "js", value = "return this._date.getDate();")
    native public int getDate();

	@Deprecated
	@JTranscMethodBody(target = "js", value = "this._date.setDate(p0);")
	native public void setDate(int date);

	@Deprecated
	@HaxeMethodBody("return _date.getDay();")
	@JTranscMethodBody(target = "js", value = "return this._date.getDay();")
	native public int getDay();

	@Deprecated
    @HaxeMethodBody("return _date.getHours();")
	@JTranscMethodBody(target = "js", value = "return this._date.getHours();")
	native public int getHours();

	@Deprecated
	@JTranscMethodBody(target = "js", value = "this._date.setHours(p0);")
	native public void setHours(int hours);

	@Deprecated
	@HaxeMethodBody("return _date.getMinutes();")
	@JTranscMethodBody(target = "js", value = "return this._date.getMinutes();")
	native public int getMinutes();

	@Deprecated
	@JTranscMethodBody(target = "js", value = "this._date.setMinutes(p0);")
	native public void setMinutes(int minutes);

	@Deprecated
	@HaxeMethodBody("return _date.getSeconds();")
	@JTranscMethodBody(target = "js", value = "return this._date.getSeconds();")
	native public int getSeconds();

	@Deprecated
	@JTranscMethodBody(target = "js", value = "this._date.setSeconds(p0);")
	native public void setSeconds(int seconds);

    @HaxeMethodBody("return HaxeNatives.floatToLong(_date.getTime());")
	@JTranscMethodBody(target = "js", value = "return Int64.ofFloat(this._date.getTime());")
	native public long getTime();

	@JTranscMethodBody(target = "js", value = "this._date.setTime(Int64.toFloat(p0));")
	native public void setTime(long time);

	public boolean before(Date when) {
		return getMillisOf(this) < getMillisOf(when);
	}

	public boolean after(Date when) {
		return getMillisOf(this) > getMillisOf(when);
	}

	public boolean equals(Object obj) {
		return obj instanceof Date && getTime() == ((Date) obj).getTime();
	}

	static final long getMillisOf(Date date) {
		return date.getTime();
	}

	public int compareTo(Date anotherDate) {
		long thisTime = getMillisOf(this);
		long anotherTime = getMillisOf(anotherDate);
		return (thisTime < anotherTime ? -1 : (thisTime == anotherTime ? 0 : 1));
	}

	public int hashCode() {
		long ht = this.getTime();
		return (int) ht ^ (int) (ht >> 32);
	}

	private static StringBuilder convertToAbbr(StringBuilder sb, String name) {
		sb.append(Character.toUpperCase(name.charAt(0)));
		sb.append(name.charAt(1)).append(name.charAt(2));
		return sb;
	}

	@HaxeMethodBody("return HaxeNatives.str(_date.toString());")
	@JTranscMethodBody(target = "js", value = "return N.str(this._date.toString());")
	native public String toString();

	@Deprecated
	@JTranscMethodBody(target = "js", value = "return N.str(this._date.toLocaleString());")
	native public String toLocaleString();

	@Deprecated
	@JTranscMethodBody(target = "js", value = "return N.str(this._date.toGMTString());")
	native public String toGMTString();

	@Deprecated
	@JTranscMethodBody(target = "js", value = "return this._date.getTimezoneOffset();")
	native public int getTimezoneOffset();
}
