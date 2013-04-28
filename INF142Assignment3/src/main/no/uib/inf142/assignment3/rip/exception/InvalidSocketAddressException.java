package no.uib.inf142.assignment3.rip.exception;

public class InvalidSocketAddressException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidSocketAddressException() {
		super();
	}

	public InvalidSocketAddressException(String msg) {
		super(msg);
	}
}
