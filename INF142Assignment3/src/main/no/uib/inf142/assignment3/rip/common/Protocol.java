package no.uib.inf142.assignment3.rip.common;

import java.nio.charset.Charset;

public class Protocol {

	/**
	 * The charset used when converting to and from byte arrays.
	 */
	public static final Charset CHARSET = Charset.forName("UTF-8");

	/**
	 * The delimiter which separates the datafields in the packetdata.
	 */
	public static final String DATAFIELD_DELIMITER = ";";

	/**
	 * The length of the data in a packet.
	 */
	public static final int PACKETDATA_LENGTH = 64;

	/**
	 * The length of the checksum datafield in the packetdata.
	 */
	public static final int CHECKSUM_LENGTH = 3;

	/**
	 * The length of the sequence datafield in the packetdata.
	 */
	public static final int SEQUENCE_LENGTH = PacketUtils.MAX_HEX_LENGTH;

	/**
	 * The length of the IP datafield in the packetdata.
	 */
	public static final int MAX_IP_LENGTH = 16;

	/**
	 * The length of the port datafield in the packetdata.
	 */
	public static final int MAX_PORT_LENGTH = 5;

	/**
	 * The number which the sequence on each end will start on.
	 */
	public static final int SEQUENCE_START = 0;

	/**
	 * Size of the window in Go-Back-N. Determines the maximum amount of
	 * outstanding unacknowledged packets there may be.
	 */
	public static final int WINDOW_SIZE = 10;

	/**
	 * The minimum time in milliseconds a sender will wait before it tries to
	 * resend a packet.
	 */
	public static final long SENDER_TIMEOUT = 1000;

	/**
	 * The minimum time in milliseconds the server will wait after receiving a
	 * packet, before it closes the connection.
	 */
	public static final long SERVER_TIMEOUT = 60000;

	/**
	 * The minimum time in milliseconds the client will wait after sending the
	 * last ACK in connection tear-down, before closing.
	 */
	public static final long FIN_TIME_WAIT = 5000;

	/**
	 * The maximum number of attempts a sender will send a packet before giving
	 * up.
	 */
	public static final int CONNECTION_ATTEMPTS = 3;

	/**
	 * The default port which the server listens on.
	 */
	public static final int SERVER_LISTENING_PORT = 55555;

	/**
	 * The default port which the relay listens on.
	 */
	public static final int RELAY_LISTENING_PORT = 11111;
}
