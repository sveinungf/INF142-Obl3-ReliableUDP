package no.uib.inf142.assignment3.rip.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPThread;

public class PacketReceiverThread extends RIPThread {

	private BlockingQueue<DatagramPacket> packetBuffer;
	private DatagramSocket socket;

	public PacketReceiverThread(DatagramSocket socket,
			BlockingQueue<DatagramPacket> packetBuffer) {

		this.packetBuffer = packetBuffer;
		this.socket = socket;
	}

	@Override
	public void run() {
		while (active) {
			byte[] byteData = new byte[Protocol.MAX_PACKET_LENGTH];
			DatagramPacket packet = new DatagramPacket(byteData,
					byteData.length);

			try {
				socket.receive(packet);
				String data = new String(packet.getData(), 0,
						packet.getLength());
				System.out.println("[PacketReceiver] Received: \"" + data
						+ "\"");

				packetBuffer.put(packet);
			} catch (IOException | InterruptedException e) {
				active = false;
				exception = e;
			}
		}
	}
}
