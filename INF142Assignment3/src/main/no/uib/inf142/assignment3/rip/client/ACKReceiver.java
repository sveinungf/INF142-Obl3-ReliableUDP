package no.uib.inf142.assignment3.rip.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPPacket;
import no.uib.inf142.assignment3.rip.common.Signal;
import no.uib.inf142.assignment3.rip.common.SignalMap;
import no.uib.inf142.assignment3.rip.exception.InvalidPacketException;

public class ACKReceiver implements Runnable {

	private boolean active;
	private int expectedSequence;
	private BlockingQueue<RIPPacket> window;
	private DatagramSocket socket;

	public ACKReceiver(BlockingQueue<RIPPacket> window, DatagramSocket socket,
			int startingSequence) {

		active = true;
		expectedSequence = startingSequence;
		this.window = window;
		this.socket = socket;
	}

	@Override
	public void run() {
		System.out.println("ACK receiver: ready");
		System.out.println("ACK receiver: socketport " + socket.getLocalPort());

		while (active) {
			try {
				byte[] byteData = new byte[Protocol.MAX_PACKET_LENGTH];
				DatagramPacket packet = new DatagramPacket(byteData,
						byteData.length);

				System.out.println("ACK receiver: waiting for ACK");
				socket.receive(packet);

				String data = new String(packet.getData(), 0,
						packet.getLength());

				System.out.println("ACK receiver: received: " + data);
				String[] items = data.split(Protocol.PACKET_DELIMITER);

				// TODO substitute literal
				if (items.length < 4) {
					throw new InvalidPacketException(
							"Packet contains too few datafields");
				}

				// TODO substitute literals
				String sequenceString = items[2];
				String signalString = items[3];

				Signal signal = SignalMap.getInstance().getByString(
						signalString);

				if (signal == null) {
					throw new InvalidPacketException("Illegal signal in packet");
				} else if (signal != Signal.ACK) {
					throw new InvalidPacketException("Invalid signal in packet");
				}

				int sequence = Integer.parseInt(sequenceString);

				if (sequence >= expectedSequence) {
					System.out.println("ACK receiver: got seq >= expected seq");
					Iterator<RIPPacket> it = window.iterator();
					System.out.println("ACK receiver: window size "
							+ window.size());

					while (it.hasNext()) {
						System.out
								.println("ACK receiver: checks a packet in the window");
						RIPPacket currentRIPPacket = it.next();

						if (currentRIPPacket.getSequence() <= sequence) {
							System.out
									.println("ACK receiver: packets in window before removal: "
											+ window.size());
							it.remove();
							System.out
									.println("ACK receiver: packets in window after removal: "
											+ window.size());
						}
					}
					System.out
							.println("ACK receiver: no more packets in window");

					expectedSequence = sequence + 1;
				}

			} catch (InvalidPacketException | NumberFormatException
					| IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
