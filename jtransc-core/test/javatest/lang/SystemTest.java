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

package javatest.lang;

/**
 * Created by mike on 4/11/15.
 */
public class SystemTest {

    public static void main(String[] args) throws Throwable {
        systemOutTest();
        systemPropertiesTest();
    }

    private static void systemOutTest() {
        System.out.print("HELLO");
        System.out.println(" WORLD!");
        System.out.println("HELLO WORLD!");
    }

    private static void systemPropertiesTest() {
        System.out.println("java.runtime.name:" + System.getProperty("java.runtime.name"));
        System.out.println("path.separator:" + System.getProperty("path.separator"));
    }

}