package no.uib.inf142.assignment3.rip.common.enums;

import java.util.HashMap;
import java.util.Map;


public final class SignalMap {

	private static final SignalMap INSTANCE = new SignalMap();

	private final Map<String, Signal> map;
	private final int maxSignalLength;

	private SignalMap() {
		int maxLength = 0;
		map = new HashMap<String, Signal>();

		for (Signal signal : Signal.values()) {
			String signalString = signal.getString();
			int signalLength = signalString.length();

			map.put(signalString, signal);

			if (signalLength > maxLength) {
				maxLength = signalLength;
			}
		}

		maxSignalLength = maxLength;
	}

	public Signal getByString(final String string) {
		return map.get(string);
	}

	public int getMaxSignalLength() {
		return maxSignalLength;
	}

	public static SignalMap getInstance() {
		return INSTANCE;
	}
}
