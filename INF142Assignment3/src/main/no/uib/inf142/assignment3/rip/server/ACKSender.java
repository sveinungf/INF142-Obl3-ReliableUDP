package no.uib.inf142.assignment3.rip.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Datafield;
import no.uib.inf142.assignment3.rip.common.PacketUtils;
import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.PacketGenerator;
import no.uib.inf142.assignment3.rip.common.Signal;
import no.uib.inf142.assignment3.rip.common.SignalMap;
import no.uib.inf142.assignment3.rip.exception.InvalidPacketException;
import no.uib.inf142.assignment3.rip.exception.InvalidSocketAddressException;
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
			int startingSequence) throws IOException {

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
				InetSocketAddress relay = new InetSocketAddress(relayAddress, relayListeningPort);
				
				byte[] byteData = packet.getData();
				String data = new String(byteData, 0, packet.getLength());

				String[] items = data.split(Protocol.PACKET_DELIMITER);

				int datafields = Datafield.values().length + 1;
				if (items.length < datafields) {
					throw new InvalidPacketException(
							"Packet contains too few datafields");
				}

				int lastDelimiter = data.lastIndexOf(Protocol.PACKET_DELIMITER);
				String toValidate = data.substring(0, lastDelimiter);
				String checksum = items[Datafield.CHECKSUM.ordinal() + 1];
				boolean checksumOk = PacketUtils.validChecksum(toValidate,
						checksum);

				if (!checksumOk) {
					throw new InvalidPacketException("Wrong checksum in packet");
				}
				// TODO check checksum, then check seqnum, then send ACK

				String sequenceString = items[Datafield.SEQUENCE.ordinal()];
				int sequence = PacketUtils.convertFromHexString(sequenceString);

				String ipString = items[Datafield.IP.ordinal()];
				String portString = items[Datafield.PORT.ordinal()];
				InetSocketAddress source = PacketUtils.parseSocketAddress(
						ipString, portString);

				if (sequence == expectedSequence) {
					System.out.println("ACK sender: got expected sequence");

					PacketGenerator packetGen = new PacketGenerator(source,
							relay);
					DatagramPacket ackPacket = packetGen
							.makeACKPacket(sequence);
					socket.send(ackPacket);

					String signalString = items[3];
					Signal signal = SignalMap.getInstance().getByString(
							signalString);
					boolean dataComplete = signal == Signal.REGULAR;

					String d = items[4];
					stringBuilder.append(d);

					if (dataComplete) {
						dataBuffer.put(stringBuilder.toString());

						stringBuilder = new StringBuilder();

						System.out.println("ACK sender: buffered data");
					}

					++expectedSequence;
				} else {
					System.out
							.println("ACK sender: got an unexpected sequence");
				}
			} catch (InterruptedException | InvalidPacketException
					| NumberFormatException e) {
				e.printStackTrace();
				active = false;
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TooShortPacketLengthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidSocketAddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("sender: done");
	}

	@Override
	public void close() {
		socket.close();
	}
}
