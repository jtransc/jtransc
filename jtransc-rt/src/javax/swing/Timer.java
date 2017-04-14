package javax.swing;

import com.jtransc.widgets.JTranscWidgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Timer {
	int delay;
	ActionListener listener;
	private volatile String actionCommand;
	private boolean running = false;
	Runnable step;

	public Timer(int delay, ActionListener listener) {
		this.delay = delay;
		this.listener = listener;
		this.step = new Runnable() {
			@Override
			public void run() {
				step();
			}
		};
	}

	private void step() {
		if (running) {
			listener.actionPerformed(new ActionEvent(this, 0, actionCommand));
			JTranscWidgets.impl.setTimeout(delay, step);
		}
	}

	public void stop() {
		if (!running) return;
		running = false;
	}

	public void restart() {
		// @TODO: Must restart
		System.out.println("Not implemented Timer.start()");
		running = true;
	}

	public void start() {
		if (running) return;
		running = true;
		JTranscWidgets.impl.setTimeout(delay, step);
	}
}
