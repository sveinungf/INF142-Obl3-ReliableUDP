package no.uib.inf142.assignment3.rip.client;

import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;

public class ACKReceiverThread implements Runnable {

	private BlockingQueue<DatagramPacket> window;

	public ACKReceiverThread(BlockingQueue<DatagramPacket> window) {
		this.window = window;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
