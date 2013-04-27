package no.uib.inf142.assignment3.rip.common;

public enum Signal {

	REGULAR('R'),
	PARTIAL('P'),

	SYN('S'),
	ACK('A'),
	SYNACK('B'),
	FIN('F');

	private char c;

	private Signal(char c) {
		this.c = c;
	}

	public char getChar() {
		return c;
	}

	public String getString() {
		return "" + c;
	}
}
