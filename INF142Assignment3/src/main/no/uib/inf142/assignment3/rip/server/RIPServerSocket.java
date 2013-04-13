package no.uib.inf142.assignment3.rip.server;

import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

public class RIPServerSocket {

	private ReceiverThread receiver;

	public RIPServerSocket(int port) throws SocketException {
		receiver = new ReceiverThread(port);
	}

	public String receive() {
		BlockingQueue<DatagramPacket> buffer = receiver.getBuffer();
		new Thread(receiver).start();
		
		try {
			buffer.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("done");
		
		return null;
	}

	public void close() {
		// TODO
	}
}

// - a constructor that produces a RIPServerSocket object, and which similarly
// to a TCP ServerSocket constructor assigns the RIPServerSocket to a port
// number.
//
// - a method String receive() that returns a String object received on the
// connection.
//
// - a method close() that closes the connection.
//
// - The RIPServerSocket should also notify the user of errors and other
// situations that can occur (e.g. lost connection, bad data, data out of order
// (really should not happen), the other party closed connection, or similar),
// for example by throwing an exception.
