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

package javatest.async;

/**





 */
public class AsyncTest2 {

    public static void main(String[] args) throws Throwable {
        System.out.println(asyncWeirdSum(3, 5));
        System.out.println(asyncWeirdSum(2, 2));
        System.out.println(asyncFact(5)); // 120

        System.out.println("-----------------");
        // FOR
        for (int i=0; i<5; i++) {
            System.out.println(asyncWeirdSum(0, i));
        }

        System.out.println("-----------------");
        // While
        int i = 5;
        while(i-- > 0) {
            System.out.println(asyncWeirdSum(0, i));
        }
    }

    private static int asyncWeirdSum(int a, int b) {
        if (a % 2 == 0) {
            return asyncInteger(a) + asyncInteger(b);
        } else {
            return 2 * (asyncInteger(a) + asyncInteger(b));
        }
    }

    private static int asyncFact(int n) {
        return n == 0 ? 1 : n * asyncFact(n - 1);
    }

    private static native int asyncInteger(int n);

}