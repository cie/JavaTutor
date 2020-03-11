package javatutor.engine;

import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import javatutor.engine.Matching.Match;

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
				new String[] { "world(); for (int i = 0; i < 10; ++i) { }" }
		));
		assertEquals(
				of(new Hint("Oops, `i` should go from 0.", of(new Match(of(new HashMap<>()))),
						of(new Match(of(new HashMap<>()))))),
				engine.suggest(
				"k = 3; hello(); for (int i = 1; i < 10; ++i) { }; if (false) {}",
				new String[] { "world(); for (int i = 0; i < 10; ++i) { }" }
		));
	}

}
