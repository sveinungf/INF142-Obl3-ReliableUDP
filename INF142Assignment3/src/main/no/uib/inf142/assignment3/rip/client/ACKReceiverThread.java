package no.uib.inf142.assignment3.rip.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;

public class ACKReceiverThread implements Runnable {

	private boolean active;
	private BlockingQueue<DatagramPacket> window;
	private DatagramSocket socket;

	public ACKReceiverThread(BlockingQueue<DatagramPacket> window,
			DatagramSocket socket) {

		active = true;
		this.window = window;
		this.socket = socket;
	}

	@Override
	public void run() {
		System.out.println("ACK receiver: ready");

		while (active) {
			try {
				DatagramPacket packet = window.take();
				String data = new String(packet.getData(), 0,
						packet.getLength());
				
				String[] items = data.split(Protocol.PACKET_DELIMITER);
				String sequence = items[2];
				String signal = items[3];
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
