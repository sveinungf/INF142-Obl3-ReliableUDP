package no.uib.inf142.assignment3.rip.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Datafield;
import no.uib.inf142.assignment3.rip.common.PacketUtils;
import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPPacket;
import no.uib.inf142.assignment3.rip.common.RIPThread;
import no.uib.inf142.assignment3.rip.common.Signal;
import no.uib.inf142.assignment3.rip.common.SignalMap;
import no.uib.inf142.assignment3.rip.exception.InvalidPacketException;

public class ACKReceiverThread extends RIPThread {

    private int expectedSequence;
    private BlockingQueue<RIPPacket> inPacketBuffer;
    private BlockingQueue<RIPPacket> window;
    private DatagramSocket socket;

    public ACKReceiverThread(final BlockingQueue<RIPPacket> inPacketBuffer,
            final BlockingQueue<RIPPacket> window, final DatagramSocket socket,
            final int startingSequence) {

        expectedSequence = startingSequence;
        this.inPacketBuffer = inPacketBuffer;
        this.window = window;
        this.socket = socket;
    }

    @Override
    public final void run() {
        while (active && !Thread.interrupted()) {
            try {
                byte[] byteData = new byte[Protocol.PACKETDATA_LENGTH];
                DatagramPacket packet = new DatagramPacket(byteData,
                        byteData.length);

                socket.receive(packet);

                String payload = PacketUtils.getPayloadFromPacket(packet);
                String[] datafields = PacketUtils.getDatafields(payload);

                String signalString = datafields[Datafield.SIGNAL.ordinal()];
                Signal signal = SignalMap.getInstance().getByString(
                        signalString);

                if (signal == null
                        || !(signal == Signal.ACK || signal == Signal.SYNACK)) {

                    throw new InvalidPacketException(
                            "Invalid signal in packet: " + signal);
                }

                String sequenceString = datafields[Datafield.SEQUENCE.ordinal()];
                int sequence = PacketUtils.convertFromHexString(sequenceString);

                if (sequence >= expectedSequence) {
                    System.out.println("[ACKReceiver] Received expected: \""
                            + payload + "\"");

                    if (signal == Signal.SYNACK) {
                        RIPPacket synack = new RIPPacket(sequence, packet);
                        inPacketBuffer.put(synack);
                    }
                    
                    Iterator<RIPPacket> it = window.iterator();

                    while (it.hasNext()) {
                        RIPPacket currentRIPPacket = it.next();

                        if (sequence >= currentRIPPacket.getSequence()) {
                            it.remove();
                        }
                    }

                    expectedSequence = sequence + 1;
                } else {
                    System.out.println("[ACKReceiver] Received unexpected: \""
                            + payload + "\"");
                }

            } catch (InvalidPacketException e) {
                System.out.println("[ACKReceiver] " + e.getMessage());
            } catch (IOException e) {
                active = false;
                exception = e;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
