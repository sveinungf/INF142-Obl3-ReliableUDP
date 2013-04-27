package no.uib.inf142.assignment3.rip.client;

import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleTimerTest {

	@Test
	public void timedOut() throws InterruptedException {
		long ms = 1;
		SimpleTimer timer = new SimpleTimer(ms);
		Thread.sleep(ms + 1);

		assertTrue(timer.timedOut());
	}

	@Test
	public void notTimedOut() {
		long ms = 1000;
		SimpleTimer timer = new SimpleTimer(ms);

		assertFalse(timer.timedOut());
	}

	@Test
	public void noLongertimedOutAfterRestart() throws InterruptedException {
		long ms = 1;
		SimpleTimer timer = new SimpleTimer(ms);
		Thread.sleep(ms + 1);

		assertTrue(timer.timedOut());
		timer.setDelayInMillis(1000);
		timer.restart();
		assertFalse(timer.timedOut());
	}
}
