package relay;

import static org.junit.Assert.*;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import no.uib.inf142.assignment3.rip.common.PacketUtils;
import no.uib.inf142.assignment3.rip.exception.InvalidPacketException;

import org.junit.Test;

public class RelayPakkeTest {

	@Test
	public void reformat() throws UnknownHostException, SocketException,
			InvalidPacketException {

		InetAddress initialtStoredIP = InetAddress.getByName("1.1.1.1");
		int initialStoredPort = 1;
		InetAddress initialSourceAddress = InetAddress.getByName("2.2.2.2");
		int initialSourcePort = 2;

		InetSocketAddress initialStoredAddress = new InetSocketAddress(
				initialtStoredIP, initialStoredPort);
		InetSocketAddress initialSource = new InetSocketAddress(
				initialSourceAddress, initialSourcePort);

		String initialData = PacketUtils.buildDelimitedString(";",
				initialtStoredIP.getHostAddress(), "" + initialStoredPort,
				"data");

		byte[] initialByteData = initialData.getBytes();
		DatagramPacket initialPacket = new DatagramPacket(initialByteData,
				initialByteData.length, initialSource);

		DatagramPacket alteredPacket = RelayPakke.reformat(initialPacket);

		String alteredData = new String(alteredPacket.getData(), 0,
				alteredPacket.getLength());
		String[] items = alteredData.split(";");

		InetSocketAddress storedAddress = PacketUtils.parseSocketAddress(
				items[0], items[1]);
		InetSocketAddress source = new InetSocketAddress(
				alteredPacket.getAddress(), alteredPacket.getPort());

		assertEquals(initialSource, storedAddress);
		assertEquals(initialStoredAddress, source);
	}
}
