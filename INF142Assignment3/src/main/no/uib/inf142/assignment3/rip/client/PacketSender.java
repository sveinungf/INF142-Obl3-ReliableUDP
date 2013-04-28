package no.uib.inf142.assignment3.rip.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPPacket;

public class PacketSender implements Closeable, Runnable {

	private boolean active;
	private BlockingQueue<RIPPacket> packetBuffer;
	private BlockingQueue<RIPPacket> window;
	private DatagramSocket socket;
	private SimpleTimer timer;

	public PacketSender(DatagramSocket socket,
			BlockingQueue<RIPPacket> packetBuffer,
			BlockingQueue<RIPPacket> window) {

		active = true;
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

		while (active) {
			boolean timeout = timer.timedOut();
			boolean windowFull = window.size() > maxWindowSize;

			if (timeout) {
				System.out.println("packetsender: timeout");
				try {
					for (RIPPacket ripPacket : window) {
						socket.send(ripPacket.getDatagramPacket());
						System.out.println("packetsender: sent a packet");
					}
				} catch (IOException e) {
					// TODO
				}

				timer.restart();
				System.out.println("packetsender: restarted timer");
			} else if (!packetBuffer.isEmpty() && !windowFull) {
				try {
					RIPPacket ripPacket = packetBuffer.take();
					
					System.out.println("packetsender: got packet from buffer");

					if (window.isEmpty()) {
						timer.restart();
						System.out.println("packetsender: restarted timer");
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
		active = false;
		socket.close();
	}

}
