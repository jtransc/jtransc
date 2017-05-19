---
layout: default
title: "Call Haxe Libraries"
---

You can declare existant haxe classes using the `@JTranscNativeClass` annotation and marking methods as native.

An example could be:
{% highlight java %}
{% raw %}
@JTranscNativeClass("haxe.crypto.Adler32")
private static class HaxeAdler32 {
	public HaxeAdler32() { }
	native public boolean equals(HaxeAdler32 that);
	native public int get();
	native public void update(byte[] b, int pos, int len);
	native static public int make(byte[] b);
	native static public HaxeAdler32 read(InputStream i);
}
{% endraw %}
{% endhighlight %}

Or:

{% highlight java %}
{% raw %}
@JTranscNativeClass("StringTools")
static private class HaxeStringTools {
	native static String htmlEscape(String s);
	native static String htmlEscape(String s, boolean quotes);
}
{% endraw %}
{% endhighlight %}

Take into account that some types are converted when called this way.

* Primitive types: integral types will be converted to `Int`, but `long` that will be converted to `haxe.Int64`, and `float` and `double` will be converted to haxe `Float`.
* `byte[]` will be converted into `haxe.io.Bytes`
* `java.lang.String` will be converted into `haxe.String`.
* `java.io.InputStream` will be converted into `haxe.io.Input`. When returning, it will do the opposite.
* Other classes marked with `@JTranscNativeClass` will work directly without conversions.
* There is [`@HaxeNativeConversion`](JTransc-Annotations#haxe-specific) to specify a custom conversions.

This approach just works when targeting Haxe, so plase before using it, ensure you are using JTransc. Calling `JTranscSystem.isJTransc()` for example or with different mains injecting dependencies.

You can include `haxelib` libraries with the `@HaxeAddLibraries` annotation, and use `MiniTemplates` to reference java methods from Haxe code.

{% highlight java %}
{% raw %}
@HaxeAddMembers("var ws:haxe.net.WebSocket;")
@HaxeAddLibraries("haxe-ws:0.0.6")
class WebSocketHaxe(url: String, subprotocols: Array<String>?) : WebSocket(url) {
	init {
		ws_init(url, subprotocols)
		process()
	}

	@HaxeMethodBody("""
		if (p1 != null) {
			this.ws = haxe.net.WebSocket.create(p0._str, cast p1.toArray()); // Array<String>
		} else {
			this.ws = haxe.net.WebSocket.create(p0._str);
		}
		this.ws.onopen = function() { this{% IMETHOD nova.net.ws.WebSocketHaxe:onConnectSend %}(); };
		this.ws.onclose = function() { this{% IMETHOD nova.net.ws.WebSocketHaxe:onDisconnectedSend %}(); };
		this.ws.onmessageString = function(m:String) { this{% IMETHOD nova.net.ws.WebSocketHaxe:onStringMessageSend %}(N.str(m)); };
	""")
	private fun ws_init(url: String, subprotocols: Array<String>?) {
	}

	@HaxeMethodBody("this.ws.process();")
	private fun process_int(): Unit {
	}

	private fun process(): Unit {
		process_int()
		EventLoop.setTimeout(20) { process() }
	}

	@JTranscKeep
	private fun onConnectSend() = onConnect.dispatch(Unit)

	@JTranscKeep
	private fun onDisconnectedSend() = onDisconnected.dispatch(Unit)

	@JTranscKeep
	private fun onStringMessageSend(msg: String) = onStringMessage.dispatch(msg)

	@JTranscKeep
	private fun onBinaryMessageSend(msg: ByteArray) = onBinaryMessage.dispatch(msg)

	@HaxeMethodBody("")
	override fun connect() = super.connect()

	@HaxeMethodBody("this.ws.sendString(p0._str);")
	override fun send(message: String) = super.send(message)

	//@HaxeMethodBody("this.ws.sendBytes(haxe.io.Bytes.ofData(p0.data.view.getData()));")
	override fun send(message: ByteArray) = super.send(message)
}
{% endraw %}
{% endhighlight %}
