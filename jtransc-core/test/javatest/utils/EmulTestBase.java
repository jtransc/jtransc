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

/**
 * Created by mike on 10/12/15.
 */
public class EmulTestBase {

    protected void assertEquals(Object o1, Object o2) {
        assertEquals("EQUAL: ", o1, o2);
    }

    protected void assertEquals(String msg, Object o1, Object o2) {
        System.out.println(msg + " = " + (o1 == null ? o2 == null : o1.equals(o2)));
    }

    protected void assertSame(String msg, Object o1, Object o2) {
        System.out.println(msg + " === " + o1 == o2);
    }

    protected void assertTrue(boolean b) {
        assertTrue("Should be TRUE: ", b);
    }

    protected void assertTrue(String msg, boolean b) {
        System.out.println(msg + " = " + (b == true));
    }

    protected static void assertFalse(String msg, boolean b) {
        System.out.println(msg + " = " + (b == false));
    }

    protected static void assertNull(String msg, Object o) {
        System.out.println(msg + " = " + (o == null));
    }

    protected void fail() {
        fail("");
    }

    protected void fail(String msg) {
        System.out.println("FAIL: " + msg);
    }
}
