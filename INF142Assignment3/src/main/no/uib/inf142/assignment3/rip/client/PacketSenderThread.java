package no.uib.inf142.assignment3.rip.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;

public class PacketSenderThread implements Closeable, Runnable {

	private boolean receiving;
	private BlockingQueue<DatagramPacket> window;
	private BlockingQueue<DatagramPacket> packetBuffer;
	private DatagramSocket socket;
	private SimpleTimer timer;

	public PacketSenderThread(DatagramSocket socket,
			BlockingQueue<DatagramPacket> window,
			BlockingQueue<DatagramPacket> packetBuffer) {

		receiving = true;
		this.window = window;
		this.packetBuffer = packetBuffer;
		this.socket = socket;
		timer = new SimpleTimer(Protocol.TIMEOUT_IN_MILLIS);
	}

	@Override
	public void run() {
		int maxWindowSize = Protocol.WINDOW_SIZE;
		System.out.println("packetsender: ready");
		timer.restart();

		while (receiving) {
			boolean timeout = timer.timedOut();
			boolean windowFull = window.size() > maxWindowSize;

			if (timeout) {
				try {
					for (DatagramPacket packet : window) {
						socket.send(packet);
					}
				} catch (IOException e) {
				}

				timer.restart();
			} else if (!packetBuffer.isEmpty() && !windowFull) {
				try {
					DatagramPacket packet = packetBuffer.take();

					if (window.isEmpty()) {
						timer.restart();
					}

					window.put(packet);
					socket.send(packet);

					System.out.println("packetsender: sent a packet");

				} catch (InterruptedException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					Thread.sleep(Protocol.WAITTIME_IN_MILLIS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void close() {
		receiving = false;
		socket.close();
	}

}
