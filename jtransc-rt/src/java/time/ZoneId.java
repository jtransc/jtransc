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
package java.time;

import java.io.Serializable;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAccessor;
import java.time.zone.ZoneRules;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class ZoneId implements Serializable {
	public static final Map<String, String> SHORT_IDS = new HashMap<String, String>(64) {{
		put("ACT", "Australia/Darwin");
		put("AET", "Australia/Sydney");
		put("AGT", "America/Argentina/Buenos_Aires");
		put("ART", "Africa/Cairo");
		put("AST", "America/Anchorage");
		put("BET", "America/Sao_Paulo");
		put("BST", "Asia/Dhaka");
		put("CAT", "Africa/Harare");
		put("CNT", "America/St_Johns");
		put("CST", "America/Chicago");
		put("CTT", "Asia/Shanghai");
		put("EAT", "Africa/Addis_Ababa");
		put("ECT", "Europe/Paris");
		put("IET", "America/Indiana/Indianapolis");
		put("IST", "Asia/Kolkata");
		put("JST", "Asia/Tokyo");
		put("MIT", "Pacific/Apia");
		put("NET", "Asia/Yerevan");
		put("NST", "Pacific/Auckland");
		put("PLT", "Asia/Karachi");
		put("PNT", "America/Phoenix");
		put("PRT", "America/Puerto_Rico");
		put("PST", "America/Los_Angeles");
		put("SST", "Pacific/Guadalcanal");
		put("VST", "Asia/Ho_Chi_Minh");
		put("EST", "-05:00");
		put("MST", "-07:00");
		put("HST", "-10:00");
	}};

	ZoneId() {
	}

	public static ZoneId systemDefault() {
		return ZoneOffset.UTC;
	}

	public static Set<String> getAvailableZoneIds() {
		return SHORT_IDS.keySet();
	}

	public static ZoneId of(String zoneId, Map<String, String> aliasMap) {
		return ZoneOffset.UTC;
	}

	public static ZoneId of(String zoneId) {
		return ZoneOffset.UTC;
	}

	public static ZoneId ofOffset(String prefix, ZoneOffset offset) {
		return ZoneOffset.UTC;
	}

	public static ZoneId from(TemporalAccessor temporal) {
		return ZoneOffset.UTC;
	}

	public abstract String getId();

	public String getDisplayName(TextStyle style, Locale locale) {
		return getId();
	}

	public abstract ZoneRules getRules();

	public ZoneId normalized() {
		return this;
	}

	public boolean equals(Object obj) {
		return this == obj;
	}

	public int hashCode() {
		return getId().hashCode();
	}

	public String toString() {
		return getId();
	}
}
