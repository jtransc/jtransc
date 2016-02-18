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

package javatest.utils;

import java.util.*;

/**
 * Created by mike on 4/11/15.
 */
public class CollectionsTest {

    public static void main(String[] args) throws Throwable {
        setTest();
        mapTest();
        listTest();

        System.out.println("---------------------------- ArrayList test");
        new TestArrayList().test();
        System.out.println("---------------------------- HashMap test");
        new TestMap().test();
    }

    private static void setTest() {
        Set<String> set = new HashSet<String>();

        // add
        set.add("first");
        set.add("first");
        set.add("second");

        // size
        out("HashSet.size() = 2: " + set.size());

        // remove
        set.remove("first");
        out("HashSet.size() = 1: " + set.size());

        // clear
        set.clear();
        out("HashSet.size() = 0: " + set.size());

        // toArray
        set.add("first");
        set.add("second");
        set.add("third");
        Object[] items = set.toArray();
        out("HashSet.toArray: item " + items.length);

        // addAll
        Set<String> set2 = new HashSet<String>();
        set2.add("1");
        set2.add("2");

        set.addAll(set2);
        out("HashSet.addAll: items " + set.size());

        // Iterator
        StringBuilder sb = new StringBuilder();
        for(String key : set) {
            if (key.equals("first") || key.equals("second") || key.equals("third")) sb.append("*");
        }
        out("Elements: " + sb.toString());
    }

    private static void mapTest() {
        Map<String, String> map = new HashMap<String, String>();
        StringBuilder sb = new StringBuilder();

        // put
        sb.append(map.size());
        map.put("hello", "world");

        // get
        sb.append(map.get("hello"));
        sb.append(map.size());
        map.put("hello", "world");
        sb.append(map.size());
        map.put("hello2", "world");
        sb.append(map.size());

        // isEmpty
        out("HashMap.isEmpty false:" + map.isEmpty());

        // clear
        map.clear();
        out("HashMap.clear:" + map.size());
        out("HashMap.isEmpty true:" + map.isEmpty());

        // putAll
        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("hello3", "world");
        map.putAll(map2);
        out("HashMap.putAll: " + map);

        // containsKey
        out("HashMap.containsKey true: " + map.containsKey("hello3"));
        out("HashMap.containsKey false: " + map.containsKey("helloX"));

        // containsValue
        out("HashMap.containsValue true: " + map.containsValue("world"));
        out("HashMap.containsValue false: " + map.containsValue("XXXX"));

        // remove
        out("HashMap.remove: " + map2.remove("hello3"));

        // keySet
        for (String key: map.keySet()) {
            out("HashMap.keySet: " + key);
        }

        // values
        for (String value: map.values()) {
            out("HashMap.values: " + value);
        }

        // entrySet
        Set<Map.Entry<String, String>> entries = map.entrySet();
        for (Map.Entry entry : entries) {
            out(entry.getKey() + ":" + entry.getValue());
        }
    }

    private static void listTest() {
        List<String> list = new ArrayList<String>();

        // add/get
        list.add("first");
        list.add("second");
        out("List.add/get(0): " + list.get(0));
        out("List.add/get(1): " + list.get(1));
        list.add(0, "newfirst");
        out("List.add/get(0): " + list.get(0));
        out("List.add/get(1): " + list.get(1));

        // size
        out("List.size: " + list.size());

        // addAll
        List<String> list2 = new ArrayList<String>();
        list2.add("3");
        list2.add("4");
        list.addAll(list2);
        out("List.addAll: " + list.size());
    }

    private static void out(String msg) {
        System.out.println(msg);
    }
}