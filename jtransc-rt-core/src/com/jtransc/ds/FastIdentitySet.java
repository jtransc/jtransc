package com.jtransc.ds;

import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscMethodBody;

import java.util.IdentityHashMap;

@JTranscInvisible
public class FastIdentitySet<T> {
	private IdentityHashMap<T, Boolean> set;

	@JTranscMethodBody(target = "js", value = "this.data = new Set();")
	public FastIdentitySet() {
		this.set = new IdentityHashMap<T, Boolean>();
	}

	@JTranscMethodBody(target = "js", value = "this.data.add(p0);")
	public void add(T value) {
		set.put(value, true);
	}

	@JTranscMethodBody(target = "js", value = "return this.data.size;")
	public int size() {
		return set.size();
	}

	public void addAll(T[] values) {
		for (T value : values) add(value);
	}

	@JTranscMethodBody(target = "js", value = "return this.data.has(p0);")
	public boolean has(T value) {
		return set.containsKey(value);
	}

	@JTranscMethodBody(target = "js", value = "return JA_L.fromArray(Array.from(this.data), p0.desc);")
	public T[] toArray(T[] out) {
		return set.keySet().toArray(out);
	}

}
