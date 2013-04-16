package no.uib.inf142.assignment3.rip.common;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;

import no.uib.inf142.assignment3.rip.ProtocolConstants;
import no.uib.inf142.assignment3.rip.Signal;

public class RIPPacket {

	private static final String DELIMITER = ";";

	private InetSocketAddress destination;
	private InetSocketAddress finalDestination;
	private Signal signal;
	private int seq;
	private int ack;
	private String data;

	public RIPPacket(InetSocketAddress dest, InetSocketAddress finalDest,
			Signal signal) {

		destination = dest;
		finalDestination = finalDest;
		this.signal = signal;
		seq = 0;
		ack = 0;
		data = "";
	}

	public RIPPacket(InetSocketAddress dest, InetSocketAddress finalDest,
			Signal signal, String data) {

		this(dest, finalDest, signal);
		this.data = data;
	}

	public RIPPacket(InetSocketAddress dest, InetSocketAddress finalDest,
			Signal signal, int seq, int ack, String data) {

		this(dest, finalDest, signal, data);
		this.seq = seq;
		this.ack = ack;
	}

	public static String buildDelimitedString(String... values) {
		StringBuilder sb = new StringBuilder();

		if (values != null && values.length > 0) {
			sb.append(values[0]);

			for (int i = 1; i < values.length; ++i) {
				sb.append(DELIMITER);
				sb.append(values[i]);
			}
		}

		return sb.toString();
	}

	public List<DatagramPacket> makeDatagramPackets() {
		String ip = finalDestination.getAddress().getHostAddress();
		String port = "" + finalDestination.getPort();

		String header = buildDelimitedString(ip, port, "" + seq, "" + ack);
		int signalSpace = signal.getString().getBytes().length;
		int partialSignalSpace = Signal.PARTIAL.getString().getBytes().length;
		int maxSignalSpace = Math.max(signalSpace, partialSignalSpace) + 2; // semicolons

		byte[] headerData = header.getBytes();
		int spaceLeft = ProtocolConstants.PACKET_LENGTH - headerData.length
				- maxSignalSpace;
		// make checksum

		return null;
	}

	public static DatagramPacket makePacket(InetSocketAddress dest,
			InetSocketAddress finalDest, String data) throws SocketException {

		String ip = finalDest.getAddress().getHostAddress();
		String port = "" + finalDest.getPort();

		String payload = buildDelimitedString(ip, port, data);
		byte[] byteData = payload.getBytes();

		return new DatagramPacket(byteData, byteData.length, dest);
	}

	public static DatagramPacket makePacket(InetSocketAddress dest,
			InetSocketAddress finalDest, Signal signal, String data)
			throws SocketException {

		String payload = buildDelimitedString(signal.getString(), data);

		return makePacket(dest, finalDest, payload);
	}

	public static DatagramPacket makePacket(InetSocketAddress dest,
			InetSocketAddress finalDest, Signal signal, int seq, int ack,
			String checksum, String data) throws SocketException {

		String seqString = "" + seq;
		String ackString = "" + ack;

		String payload = buildDelimitedString(seqString, ackString, checksum,
				data);

		return makePacket(dest, finalDest, signal, payload);
	}
}
