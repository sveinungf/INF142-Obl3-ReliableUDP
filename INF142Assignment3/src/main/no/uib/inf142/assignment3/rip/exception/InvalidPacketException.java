package no.uib.inf142.assignment3.rip.exception;

public class InvalidPacketException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidPacketException() {
		super();
	}

	public InvalidPacketException(String msg) {
		super(msg);
	}
}
