package javatutor.engine;

public class Suggestion {

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Suggestion other = (Suggestion) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}

	public String message;

	public Suggestion(String message) {
		this.message = message;
		
	}

	@Override
	public String toString() {
		return "Suggestion [message=" + message + "]";
	}
	
	
	
}