package javatutor.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

import javatutor.engine.Matching.Match;

public class Engine {
	static class Rule {
		private String message;
		private Block studentPattern;
		private Block correctPattern;

		public Rule(String student, String correct, String message) {
			this.studentPattern = parseStatements(student);
			this.correctPattern = parseStatements(correct);
			this.message = message;
		}

		public Optional<Suggestion> match(ASTNode student, ASTNode correct) {
			List<Suggestion> candidates = new ArrayList<>();
			for (Match mc : Matching.find(correctPattern, correct, Optional.empty())) {
				for (Match ms : Matching.find(studentPattern, student, Optional.of(mc.bindings))) {
					String message = this.message;
					for (Entry<String, ASTNode> e : ms.bindings.entrySet()) {
						message = message.replaceAll(Pattern.quote('$' + e.getKey()), e.getValue().toString());
					}
					candidates.add(new Suggestion(message));
				}
			}
			if (candidates.size() == 1)
				return Optional.of(candidates.get(0));
			return Optional.empty();
		}
	}

	static Block parseStatements(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS13);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setStatementsRecovery(true);
		return (Block) parser.createAST(null);
	}

	List<Rule> rules = new ArrayList<>();

	public void addRule(String student, String correct, String message) {
		rules.add(new Rule(student, correct, message));
	}

	public Optional<Suggestion> suggest(String student, String correct) {
		Block studentTree = parseStatements(student);
		Block correctTree = parseStatements(correct);
		List<Suggestion> suggestions = new ArrayList<>();
		for (Rule rule : rules) {
			rule.match(studentTree, correctTree).ifPresent(suggestions::add);
		}
		if (suggestions.size() > 0) return Optional.of(suggestions.get(0));
		return Optional.empty();
	}

}
