package javatutor.engine;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Engine {
	static class Rule {
		private String message;
		private String student;
		private Block studentTree;
		private Block correctTree;

		public Rule(String student, String correct, String message) {
			this.studentTree = parseStatements(student);
			this.correctTree = parseStatements(message);
			this.message = message;
		}

		public boolean matches(String student, String correct) {
			Block studentTree = parseStatements(student);
			Block correctTree = parseStatements(correct);
			return this.student.equals(student);
		}
	}
	
	static Block parseStatements(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS13);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setStatementsRecovery(true);
		return  (Block) parser.createAST(null);
	}

	List<Rule> rules = new ArrayList<>();

	public void addRule(String student, String correct, String message) {
		rules.add(new Rule(student, correct, message));
	}

	public Suggestion suggest(String student, String correct) {
		if (rules.get(0).matches(student, correct))
			return new Suggestion(rules.get(0).message);
		return null;
	}

}
