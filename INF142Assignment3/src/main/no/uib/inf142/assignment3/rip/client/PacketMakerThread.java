package no.uib.inf142.assignment3.rip.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class PacketMakerThread implements Closeable, Runnable {

	private boolean active;
	private BlockingQueue<String> dataBuffer;
	private BlockingQueue<DatagramPacket> packetBuffer;
	private RIPPacketGenerator packetGen;

	public PacketMakerThread(BlockingQueue<String> dataBuffer,
			BlockingQueue<DatagramPacket> packetBuffer,
			InetSocketAddress finalDestination, InetSocketAddress relay) {

		active = true;
		this.dataBuffer = dataBuffer;
		this.packetBuffer = packetBuffer;
		packetGen = new RIPPacketGenerator(finalDestination, relay);
	}

	@Override
	public void run() {
		while (active) {
			System.out.println("packetmaker: ready");

			try {
				String data = dataBuffer.take();
				System.out.println("packetmaker: got some data");

				List<DatagramPacket> packetList = packetGen.makePackets(data);

				for (DatagramPacket packet : packetList) {
					packetBuffer.put(packet);
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
