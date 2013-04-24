package no.uib.inf142.assignment3.rip.common;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import no.uib.inf142.assignment3.rip.ProtocolConstants;
import no.uib.inf142.assignment3.rip.Signal;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

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

	public List<DatagramPacket> makeDatagramPackets() throws SocketException,
			TooShortPacketLengthException {
		List<DatagramPacket> packetList = new ArrayList<DatagramPacket>();

		String ip = finalDestination.getAddress().getHostAddress();
		String port = "" + finalDestination.getPort();

		String header = buildDelimitedString(ip, port, "" + seq, "" + ack);
		int signalSpace = signal.getString().getBytes().length;
		int partialSignalSpace = Signal.PARTIAL.getString().getBytes().length;
		int maxSignalSpace = Math.max(signalSpace, partialSignalSpace) + 2; // semicolons

		byte[] headerData = header.getBytes();
		int spaceLeft = ProtocolConstants.PACKET_LENGTH - headerData.length
				- maxSignalSpace;

		System.out.println("space left: " + spaceLeft);

		if (spaceLeft <= 0) {
			throw new TooShortPacketLengthException("Packet length "
					+ ProtocolConstants.PACKET_LENGTH + " too short");
		}

		String dataLeft = data;
		boolean done = false;

		while (!done) {
			String packetData;
			String signal;

			if (dataLeft.length() > spaceLeft) {
				packetData = dataLeft.substring(0, spaceLeft);
				dataLeft = dataLeft.substring(spaceLeft);
				signal = Signal.PARTIAL.getString();
			} else {
				packetData = dataLeft;
				signal = Signal.REGULAR.getString();
				done = true;
			}
			// make checksum
			String payload = buildDelimitedString(header, signal, packetData);
			byte[] byteData = payload.getBytes();
			DatagramPacket packet = new DatagramPacket(byteData,
					byteData.length, destination);
			packetList.add(packet);
		}

		return packetList;
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

	public static String buildDelimitedString(String... values) {
		return RIPUtils.buildDelimitedString(DELIMITER, values);
	}
}
