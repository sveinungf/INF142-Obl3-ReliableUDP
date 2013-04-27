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

public class ACKReceiverThread implements Runnable {

	private boolean active;
	private int expectedSequence;
	private BlockingQueue<RIPPacket> window;
	private DatagramSocket socket;

	public ACKReceiverThread(BlockingQueue<RIPPacket> window,
			DatagramSocket socket, int startingSequence) {

		active = true;
		expectedSequence = startingSequence;
		this.window = window;
		this.socket = socket;
	}

	@Override
	public void run() {
		System.out.println("ACK receiver: ready");

		while (active) {
			try {
				byte[] byteData = new byte[Protocol.PACKET_LENGTH];
				DatagramPacket packet = new DatagramPacket(byteData,
						byteData.length);

				socket.receive(packet);
				System.out.println("ACK receiver: received something");

				String data = new String(packet.getData(), 0,
						packet.getLength());
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
					throw new InvalidPacketException("Illegal signal");
				} else if (signal != Signal.ACK) {
					throw new InvalidPacketException("Invalid signal");
				}

				int sequence = Integer.parseInt(sequenceString);

				if (sequence == expectedSequence) {
					++expectedSequence;
				} else if (sequence > expectedSequence) {
					Iterator<RIPPacket> it = window.iterator();

					while (it.hasNext()) {
						RIPPacket currentRIPPacket = it.next();

						if (currentRIPPacket.getSequence() < sequence) {
							it.remove();
						}
					}

					expectedSequence = sequence + 1;
				}

			} catch (InvalidPacketException | NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
