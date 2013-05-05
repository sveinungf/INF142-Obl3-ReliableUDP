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

    public static String buildDelimitedString(final String delimiter,
            final String... values) {

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

    public static String convertIntToHexString(final int number) {
        return String.format("%08x", Integer.valueOf(number));
    }

    public static int convertFromHexStringToInt(final String hex)
            throws InvalidPacketException {

        return Integer.parseInt(hex, HEXADECIMAL);
    }

    public static String makeSpaces(final int length) {
        return new String(new char[length]).replace('\0', ' ');
    }

    public static String calculateMD5(final String data) {
        String checksum;

        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] digest = md.digest(data.getBytes());
            BigInteger number = new BigInteger(1, digest);
            checksum = number.toString(HEXADECIMAL);
        } catch (NoSuchAlgorithmException e) {
            checksum = null;
        }

        return checksum;
    }

    public static String getChecksum(final int checksumLength, final String data) {
        String md5 = calculateMD5(data);
        return md5.substring(0, checksumLength);
    }

    public static boolean validChecksum(final String toValidate,
            final String checksum) {

        String expected = getChecksum(checksum.length(), toValidate);
        return expected.equals(checksum);
    }

    public static void verifyChecksumInPayload(final String payload)
            throws InvalidPacketException {

        int splits = Datafield.SEQUENCE.ordinal() + 1;
        String[] items = payload.split(Protocol.DATAFIELD_DELIMITER, splits);
        String interestingPart = items[splits - 1];

        int lastDelimiter = interestingPart
                .lastIndexOf(Protocol.DATAFIELD_DELIMITER);

        if (lastDelimiter == -1) {
            throw new InvalidPacketException("Invalid packet");
        }

        String toValidate = interestingPart.substring(0, lastDelimiter);
        String checksum = interestingPart.substring(lastDelimiter + 1).trim();

        if (!validChecksum(toValidate, checksum)) {
            throw new InvalidPacketException("Wrong checksum");
        }
    }

    public static String getPayloadFromPacket(final DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength());
    }

    /**
     * Splits the packet payload into an array of datafields. The actual data
     * may contain the datafield delimiter.
     * 
     * @param payload
     *            - the packet payload.
     * @return the array of datafields.
     * @throws InvalidPacketException
     */
    public static String[] getDatafields(final String payload)
            throws InvalidPacketException {
        String delimiter = Protocol.DATAFIELD_DELIMITER;
        int dataFields = Datafield.values().length;

        String[] items = payload.split(delimiter, dataFields - 1);

        if (items.length < dataFields - 1) {
            throw new InvalidPacketException(
                    "Packet contains too few datafields");
        }

        String signalString = items[Datafield.SIGNAL.ordinal()];
        Signal signal = SignalMap.getInstance().getByString(signalString);
        int lastIndex = items.length - 1;

        if (signal == Signal.REGULAR || signal == Signal.PARTIAL) {
            String dataAndChecksum = items[lastIndex];
            int lastDelimiter = dataAndChecksum.lastIndexOf(delimiter);
            String data = dataAndChecksum.substring(0, lastDelimiter);
            String checksum = dataAndChecksum.substring(lastDelimiter + 1);

            int i = 0;
            String[] fields = new String[items.length + 1];

            while (i < lastIndex) {
                fields[i] = items[i];
                ++i;
            }

            fields[i] = data;
            ++i;
            fields[i] = checksum.trim();

            return fields;
        } else {
            items[lastIndex] = items[lastIndex].trim();
            return items;
        }
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
