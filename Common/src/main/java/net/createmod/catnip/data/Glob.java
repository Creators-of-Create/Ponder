package net.createmod.catnip.data;

import java.util.regex.PatternSyntaxException;

public class Glob {
	private static final String REGEX_META_CHARS = ".^$+{[]|()";
	private static final String GLOB_META_CHARS = "\\*?[{";
	private static final char EOL = 0;

	public static boolean isRegexMeta(char c) {
		return REGEX_META_CHARS.indexOf(c) != -1;
	}

	public static boolean isGlobMeta(char c) {
		return GLOB_META_CHARS.indexOf(c) != -1;
	}

	private static char next(String glob, int i) {
		return i < glob.length() ? glob.charAt(i) : EOL;
	}

	public static String toRegexPattern(String globPattern) throws PatternSyntaxException {
		boolean inGroup = false;
		StringBuilder regex = new StringBuilder("^");
		int i = 0;

		while(i < globPattern.length()) {
			char c = globPattern.charAt(i++);

			switch(c) {
				case '*' -> regex.append(".*");
				case '?' -> regex.append(".");
				case ',' -> {
					if (inGroup) {
						regex.append(")|(?:");
					} else {
						regex.append(',');
					}
				}
				case '[' -> {
					if (next(globPattern, i) == ']') {
						throw new PatternSyntaxException("Cannot have set with no entries", globPattern, i);
					}

					regex.append("[");

					if (next(globPattern, i) == '^') {
						regex.append("\\^");
						++i;
					} else {
						if (next(globPattern, i) == '!') {
							regex.append('^');
							++i;
						}

						if (next(globPattern, i) == '-') {
							regex.append('-');
							++i;
						}
					}

					boolean hasRangeStart = false;
					char last = 0;

					while (i < globPattern.length()) {
						c = globPattern.charAt(i++);
						if (c == ']') {
							break;
						}

						if (c == '\\' || c == '[' || c == '&' && next(globPattern, i) == '&') {
							regex.append('\\');
						}

						regex.append(c);
						if (c == '-') {
							if (!hasRangeStart) {
								throw new PatternSyntaxException("Invalid range", globPattern, i - 1);
							}

							if ((c = next(globPattern, i++)) == EOL || c == ']') {
								break;
							}

							if (c < last) {
								throw new PatternSyntaxException("Invalid range", globPattern, i - 3);
							}

							regex.append(c);
							hasRangeStart = false;
						} else {
							hasRangeStart = true;
							last = c;
						}
					}

					if (c != ']') {
						throw new PatternSyntaxException("Missing ']'", globPattern, i - 1);
					}

					regex.append("]");
				}
				case '\\' -> {
					if (i == globPattern.length()) {
						throw new PatternSyntaxException("No character to escape", globPattern, i - 1);
					}

					char next = globPattern.charAt(i++);
					if (isGlobMeta(next) || isRegexMeta(next)) {
						regex.append('\\');
					}

					regex.append(next);
				}
				case '{' -> {
					if (inGroup) {
						throw new PatternSyntaxException("Cannot nest groups", globPattern, i - 1);
					}

					regex.append("((?:");
					inGroup = true;
				}
				case '}' -> {
					if (inGroup) {
						regex.append("))");
						inGroup = false;
					} else {
						regex.append('}');
					}
				}
				default -> {
					if (isRegexMeta(c)) {
						regex.append('\\');
					}

					regex.append(c);
				}
			}
		}

		if (inGroup) {
			throw new PatternSyntaxException("Missing '}'", globPattern, i - 1);
		} else {
			return regex.append('$').toString();
		}
	}

	public static String toRegexPattern(String globPattern, String defaultPatternIfError) {
		try {
			return toRegexPattern(globPattern);
		} catch (PatternSyntaxException ignored) {
			return defaultPatternIfError;
		}
	}
}
