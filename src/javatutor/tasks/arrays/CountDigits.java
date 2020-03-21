package javatutor.tasks.arrays;

import javatutor.engine.HintGenerator;

public class CountDigits extends HintGenerator {

	@Override
	protected String getHint() {
		if (!findVar("int[]", "$x"))
			return "Let's start with an array of <code>int</code>.";
		if (findVar("int[]", "$x", "new int[$n]") && !isExpr("$n", "10"))
			return "!Make sure the length of the array is 10.";
		if (!findStmt("for($t $a = $s;$b;$c)$stmt"))
			return "Make a loop.";
		if (findStmt("for($t $i = $s;$i < 10;$c)$stmt") ||
				findStmt("for($t $i = $s;$i < $arr.length;$c)$stmt"))
			return "!The loop should not go through the array.";
		if (!findExpr("Math.random()")) 
			return "Remember, to generate random numbers, you can use <code>Math.random()</code> function. It will give double numbers between <code>0.0</code> and <code>1.0</code>.";
		if (!(findExpr("Math.random() * 10") || findExpr("10 * Math.random()"))) 
			return "To generate random numbers within 0 to 9, you can write <code>Math.random() * 10</code>.";
		if (!(findExpr("(int)(Math.random() * 10)") || findExpr("(int)(10 * Math.random())"))) 
			return "We need integers, so we can use casting (<code>(int)number</code>) to turn double to integer.";
		return null;
	}

}
