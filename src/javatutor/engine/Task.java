package javatutor.engine;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;

import javatutor.engine.Matching.Match;

public abstract class Task {
	public class TaskException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public TaskException(String message) {
			super(message);
		}
	}

	// current context of the DSL functions
	private ASTNode currentInput;
	private Optional<Match> currentMatch = empty();

	public synchronized Optional<Hint> generateHint(CompilationUnit ast) {
		if (ast == null)
			return empty();
		currentInput = ast;
		currentMatch = empty();
		Optional<String> hint = Optional.ofNullable(getHint());
		hint = hint.map(s -> resolveTemplate(s, currentMatch));
		return hint.map(text -> new Hint(text, currentMatch, empty()));
	}

	private static String resolveTemplate(String s, Optional<Match> match) {
		if (!match.isPresent())
			return s;
		Map<String, ASTNode> bindings = match.get().bindings;
		Matcher m = Matching.VARIABLE.matcher(s);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb,
					Optional.ofNullable(bindings.get(m.group(1))).map(ASTNode::toString).orElse(m.group(1)));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	protected abstract String getHint();

	protected boolean findStmt(String pattern) {
		return findStmt(pattern, () -> true);
	}

	protected boolean findStmt(String pattern, Supplier<Boolean> condition) {
		return firstMatch(Matching.findStmt(pattern, currentInput, empty()), condition);
	}

	protected boolean findExpr(String pattern) {
		return findExpr(pattern, () -> true);
	}

	protected boolean findExpr(String pattern, Supplier<Boolean> condition) {
		return firstMatch(Matching.findExpr(pattern, currentInput, empty()), condition);
	}

	protected boolean findVar(String type, String name) {
		return findVar(type, name, () -> true);
	}

	protected boolean findVar(String type, String name, Supplier<Boolean> condition) {
		return firstMatch(Matching.findVar(type, name, currentInput, empty()), condition);
	}

	protected boolean matches(String varName, String regexp) {
		return getVar(varName, "matches").toString().matches(regexp);
	}

	protected boolean isExpr(String varName, String pattern) {
		return isExpr(varName, pattern, () -> true);
	}
	protected boolean isExpr(String varName, String pattern, Supplier<Boolean> condition) {
		 Optional<Match> match = Matching.match(Matching.parseExpr(pattern), getVar(varName, "is"), of(currentMatch.get().bindings));
		 if (!match.isPresent()) return false;
		 Optional<Match> lastMatch = currentMatch;
		 try {
			 currentMatch = match;
			 return condition.get();
		 } finally {
			 currentMatch = lastMatch;
		 }
	}

	protected boolean isOfType(String varName, String typeName) {
		ASTNode var = getVar(varName, "matches");
		if (!(var instanceof Expression))
			throw new TaskException("isOfType: Variable " + varName + "must refer to an expression.");
		return ((SimpleName) var).resolveTypeBinding().getName().equals(typeName);
	}

	private boolean firstMatch(List<Match> matches, Supplier<Boolean> condition) {
		for (Match match : matches) {
			currentMatch = of(match);
			if (condition.get()) {
				return true;
			}
		}
		currentMatch = empty();
		return false;
	}

	private static final Pattern VARIABLE_LIKE = Pattern.compile("\\$.*");

	private ASTNode getVar(String varName, String method) {
		if (!currentMatch.isPresent()) {
			throw new TaskException(method + "() called without a successful find() before");
		}
		if (!VARIABLE_LIKE.matcher(varName).matches()) {
			throw new TaskException("first argument of " + method + "() must be a variable, like $x");
		}
		ASTNode node = currentMatch.get().bindings.get(varName.substring(1));
		if (node == null) {
			throw new TaskException("Variable " + varName + " is not bound.");
		}
		return node;
	}

}
