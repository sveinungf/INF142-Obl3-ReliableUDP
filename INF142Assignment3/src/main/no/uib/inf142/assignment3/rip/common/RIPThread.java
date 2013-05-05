package no.uib.inf142.assignment3.rip.common;

public abstract class RIPThread extends Thread {

    protected boolean active;
    protected boolean closed;
    protected Exception exception;

    public RIPThread() {
        active = true;
        closed = false;
        exception = new Exception("An error occured");
    }

    public final boolean isClosed() {
        return closed;
    }

    public final Exception getException() {
        return exception;
    }

    @Override
    public abstract void run();
}
