package no.uib.inf142.assignment3.rip.client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPPacket;

public class PacketSender implements Runnable {

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
		timer.restart();

		while (active && !Thread.interrupted()) {
			boolean timeout = timer.timedOut();
			boolean windowFull = window.size() > maxWindowSize;

			try {
				if (timeout) {
					for (RIPPacket ripPacket : window) {
						socket.send(ripPacket.getDatagramPacket());
					}

					timer.restart();
				} else if (!packetBuffer.isEmpty() && !windowFull) {
					RIPPacket ripPacket = packetBuffer.take();

					if (window.isEmpty()) {
						timer.restart();
					}

					window.put(ripPacket);
					socket.send(ripPacket.getDatagramPacket());
				} else {
					Thread.sleep(Protocol.WAITTIME_IN_MILLIS);
				}
			} catch (IOException | InterruptedException e) {
				active = false;
			}
		}
	}
}
