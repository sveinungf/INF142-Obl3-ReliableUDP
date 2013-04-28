package no.uib.inf142.assignment3.rip.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import no.uib.inf142.assignment3.rip.common.Protocol;

public class ClientMain {

	public static void main(String[] args) {
		InetAddress localhost = null;
		
		try {
			localhost = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
		}
		
		InetSocketAddress server = new InetSocketAddress(localhost, Protocol.SERVERPORT);
		InetSocketAddress relay = new InetSocketAddress(localhost, Protocol.RELAYPORT);
		
		try {
			RIPSocket ripsocket = new RIPSocket(server, relay);
			ripsocket.send("hei alle sammen");
			//ripsocket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
}
