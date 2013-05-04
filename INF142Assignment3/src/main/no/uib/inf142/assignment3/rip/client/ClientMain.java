package no.uib.inf142.assignment3.rip.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import no.uib.inf142.assignment3.rip.common.Protocol;

public class ClientMain {
    // TODO connection setup
    // TODO connection tear-down
    // TODO ACKSender: DON'T ignore sequence < expected, send an ACK
    public static void main(final String[] args) {
        InetAddress localhost;

        try {
            localhost = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            localhost = null;
        }

        InetSocketAddress server = new InetSocketAddress(localhost,
                Protocol.SERVER_LISTENING_PORT);
        InetSocketAddress relay = new InetSocketAddress(localhost,
                Protocol.RELAY_LISTENING_PORT);

        try {
            RIPSocket ripsocket = new RIPSocket(server, relay);
            Scanner kbd = new Scanner(System.in);

            String input = "";

            while (!input.equals("exit")) {
                input = kbd.nextLine();

                ripsocket.send(input);
            }
            kbd.close();
            ripsocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
