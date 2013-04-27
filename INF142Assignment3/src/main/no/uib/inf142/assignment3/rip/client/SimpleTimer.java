package no.uib.inf142.assignment3.rip.client;

import java.util.Calendar;

public class SimpleTimer {

	private long delayInMillis;
	private long expirationTimeInMillis;

	public SimpleTimer(long delayInMillis) {
		this.delayInMillis = delayInMillis;
		restart();
	}

	public long getDelayInMillis() {
		return delayInMillis;
	}

	public void setDelayInMillis(long delayInMillis) {
		this.delayInMillis = delayInMillis;
	}

	public boolean timedOut() {
		Calendar now = Calendar.getInstance();
		return now.getTimeInMillis() > expirationTimeInMillis;
	}

	public void restart() {
		Calendar now = Calendar.getInstance();
		expirationTimeInMillis = now.getTimeInMillis() + delayInMillis;
	}
}
