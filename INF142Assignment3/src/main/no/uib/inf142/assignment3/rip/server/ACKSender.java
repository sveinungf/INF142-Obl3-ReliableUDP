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

import no.uib.inf142.assignment3.rip.common.PacketUtils;
import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPPacketGenerator;
import no.uib.inf142.assignment3.rip.common.Signal;
import no.uib.inf142.assignment3.rip.common.SignalMap;
import no.uib.inf142.assignment3.rip.exception.InvalidPacketException;
import no.uib.inf142.assignment3.rip.exception.InvalidSocketAddressException;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class ACKSender implements Closeable, Runnable {

	private boolean receiving;
	private int expectedSequence;
	private BlockingQueue<DatagramPacket> packetBuffer;
	private BlockingQueue<String> dataBuffer;
	private DatagramSocket socket;
	private StringBuilder stringBuilder;

	public ACKSender(DatagramSocket socket,
			BlockingQueue<DatagramPacket> packetBuffer,
			BlockingQueue<String> dataBuffer) throws IOException {

		receiving = true;
		expectedSequence = 0;
		this.packetBuffer = packetBuffer;
		this.dataBuffer = dataBuffer;
		this.socket = socket;
		stringBuilder = new StringBuilder();
	}

	@Override
	public void run() {
		System.out.println("ACK sender: ready");

		while (receiving) {
			try {
				System.out.println("ACK sender: waiting for packet in buffer");
				DatagramPacket packet = packetBuffer.take();

				System.out.println("ACK sender: got a packet");
				byte[] byteData = packet.getData();
				String payload = new String(byteData, 0, packet.getLength());

				InetAddress relayAddress = packet.getAddress();
				int relayPort = Protocol.RELAY_LISTENING_PORT;
				InetSocketAddress relay = new InetSocketAddress(relayAddress,
						relayPort);

				System.out.println("ACK sender: packet from "
						+ relayAddress.getHostAddress() + ":" + relayPort);
				// TODO check checksum, then check seqnum, then send ACK

				String[] items = payload.split(Protocol.PACKET_DELIMITER);
				// TODO substitute literal
				if (items.length < 4) {
					throw new InvalidPacketException(
							"Packet contains too few datafields");
				}

				String sequenceString = items[2];
				int sequence = Integer.parseInt(sequenceString);

				InetSocketAddress source = PacketUtils.parseSocketAddress(
						items[0], items[1]);

				if (sequence == expectedSequence) {
					System.out.println("ACK sender: got expected sequence");

					RIPPacketGenerator packetGen = new RIPPacketGenerator(
							source, relay, 0);
					DatagramPacket ackPacket = packetGen
							.makeACKPacket(sequence);
					socket.send(ackPacket);

					String signalString = items[3];
					Signal signal = SignalMap.getInstance().getByString(
							signalString);
					boolean dataComplete = signal == Signal.REGULAR;

					String data = items[4];
					stringBuilder.append(data);

					if (dataComplete) {
						dataBuffer.put(stringBuilder.toString());
						receiving = false;

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
				System.out.println(e.getMessage());
				receiving = false;
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
