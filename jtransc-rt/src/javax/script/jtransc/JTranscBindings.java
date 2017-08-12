/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package javax.script.jtransc;

import javax.script.Bindings;
import java.util.HashMap;
import java.util.Map;

public class JTranscBindings extends HashMap<String, Object> implements Bindings {
	public Object put(String name, Object value) {
		return super.put(name, value);
	}

	public void putAll(Map<? extends String, ? extends Object> toMerge) {
		super.putAll(toMerge);
	}

	public boolean containsKey(Object key) {
		return super.containsKey(key);
	}

	public Object get(Object key) {
		return super.get(key);
	}

	public Object remove(Object key) {
		return super.remove(key);
	}
}
