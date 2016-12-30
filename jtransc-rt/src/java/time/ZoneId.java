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
	public static final Map<String, String> SHORT_IDS = new HashMap<>(64);

	ZoneId() {
	}

	static {
		SHORT_IDS.put("ACT", "Australia/Darwin");
		SHORT_IDS.put("AET", "Australia/Sydney");
		SHORT_IDS.put("AGT", "America/Argentina/Buenos_Aires");
		SHORT_IDS.put("ART", "Africa/Cairo");
		SHORT_IDS.put("AST", "America/Anchorage");
		SHORT_IDS.put("BET", "America/Sao_Paulo");
		SHORT_IDS.put("BST", "Asia/Dhaka");
		SHORT_IDS.put("CAT", "Africa/Harare");
		SHORT_IDS.put("CNT", "America/St_Johns");
		SHORT_IDS.put("CST", "America/Chicago");
		SHORT_IDS.put("CTT", "Asia/Shanghai");
		SHORT_IDS.put("EAT", "Africa/Addis_Ababa");
		SHORT_IDS.put("ECT", "Europe/Paris");
		SHORT_IDS.put("IET", "America/Indiana/Indianapolis");
		SHORT_IDS.put("IST", "Asia/Kolkata");
		SHORT_IDS.put("JST", "Asia/Tokyo");
		SHORT_IDS.put("MIT", "Pacific/Apia");
		SHORT_IDS.put("NET", "Asia/Yerevan");
		SHORT_IDS.put("NST", "Pacific/Auckland");
		SHORT_IDS.put("PLT", "Asia/Karachi");
		SHORT_IDS.put("PNT", "America/Phoenix");
		SHORT_IDS.put("PRT", "America/Puerto_Rico");
		SHORT_IDS.put("PST", "America/Los_Angeles");
		SHORT_IDS.put("SST", "Pacific/Guadalcanal");
		SHORT_IDS.put("VST", "Asia/Ho_Chi_Minh");
		SHORT_IDS.put("EST", "-05:00");
		SHORT_IDS.put("MST", "-07:00");
		SHORT_IDS.put("HST", "-10:00");
	}

	native public static ZoneId systemDefault();

	native public static Set<String> getAvailableZoneIds();

	native public static ZoneId of(String zoneId, Map<String, String> aliasMap);

	native public static ZoneId of(String zoneId);

	native public static ZoneId ofOffset(String prefix, ZoneOffset offset);

	native public static ZoneId from(TemporalAccessor temporal);

	public abstract String getId();

	native public String getDisplayName(TextStyle style, Locale locale);

	public abstract ZoneRules getRules();

	native public ZoneId normalized();

	native public boolean equals(Object obj);

	native public int hashCode();

	native public String toString();
}
