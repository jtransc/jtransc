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

import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscSync;




import java.util.HashMap;

@JTranscInvisible

public class FastIntIntMap {
    private HashMap<Integer, Integer> map;

	@JTranscMethodBody(target = "js", value = "this.map = new Map();")
	@JTranscSync
    public FastIntIntMap() {
        this.map = new HashMap<Integer, Integer>();
    }

	@JTranscMethodBody(target = "js", value = "return this.map.get(p0);")
	@JTranscSync
    public int get(int key) {
        return this.map.get(key);
    }

	@JTranscSync
	public Integer getOrNull(int key) {
		return has(key) ? get(key) : null;
	}

	@JTranscSync
	public int getOrDefault(int key, int defaultValue) {
		return has(key) ? get(key) : defaultValue;
	}

	@JTranscMethodBody(target = "js", value = "this.map.set(p0, p1);")
	@JTranscSync
    public void set(int key, int value) {
        this.map.put(key, value);
    }

	@JTranscMethodBody(target = "js", value = "return this.map.has(p0);")
	@JTranscSync
    public boolean has(int key) {
        return this.map.containsKey(key);
    }

	@JTranscMethodBody(target = "js", value = "this.map.delete(p0);")
	@JTranscSync
	public void remove(int key) {
		this.map.remove(key);
	}

	//
	//@JTranscMethodBody(target = "js", value = "return this.map.size;")
	//public int size() {
	//	return this.map.size();
	//}
}
