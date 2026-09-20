package snownee.kiwi.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

class TemplateTest {
	private static String render(String source, Map<String, Boolean> conditions) {
		return Template.parse(source).render(condition -> conditions.getOrDefault(condition, false));
	}

	@Test
	void ifElse() {
		Map<String, Boolean> conditions = Map.of("a", true, "b", false);
		assertEquals("x", render("<#if a>x<#else>y<#endif>", conditions));
		assertEquals("y", render("<#if b>x<#else>y<#endif>", conditions));
	}

	@Test
	void elif() {
		Map<String, Boolean> conditions = Map.of("a", false, "b", true);
		assertEquals("B", render("<#if a>A<#elif b>B<#else>C<#endif>", conditions));
	}

	@Test
	void nested() {
		Map<String, Boolean> conditions = Map.of("a", true, "b", true);
		assertEquals("A[B]C", render("<#if a>A[<#if b>B<#endif>]C<#endif>", conditions));
	}

	@Test
	void escape() {
		assertEquals("<#if a>", render("<##if a>", Map.of()));
	}

	@Test
	void unknownDirectiveIsLiteral() {
		String source = "<#foo baz>bar";
		assertEquals(source, Template.parse(source).render(condition -> true));
	}

	@Test
	void greaterEqualsDoesNotTerminate() {
		assertEquals("new", render("<#if MC[0] >= 1.21>new<#else>old<#endif>", Map.of("MC[0] >= 1.21", true)));
	}

	@Test
	void greaterInsideQuotesDoesNotTerminate() {
		String condition = "STR_CONTAINS(x, '>')";
		assertEquals("y", render("<#if " + condition + ">y<#endif>", Map.of(condition, true)));
	}

	@Test
	void missingEndifThrows() {
		assertThrows(Template.TemplateException.class, () -> Template.parse("<#if a>x"));
	}

	@Test
	void orphanEndifThrows() {
		assertThrows(Template.TemplateException.class, () -> Template.parse("<#endif>"));
	}

	@Test
	void orphanElseThrows() {
		assertThrows(Template.TemplateException.class, () -> Template.parse("<#else>x"));
	}

	@Test
	void elifAfterElseThrows() {
		assertThrows(Template.TemplateException.class, () -> Template.parse("<#if a>x<#else>y<#elif b>z<#endif>"));
	}

	@Test
	void emptyConditionThrows() {
		assertThrows(Template.TemplateException.class, () -> Template.parse("<#if >x<#endif>"));
	}

	@Test
	void textBeforeDirectiveIsKept() {
		assertEquals("111x", render("111<#if a>x<#endif>", Map.of("a", true)));
		assertEquals("111y", render("111<#if a>x<#else>y<#endif>", Map.of("a", false)));
	}

	@Test
	void multilineDirectivesBecomeSingleLine() {
		String source = "<#if a>\nline A\n<#else>\nline B\n<#endif>";
		assertEquals("line A", render(source, Map.of("a", true)));
		assertEquals("line B", render(source, Map.of("a", false)));
	}

	@Test
	void newlineBeforeEmptyConditionalIsRemoved() {
		assertEquals("A", render("A\n<#if a>B<#endif>", Map.of("a", false)));
		assertEquals("A\nB", render("A\n<#if a>B<#endif>", Map.of("a", true)));
	}

	@Test
	void crlfBeforeEmptyConditionalIsRemoved() {
		assertEquals("A", render("A\r\n<#if a>B<#endif>", Map.of("a", false)));
	}

	@Test
	void evalDirective() {
		assertEquals("value=3", Template.parse("value=<#eval 1 + 2>").render(condition -> false, expression -> "3"));
	}

	@Test
	void evalKeepsSurroundingNewlines() {
		assertEquals("A\n3\nB", Template.parse("A\n<#eval e>\nB").render(condition -> false, expression -> "3"));
	}

	@Test
	void evalInsideConditional() {
		assertEquals("X=3", Template.parse("<#if a>X=<#eval e><#endif>").render(condition -> true, expression -> "3"));
	}

	@Test
	void nestedLinesArePreserved() {
		Map<String, Boolean> conditions = Map.of("a", true, "b", true);
		assertEquals("A\nB\nC", render("<#if a>A\n<#if b>B<#endif>\nC<#endif>", conditions));
	}

	@Test
	void blankLineKeptWhenSatisfiedAndRemovedWhenNot() {
		assertEquals("A\n\nB", render("A\n\n<#if a>B<#endif>", Map.of("a", true)));
		assertEquals("A\n", render("A\n\n<#if a>B<#endif>", Map.of("a", false)));
	}

	@Test
	void innerNewlinesArePreserved() {
		assertEquals("l1\nl2", render("<#if a>l1\nl2<#endif>", Map.of("a", true)));
	}

	@Test
	void inlineSpacesArePreserved() {
		assertEquals("Hello world", render("Hello <#if a>world<#endif>", Map.of("a", true)));
	}

	@Test
	void emptyElseKeepsKeyEmpty() {
		assertEquals("", render("<#if a>x<#else><#endif>", Map.of("a", false)));
		assertEquals("", render("<#if a>x<#endif>", Map.of("a", false)));
	}

	@Test
	void plainTextIsUntouched() {
		assertEquals("nothing to see", Template.parse("nothing to see").render(condition -> {
			throw new AssertionError("should not evaluate");
		}));
	}
}
