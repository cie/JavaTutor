package javatutor.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class SuggestionsTest {
	Engine engine = new Engine();
	
	@Test
	void forLoopCount() {
		engine.addRule(
				"for (int $SimpleName1 = 1; $Expression; $Expression) $Statement",
				"for (int $SimpleName1 = 0; $Expression; $Expression) $Statement",
				"Oops, `$SimpleName1` should go from 0."
		);
		assertEquals(Optional.empty(), engine.suggest(
				"k = 3; hello(); for (int i = 0; i < 10; ++i) { }; if (false) {}",
				"world(); for (int i = 0; i < 10; ++i) { }"
		));
		assertEquals(Optional.of(new Suggestion("Oops, `i` should go from 0.")), engine.suggest(
				"k = 3; hello(); for (int i = 1; i < 10; ++i) { }; if (false) {}",
				"world(); for (int i = 0; i < 10; ++i) { }"
		));
	}

}
