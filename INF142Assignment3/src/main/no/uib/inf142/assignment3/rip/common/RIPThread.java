package no.uib.inf142.assignment3.rip.common;

public abstract class RIPThread extends Thread {

    protected boolean active;
    protected Exception exception;

    public RIPThread() {
        active = true;
        exception = new Exception("An error occured");
    }

    public final Exception getException() {
        return exception;
    }

    public final boolean isActive() {
        return active;
    }

    public final void setActive(final boolean active) {
        this.active = active;
    }

    @Override
    public abstract void run();
}
