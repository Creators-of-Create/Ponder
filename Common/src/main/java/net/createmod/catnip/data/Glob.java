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
		boolean isNegativeLookaround = false;
		boolean isAnchored = true;

		while (i < globPattern.length()) {
			char c = globPattern.charAt(i++);

			switch (c) {
				case '*' -> {
					regex.append(".*");
					if (!inGroup) {
						isAnchored = false;
					}
				}
				case '?' -> {
					regex.append(".");
					if (!inGroup) {
						isAnchored = true;
					}
				}
				case ',' -> {
					if (inGroup) {
						regex.append("|");
					} else {
						regex.append(',');
						isAnchored = true;
					}
				}
				case '[' -> {
					if (next(globPattern, i) == ']' || (next(globPattern, i) == '!' && next(globPattern, i+1) == ']')) {
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

						if (c == '\\') {
							//escape next character if available and needed (only '-' and ']' have special meaning within sets, all other characters are treated as literals 
							if (i == globPattern.length()) {
								throw new PatternSyntaxException("No character to escape", globPattern, i - 1);
							}
							if (next(globPattern, i) == ']' || next(globPattern, i)=='-' || next(globPattern, i)=='\\') {
								regex.append('\\');
							}
							regex.append(next(globPattern, i++));
							continue; //Character is escaped and will not be processed further
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
					if (!inGroup) {
						isAnchored = true;
					}
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
					if (!inGroup) {
						isAnchored = true;
					}
				}
				case '{' -> {
					if (inGroup) {
						throw new PatternSyntaxException("Cannot nest groups", globPattern, i - 1);
					}

					regex.append("(?");
					if (next(globPattern, i) == '!') {
						isNegativeLookaround = true;
						if (!isAnchored) {
							regex.append('<');
						}
						regex.append('!');
						++i;
					} else {
						isNegativeLookaround = false;
						regex.append(":");
					}

					inGroup = true;
				}
				case '}' -> {
					if (inGroup) {
						regex.append(")");
						if (isAnchored && isNegativeLookaround) {
							regex.append(".*");
							isAnchored = false;
						}
						inGroup = false;
					} else {
						regex.append('}');
						isAnchored = true;
					}
				}
				default -> {
					if (isRegexMeta(c)) {
						regex.append('\\');
					}

					regex.append(c);
					if (!inGroup) {
						isAnchored = true;
					}
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
