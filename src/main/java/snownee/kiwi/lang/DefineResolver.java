package snownee.kiwi.lang;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

public final class DefineResolver {
	private DefineResolver() {
	}

	public static String prepare(
			String expression,
			Map<String, String> defines,
			ConditionEvaluator evaluator,
			WarningSink warnings,
			String warningKey) {
		StringBuilder out = new StringBuilder(expression.length() + 16);
		expand(expression, defines, evaluator, warnings, warningKey, out, new ArrayDeque<>());
		return out.toString();
	}

	private static void expand(
			String source,
			Map<String, String> defines,
			ConditionEvaluator evaluator,
			WarningSink warnings,
			String warningKey,
			StringBuilder out,
			Deque<String> stack) {
		int i = 0;
		int n = source.length();
		while (i < n) {
			char c = source.charAt(i);
			if (c == '\'' || c == '"') {
				int end = skipQuoted(source, i);
				out.append(source, i, end);
				i = end;
			} else if (isIdentifierStart(c)) {
				int end = i + 1;
				while (end < n && isIdentifierPart(source.charAt(end))) {
					end++;
				}
				String name = source.substring(i, end);
				if (end < n && source.charAt(end) == ':') {
					if (name.equals("has")) {
						int idEnd = parseHasId(source, end + 1);
						if (idEnd > end + 1) {
							out.append("HAS(\"").append(source, end + 1, idEnd).append("\")");
							i = idEnd;
							continue;
						}
					} else if (name.equals("cfg")) {
						int idEnd = parseCfgPath(source, end + 1);
						if (idEnd > end + 1) {
							out.append("CFG(\"").append(source, end + 1, idEnd).append("\")");
							i = idEnd;
							continue;
						}
					}
				} else {
					String value = defines.get(name);
					if (value != null && !evaluator.isReservedWord(name)) {
						if (stack.contains(name)) {
							warnings.warn(
									warningKey + "|cycle:" + name,
									"Cyclic define '" + name + "' while preprocessing " + warningKey);
							out.append("false");
						} else {
							stack.push(name);
							expand(value, defines, evaluator, warnings, warningKey, out, stack);
							stack.pop();
						}
						i = end;
						continue;
					}
				}
				out.append(name);
				i = end;
			} else {
				out.append(c);
				i++;
			}
		}
	}

	private static int skipQuoted(String source, int start) {
		char quote = source.charAt(start);
		int i = start + 1;
		int n = source.length();
		while (i < n) {
			char c = source.charAt(i);
			if (c == '\\' && i + 1 < n) {
				i += 2;
			} else if (c == quote) {
				return i + 1;
			} else {
				i++;
			}
		}
		return n;
	}

	private static int parseHasId(String source, int start) {
		int i = start;
		int n = source.length();
		if (i < n && source.charAt(i) == '@') {
			i++;
			int modStart = i;
			while (i < n && isIdPart(source.charAt(i))) {
				i++;
			}
			if (i == modStart) {
				return start;
			}
			if (i < n && source.charAt(i) == ':') {
				i++;
				int pathStart = i;
				while (i < n && isPathPart(source.charAt(i))) {
					i++;
				}
				if (i == pathStart) {
					return start;
				}
			}
			return i;
		}
		int modStart = i;
		while (i < n && isIdPart(source.charAt(i))) {
			i++;
		}
		return i == modStart ? start : i;
	}

	private static int parseCfgPath(String source, int start) {
		int i = start;
		int n = source.length();
		while (i < n && isIdPart(source.charAt(i))) {
			i++;
		}
		return i == start ? start : i;
	}

	private static boolean isIdentifierStart(char c) {
		return c == '_' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}

	private static boolean isIdentifierPart(char c) {
		return isIdentifierStart(c) || c >= '0' && c <= '9';
	}

	private static boolean isIdPart(char c) {
		return c == '_' || c == '.' || c == '-' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
	}

	private static boolean isPathPart(char c) {
		return isIdPart(c) || c == '/' || c == ':';
	}
}
