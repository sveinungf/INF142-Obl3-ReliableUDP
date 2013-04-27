package no.uib.inf142.assignment3.rip.client;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import no.uib.inf142.assignment3.rip.common.PacketUtils;
import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.Signal;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class RIPPacketGenerator {

	private InetSocketAddress finalDestination;
	private InetSocketAddress relay;
	private int nextSequence;

	public RIPPacketGenerator(InetSocketAddress finalDestination,
			InetSocketAddress relay) {

		this.finalDestination = finalDestination;
		this.relay = relay;
		nextSequence = 0;
	}

	private String buildHeaderString() {
		String ip = finalDestination.getAddress().getHostAddress();
		String port = "" + finalDestination.getPort();
		String sequence = "" + nextSequence;
		++nextSequence;

		return buildDelimitedString(ip, port, sequence);
	}

	public DatagramPacket makeSignalPacket(final Signal signal)
			throws TooShortPacketLengthException, SocketException {

		int maxPacketLength = Protocol.PACKET_LENGTH;
		String payload = buildHeaderString();

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

		int maxPacketLength = Protocol.PACKET_LENGTH;
		int delimiterLength = Protocol.PACKET_DELIMITER.length();

		int signalSpace = Signal.PARTIAL.getString().length() + delimiterLength;
		int checksumSpace = delimiterLength + Protocol.CHECKSUM_LENGTH;

		int spaceLeft = maxPacketLength - signalSpace - checksumSpace
				- delimiterLength;

		String dataLeft = data;
		boolean done = false;

		while (!done) {
			String header = buildHeaderString();
			int spaceLeftForData = spaceLeft - header.length();

			if (spaceLeftForData <= 0) {
				throw new TooShortPacketLengthException("Packet length "
						+ maxPacketLength + " too short");
			}

			String packetData;
			String signal;

			if (dataLeft.length() > spaceLeftForData) {
				packetData = dataLeft.substring(0, spaceLeftForData);
				dataLeft = dataLeft.substring(spaceLeftForData);
				signal = Signal.PARTIAL.getString();
			} else {
				packetData = dataLeft;
				signal = Signal.REGULAR.getString();
				done = true;
			}

			String payload = buildDelimitedString(header, signal, packetData);
			String checksum = PacketUtils.getChecksum(payload);

			String finalPayload = buildDelimitedString(payload, checksum);
			byte[] byteData = finalPayload.getBytes();

			DatagramPacket packet = new DatagramPacket(byteData,
					byteData.length, relay);
			packetList.add(packet);
		}

		return packetList;
	}

	private static String buildDelimitedString(String... values) {
		return PacketUtils.buildDelimitedString(Protocol.PACKET_DELIMITER,
				values);
	}
}