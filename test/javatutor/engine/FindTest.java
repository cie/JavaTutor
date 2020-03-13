package javatutor.engine;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static javatutor.engine.Matching.parseStatements;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import javatutor.engine.Matching.Match;

class FindTest {
	private void checkFindStmt(String expected, String pattern, String concrete) {
		assertEquals(expected, Matching.findStmt(pattern, parseStatements(concrete), empty()).stream()
				.map(Match::getBindings).collect(toList()).toString());
	}

	private void checkFindExpr(String expected, String pattern, String concrete) {
		assertEquals(expected, Matching.findExpr(pattern, parseStatements(concrete), empty()).stream()
				.map(Match::getBindings).collect(toList()).toString());
	}

	private void checkFindVar(String expected, String type, String var, String concrete) {
		assertEquals(expected, Matching.findVar(type, var, parseStatements(concrete), empty()).stream()
				.map(Match::getBindings).collect(toList()).toString());
	}

	// -- find

	@Test
	void findStmt() {
		checkFindStmt("[{x=f}, {x=g}, {x=h}]", "$x()", "f();g();h();");
	}

	@Test
	void findExpr() {
		checkFindExpr("[{x=f(3 + 4), y=g()}, {x=3, y=4}]", "$x+$y", "y = f(3+4)+g()");
	}

	/*
	 * @Test void find_consecutiveStatements() { checkFindStatement("$x();",
	 * "f();g();h();", "f();|g();|g();"); List<Match> matches =
	 * Matching.find(parseStatements("$x = 3; $stmt; $x = 4"),
	 * parseStatements("a=1; a=3; a=2; a=4"), Optional.empty()); // assertEquals(1,
	 * matches.size()); // TODO }
	 */

	@Test
	void findVar() {
		checkFindVar("[{x=b}, {x=a}]", "int", "$x", "int b = 7; int a;");
	}

	/*
	 * @Test void findVar_initializer() { checkFindVar("int", "$x", "0",
	 * "int a=0,b,c=0; b=0;", "{x=a},{x=b},{x=c}"); }
	 */

}
