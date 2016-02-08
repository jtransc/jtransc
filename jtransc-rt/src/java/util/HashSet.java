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

package java.util;

// @TODO: Very slow implementation!
public class HashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable, java.io.Serializable {
    private ArrayList<E> items = new ArrayList<E>();

    public HashSet() {
    }

    public HashSet(Collection<? extends E> c) {
        //this();
        addAll(c);
    }

    public HashSet(int initialCapacity, float loadFactor) {
        //this();
    }

    public HashSet(int initialCapacity) {
        //this();
    }

    HashSet(int initialCapacity, float loadFactor, boolean dummy) {
        //this();
    }

    public Iterator<E> iterator() {
        return items.iterator();
    }

    public int size() {
        return items.size();
    }

    public boolean contains(Object o) {
        return items.contains(o);
    }

    public boolean add(E e) {
        if (!contains(e)) {
            items.add(e);
            return true;
        } else {
            return false;
        }
    }

    public boolean remove(Object o) {
        return items.remove(o);
    }

    public void clear() {
        this.items.clear();
    }

    public Object clone() {
        return new HashSet<E>(items);
    }
}
