package no.uib.inf142.assignment3.rip.common;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import no.uib.inf142.assignment3.rip.exception.InvalidSocketAddressException;

public class PacketUtils {

	public static final int HEXADECIMAL = 16;

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

	public static InetSocketAddress parseSocketAddress(final String ipString,
			final String portString) throws InvalidSocketAddressException {

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
			throw new InvalidSocketAddressException("Port not valid");
		} catch (UnknownHostException e) {
			throw new InvalidSocketAddressException("IP not valid");
		}

		return socketAddress;
	}
}
