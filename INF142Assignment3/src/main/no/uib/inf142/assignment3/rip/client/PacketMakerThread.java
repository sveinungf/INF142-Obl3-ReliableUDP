package no.uib.inf142.assignment3.rip.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.uib.inf142.assignment3.rip.common.RIPPacket;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class PacketMakerThread implements Closeable, Runnable {

	private boolean active;
	private BlockingQueue<String> dataBuffer;
	private BlockingQueue<RIPPacket> packetBuffer;
	private RIPPacketGenerator packetGen;

	public PacketMakerThread(BlockingQueue<String> dataBuffer,
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
		while (active) {
			System.out.println("packetmaker: ready");

			try {
				String data = dataBuffer.take();
				System.out.println("packetmaker: got some data");

				List<RIPPacket> packetList = packetGen.makePackets(data);

				for (RIPPacket ripPacket : packetList) {
					packetBuffer.put(ripPacket);
					System.out.println("packetmaker: buffered packet");
				}
			} catch (InterruptedException | SocketException
					| TooShortPacketLengthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close() throws IOException {
		active = false;
	}
}
