package no.uib.inf142.assignment3.rip.common;

import java.util.HashMap;
import java.util.Map;

public class SignalMap {

	private static final SignalMap instance = new SignalMap();

	private Map<String, Signal> map;

	private SignalMap() {
		map = new HashMap<String, Signal>();

		for (Signal signal : Signal.values()) {
			map.put(signal.getString(), signal);
		}
	}

	public Signal getByString(final String string) {
		return map.get(string);
	}

	public static SignalMap getInstance() {
		return instance;
	}
}
