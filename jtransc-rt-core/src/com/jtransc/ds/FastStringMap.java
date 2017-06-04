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
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeRemoveField;

import java.util.HashMap;
import java.util.Set;

@JTranscInvisible
@HaxeAddMembers({"var _map = new Map<String, Dynamic>();"})
@JTranscAddMembersList({
	@JTranscAddMembers(target = "php", value = "public $data = null;"),
})
public class FastStringMap<T> {
    @HaxeRemoveField
	private HashMap<String, T> map;

    @HaxeMethodBody("")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "this.data = new Map();"),
		@JTranscMethodBody(target = "php", value = "$this->data = [];"),
	})
	public FastStringMap() {
		this.map = new HashMap<String, T>();
	}

	@HaxeMethodBody("return _map.get(p0._str);")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "return this.data.get(N.istr(p0));"),
		@JTranscMethodBody(target = "php", value = "return $this->data[N::istr($p0)];"),
	})
    public T get(String key) {
		return this.map.get(key);
	}

    @HaxeMethodBody("_map.set(p0._str, p1);")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "this.data.set(N.istr(p0), p1);"),
		@JTranscMethodBody(target = "php", value = "$this->data[N::istr($p0)] = $p1;"),
	})
	public void set(String key, T value) {
		this.map.put(key, value);
	}

    @HaxeMethodBody("return _map.exists(p0._str);")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "return this.data.has(N.istr(p0));"),
		@JTranscMethodBody(target = "php", value = "return isset($this->data[N::istr($p0)]);"),
	})
	public boolean has(String key) {
		return this.map.containsKey(key);
	}

	@HaxeMethodBody("return N.haxeStringArrayToJavaArray(N.haxeIteratorToArray(_map.keys()));")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "return JA_L.fromArray1(Array.from(this.data.keys()).map(function(it) { return N.str(it); }), 'Ljava/lang/String;');"),
		@JTranscMethodBody(target = "php", value = "return N::strArray(array_keys($this->data));"),
	})
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
