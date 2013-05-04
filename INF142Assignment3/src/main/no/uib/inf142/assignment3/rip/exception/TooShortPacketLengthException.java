package no.uib.inf142.assignment3.rip.exception;

public class TooShortPacketLengthException extends Exception {

	private static final long serialVersionUID = 1L;

	public TooShortPacketLengthException() {
		super();
	}

	public TooShortPacketLengthException(final String msg) {
		super(msg);
	}
}
