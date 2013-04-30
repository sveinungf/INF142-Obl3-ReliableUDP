package no.uib.inf142.assignment3.rip.common;

public abstract class RIPThread extends Thread {

	protected boolean active;
	protected Exception exception;

	public RIPThread() {
		active = true;
		exception = null;
	}

	public Exception getException() {
		return exception;
	}

	@Override
	public abstract void run();
}