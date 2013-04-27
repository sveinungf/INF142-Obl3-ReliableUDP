package no.uib.inf142.assignment3.rip.common;

import static org.junit.Assert.*;

import org.junit.Test;

public class PacketUtilsTest {

	@Test
	public void regularDelimitedString() {
		String a = "a";
		String b = "b";
		String expected = "a;b";
		String actual = PacketUtils.buildDelimitedString(";", a, b);

		assertEquals(expected, actual);
	}

	@Test
	public void emptyStringInDelimitedString() {
		String empty = "";
		String b = "b";
		String expected = ";;b;";
		String actual = PacketUtils.buildDelimitedString(";", empty, empty, b,
				empty);

		assertEquals(expected, actual);
	}

	@Test
	public void validMD5() {
		String string = "teststring";
		String expected = "d67c5cbf5b01c9f91932e3b8def5e5f8";
		String actual = PacketUtils.calculateMD5(string);

		assertEquals(expected, actual);
	}

	@Test
	public void invalidMD5() {
		String string = "teststring ";
		String expected = "d67c5cbf5b01c9f91932e3b8def5e5f8";
		String actual = PacketUtils.calculateMD5(string);

		assertFalse(expected.equals(actual));
	}

	@Test
	public void validChecksumLength() {
		String string = "data";
		String checksum = PacketUtils.getChecksum(string);

		assertEquals(ProtocolConstants.CHECKSUM_LENGTH, checksum.length());
	}
}
