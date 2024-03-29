package no.uib.inf142.assignment3.rip.common;

import static org.junit.Assert.*;

import no.uib.inf142.assignment3.rip.exception.InvalidPacketException;

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
    public void validChecksumInNormalPacket() throws InvalidPacketException {
        String packetData = "/127.0.0.1;62428;00000001;P;hijklmn;d4c  ";

        PacketUtils.verifyChecksumInPayload(packetData);
    }

    @Test
    public void validChecksumInPacketWithDelimiter() throws InvalidPacketException {
        String packetData = "/127.0.0.1;62428;00000001;P;  hi;jklmn;  ;6b2  ";

        PacketUtils.verifyChecksumInPayload(packetData);
    }

    @Test
    public void validChecksumInSignalPacket() throws InvalidPacketException {
        String packetData = "/127.0.0.1;55939;00000000;S;166              ";

        PacketUtils.verifyChecksumInPayload(packetData);
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
        String actual = PacketUtils.convertIntToHexString(number);

        assertEquals(expected, actual);
    }

    @Test
    public void noLeadingZerosInHexString() {
        int number = 161;
        String expected = "a1";
        String actual = PacketUtils.convertIntToHexString(number);

        assertFalse(expected.equals(actual));
    }

    @Test
    public void hexOfNumberGreaterThanIntegerMax() {
        int number = Integer.MAX_VALUE;
        String expected = PacketUtils.convertIntToHexString(Integer.MIN_VALUE);
        String actual = PacketUtils.convertIntToHexString(number + 1);

        assertEquals(expected, actual);
    }

    @Test
    public void hexToIntAndBack() throws InvalidPacketException {
        String expected = "0000e0c4";
        int number = PacketUtils.convertFromHexStringToInt(expected);
        String actual = PacketUtils.convertIntToHexString(number);

        assertEquals(expected, actual);
    }

    @Test
    public void intToHexAndBack() throws InvalidPacketException {
        int expected = 846205;
        String hex = PacketUtils.convertIntToHexString(expected);
        int actual = PacketUtils.convertFromHexStringToInt(hex);

        assertEquals(expected, actual);
    }

    @Test
    public void makeSpaces() {
        int length = 10;
        String expected = "          ";
        String actual = PacketUtils.makeSpaces(length);

        assertEquals(expected, actual);
    }

    @Test
    public void getFieldsNormalData() throws InvalidPacketException {
        String data = "/127.0.0.1;55555;00000001;P;lashdf;d9e        ";
        String[] datafields = PacketUtils.getDatafields(data);

        assertEquals("/127.0.0.1", datafields[0]);
        assertEquals("55555", datafields[1]);
        assertEquals("00000001", datafields[2]);
        assertEquals("P", datafields[3]);
        assertEquals("lashdf", datafields[4]);
        assertEquals("d9e", datafields[5]);
    }

    @Test
    public void getFieldsDelimiterInData() throws InvalidPacketException {
        String data = "/127.0.0.1;55555;00000001;P;las;hdf;d9e        ";
        String[] datafields = PacketUtils.getDatafields(data);

        assertEquals("/127.0.0.1", datafields[0]);
        assertEquals("55555", datafields[1]);
        assertEquals("00000001", datafields[2]);
        assertEquals("P", datafields[3]);
        assertEquals("las;hdf", datafields[4]);
        assertEquals("d9e", datafields[5]);
    }

    @Test
    public void getFieldsSpaceInData() throws InvalidPacketException {
        String data = "/127.0.0.1;55555;00000001;P;  ;la s;   ;d9e        ";
        String[] datafields = PacketUtils.getDatafields(data);

        assertEquals("/127.0.0.1", datafields[0]);
        assertEquals("55555", datafields[1]);
        assertEquals("00000001", datafields[2]);
        assertEquals("P", datafields[3]);
        assertEquals("  ;la s;   ", datafields[4]);
        assertEquals("d9e", datafields[5]);
    }

    @Test
    public void getFieldsSignalPacket() throws InvalidPacketException {
        String data = "/127.0.0.1;63164;00000000;S;166              ";
        String[] datafields = PacketUtils.getDatafields(data);

        assertEquals("/127.0.0.1", datafields[0]);
        assertEquals("63164", datafields[1]);
        assertEquals("00000000", datafields[2]);
        assertEquals("S", datafields[3]);
    }
}
