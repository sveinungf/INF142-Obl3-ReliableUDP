package no.uib.inf142.assignment3.rip.client;

import java.util.Calendar;

public final class SimpleTimer {

    private long delayInMillis;
    private long expirationTimeInMillis;

    public SimpleTimer(final long delayInMillis) {
        this.delayInMillis = delayInMillis;
        restart();
    }

    public long getDelayInMillis() {
        return delayInMillis;
    }

    public void setDelayInMillis(final long delayInMillis) {
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
