package no.uib.inf142.assignment3.rip.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;

public class PacketReceiver implements Closeable, Runnable {

	private boolean receiving;
	private BlockingQueue<DatagramPacket> packetBuffer;
	private DatagramSocket socket;

	public PacketReceiver(DatagramSocket socket,
			BlockingQueue<DatagramPacket> packetBuffer) {

		receiving = true;
		this.packetBuffer = packetBuffer;
		this.socket = socket;
	}

	@Override
	public void run() {
		System.out.println("packetreceiver: ready");
		System.out.println("packetreceiver: socketport " + socket.getLocalPort());

		while (receiving) {
			byte[] data = new byte[Protocol.PACKET_LENGTH];
			DatagramPacket packet = new DatagramPacket(data, data.length);

			try {
				System.out.println("packetreceiver: waiting for packet");
				socket.receive(packet);
				System.out.println("packetreceiver: received something");

				packetBuffer.put(packet);
				System.out.println("packetreceiver: buffered packet");
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
