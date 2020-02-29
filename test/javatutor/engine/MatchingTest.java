package javatutor.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import javatutor.engine.Matching.InvalidVariableException;
import javatutor.engine.Matching.Match;
import javatutor.engine.Matching.UnknownNodeTypeException;

class MatchingTest {

	private void check(String expected, String pattern, String concrete) {
		assertEquals(expected,
				Matching.match(Engine.parseStatements(pattern), Engine.parseStatements(concrete), Optional.empty())
						.map(match -> match.bindings.toString()).orElse(null));
	}

	// -- no variables

	@Test
	void noVariables_same() {
		check("{}", "f()", "f()");
	}

	@Test
	void noVariables_differentTypes() {
		check(null, "f()", "if(true){}");
	}

	@Test
	void noVariables_differentNames() {
		check(null, "f()", "g()");
	}

	// -- invalid patterns
	@Test
	void unknownType() {
		assertThrows(UnknownNodeTypeException.class, () -> check("", "$blah()", "g()"));
	}

	@Test
	void invalidVariable() {
		assertThrows(InvalidVariableException.class, () -> check("", "$invalid_()", "g()"));
	}

	// -- wildcard variables

	@Test
	void wildcardVariable_functionName() {
		check("{}", "$SimpleName()", "g()");
	}

	// -- normal variables

	@Test
	void variable_once() {
		check("{SimpleName1=g}", "$SimpleName1()", "g()");
	}

	@Test
	void variable_twice() {
		check("{Expression1=3}", "Math.max($Expression1,$Expression1)", "Math.max(  3, 3)");
		check(null, "Math.max($Expression1,$Expression1)", "Math.max(  3, 4)");
	}

	// -- statement variables
	@Test
	void statementVariable_expression() {
		check("{Statement1=f();\n}", "if (true) $Statement1", "if (true) f();");
		check("{Statement1=f();\n}", "if (true) $Statement1", "if (true) f()");
		check("{Statement1=f();\n}", "if (true) $Statement1;", "if (true) f()");
		check("{Statement1=f();\n}", "if (true) $Statement1;", "if (true) f();");
	}

	@Test
	void statementVariable_block() {
		check("{Statement1={\n  f();\n}\n}", "if (true) $Statement1", "if (true) { f(); }");
	}

	@Test
	void statementVariable_if() {
		check("{Statement1=if (hello) f();\n}", "if (true) $Statement1", "if (true) if (hello) f();");
		check("{IfStatement1=if (hello) f();\n}", "if (true) $IfStatement1", "if (true) if (hello) f();");
		check(null, "if (true) $IfStatement1", "if (true) while (hello) f();");
	}

	// -- find

	@Test
	void find() {
		List<Match> matches = Matching.find(Engine.parseStatements("g()"), Engine.parseStatements("f(); g(); h();"),
				Optional.empty());
		assertEquals(1, matches.size());
		assertEquals("g();\n", matches.get(0).matchedNode.toString());
	}

	@Test
	void find_variables() {
		List<Match> matches = Matching.find(Engine.parseStatements("$SimpleName()"),
				Engine.parseStatements("f(); g(); h();"), Optional.empty());
		assertEquals(3, matches.size());
		assertEquals("f();\n", matches.get(0).matchedNode.toString());
		assertEquals("g();\n", matches.get(1).matchedNode.toString());
		assertEquals("h();\n", matches.get(2).matchedNode.toString());
	}

}
