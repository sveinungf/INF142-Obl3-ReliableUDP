package no.uib.inf142.assignment3.rip;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class RIPPacket {

	public static final String DELIMITER = ";";

	public static DatagramPacket makePacket(InetSocketAddress toBeStored,
			InetSocketAddress destination, String data) throws SocketException {

		String ip = toBeStored.getAddress().getHostAddress();
		String portString = "" + toBeStored.getPort();

		String payload = buildDelimitedString(ip, portString, data);
		byte[] byteData = payload.getBytes();

		return new DatagramPacket(byteData, byteData.length, destination);
	}

	public static DatagramPacket makePacket(InetSocketAddress toBeStored,
			InetSocketAddress destination, int seq, int ack, Signal signal,
			String data) throws SocketException {

		String seqString = "" + seq;
		String ackString = "" + ack;

		String partialPayload = buildDelimitedString(seqString, ackString,
				signal.getString());

		return makePacket(toBeStored, destination, partialPayload);
	}

	public static DatagramPacket reformat(DatagramPacket packet)
			throws RIPException, SocketException {

		String data = new String(packet.getData(), 0, packet.getLength());
		InetSocketAddress source = new InetSocketAddress(packet.getAddress(),
				packet.getPort());
		InetSocketAddress destination;
		String[] items = data.split(DELIMITER, 3);

		try {
			InetAddress ip = InetAddress.getByName(items[0]);
			int port = Integer.parseInt(items[1]);
			destination = new InetSocketAddress(ip, port);
		} catch (UnknownHostException e) {
			throw new RIPException("Illegal hostname");
		} catch (NumberFormatException e) {
			throw new RIPException("Illegal port");
		}

		return makePacket(source, destination, items[items.length - 1]);
	}

	public static String buildDelimitedString(String... values) {
		StringBuilder sb = new StringBuilder();

		if (values != null && values.length > 0) {
			sb.append(values[0]);

			for (int i = 1; i < values.length; ++i) {
				sb.append(DELIMITER);
				sb.append(values[i]);
			}
		}

		return sb.toString();
	}
}
