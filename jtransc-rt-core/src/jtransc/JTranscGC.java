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

package jtransc;

import jtransc.annotation.haxe.HaxeMethodBody;

public class JTranscGC {
    @HaxeMethodBody("HaxeNatives.gcDisable();")
    static public void disable() {
    }

    @HaxeMethodBody("HaxeNatives.gcEnable();")
    static public void enable() {
    }

    @HaxeMethodBody("HaxeNatives.gc();")
    static public void gc() {
        System.gc();
    }
}
