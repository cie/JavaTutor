package javatutor.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class Matching {

	public static class InvalidPatternException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public final String pattern;

		public InvalidPatternException(String pattern) {
			super("Invalid pattern: " + pattern);
			this.pattern = pattern;
		}
	}

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

	public static class Match {
		public Map<String, ASTNode> bindings;
		public ASTNode matchedNode;

		public Match(Optional<Map<String, ASTNode>> bindings) {
			this.bindings = bindings.orElse(new HashMap<>());
		}

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
		
		public Map<String, ASTNode> getBindings() {
			return bindings;
		}

	}

	private static class PatternMatcher extends ASTMatcher {
		private static final Pattern VARIABLE_LIKE = Pattern.compile("\\$.*");
		private static final Pattern WILDCARD_TYPED_VARIABLE = Pattern.compile("\\$([A-Z][a-zA-Z]*)");
		private static final Pattern TYPED_VARIABLE = Pattern.compile("\\$(([A-Z][a-zA-Z]*)[0-9]+)");
		private static final Pattern UNTYPED_VARIABLE = Pattern.compile("\\$([a-z][a-zA-Z0-9]*)");
		private final Match match;

		public PatternMatcher(Optional<Map<String, ASTNode>> bindings) {
			match = new Match(bindings);
		}

		public Match getMatch() {
			return match;
		}

		@Override
		public boolean match(SimpleName node, Object other) {
			String name = node.getIdentifier();
			if (!VARIABLE_LIKE.matcher(name).matches())
				return super.match(node, other);
			Matcher m;
			if ((m = WILDCARD_TYPED_VARIABLE.matcher(name)).matches()) {
				String varName = m.group(1);
				return findASTNodeClass(varName).isInstance(other);
			}
			if ((m = TYPED_VARIABLE.matcher(name)).matches()) {
				String varName = m.group(1), typeName = m.group(2);
				if (!findASTNodeClass(typeName).isInstance(other))
					return false;
				ASTNode otherNode = (ASTNode) other;
				if (!match.bindings.containsKey(varName)) {
					match.bindings.put(varName, otherNode);
					return true;
				} else {
					// check equality with the stored value
					return match.bindings.get(varName).subtreeMatch(new ASTMatcher(), otherNode);
				}
			}
			if ((m = UNTYPED_VARIABLE.matcher(name)).matches()) {
				String varName = m.group(1);
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
		public boolean match(SimpleType node, Object other) {
			// We need to treat types specially: `$x` is a SimpleType and `int` is not, so
			// the default ASTMatcher does not let them through.
			String name = node.toString();
			if (!VARIABLE_LIKE.matcher(name).matches())
				return super.match(node, other);
			// if a variable
			// match any type, not only SimpleType
			if (!(other instanceof Type))
				return false;
			return match(node.getAST().newSimpleName(name), other);
		}
		
		@Override
		public boolean match(MethodInvocation node, Object other) {
			// TODO Auto-generated method stub
			return super.match(node, other);
		}

		@Override
		public boolean match(Assignment node, Object other) {
			return matchAssignmentVariable(node, other) || super.match(node, other);
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
			if (node.getExpression() instanceof Assignment) {
				return matchAssignmentVariable((Assignment) node.getExpression(), other);
			}
			return false;
		}

		private boolean matchAssignmentVariable(Assignment a, Object other) {
			// this happens if a single identifier is there instead of a statement
			if (!(a.getRightHandSide() instanceof SimpleName))
				return false;
			SimpleName right = (SimpleName) a.getRightHandSide();
			// JDT parser uses the id $missing$ in this case
			if (!right.getIdentifier().equals("$missing$"))
				return false;
			if (!(a.getLeftHandSide() instanceof SimpleName))
				return false;
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
			return (Class<? extends ASTNode>) clazz;
		}
	}

	public static Optional<Match> match(ASTNode pattern, ASTNode concrete, Optional<Map<String, ASTNode>> bindings)
			throws UnknownNodeTypeException {
		PatternMatcher matcher = new PatternMatcher(bindings);
		if (pattern.subtreeMatch(matcher, concrete)) {
			Match match = matcher.getMatch();
			match.matchedNode = concrete;
			return Optional.of(match);
		}
		return Optional.empty();
	}
	
	public static List<Match> findStmt(String pattern, ASTNode concrete, Optional<Map<String, ASTNode>> bindings) {
		return findNode(parseStatement(pattern), concrete, bindings);
	}

	public static List<Match> findExpr(String pattern, ASTNode concrete, Optional<Map<String, ASTNode>> bindings) {
		return findNode(parseExpr(pattern), concrete, bindings);
	}

	private static List<Match> findNode(ASTNode pattern, ASTNode haystack, Optional<Map<String, ASTNode>> bindings) {
		List<Match> matches = new ArrayList<>();
		haystack.accept(new ASTVisitor() {
			@Override
			public void preVisit(ASTNode node) {
				match(pattern, node, bindings).ifPresent(matches::add);
			}
		});
		return matches;
	}
			

	
	private static List<Match> findConsecutiveStatements(Block pattern, ASTNode haystack, Optional<Map<String, ASTNode>> bindings) {
		List<Match> matches = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Statement> patternStatements = pattern.statements();
		haystack.accept(new ASTVisitor() {
			@Override
			public boolean visit(Block block) {
				@SuppressWarnings("unchecked")
				List<Statement> statements = block.statements();

				// find a matching sublist of statements
				for (int start = 0; start < statements.size() - patternStatements.size() + 1; ++start) {
					for (int i = 0; i < patternStatements.size(); ++i) {
						match(patternStatements.get(i), statements.get(start + i), bindings)
								.ifPresent(matches::add); // FIXME only add match after the  inner loop
					}
					// here
				}
				return true;
			}
		});
		return matches;
	}

	private static List<Match> findVariableDeclaration(VariableDeclarationStatement statement, ASTNode haystack,
			Optional<Map<String, ASTNode>> bindings) {
		if (statement.fragments().size() > 1) {
			throw new InvalidPatternException("Unsupported pattern: Variable declaration with multiple fragments.");
		}
		// If we search for int x = 5;  then we also accept  int y, x = 5, z;  and TODO maybe int x, y; x = 5; too.
		// TODO If we search for int x; then also accept int y, x = 5, z;
		List<Match> matches = new ArrayList<>();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		haystack.accept(new ASTVisitor() {
			@Override
			public void preVisit(ASTNode node) {
				match(fragment, node, bindings).ifPresent(matches::add);
			}
		});
		return matches;
	}

	public static Block parseStatements(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS13);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setStatementsRecovery(true);
		Block block = (Block) parser.createAST(null);
		if (block.statements().size() == 0)  {
			throw new InvalidPatternException("Empty pattern");
		}
		return block;
	}

	private static Statement parseStatement(String source) {
		Block block = parseStatements(source);
		if (block.statements().size() > 1)  {
			throw new InvalidPatternException("Expected a single statement, but found more than one.");
		}
		return (Statement) block.statements().get(0);
	}

	public static Expression parseExpr(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS13);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setStatementsRecovery(true);
		return (Expression) parser.createAST(null);
	}

}
