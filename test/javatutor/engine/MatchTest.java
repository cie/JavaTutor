package javatutor.engine;

import static javatutor.engine.Matching.parseStatements;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import javatutor.engine.Matching.InvalidVariableException;
import javatutor.engine.Matching.UnknownNodeTypeException;

class MatchTest {

	private void check(String expected, String pattern, String concrete) {
		assertEquals(expected,
				Matching.match(parseStatements(pattern), parseStatements(concrete), Optional.empty())
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
		assertThrows(UnknownNodeTypeException.class, () -> check("", "$BlahNode()", "g()"));
	}

	@Test
	void invalidVariable() {
		assertThrows(InvalidVariableException.class, () -> check("", "$invalid_()", "g()"));
	}

	// -- wildcard typed variables

	@Test
	void wildcardVariable_functionName() {
		check("{}", "$SimpleName()", "g()");
	}

	// -- typed variables

	@Test
	void variable_once() {
		check("{SimpleName1=g}", "$SimpleName1()", "g()");
	}

	@Test
	void variable_twice() {
		check("{Expression1=3}", "Math.max($Expression1,$Expression1)", "Math.max(  3, 3)");
		check(null, "Math.max($Expression1,$Expression1)", "Math.max(  3, 4)");
	}

	// -- untyped variables
	@Test
	void untypedVariable() {
		check("{x=k}", "$x++", "k++");
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

	// -- various node types
	@Test
	void type() {
		check("{t=int}", "$t x;", "int x;");
		check("{t=int[]}", "$t x;", "int[] x;");
		check("{t=A.B<q>[]}", "$t x;", "A.B<q>[] x;");

		check("{t=int}", "$t[] x", "int[] x;");
		check(null, "$t[] x", "int x;");

		check("{t=String}", "List<$t> x", "List<String> x;");
	}
	
}
