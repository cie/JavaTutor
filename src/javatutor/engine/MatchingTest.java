package javatutor.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import javatutor.engine.Matching.Match;

class MatchingTest {

	private void check(Match expected, String pattern, String concrete) {
		Match match = Matching.match(Engine.parseStatements(pattern), Engine.parseStatements(concrete));
		assertEquals(expected, match);
	}

	@Test
	void noVariables_same() {
		check(new Match(), "f()", "f()");
	}

	@Test
	void noVariables_differentTypes() {
		check(null, "f()", "if(true){}");
	}

	@Test
	void noVariables_differentNames() {
		check(null, "f()", "g()");
	}

	@Test
	void wildcardVariable_functionName() {
		check(new Match(), "$SimpleName()", "g()");
	}

}
