/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jtransc;

public class JTranscEventLoop {
	static public Impl impl = new Impl() {
		@Override
		public void init(Runnable init) {
			init.run();
		}

		@Override
		public void loop(Runnable update, Runnable render) {
			try {
				while (true) {
					update.run();
					render.run();
					Thread.sleep(20L);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	public interface Impl {
		void init(Runnable init);
		void loop(Runnable update, Runnable render);
	}

	static public void init(Runnable init) {
		impl.init(init);
	}

	static public void loop(Runnable update, Runnable render) {
		impl.loop(update, render);
	}
}

// import java.util.*;
//public class JTranscEventLoop {
//	// @TODO: Very slow! But enough because it will be replaced with a native implementation!
//	static private final List<QueuedEvent> all = new ArrayList<QueuedEvent>();
//	static private final PriorityQueue<QueuedEvent> queued = new PriorityQueue<QueuedEvent>(0, new Comparator<QueuedEvent>() {
//		@Override
//		public int compare(QueuedEvent o1, QueuedEvent o2) {
//			return Long.compare(o1.time, o2.time);
//		}
//	});
//	static private final Queue<QueuedEvent> available = new LinkedList<QueuedEvent>();
//
//	static public void start(Runnable run) {
//		run.run();
//		try {
//			process();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
//
//	static private void process() throws InterruptedException {
//		while (queued.size() > 0) {
//			long now = System.currentTimeMillis();
//			while (queued.size() > 0) {
//				QueuedEvent event = queued.peek();
//				if (now >= event.time) {
//					boolean removed = true;
//					queued.remove();
//
//					if (!event.cancelled) {
//						if (event.timeIncrement != 0) {
//							event.time += event.timeIncrement;
//							queued.add(event.updateTime(event.time + event.timeIncrement));
//							removed = false;
//						}
//					}
//
//					if (removed) {
//						available.add(event);
//					}
//					event.run.run();
//				} else {
//					break;
//				}
//			}
//			Thread.sleep(1L);
//		}
//	}
//
//	static private QueuedEvent alloc() {
//		if (available.size() == 0) {
//			QueuedEvent event = new QueuedEvent(all.size());
//			all.add(event);
//			available.add(event);
//		}
//		return available.remove();
//	}
//
//	static public QueuedEvent get(int id) {
//		return all.get(id);
//	}
//
//	static public void queue(Runnable run) {
//		QueuedEvent event = alloc().set(run, 0, 0);
//		queued.add(event);
//	}
//
//	static public int setTimeout(Runnable run, int ms) {
//		QueuedEvent event = alloc().set(run, System.currentTimeMillis() + ms, 0);
//		queued.add(event);
//		return event.id;
//	}
//
//	static public int setInterval(Runnable run, int ms) {
//		QueuedEvent event = alloc().set(run, System.currentTimeMillis() + ms, ms);
//		queued.add(event);
//		return event.id;
//	}
//
//	static public void clearInterval(int interval) {
//		get(interval).cancelled = true;
//	}
//
//	static public void clearTimeout(int timeout) {
//		get(timeout).cancelled = true;
//	}
//}
//
//class QueuedEvent {
//	public final int id;
//	public Runnable run;
//	public boolean cancelled;
//	public long time;
//	public long timeIncrement;
//
//	public QueuedEvent(int id) {
//		this.id = id;
//	}
//
//	public QueuedEvent updateTime(long time) {
//		this.time = time;
//		return this;
//	}
//
//	public QueuedEvent set(Runnable run, long time, long timeIncrement) {
//		this.run = run;
//		this.time = time;
//		this.timeIncrement = timeIncrement;
//		this.cancelled = false;
//		return this;
//	}
//}
