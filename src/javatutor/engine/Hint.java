package javatutor.engine;

import java.util.Optional;

import javatutor.engine.Matching.Match;

public class Hint {

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Hint other = (Hint) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}

	public String message;
	public Optional<Match> studentMatch;
	public Optional<Match> correctMatch;

	public Hint(String message, Optional<Match> studentMatch, Optional<Match> correctMatch) {
		this.message = message;
		this.studentMatch = studentMatch;
		this.correctMatch = correctMatch;
		
	}

	@Override
	public String toString() {
		return "Suggestion [message=" + message + "]";
	}
	
	
	
}