package no.uib.inf142.assignment3.rip;

public class RIPPacketData {

	public static final char SEPARATOR = ';';

	public static String prependSignal(final Signal signal, final String data) {
		return signal.getChar() + SEPARATOR + data;
	}
}
