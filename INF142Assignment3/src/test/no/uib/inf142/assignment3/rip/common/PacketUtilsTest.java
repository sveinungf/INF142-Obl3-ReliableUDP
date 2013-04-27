package no.uib.inf142.assignment3.rip.common;

import static org.junit.Assert.*;

import org.junit.Test;

public class PacketUtilsTest {

	@Test
	public void validChecksum() {
		String string = "teststring";
		String md5hash = "d67c5cbf5b01c9f91932e3b8def5e5f8";
		String checksum = PacketUtils.makeChecksum(string);

		assertEquals(md5hash.substring(1, 2), checksum);
	}
}
