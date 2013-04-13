package no.uib.inf142.assignment3.rip;

public enum Signal {

	NONE('0'),
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