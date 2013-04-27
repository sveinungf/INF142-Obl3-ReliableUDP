package no.uib.inf142.assignment3.rip.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPPacket;

public class PacketSender implements Closeable, Runnable {

	private boolean receiving;
	private BlockingQueue<RIPPacket> packetBuffer;
	private BlockingQueue<RIPPacket> window;
	private DatagramSocket socket;
	private SimpleTimer timer;

	public PacketSender(DatagramSocket socket,
			BlockingQueue<RIPPacket> packetBuffer,
			BlockingQueue<RIPPacket> window) {

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
					for (RIPPacket ripPacket : window) {
						socket.send(ripPacket.getDatagramPacket());
					}
				} catch (IOException e) {
					// TODO
				}

				timer.restart();
			} else if (!packetBuffer.isEmpty() && !windowFull) {
				try {
					RIPPacket ripPacket = packetBuffer.take();
					// DatagramPacket packet = packetBuffer.take();

					if (window.isEmpty()) {
						timer.restart();
					}

					window.put(ripPacket);
					socket.send(ripPacket.getDatagramPacket());

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
