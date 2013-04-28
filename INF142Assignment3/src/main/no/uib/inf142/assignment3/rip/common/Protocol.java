package no.uib.inf142.assignment3.rip.common;

public class Protocol {

	public static final String PACKET_DELIMITER = ";";

	public static final int PACKET_LENGTH = 30;
	public static final int CHECKSUM_LENGTH = 3;

	public static final int WINDOW_SIZE = 10;
	public static final int TIMEOUT_IN_MILLIS = 5000;
	public static final int WAITTIME_IN_MILLIS = 10;

	public static final int SERVER_LISTENING_PORT = 55555;
	public static final int RELAY_LISTENING_PORT = 11111;
	public static final int RELAY_SENDING_PORT = 11112;
}
