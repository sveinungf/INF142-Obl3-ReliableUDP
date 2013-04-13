package no.uib.inf142.assignment3.rip.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.plaf.SliderUI;

import no.uib.inf142.assignment3.rip.ProtocolConstants;

public class ReceiverThread implements Runnable {

	private boolean receiving;
	private BlockingQueue<DatagramPacket> packetBuffer;
	private DatagramSocket socket;

	public ReceiverThread(int port, BlockingQueue<DatagramPacket> packetBuffer)
			throws SocketException {

		receiving = true;
		this.packetBuffer = packetBuffer;
		socket = new DatagramSocket(port);
	}

	@Override
	public void run() {
		System.out.println("ready to receive");

		while (receiving) {
			byte[] data = new byte[ProtocolConstants.PACKET_LENGTH];
			DatagramPacket packet = new DatagramPacket(data, data.length);

			try {
				socket.receive(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
				e.printStackTrace();
			}

			try {
				packetBuffer.put(packet);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
