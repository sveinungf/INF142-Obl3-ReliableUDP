package no.uib.inf142.assignment3.rip.common;

public enum Datafield {

	IP(Protocol.MAX_IP_LENGTH),
	PORT(Protocol.MAX_PORT_LENGTH),
	SEQUENCE(Protocol.SEQUENCE_LENGTH),
	SIGNAL(SignalMap.getInstance().getMaxSignalLength()),
	CHECKSUM(Protocol.CHECKSUM_LENGTH);

	private int length;

	private Datafield(int length) {
		this.length = length;
	}

	public int getLength() {
		return length;
	}

	public static int calculateDataLength() {
		int delimiterLength = Protocol.PACKET_DELIMITER.length();
		int dataLength = Protocol.MAX_PACKET_LENGTH;

		for (Datafield datafield : Datafield.values()) {
			dataLength -= datafield.getLength();
			dataLength -= delimiterLength;
		}

		return dataLength;
	}
}
