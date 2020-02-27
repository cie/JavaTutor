package javatutor.engine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.SimpleName;

import javatutor.engine.Matching.UnknownNodeTypeException;

public class Matching {

	public static class UnknownNodeTypeException extends RuntimeException {

		public final String typeName;

		public UnknownNodeTypeException(String typeName) {
			super("Unknown node type: " + typeName);
			this.typeName = typeName;
		}

	}

	static class Match {
		
		int position;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + position;
			return result;
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
			if (position != other.position)
				return false;
			return true;
		}
		
	}
	
	private static class PatternMatcher extends ASTMatcher {
		static final Pattern WILDCARD_VARIABLE = Pattern.compile("\\$([a-zA-Z]+)");
		Match match = new Match();

		@Override
		public boolean match(SimpleName node, Object other) {
			Matcher m;
			if ((m = WILDCARD_VARIABLE.matcher(node.getIdentifier())).matches()) {
				String typeName = m.group(1);
				Class<?> clazz;
				try {
					clazz = getClass().getClassLoader().loadClass("org.eclipse.jdt.core.dom." + typeName);
				} catch (ClassNotFoundException e) {
					throw new UnknownNodeTypeException(typeName);
				}
				return clazz.isInstance(other);
			}
			return super.match(node, other);
		}
		
		public Match getMatch() {
			return new Match();
		}
		
	}
	

	public static Match match(ASTNode pattern, ASTNode concrete) throws UnknownNodeTypeException {
		PatternMatcher matcher = new PatternMatcher();
		return pattern.subtreeMatch(matcher, concrete) ? matcher.getMatch() : null;
	}

}
