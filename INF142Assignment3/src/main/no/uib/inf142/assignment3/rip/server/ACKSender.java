package no.uib.inf142.assignment3.rip.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Datafield;
import no.uib.inf142.assignment3.rip.common.PacketUtils;
import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.PacketGenerator;
import no.uib.inf142.assignment3.rip.common.Signal;
import no.uib.inf142.assignment3.rip.common.SignalMap;
import no.uib.inf142.assignment3.rip.exception.InvalidPacketException;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class ACKSender implements Closeable, Runnable {

	private boolean active;
	private int expectedSequence;
	private int relayListeningPort;
	private BlockingQueue<DatagramPacket> packetBuffer;
	private BlockingQueue<String> dataBuffer;
	private DatagramSocket socket;
	private StringBuilder stringBuilder;

	public ACKSender(DatagramSocket socket,
			BlockingQueue<DatagramPacket> packetBuffer,
			BlockingQueue<String> dataBuffer, int relayListeningPort,
			int startingSequence) {

		active = true;
		expectedSequence = startingSequence;
		this.relayListeningPort = relayListeningPort;
		this.packetBuffer = packetBuffer;
		this.dataBuffer = dataBuffer;
		this.socket = socket;
		stringBuilder = new StringBuilder();
	}

	@Override
	public void run() {
		while (active) {
			try {
				DatagramPacket packet = packetBuffer.take();

				InetAddress relayAddress = packet.getAddress();
				InetSocketAddress relay = new InetSocketAddress(relayAddress,
						relayListeningPort);

				String payload = PacketUtils.getDataFromPacket(packet);
				String[] items = payload.split(Protocol.PACKET_DELIMITER);

				int datafields = Datafield.values().length + 1;
				if (items.length < datafields) {
					throw new InvalidPacketException(
							"Packet contains too few datafields");
				}

				boolean checksumOk = PacketUtils.validChecksumInPacket(payload);

				if (!checksumOk) {
					throw new InvalidPacketException("Wrong checksum in packet");
				}

				String sequenceString = items[Datafield.SEQUENCE.ordinal()];
				int sequence = PacketUtils.convertFromHexString(sequenceString);

				String ipString = items[Datafield.IP.ordinal()];
				String portString = items[Datafield.PORT.ordinal()];

				InetSocketAddress source = PacketUtils.parseSocketAddress(
						ipString, portString);

				if (sequence == expectedSequence) {
					++expectedSequence;

					PacketGenerator packetGen = new PacketGenerator(source,
							relay);

					DatagramPacket ack = packetGen.makeACKPacket(sequence);
					socket.send(ack);

					System.out
							.println("[ACKSender] Got expected sequence, sent ACK");

					String signalString = items[Datafield.SIGNAL.ordinal()];
					Signal signal = SignalMap.getInstance().getByString(
							signalString);

					boolean dataComplete = signal == Signal.REGULAR;
					String data = items[Datafield.DATA.ordinal()];
					stringBuilder.append(data);

					if (dataComplete) {
						dataBuffer.put(stringBuilder.toString());
						stringBuilder = new StringBuilder();
					}

				} else {
					System.out.println("[ACKSender] "
							+ "Got unexpected sequence, ignored");
				}
			} catch (InvalidPacketException e) {
				System.out.println("[ACKSender] " + e.getMessage());
			} catch (InterruptedException | IOException
					| TooShortPacketLengthException e) {

				active = false;
				System.out.println("[ACKSender] Closing, " + e.getMessage());
			}
		}
	}

	@Override
	public void close() {
		socket.close();
	}
}
