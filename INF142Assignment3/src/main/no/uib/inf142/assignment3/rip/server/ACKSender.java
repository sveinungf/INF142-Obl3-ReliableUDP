package no.uib.inf142.assignment3.rip.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;

public class ACKSender implements Closeable, Runnable {

	private boolean receiving;
	private BlockingQueue<DatagramPacket> packetBuffer;
	private BlockingQueue<String> dataBuffer;
	private DatagramSocket socket;
	private StringBuilder stringBuilder;

	public ACKSender(DatagramSocket socket,
			BlockingQueue<DatagramPacket> packetBuffer,
			BlockingQueue<String> dataBuffer) throws IOException {

		receiving = true;
		this.packetBuffer = packetBuffer;
		this.dataBuffer = dataBuffer;
		this.socket = socket;
		stringBuilder = new StringBuilder();
	}

	@Override
	public void run() {
		System.out.println("sender: ready");

		while (receiving) {
			try {
				DatagramPacket packet = packetBuffer.take();

				System.out.println("sender: got a packet");
				byte[] byteData = packet.getData();
				String data = new String(byteData, 0, packet.getLength());
				InetAddress relayAddress = packet.getAddress();
				int relayPort = packet.getPort();

				System.out.println("sender: packet from "
						+ relayAddress.getHostAddress() + ":" + relayPort);
				// TODO check checksum, then check seqnum, then send ACK

				// TODO check if data complete
				boolean dataComplete = true;
				stringBuilder.append(data);

				if (dataComplete) {
					dataBuffer.put(stringBuilder.toString());
					receiving = false;

					System.out.println("sender: buffered data");
				}
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				receiving = false;
			}
		}

		System.out.println("sender: done");
	}

	@Override
	public void close() {
		socket.close();
	}
}
