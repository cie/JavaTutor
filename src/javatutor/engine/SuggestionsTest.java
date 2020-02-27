package javatutor.engine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SuggestionsTest {
	Engine brain = new Engine();
	
	//@Test
	void forLoopCount() {
		brain.addRule(
				"for (int $SimpleName1 = 1; $Expression; $Expression) $Statement",
				"for (int $SimpleName1 = 0; $Expression; $Expression) $Statement",
				"Oops"
		);
		assertEquals(brain.suggest(
				"for (int i = 0; i < 10; ++i) { }",
				"for (int i = 0; i < 10; ++i) { }"),
				null);
		assertEquals(brain.suggest(
				"for (int i = 1; i < 10; ++i) { }",
				"for (int i = 0; i < 10; ++i) { }"),
				new Suggestion("Oops"));
	}

}
