package no.uib.inf142.assignment3.rip.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestingServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		byte[] data = "hei".getBytes();
		InetAddress dest = null;
		try {
			dest = InetAddress.getLocalHost();
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		DatagramPacket packet = new DatagramPacket(data, data.length, dest, 54322);
		DatagramSocket socket;
		try {
			socket = new DatagramSocket();
			socket.send(packet);
			socket.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println("test done");
	}

}
