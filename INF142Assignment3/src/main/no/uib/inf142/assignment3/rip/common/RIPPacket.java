package no.uib.inf142.assignment3.rip.common;

import java.net.DatagramPacket;

public class RIPPacket {

    private final int sequence;
    private final DatagramPacket datagramPacket;

    public RIPPacket(final int sequence, final DatagramPacket datagramPacket) {
        this.sequence = sequence;
        this.datagramPacket = datagramPacket;
    }

    public final int getSequence() {
        return sequence;
    }

    public final DatagramPacket getDatagramPacket() {
        return datagramPacket;
    }
}
