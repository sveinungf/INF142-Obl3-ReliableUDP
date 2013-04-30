package no.uib.inf142.assignment3.rip.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPPacket;
import no.uib.inf142.assignment3.rip.common.RIPThread;

public class PacketSenderThread extends RIPThread {

	private BlockingQueue<RIPPacket> packetBuffer;
	private BlockingQueue<RIPPacket> window;
	private DatagramSocket socket;
	private SimpleTimer timer;

	public PacketSenderThread(DatagramSocket socket,
			BlockingQueue<RIPPacket> packetBuffer,
			BlockingQueue<RIPPacket> window) {

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
					if (!window.isEmpty()) {
						for (RIPPacket ripPacket : window) {
							socket.send(ripPacket.getDatagramPacket());
						}
						System.out.println("[PacketSender] "
								+ "Timeout, sent all in window");
					}

					timer.restart();
				} else if (!packetBuffer.isEmpty() && !windowFull) {
					RIPPacket ripPacket = packetBuffer.take();

					if (window.isEmpty()) {
						timer.restart();
					}

					window.put(ripPacket);

					DatagramPacket packet = ripPacket.getDatagramPacket();
					socket.send(packet);

					String data = new String(packet.getData(), 0,
							packet.getLength());
					System.out.println("[PacketSender] Sent: \"" + data + "\"");
				} else {
					Thread.sleep(Protocol.WAITTIME_IN_MILLIS);
				}
			} catch (IOException | InterruptedException e) {
				active = false;
				exception = e;
			}
		}
	}
}
