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

package com.jtransc.ds;

import com.jtransc.annotation.*;




import java.util.HashMap;

@JTranscInvisible
public class FastStringMap<T> {
	private HashMap<String, T> map;


	@JTranscMethodBody(target = "js", value = "this.data = new Map();")
	public FastStringMap() {
		this.map = new HashMap<String, T>();
	}


	@JTranscMethodBody(target = "js", value = "return this.data.get(N.istr(p0));")
	public T get(String key) {
		return this.map.get(key);
	}


	@JTranscMethodBody(target = "js", value = "this.data.set(N.istr(p0), p1);")
	public void set(String key, T value) {
		this.map.put(key, value);
	}


	@JTranscMethodBody(target = "js", value = "return this.data.has(N.istr(p0));")
	public boolean has(String key) {
		return this.map.containsKey(key);
	}


	@JTranscMethodBody(target = "js", value = "return JA_L.fromArray1(Array.from(this.data.keys()).map(function(it) { return N.str(it); }), 'Ljava/lang/String;');")
	public String[] getKeys() {
		String[] out = new String[map.size()];
		int n = 0;
		for (String key : this.map.keySet()) out[n++] = key;
		return out;
	}

	public T[] getValues() {
		String[] keys = getKeys();
		Object[] values = new Object[keys.length];
		for (int n = 0; n < keys.length; n++) values[n] = get(keys[n]);
		return (T[])values;
	}
}
