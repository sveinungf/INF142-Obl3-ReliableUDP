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
		String checksum = PacketUtils.getChecksum(3, string);

		assertEquals(3, checksum.length());
	}

	@Test
	public void validChecksum() {
		String text = "qwertyuiop!";
		String checksum = PacketUtils.getChecksum(3, text);

		assertTrue(PacketUtils.validChecksum(text, checksum));
	}

	@Test
	public void invalidChecksum() {
		String text = "qwertyuiop!";
		String checksum = PacketUtils.getChecksum(3, text);

		assertFalse(PacketUtils.validChecksum(text + " ", checksum));
	}

	@Test
	public void leadingZerosInHexString() {
		int number = 161;
		String expected = "000000a1";
		String actual = PacketUtils.convertToHexString(number);

		assertEquals(expected, actual);
	}

	@Test
	public void noLeadingZerosInHexString() {
		int number = 161;
		String expected = "a1";
		String actual = PacketUtils.convertToHexString(number);

		assertFalse(expected.equals(actual));
	}

	@Test
	public void hexOfNumberGreaterThanIntegerMax() {
		int number = Integer.MAX_VALUE;
		String expected = PacketUtils.convertToHexString(Integer.MIN_VALUE);
		String actual = PacketUtils.convertToHexString(number + 1);

		assertEquals(expected, actual);
	}

	@Test
	public void hexToIntAndBack() {
		String expected = "0000e0c4";
		int number = PacketUtils.convertFromHexString(expected);
		String actual = PacketUtils.convertToHexString(number);

		assertEquals(expected, actual);
	}

	@Test
	public void intToHexAndBack() {
		int expected = 846205;
		String hex = PacketUtils.convertToHexString(expected);
		int actual = PacketUtils.convertFromHexString(hex);

		assertEquals(expected, actual);
	}

	@Test
	public void makeSpaces() {
		int length = 10;
		String expected = "          ";
		String actual = PacketUtils.makeSpaces(length);

		assertEquals(expected, actual);
	}
}
