package javatutor.tasks.arrays.above_average;

import javatutor.engine.Task;

public class AboveBelowAverageTask extends Task {

	@Override
	protected String getHint() {
		if (!findExpr("new Scanner($x)"))
			return "Start with making a Scanner variable to take inputs from the user";

		if (!findStmt("System.out.$p($x)", () -> matches("$p", "print(ln)?") && matches("$x", ".*score.*")))
			return "Ask the user to enter a collection of scores";
		if (!findStmt("System.out.$p($x)", () -> matches("$p", "print(ln)?")
				&& (matches("$x", ".*negative.*") || matches("$x", "<\\s*0") || matches("$x", "-1"))))
			return "Tell the user that a negative number signifies the end";

		if (!(findVar("$t[]", "$x")))
			return "Probably you'll need an array";
		if (findVar("$t[]", "$x", () -> !matches("$t", "int")))
			return "Make sure you use an <tt>int</tt> array.";
		if (findVar("int[]", "$missing$"))
			return "You could call it <tt>scores</tt>";

		if (findExpr("new int[$n]", () -> !matches("$n", "100")))
			return "The max size of the array should be 100.";
		
		if (findStmt("for($t $i = $x;$b;$c) $stmt;")) {
			if (!matches("$t", "int")) return "Loop variable should be an <tt>int</tt>.";
			if (isExpr("$b", "$i < $max", () -> !matches("$max", "100"))) {
				return "Loop should go to 100";
			}
			//if (within("$stmt", () -> findVar())) {
				
			//}
		} else if (findStmt("while($x) $stmt;")) {
			return "Please use a for loop for now.";
		} else if (findStmt("do $stmt while ($x)")) {
			return "Please use a for loop for now.";
		} else return "You might need a loop...";

		if (!findVar("Scanner", "$x") && findExpr("new Scanner($x)"))
			return "You should save the Scanner instance to a variable";
		if (!findExpr("$scanner.nextInt()", () -> isOfType("$scanner", "Scanner")) && findVar("Scanner", "$s"))
			return "Use <tt>$scanner.nextInt()</tt> to read numbers from the user.";
		if (findVar("Scanner", "$scanner", () -> findStmt("$scanner.nextInt()")))
			return "Save <tt>$scanner.nextInt()</tt> to a variable.";


		return null;
	}

}
