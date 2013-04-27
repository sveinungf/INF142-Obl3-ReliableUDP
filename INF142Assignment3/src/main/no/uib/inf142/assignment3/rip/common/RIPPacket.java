package no.uib.inf142.assignment3.rip.common;

import java.net.DatagramPacket;

public class RIPPacket {

	private final int sequence;
	private final DatagramPacket datagramPacket;

	public RIPPacket(int sequence, DatagramPacket datagramPacket) {
		this.sequence = sequence;
		this.datagramPacket = datagramPacket;
	}

	public int getSequence() {
		return sequence;
	}

	public DatagramPacket getDatagramPacket() {
		return datagramPacket;
	}
}
