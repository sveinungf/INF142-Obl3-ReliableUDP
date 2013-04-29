package no.uib.inf142.assignment3.rip.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Datafield;
import no.uib.inf142.assignment3.rip.common.PacketUtils;
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
		while (active && !Thread.interrupted()) {
			try {
				byte[] byteData = new byte[Protocol.MAX_PACKET_LENGTH];
				DatagramPacket packet = new DatagramPacket(byteData,
						byteData.length);

				socket.receive(packet);

				String data = PacketUtils.getDataFromPacket(packet);
				String[] items = data.split(Protocol.PACKET_DELIMITER);

				int datafields = Datafield.values().length;
				if (items.length < datafields) {
					throw new InvalidPacketException(
							"Packet contains too few datafields");
				}

				String sequenceString = items[Datafield.SEQUENCE.ordinal()];
				String signalString = items[Datafield.SIGNAL.ordinal()];

				Signal signal = SignalMap.getInstance().getByString(
						signalString);

				if (signal == null || signal != Signal.ACK) {
					throw new InvalidPacketException("Invalid signal in packet");
				}

				int sequence = PacketUtils.convertFromHexString(sequenceString);
				if (sequence >= expectedSequence) {
					System.out.println("[ACKReceiver] Received expected: \""
							+ data + "\"");

					Iterator<RIPPacket> it = window.iterator();

					while (it.hasNext()) {
						RIPPacket currentRIPPacket = it.next();

						if (sequence >= currentRIPPacket.getSequence()) {
							it.remove();
						}
					}

					expectedSequence = sequence + 1;
				} else {
					System.out.println("[ACKReceiver] Received unexpected: \""
							+ data + "\"");
				}

			} catch (InvalidPacketException e) {
				System.out.println("[ACKReceiver] " + e.getMessage());
			} catch (IOException e) {
				active = false;
				System.out.println("[ACKReceiver] Closing, " + e.getMessage());
			}
		}
	}
}
