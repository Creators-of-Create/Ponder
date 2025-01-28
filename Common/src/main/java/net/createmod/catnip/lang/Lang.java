package net.createmod.catnip.lang;

import java.util.Locale;

public class Lang {
	public static String asId(String name) {
		return name.toLowerCase(Locale.ROOT);
	}

	public static String nonPluralId(String name) {
		String asId = asId(name);
		return asId.endsWith("s") ? asId.substring(0, asId.length() - 1) : asId;
	}

	public static LangBuilder builder(String namespace) {
		return new LangBuilder(namespace);
	}
}
