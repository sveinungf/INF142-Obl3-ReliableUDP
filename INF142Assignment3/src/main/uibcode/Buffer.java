package uibcode;

import java.net.*;

public class Buffer extends Thread {
	private int lengde;
	private int transmitDelay;
	private DatagramPacket[] pakke;
	private int first;
	private int pnr;
	private int currentLengde;
	private boolean alive;

	public Buffer(int lengde, int transmitDelay) {
		this.lengde = lengde;
		this.transmitDelay = transmitDelay;
		pakke = new DatagramPacket[lengde];
		first = 0;
		currentLengde = 0;
		alive = true;
		pnr = 0;
	}

	public synchronized void insert(DatagramPacket p) {
		System.out.println("entering insert");
		if (currentLengde == lengde)
			System.out.println("FULLT " + pnr);
		else {
			int ind = index(first + currentLengde);
			pakke[ind] = p;
			String s = new String(p.getData()).substring(0, 40);
			currentLengde++;
			System.out.println("insert: " + pnr + " cL:" + currentLengde
					+ " : " + s);
			if (currentLengde > 0) {
				System.out.println("insert: " + pnr + " cL:" + currentLengde
						+ " :time to wake up getPacket... " + currentLengde);
				notifyAll();
			}
		}
		System.out.println("leaving insert");
		pnr++;
	}

	public void run() {
		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(11112);
		} catch (Exception e) {
		}

		DatagramPacket p;
		while (alive) {
			System.out.println("entering Buffer loop");
			p = getPacket();
			if (p != null)
				try {
					String s = new String(p.getData()).substring(0, 40);
					System.out.println("Sending: " + s);
					serverSocket.send(p);
					System.out.println("going to sleep");
					sleep(transmitDelay);
					System.out.println("waking up");
				} catch (Exception e) {
				}
			else
				System.out.println("p var null");
			System.out.println("completing Buffer loop");
		}
	}

	public synchronized DatagramPacket getPacket() {
		System.out.println("entering getPacket");
		DatagramPacket p;
		p = null;
		while (currentLengde == 0) {
			try {
				wait(1);
			} catch (InterruptedException e) {
				System.out.println("...caught wakeupException");
			}
		}

		p = pakke[first];
		first = index(first + 1);
		currentLengde--;
		String s = new String(p.getData()).substring(0, 40);
		System.out.println("getPacket: finds one: " + s);

		System.out.println("first:" + first + " cL:" + currentLengde);
		notifyAll();
		System.out.println("leaving getPacket");
		return p;
	}

	private int index(int i) {
		return (i % lengde);
	}
}
