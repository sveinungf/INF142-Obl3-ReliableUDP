package no.uib.inf142.assignment3.rip.common;

public enum Signal {

	REGULAR("R"),
	PARTIAL("P"),

	SYN("S"),
	ACK("A"),
	SYNACK("B"),
	FIN("F");

	private String string;

	private Signal(String string) {
		this.string = string;
	}

	public String getString() {
		return string;
	}
}
