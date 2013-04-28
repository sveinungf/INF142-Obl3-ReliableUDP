package no.uib.inf142.assignment3.rip.client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.RIPPacket;
import no.uib.inf142.assignment3.rip.common.RIPPacketGenerator;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class PacketMaker implements Runnable {

	private boolean active;
	private BlockingQueue<String> dataBuffer;
	private BlockingQueue<RIPPacket> packetBuffer;
	private RIPPacketGenerator packetGen;

	public PacketMaker(BlockingQueue<String> dataBuffer,
			BlockingQueue<RIPPacket> packetBuffer,
			InetSocketAddress finalDestination, InetSocketAddress relay,
			int startingSequence) {

		active = true;
		this.dataBuffer = dataBuffer;
		this.packetBuffer = packetBuffer;

		packetGen = new RIPPacketGenerator(finalDestination, relay,
				startingSequence);
	}

	@Override
	public void run() {
		while (active && !Thread.interrupted()) {
			try {
				String data = dataBuffer.take();
				List<RIPPacket> packetList = packetGen.makePackets(data);

				for (RIPPacket packet : packetList) {
					packetBuffer.put(packet);
				}
			} catch (InterruptedException | TooShortPacketLengthException e) {
				active = false;
			}
		}
	}
}
