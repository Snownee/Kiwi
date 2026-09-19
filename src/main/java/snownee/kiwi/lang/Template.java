package snownee.kiwi.lang;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

public final class Template {
	private static final Map<String, Directive> DIRECTIVES = new HashMap<>();

	static {
		for (Directive directive : Directive.values()) {
			DIRECTIVES.put(directive.keyword, directive);
		}
	}

	private final List<Node> nodes;

	private Template(List<Node> nodes) {
		this.nodes = nodes;
	}

	public static Template parse(String source) {
		List<Node> root = new ArrayList<>();
		Deque<Frame> stack = new ArrayDeque<>();
		List<Node> current = root;
		StringBuilder text = new StringBuilder();
		int i = 0;
		int n = source.length();
		while (i < n) {
			char c = source.charAt(i);
			if (c == '<' && i + 1 < n && source.charAt(i + 1) == '#') {
				if (i + 2 < n && source.charAt(i + 2) == '#') {
					text.append("<#");
					i += 3;
					continue;
				}
				int keywordStart = i + 2;
				int keywordEnd = keywordStart;
				while (keywordEnd < n && isAsciiLetter(source.charAt(keywordEnd))) {
					keywordEnd++;
				}
				Directive directive = DIRECTIVES.get(source.substring(keywordStart, keywordEnd));
				if (directive == null) {
					text.append("<#");
					i += 2;
					continue;
				}
				if (directive == Directive.ELSE || directive == Directive.ENDIF) {
					int end = skipWhitespace(source, keywordEnd);
					if (end < n && source.charAt(end) == '>') {
						stripTrailingNewline(text);
						flush(text, current);
						i = skipLeadingNewline(source, end + 1);
						current = applySimple(directive, stack, current);
						continue;
					}
					text.append("<#");
					i += 2;
					continue;
				}
				int conditionStart = skipWhitespace(source, keywordEnd);
				int conditionEnd = findConditionEnd(source, conditionStart);
				if (conditionEnd >= 0) {
					String condition = source.substring(conditionStart, conditionEnd).trim();
					if (condition.isEmpty()) {
						throw new TemplateException("Empty condition in <#" + directive.keyword + ">");
					}
					stripTrailingNewline(text);
					flush(text, current);
					i = skipLeadingNewline(source, conditionEnd + 1);
					current = applyConditional(directive, condition, stack, current);
					continue;
				}
				text.append("<#");
				i += 2;
				continue;
			}
			text.append(c);
			i++;
		}
		flush(text, current);
		if (!stack.isEmpty()) {
			throw new TemplateException("Missing <#endif>");
		}
		return new Template(List.copyOf(root));
	}

	public String render(Predicate<String> conditionTest) {
		StringBuilder out = new StringBuilder();
		render(nodes, out, conditionTest);
		return out.toString();
	}

	private static void render(List<Node> nodes, StringBuilder out, Predicate<String> conditionTest) {
		for (Node node : nodes) {
			if (node instanceof Text text) {
				out.append(text.value());
			} else if (node instanceof Conditional conditional) {
				boolean matched = false;
				for (Branch branch : conditional.branches()) {
					if (conditionTest.test(branch.condition())) {
						render(branch.body(), out, conditionTest);
						matched = true;
						break;
					}
				}
				if (!matched && conditional.elseBody() != null) {
					render(conditional.elseBody(), out, conditionTest);
				}
			}
		}
	}

	private static List<Node> applyConditional(
			Directive directive,
			String condition,
			Deque<Frame> stack,
			List<Node> current) {
		if (directive == Directive.IF) {
			Frame frame = new Frame(condition, current);
			stack.push(frame);
			return frame.current;
		}
		if (stack.isEmpty()) {
			throw new TemplateException("<#elif> without <#if>");
		}
		Frame frame = stack.peek();
		if (frame.inElse) {
			throw new TemplateException("<#elif> after <#else>");
		}
		frame.branches.add(new Branch(frame.condition, frame.current));
		frame.condition = condition;
		frame.current = new ArrayList<>();
		return frame.current;
	}

	private static List<Node> applySimple(Directive directive, Deque<Frame> stack, List<Node> current) {
		if (directive == Directive.ELSE) {
			if (stack.isEmpty()) {
				throw new TemplateException("<#else> without <#if>");
			}
			Frame frame = stack.peek();
			if (frame.inElse) {
				throw new TemplateException("Duplicate <#else>");
			}
			frame.branches.add(new Branch(frame.condition, frame.current));
			frame.inElse = true;
			frame.elseBody = new ArrayList<>();
			frame.current = frame.elseBody;
			return frame.elseBody;
		}
		if (stack.isEmpty()) {
			throw new TemplateException("<#endif> without <#if>");
		}
		Frame frame = stack.pop();
		if (!frame.inElse) {
			frame.branches.add(new Branch(frame.condition, frame.current));
		}
		Conditional node = new Conditional(
				List.copyOf(frame.branches),
				frame.elseBody == null ? null : List.copyOf(frame.elseBody));
		frame.parentCurrent.add(node);
		return frame.parentCurrent;
	}

	private static void flush(StringBuilder text, List<Node> current) {
		if (!text.isEmpty()) {
			current.add(new Text(text.toString()));
			text.setLength(0);
		}
	}

	private static int skipWhitespace(String source, int from) {
		int i = from;
		while (i < source.length() && Character.isWhitespace(source.charAt(i))) {
			i++;
		}
		return i;
	}

	private static void stripTrailingNewline(StringBuilder text) {
		int i = text.length();
		while (i > 0 && isHorizontalWhitespace(text.charAt(i - 1))) {
			i--;
		}
		if (i == 0 || text.charAt(i - 1) != '\n' && text.charAt(i - 1) != '\r') {
			return;
		}
		int cut = i - 1;
		if (text.charAt(cut) == '\n' && cut > 0 && text.charAt(cut - 1) == '\r') {
			cut--;
		}
		while (cut > 0 && isHorizontalWhitespace(text.charAt(cut - 1))) {
			cut--;
		}
		text.setLength(cut);
	}

	private static int skipLeadingNewline(String source, int index) {
		int n = source.length();
		int i = index;
		while (i < n && isHorizontalWhitespace(source.charAt(i))) {
			i++;
		}
		if (i >= n) {
			return index;
		}
		if (source.charAt(i) == '\n') {
			return i + 1;
		}
		if (source.charAt(i) == '\r') {
			return i + 1 < n && source.charAt(i + 1) == '\n' ? i + 2 : i + 1;
		}
		return index;
	}

	private static boolean isHorizontalWhitespace(char c) {
		return c == ' ' || c == '\t';
	}

	private static int findConditionEnd(String source, int from) {
		char quote = 0;
		int n = source.length();
		for (int i = from; i < n; i++) {
			char c = source.charAt(i);
			if (quote != 0) {
				if (c == '\\') {
					i++;
				} else if (c == quote) {
					quote = 0;
				}
				continue;
			}
			if (c == '\'' || c == '"') {
				quote = c;
				continue;
			}
			if (c == '>' && (i + 1 >= n || source.charAt(i + 1) != '=')) {
				return i;
			}
		}
		return -1;
	}

	private static boolean isAsciiLetter(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}

	private enum Directive {
		IF("if"),
		ELIF("elif"),
		ELSE("else"),
		ENDIF("endif");

		private final String keyword;

		Directive(String keyword) {
			this.keyword = keyword;
		}
	}

	private static final class Frame {
		private final List<Node> parentCurrent;
		private final List<Branch> branches = new ArrayList<>();
		private String condition;
		private List<Node> current;
		private @Nullable List<Node> elseBody;
		private boolean inElse;

		private Frame(String condition, List<Node> parentCurrent) {
			this.condition = condition;
			this.parentCurrent = parentCurrent;
			this.current = new ArrayList<>();
		}
	}

	private sealed interface Node permits Text, Conditional {
	}

	private record Text(String value) implements Node {
	}

	private record Branch(String condition, List<Node> body) {
	}

	private record Conditional(List<Branch> branches, @Nullable List<Node> elseBody) implements Node {
	}

	public static final class TemplateException extends RuntimeException {
		public TemplateException(String message) {
			super(message);
		}
	}
}
