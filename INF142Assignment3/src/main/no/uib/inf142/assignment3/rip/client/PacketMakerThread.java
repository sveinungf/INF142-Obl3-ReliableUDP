package no.uib.inf142.assignment3.rip.client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.RIPPacket;
import no.uib.inf142.assignment3.rip.common.RIPThread;
import no.uib.inf142.assignment3.rip.common.SequentialRIPPacketGenerator;
import no.uib.inf142.assignment3.rip.common.Signal;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class PacketMakerThread extends RIPThread {

    private BlockingQueue<String> dataBuffer;
    private BlockingQueue<RIPPacket> inPacketBuffer;
    private BlockingQueue<RIPPacket> outPacketBuffer;
    private SequentialRIPPacketGenerator packetGen;

    public PacketMakerThread(final BlockingQueue<String> dataBuffer,
            final BlockingQueue<RIPPacket> inPacketBuffer,
            final BlockingQueue<RIPPacket> outPacketBuffer,
            final InetSocketAddress finalDestination,
            final InetSocketAddress relay, final int startingSequence) {

        super();
        this.dataBuffer = dataBuffer;
        this.inPacketBuffer = inPacketBuffer;
        this.outPacketBuffer = outPacketBuffer;

        packetGen = new SequentialRIPPacketGenerator(finalDestination, relay,
                startingSequence);
    }

    private void connectionSetup() {
        try {
            RIPPacket syn = packetGen.makeSignalPacket(Signal.SYN);
            outPacketBuffer.put(syn);

            // SYN ACK
            inPacketBuffer.take();

            RIPPacket ack = packetGen.makeSignalPacket(Signal.ACK);
            outPacketBuffer.put(ack);
        } catch (InterruptedException | TooShortPacketLengthException e) {
            active = false;
            exception = e;
        }
    }

    @Override
    public final void run() {
        connectionSetup();

        while (active && !Thread.interrupted()) {
            try {
                String data = dataBuffer.take();
                List<RIPPacket> packetList = packetGen.makePackets(data);

                for (RIPPacket packet : packetList) {
                    outPacketBuffer.put(packet);
                }
            } catch (InterruptedException | TooShortPacketLengthException e) {
                exception = e;
                interrupt();
            }
        }

        if (active) {
            connectionTeardown();
        }
        
        System.out.println("[PacketMaker] Done");
    }

    private void connectionTeardown() {
        try {
            RIPPacket fin = packetGen.makeSignalPacket(Signal.FIN);
            outPacketBuffer.put(fin);

            // ACK
            inPacketBuffer.take();

            // FIN
            inPacketBuffer.take();

            RIPPacket ack = packetGen.makeSignalPacket(Signal.ACK);
            outPacketBuffer.put(ack);
        } catch (InterruptedException | TooShortPacketLengthException e) {
            exception = e;
        }
    }
}
