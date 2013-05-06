package no.uib.inf142.assignment3.rip.client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.RIPPacket;
import no.uib.inf142.assignment3.rip.common.RIPThread;
import no.uib.inf142.assignment3.rip.common.SequentialRIPPacketGenerator;
import no.uib.inf142.assignment3.rip.common.enums.Signal;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class PacketMakerThread extends RIPThread {

	private BlockingQueue<String> dataBuffer;
	private BlockingQueue<RIPPacket> inPacketBuffer;
	private BlockingQueue<RIPPacket> outPacketBuffer;
	private SequentialRIPPacketGenerator packetGen;

	public PacketMakerThread(final BlockingQueue<String> dataBuffer,
			final BlockingQueue<RIPPacket> inPacketBuffer,
			final BlockingQueue<RIPPacket> outPacketBuffer,
			final InetSocketAddress finalDestination,
			final InetSocketAddress relay, final int startingSequence) {

		super();
		this.dataBuffer = dataBuffer;
		this.inPacketBuffer = inPacketBuffer;
		this.outPacketBuffer = outPacketBuffer;

		packetGen = new SequentialRIPPacketGenerator(finalDestination, relay,
				startingSequence);
	}

	private void connectionSetup() {
		try {
			RIPPacket syn = packetGen.makeSignalPacket(Signal.SYN);
			outPacketBuffer.put(syn);

			// SYN ACK
			RIPPacket ripPacket = null;
			Signal signal = null;
			while (signal != Signal.SYNACK) {
				ripPacket = inPacketBuffer.take();
				signal = ripPacket.getSignal();
			}

			RIPPacket ack = packetGen.makeSignalPacket(Signal.ACK);
			outPacketBuffer.put(ack);
		} catch (InterruptedException | TooShortPacketLengthException e) {
			active = false;
			exception = e;
		}
	}

	@Override
	public final void run() {
		connectionSetup();

		while (active && !Thread.interrupted()) {
			try {
				String data = dataBuffer.take();
				List<RIPPacket> packetList = packetGen.makePackets(data);

				for (RIPPacket packet : packetList) {
					outPacketBuffer.put(packet);
				}
			} catch (InterruptedException e) {
				exception = e;
				interrupt();
			} catch (TooShortPacketLengthException e) {
				active = false;
				exception = e;
				interrupt();
			}
		}

		if (active) {
			connectionTeardown();
		}
	}

	private void connectionTeardown() {
		try {
			RIPPacket fin = packetGen.makeSignalPacket(Signal.FIN);
			outPacketBuffer.put(fin);

			// ACK from server
			RIPPacket ripPacket = null;
			Signal signal = null;
			while (signal != Signal.ACK) {
				ripPacket = inPacketBuffer.take();
				signal = ripPacket.getSignal();
			}

			// FIN from server
			while (signal != Signal.FIN) {
				ripPacket = inPacketBuffer.take();
				signal = ripPacket.getSignal();
			}

			RIPPacket lastACK = packetGen.makeSignalPacket(Signal.ACK);
			outPacketBuffer.put(lastACK);
		} catch (InterruptedException | TooShortPacketLengthException e) {
			exception = e;
		}
	}
}
