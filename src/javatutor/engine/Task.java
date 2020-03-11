package javatutor.engine;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

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
		return hint.map(text -> new Hint(text, currentMatch, empty()));
	}

	protected abstract String getHint();

	protected boolean findStmt(String pattern) {
		return findStmt(pattern, () -> true);
	}

	protected boolean findExpr(String pattern, Supplier<Boolean> condition) {
		return firstMatch(Matching.findExpr(pattern, currentInput, empty()), condition);
	}

	protected boolean findStmt(String pattern, Supplier<Boolean> condition) {
		return firstMatch(Matching.findStmt(pattern, currentInput, empty()), condition);
	}

	/*protected boolean findVar(String type, String name) {
		return findVar(type, name, () -> true);
	}

	protected boolean findVar(String type, String name, String initialValue) {
		return findVar(type, name, initialValue, () -> true);
	}

	protected boolean findVar(String type, String name, Supplier<Boolean> condition) {
		return firstMatch(Matching.findVar(type, name, currentInput, empty()), condition);
	}

	protected boolean findVar(String type, String name, String initialValue, Supplier<Boolean> condition) {
		return firstMatch(Matching.findVar(type, name, initialValue, currentInput, empty()), condition);
	}*/

	protected boolean matches(String varName, String regexp) {
		return getVar(varName, "matches").toString().matches(regexp);
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
