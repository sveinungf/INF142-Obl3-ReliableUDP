package no.uib.inf142.assignment3.rip.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PacketUtils {

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

	public static String makeChecksum(String data) {
		String checksum;

		try {
			MessageDigest md = MessageDigest.getInstance("md5");
			md.update(data.getBytes());
			checksum = Integer.toHexString(md.digest()[0]);
		} catch (NoSuchAlgorithmException e) {
			checksum = "";
		}

		return checksum;
	}
}
