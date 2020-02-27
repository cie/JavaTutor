package javatutor.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.SimpleName;

public class Matching {

	public static class InvalidVariableException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public final String varName;
		public InvalidVariableException(String varName) {
			super("Invalid variable: " + varName);
			this.varName = varName;
		}
	}

	public static class UnknownNodeTypeException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public final String typeName;

		public UnknownNodeTypeException(String typeName) {
			super("Unknown node type: " + typeName);
			this.typeName = typeName;
		}

	}

	static class Match {
		Map<String, ASTNode> bindings = new HashMap<>();

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Match other = (Match) obj;
			if (!bindings.equals(other.bindings))
				return false;
			return true;
		}
		

	}

	private static class PatternMatcher extends ASTMatcher {
		private static final Pattern VARIABLE_LIKE = Pattern.compile("\\$.*");
		private static final Pattern WILDCARD_VARIABLE = Pattern.compile("\\$([a-zA-Z]+)");
		private static final Pattern NORMAL_VARIABLE = Pattern.compile("\\$(([a-zA-Z]+)[0-9]+)");
		private final Match match = new Match();

		public Match getMatch() {
			return match;
		}

		@Override
		public boolean match(SimpleName node, Object other) {
			String name = node.getIdentifier();
			if (!VARIABLE_LIKE.matcher(name).matches()) return super.match(node, other);
			Matcher m;
			if ((m = WILDCARD_VARIABLE.matcher(name)).matches()) {
				return findASTNodeClass(m.group(1)).isInstance(other);
			}
			if ((m = NORMAL_VARIABLE.matcher(name)).matches()) {
				String varName = m.group(1), typeName = m.group(2);
				if (!findASTNodeClass(typeName).isInstance(other)) return false;
				ASTNode otherNode = (ASTNode) other;
				if (!match.bindings.containsKey(varName)) {
					match.bindings.put(varName, otherNode);
					return true;
				} else {
					// check equality with the stored value
					return match.bindings.get(varName).subtreeMatch(new ASTMatcher(), otherNode);
				}
			}
			throw new InvalidVariableException(name);
		}
		@Override
		public boolean match(ExpressionStatement node, Object other) {
			return matchStatementVariable(node, other) || super.match(node, other);
		}

		private boolean matchStatementVariable(ExpressionStatement node, Object other) {
			if (node.getExpression() instanceof SimpleName) {
				SimpleName name = (SimpleName) node.getExpression();
				return match(name, other);
			}
			if (!(node.getExpression() instanceof Assignment)) return false;
			Assignment a = (Assignment) node.getExpression();
			if (!(a.getRightHandSide() instanceof SimpleName)) return false;
			SimpleName right = (SimpleName) a.getRightHandSide();
			if (!right.getIdentifier().equals("$missing$")) return false;
			// this happens if a single identifier is there instead of a statement
			if (!(a.getLeftHandSide() instanceof SimpleName)) return false;
			SimpleName left = (SimpleName) a.getLeftHandSide();
			// continue as if only the left hand side were there
			return match(left, other);
		}

		@SuppressWarnings("unchecked")
		private static Class<? extends ASTNode> findASTNodeClass(String typeName) {
			Class<?> clazz;
			try {
				clazz = PatternMatcher.class.getClassLoader().loadClass("org.eclipse.jdt.core.dom." + typeName);
			} catch (ClassNotFoundException e) {
				throw new UnknownNodeTypeException(typeName);
			}
			if (!ASTNode.class.isAssignableFrom(clazz)) {
				throw new UnknownNodeTypeException(typeName);
			}
			return (Class <? extends ASTNode>) clazz;
		}


	}

	public static Match match(ASTNode pattern, ASTNode concrete) throws UnknownNodeTypeException {
		PatternMatcher matcher = new PatternMatcher();
		return pattern.subtreeMatch(matcher, concrete) ? matcher.getMatch() : null;
	}

}
