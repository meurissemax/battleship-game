/**
 * This class is used to generate custom exceptions, particularly HTTP exceptions.
 *
 * @author Maxime Meurisse & Valentin Vermeylen
 * @version 2019.04.20
 */

public class HTTPException extends Exception {
	private static final long serialVersionUID = 123456L;

	public HTTPException() {
		super();
	}

	public HTTPException(String message) {
		super(message);
	}
}
