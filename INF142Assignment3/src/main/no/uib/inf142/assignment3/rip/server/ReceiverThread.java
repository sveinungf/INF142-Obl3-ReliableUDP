package no.uib.inf142.assignment3.rip.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.plaf.SliderUI;

public class ReceiverThread implements Runnable {

	private BlockingQueue<DatagramPacket> buffer;
	private DatagramSocket socket;

	public ReceiverThread(int port) throws SocketException {
		buffer = new LinkedBlockingQueue<DatagramPacket>();
		socket = new DatagramSocket(port);
	}


	public BlockingQueue<DatagramPacket> getBuffer() {
		return buffer;
	}


	@Override
	public void run() {
		System.out.println("receiver thread");
		byte[] b = new byte[1024];
		DatagramPacket packet = new DatagramPacket(b, b.length);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			buffer.put(packet);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
