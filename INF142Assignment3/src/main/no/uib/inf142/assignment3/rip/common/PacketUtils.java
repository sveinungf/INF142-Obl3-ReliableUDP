package no.uib.inf142.assignment3.rip.common;

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
}
