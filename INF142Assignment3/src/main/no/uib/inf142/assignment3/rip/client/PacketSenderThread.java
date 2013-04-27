package no.uib.inf142.assignment3.rip.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.ProtocolConstants;

public class PacketSenderThread implements Closeable, Runnable {

	private boolean receiving;
	private BlockingQueue<DatagramPacket> window;
	private BlockingQueue<DatagramPacket> packetBuffer;
	private DatagramSocket socket;

	public PacketSenderThread(DatagramSocket socket,
			BlockingQueue<DatagramPacket> window,
			BlockingQueue<DatagramPacket> packetBuffer) {

		receiving = true;
		this.window = window;
		this.packetBuffer = packetBuffer;
		this.socket = socket;
	}

	@Override
	public void run() {
		int maxWindowSize = ProtocolConstants.WINDOW_SIZE;
		System.out.println("packetsender: ready");

		while (receiving) {
			// TODO implement timeout
			boolean timeout = false;
			boolean windowFull = window.size() > maxWindowSize;

			if (timeout) {
				// TODO resend all packets in window
				// TODO restart timer
			} else if (!packetBuffer.isEmpty() && !windowFull) {
				try {
					DatagramPacket packet = packetBuffer.take();

					if (window.isEmpty()) {
						// TODO restart timer
					}

					// TODO send packet and put in window

					System.out.println("packetsender: got some data");

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// TODO get some sleep
			}
		}
	}

	@Override
	public void close() {
		receiving = false;
		socket.close();
	}

}
