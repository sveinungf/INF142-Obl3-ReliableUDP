package no.uib.inf142.assignment3.rip.common;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import no.uib.inf142.assignment3.rip.exception.InvalidPacketException;

public class PacketUtils {

	public static final int HEXADECIMAL = 16;
	public static final int MAX_HEX_LENGTH = 8;

	public static String buildDelimitedString(String delimiter,
			String... values) {

		StringBuilder sb = new StringBuilder();

		if (values != null && values.length > 0) {
			sb.append(values[0]);

			for (int i = 1; i < values.length; ++i) {
				sb.append(delimiter);
				sb.append(values[i]);
			}
		}

		return sb.toString();
	}

	public static String convertToHexString(int number) {
		return String.format("%08x", Integer.valueOf(number));
	}

	public static int convertFromHexString(String hex)
			throws InvalidPacketException {

		return Integer.parseInt(hex, HEXADECIMAL);
	}

	public static String makeSpaces(int length) {
		return new String(new char[length]).replace('\0', ' ');
	}

	public static String calculateMD5(String data) {
		String checksum = "";

		try {
			MessageDigest md = MessageDigest.getInstance("md5");
			byte[] digest = md.digest(data.getBytes());
			BigInteger number = new BigInteger(1, digest);
			checksum = number.toString(HEXADECIMAL);
		} catch (NoSuchAlgorithmException e) {
		}

		return checksum;
	}

	public static String getChecksum(int checksumLength, String data) {
		String md5 = calculateMD5(data);
		return md5.substring(0, checksumLength);
	}

	public static boolean validChecksum(String toValidate, String checksum) {
		String expected = getChecksum(checksum.length(), toValidate);
		return expected.equals(checksum);
	}

	public static boolean validChecksumInPacket(String packetData) {
		int splits = Datafield.SEQUENCE.ordinal() + 1;
		String[] items = packetData.split(Protocol.PACKET_DELIMITER, splits);
		String interestingPart = items[splits - 1];

		int lastDelimiter = interestingPart
				.lastIndexOf(Protocol.PACKET_DELIMITER);

		String toValidate = interestingPart.substring(0, lastDelimiter);
		String checksum = interestingPart.substring(lastDelimiter + 1).trim();

		return validChecksum(toValidate, checksum);
	}

	public static String getDataFromPacket(DatagramPacket packet) {
		return new String(packet.getData(), 0, packet.getLength());
	}

	public static String[] getFields(String payload) {
		String delimiter = Protocol.PACKET_DELIMITER;
		String[] items = payload.split(delimiter, 5);
		String dataAndChecksum = items[items.length - 1];
		int lastDelimiter = dataAndChecksum.lastIndexOf(delimiter);
		String data = dataAndChecksum.substring(0, lastDelimiter);
		String checksum = dataAndChecksum.substring(lastDelimiter + 1);
		String[] fields = new String[items.length + 1];

		int i = 0;
		while (i < items.length - 1) {
			fields[i] = items[i];
			++i;
		}

		fields[i] = data;
		++i;
		fields[i] = checksum.trim();

		return fields;
	}

	public static InetSocketAddress parseSocketAddress(final String ipString,
			final String portString) throws InvalidPacketException {

		InetSocketAddress socketAddress = null;
		String tempIPString = ipString;

		int slashPosition = tempIPString.indexOf("/");

		if (slashPosition != -1) {
			tempIPString = tempIPString.substring(slashPosition + 1);
		}

		try {
			InetAddress ip = InetAddress.getByName(tempIPString);
			int port = Integer.parseInt(portString);

			socketAddress = new InetSocketAddress(ip, port);
		} catch (IllegalArgumentException e) {
			throw new InvalidPacketException("Port not valid");
		} catch (UnknownHostException e) {
			throw new InvalidPacketException("IP not valid");
		}

		return socketAddress;
	}
}
