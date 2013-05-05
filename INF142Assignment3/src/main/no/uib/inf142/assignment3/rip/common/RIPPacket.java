package no.uib.inf142.assignment3.rip.common;

import java.net.DatagramPacket;

import no.uib.inf142.assignment3.rip.common.enums.Signal;

public class RIPPacket {

    private final int sequence;
    private final DatagramPacket datagramPacket;
    private final Signal signal;

    public RIPPacket(final int sequence, final DatagramPacket datagramPacket,
            final Signal signal) {

        this.sequence = sequence;
        this.datagramPacket = datagramPacket;
        this.signal = signal;
    }

    public final int getSequence() {
        return sequence;
    }

    public final DatagramPacket getDatagramPacket() {
        return datagramPacket;
    }

    public final Signal getSignal() {
        return signal;
    }
}
