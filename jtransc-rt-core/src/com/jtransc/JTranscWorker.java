package com.jtransc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class JTranscWorker {
	private Thread thread;
	static private WeakHashMap<Thread, JTranscWorker> workers = new WeakHashMap<>();
	private LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();

	static {
		if (isMain()) {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				for (JTranscWorker worker : new HashMap<>(workers).values()) {
					//System.out.println("Terminating");
					worker.terminate();
				}
			}));
		}
	}

	public class Message {
		public String kind;
		public byte[] data;

		public Message(String kind, byte[] data) {
			this.kind = kind;
			this.data = data;
		}
	}

	public JTranscWorker(Thread thread) {
		this.thread = thread;
	}

	static public boolean isMain() {
		return Thread.currentThread().getId() <= 1L;
	}

	static public long getId() {
		return Thread.currentThread().getId();
	}

	public void postMessage(String kind, byte[] data) {
		queue.add(new Message(kind, (data != null) ? Arrays.copyOf(data, data.length) : null));
	}

	public void terminate() {
		thread.interrupt();
		workers.remove(thread);
	}

	static public void loop(Consumer<Message> handler) {
		JTranscWorker worker = workers.get(Thread.currentThread());
		try {
			while (true) {
				Message message = worker.queue.take();
				handler.accept(message);
			}
		} catch (InterruptedException e) {

		}
	}

	static public JTranscWorker startWorker() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		StackTraceElement stackTraceElement = stackTraceElements[stackTraceElements.length - 1];
		Thread thread = new Thread(() -> {
			try {
				Class.forName(stackTraceElement.getClassName()).getMethod("main", String[].class).invoke(null, (Object) new String[] { "--worker" });
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
		thread.setDaemon(true);
		thread.start();
		JTranscWorker worker = new JTranscWorker(thread);
		workers.put(thread, worker);
		return worker;
	}

	// Sample
	static public void main(String[] args) {
		if (isMain()) {
			JTranscWorker worker1 = JTranscWorker.startWorker();
			JTranscWorker worker2 = JTranscWorker.startWorker();
			worker1.postMessage("Hello", null);
			worker2.postMessage("World", null);
		} else {
			JTranscWorker.loop(message -> {
				System.out.println("WORKER! " + getId() + " : " + message.kind);
			});
		}
		//StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		//stackTraceElements[stackTraceElements.length - 1].getClassName()
		//for (StackTraceElement stackTraceElement : stackTrace) {
		//	System.out.println(stackTraceElement);
		//}
	}
}
