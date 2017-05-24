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
import com.jtransc.time.JTranscTime;

//@HaxeAddMembers({ "var _date:Date;" })
public class Date implements java.io.Serializable, Cloneable, Comparable<Date> {
	private long _timestamp;
	private boolean updated;
	private int[] parts = new int[JTranscTime.PARTS];

	private void setTimestamp(long timestamp) {
		if (this._timestamp != timestamp) {
			this._timestamp = timestamp;
			this.updated = false;
		}
	}

	private int[] getParts() {
		if (!updated) {
			updated = true;
			JTranscTime.fillParts(_timestamp, parts);
		}
		return parts;
	}

    //@HaxeMethodBody("_date = Date.now();")
	public Date() {
		this(System.currentTimeMillis());
	}

    //@HaxeMethodBody("_date = Date.fromTime(N.longToFloat(p0));")
	@JTranscMethodBody(target = "js", value = "this._date = new Date(N.ltoFloat(p0));")
	public Date(long timestamp) {
		this.setTimestamp(timestamp);
	}

	@Deprecated
    //@HaxeMethodBody("_date = new Date(p0, p1, p2, 0, 0, 0);")
	public Date(int year, int month, int date) {
		this(year, month, date, 0, 0, 0);
	}

	@Deprecated
	//@HaxeMethodBody("_date = new Date(p0, p1, p2, p3, p4, 0);")
	public Date(int year, int month, int date, int hrs, int min) {
		this(year, month, date, hrs, min, 0);
	}

	@Deprecated
	//@HaxeMethodBody("_date = new Date(p0, p1, p2, p3, p4, p5);")
	@JTranscMethodBody(target = "js", value = "this._date = new Date(p0, p1, p2, p3, p4, p5);")
	public Date(int year, int month, int date, int hrs, int min, int sec) {
		this(JTranscTime.make(1900 + year, month, date, hrs, min, sec, 0));
	}

	@Deprecated
	//@HaxeMethodBody("_date = Date.fromString(p0._str);")
	@JTranscMethodBody(target = "js", value = "this._date = new Date(Date.parse(N.istr(p0)));")
	public Date(String s) {
		this(JTranscTime.parse(s));
	}

	public Object clone() {
		return new Date(this._timestamp);
	}

	@Deprecated
	@JTranscMethodBody(target = "js", value = "return N.lnewFloat(Date.UTC(p0, p1, p2, p3, p4, p5));")
	public static long UTC(int year, int month, int date, int hrs, int min, int sec) {
		return JTranscTime.make(1900 + year, month, date, hrs, min, sec, 0);
	}

	@Deprecated
    //@HaxeMethodBody("return N.floatToLong(Date.fromString(p0._str).getTime());")
	@JTranscMethodBody(target = "js", value = "return N.lnewFloat(Date.parse(N.istr(p0)));")
	public static long parse(String s) {
		return JTranscTime.parse(s);
	}

    @Deprecated
	//@HaxeMethodBody("return _date.getFullYear() - 1900;")
	@JTranscMethodBody(target = "js", value = "return this._date.getYear();")
	public int getYear() {
		return getFullYear() - 1900;
	}

	private int getFullYear() {
		return JTranscTime.getFullYear(getParts());
	}

	private int getMilliseconds() {
		return 0;
	}

	@Deprecated
	@JTranscMethodBody(target = "js", value = "this._date.setYear(p0);")
	public void setYear(int year) {
		this.setTimestamp(JTranscTime.make(1900 + year, getMonth(), getDate(), getHours(), getMinutes(), getSeconds(), getMilliseconds()));
	}

	@Deprecated
	//@HaxeMethodBody("return _date.getMonth();")
	@JTranscMethodBody(target = "js", value = "return this._date.getMonth();")
	public int getMonth() {
		return JTranscTime.getMonth(getParts());
	}

	@Deprecated
	@JTranscMethodBody(target = "js", value = "this._date.setMonth(p0);")
	public void setMonth(int month) {
		this.setTimestamp(JTranscTime.make(getFullYear(), month, getDate(), getHours(), getMinutes(), getSeconds(), getMilliseconds()));
	}

	@Deprecated
    //@HaxeMethodBody("return _date.getDate();")
	@JTranscMethodBody(target = "js", value = "return this._date.getDate();")
    public int getDate() {
		return JTranscTime.getMonthDay(getParts());
	}

	@Deprecated
	@JTranscMethodBody(target = "js", value = "this._date.setDate(p0);")
	public void setDate(int date) {
		this.setTimestamp(JTranscTime.make(getFullYear(), getMonth(), date, getHours(), getMinutes(), getSeconds(), getMilliseconds()));
	}

	@Deprecated
	//@HaxeMethodBody("return _date.getDay();")
	@JTranscMethodBody(target = "js", value = "return this._date.getDay();")
	public int getDay() {
		return JTranscTime.getWeekDay(getParts());
	}

	@Deprecated
    //@HaxeMethodBody("return _date.getHours();")
	@JTranscMethodBody(target = "js", value = "return this._date.getHours();")
	public int getHours() {
		return JTranscTime.getHours(getParts());
	}

	@Deprecated
	@JTranscMethodBody(target = "js", value = "this._date.setHours(p0);")
	public void setHours(int hours) {
		this.setTimestamp(JTranscTime.make(getFullYear(), getMonth(), getDate(), hours, getMinutes(), getSeconds(), getMilliseconds()));
	}

	@Deprecated
	//@HaxeMethodBody("return _date.getMinutes();")
	@JTranscMethodBody(target = "js", value = "return this._date.getMinutes();")
	public int getMinutes() {
		return JTranscTime.getMinutes(getParts());
	}

	@Deprecated
	@JTranscMethodBody(target = "js", value = "this._date.setMinutes(p0);")
	public void setMinutes(int minutes) {
		this.setTimestamp(JTranscTime.make(getFullYear(), getMonth(), getDate(), getHours(), minutes, getSeconds(), getMilliseconds()));
	}

	@Deprecated
	//@HaxeMethodBody("return _date.getSeconds();")
	@JTranscMethodBody(target = "js", value = "return this._date.getSeconds();")
	public int getSeconds() {
		return JTranscTime.getSeconds(getParts());
	}

	@Deprecated
	@JTranscMethodBody(target = "js", value = "this._date.setSeconds(p0);")
	public void setSeconds(int seconds) {
		this.setTimestamp(JTranscTime.make(getFullYear(), getMonth(), getDate(), getHours(), getMinutes(), seconds, getMilliseconds()));
	}

    //@HaxeMethodBody("return N.floatToLong(_date.getTime());")
	@JTranscMethodBody(target = "js", value = "return N.lnewFloat(this._date.getTime());")
	public long getTime() {
		return this._timestamp;
	}

	@JTranscMethodBody(target = "js", value = "this._date.setTime(N.ltoFloat(p0));")
	//@HaxeMethodBody("_date = Date.fromTime(N.longToFloat(p0));")
	public void setTime(long time) {
		this.setTimestamp(time);
	}

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

	//@HaxeMethodBody("return N.str(_date.toString());")
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
