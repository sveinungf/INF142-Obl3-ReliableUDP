package no.uib.inf142.assignment3.rip.client;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import no.uib.inf142.assignment3.rip.ProtocolConstants;
import no.uib.inf142.assignment3.rip.Signal;
import no.uib.inf142.assignment3.rip.common.PacketUtils;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class RIPPacketGenerator {

	private static final String DELIMITER = ";";

	private InetSocketAddress finalDestination;
	private InetSocketAddress relay;
	private int seq;

	public RIPPacketGenerator(InetSocketAddress finalDestination,
			InetSocketAddress relay) {

		this.finalDestination = finalDestination;
		this.relay = relay;
		seq = 0;
	}

	private String makeHeaderString() {
		String ip = finalDestination.getAddress().getHostAddress();
		String port = "" + finalDestination.getPort();
		String seqString = "" + seq;
		++seq;

		return buildDelimitedString(ip, port, seqString);
	}

	public DatagramPacket makePacket(final Signal signal)
			throws TooShortPacketLengthException, SocketException {

		int maxPacketLength = ProtocolConstants.PACKET_LENGTH;
		String header = makeHeaderString();
		String payload = buildDelimitedString(header, signal.getString());
		byte[] byteData = payload.getBytes();
		int dataLength = byteData.length;

		if (dataLength > maxPacketLength) {
			throw new TooShortPacketLengthException("Packet length "
					+ maxPacketLength + " too short");
		}

		return new DatagramPacket(byteData, dataLength, relay);
	}

	public List<DatagramPacket> makePackets(final String data)
			throws TooShortPacketLengthException, SocketException {

		List<DatagramPacket> packetList = new ArrayList<DatagramPacket>();
		int maxPacketLength = ProtocolConstants.PACKET_LENGTH;
		int signalSpace = 2;
		int checksumSpace = 3;
		String header = makeHeaderString();

		int spaceLeft = maxPacketLength - header.length() - signalSpace
				- checksumSpace;

		if (spaceLeft <= 0) {
			throw new TooShortPacketLengthException("Packet length "
					+ maxPacketLength + " too short");
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
			//TODO generate checksum
			String checksum = "X";

			String payload = buildDelimitedString(header, signal, checksum,
					packetData);
			byte[] byteData = payload.getBytes();
			DatagramPacket packet = new DatagramPacket(byteData,
					byteData.length, relay);
			packetList.add(packet);
		}

		return packetList;
	}

	private static String buildDelimitedString(String... values) {
		return PacketUtils.buildDelimitedString(DELIMITER, values);
	}
}