package javatutor.tasks.arrays.above_average;

import javatutor.engine.Task;

public class AboveAverageTask extends Task {

	@Override
	protected String getHint() {
		if (!findExpr("new Scanner($x)"))
			return "Start with making a Scanner variable to take inputs from the user";
		if (!findStmt("System.out.$p($x)", () -> matches("$p", "print(ln)?") && matches("$x", ".*score.*")))
			return "Ask the user to enter a collection of scores";
		if (!findStmt("System.out.$p($x)", () -> matches("$p", "print(ln)?")
				&& (matches("$x", ".*negative.*") || matches("$x", "<\\s*0") || matches("$x", "-1"))))
			return "Tell the user that a negative number signifies the end";
		if(!(findVar("$t[]", "$x")))
		  return "Probably you'll need an array";
		// if(findVar("$t[]", "$var", () -> !matches("$t", "int")))
		// return "Make sure to use an int array.";
		// if(findExpr("new int[$n]", () -> !matches("$n", "100")))
		// return "The max size of the array should be 100.";

		return null;
	}

}
