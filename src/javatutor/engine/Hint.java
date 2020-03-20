package javatutor.engine;

import java.util.Date;
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
		if (delay != other.delay) return false;
		return true;
	}

	public String message;
	public int delay = 10000;
	public Optional<Match> studentMatch;
	public Optional<Match> correctMatch;

	public Hint(String message, Optional<Match> studentMatch, Optional<Match> correctMatch) {
		this.message = message;
		if (message.startsWith("!")) {
			this.message = message.substring(1);
			this.delay = 3000;
		}
		this.studentMatch = studentMatch;
		this.correctMatch = correctMatch;
		
	}
	
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "Suggestion [message=" + message + "]";
	}
	

}