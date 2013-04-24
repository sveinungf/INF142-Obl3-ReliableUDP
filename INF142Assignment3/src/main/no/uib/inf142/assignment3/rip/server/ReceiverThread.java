package no.uib.inf142.assignment3.rip.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.ProtocolConstants;

public class ReceiverThread implements Closeable, Runnable {

	private boolean receiving;
	private BlockingQueue<DatagramPacket> packetBuffer;
	private DatagramSocket socket;

	public ReceiverThread(DatagramSocket socket,
			BlockingQueue<DatagramPacket> packetBuffer) {

		receiving = true;
		this.packetBuffer = packetBuffer;
		this.socket = socket;
	}

	@Override
	public void run() {
		System.out.println("receiver: ready");

		while (receiving) {
			byte[] data = new byte[ProtocolConstants.PACKET_LENGTH];
			DatagramPacket packet = new DatagramPacket(data, data.length);

			try {
				socket.receive(packet);
				System.out.println("receiver: received something");

				packetBuffer.put(packet);
				System.out.println("receiver: buffered packet");
			} catch (IOException | InterruptedException e) {
				receiving = false;
				System.out.println(e.getMessage());
			}
		}

		System.out.println("receiver: done");
	}

	@Override
	public void close() {
		socket.close();
	}
}
