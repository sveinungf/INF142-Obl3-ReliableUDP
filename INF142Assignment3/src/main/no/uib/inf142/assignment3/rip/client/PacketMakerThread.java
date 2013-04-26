package no.uib.inf142.assignment3.rip.client;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class PacketMakerThread implements Runnable {

	private BlockingQueue<String> dataBuffer;
	private BlockingQueue<DatagramPacket> packetBuffer;
	private RIPPacketGenerator packetGen;

	public PacketMakerThread(BlockingQueue<String> dataBuffer,
			BlockingQueue<DatagramPacket> packetBuffer,
			InetSocketAddress finalDestination, InetSocketAddress relay) {

		this.dataBuffer = dataBuffer;
		this.packetBuffer = packetBuffer;
		packetGen = new RIPPacketGenerator(finalDestination, relay);
	}

	@Override
	public void run() {
		try {
			String data = dataBuffer.take();

			List<DatagramPacket> packetList = packetGen.makePackets(data);

			for (DatagramPacket packet : packetList) {
				packetBuffer.put(packet);
			}
		} catch (InterruptedException | SocketException
				| TooShortPacketLengthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
