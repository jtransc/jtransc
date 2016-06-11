package com.jtransc.text

import org.junit.Assert
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Phaser

class StdoutRouterStreamTest {
	@Test
	fun testParallelCapturing() {
		val exec = Executors.newCachedThreadPool();
		val phaser = Phaser(2)
		val task1 = exec.submit(Callable({
			captureStdout {
				phaser.arriveAndAwaitAdvance()
				println("hello")
			}
		}))
		val task2 = exec.submit(Callable({
			captureStdout {
				phaser.arriveAndAwaitAdvance()
				println("world")
			}
		}))
		Assert.assertEquals("hello", task1.get().trim())
		Assert.assertEquals("world", task2.get().trim())
	}
}