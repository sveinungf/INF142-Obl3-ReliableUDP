package no.uib.inf142.assignment3.rip.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

public class SenderThread implements Runnable {

	private boolean receiving;
	private BlockingQueue<DatagramPacket> packetBuffer;
	private BlockingQueue<String> dataBuffer;
	private DatagramSocket socket;
	private StringBuilder stringBuilder;

	public SenderThread(int port, BlockingQueue<DatagramPacket> packetBuffer, BlockingQueue<String> dataBuffer)
			throws SocketException {

		receiving = true;
		this.packetBuffer = packetBuffer;
		this.dataBuffer = dataBuffer;
		socket = new DatagramSocket(port);
		stringBuilder = new StringBuilder();
	}

	@Override
	public void run() {
		while (receiving) {
			try {
				DatagramPacket packet = packetBuffer.take();
				byte[] byteData = packet.getData();
				String data = new String(byteData, 0, packet.getLength());
				// check checksum
				// send ACK
				// check if data complete
				boolean dataComplete = true;
				stringBuilder.append(data);
				
				if (dataComplete) {
					dataBuffer.put(stringBuilder.toString());
					receiving = false;
				}
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
