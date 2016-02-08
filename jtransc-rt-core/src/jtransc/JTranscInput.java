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

public class JTranscInput {
    static public class KeyInfo {
        public int keyCode;
    }

    static public class MouseInfo {
        public int x;
        public int y;
        public int buttons;
    }

    static public class GamepadInfo {

    }

    static public class TouchInfo {
    }

    static public class Handler {
        public void onKeyTyped(KeyInfo info) {
        }

        public void onKeyDown(KeyInfo info) {
        }

        public void onKeyUp(KeyInfo info) {
        }

        public void onGamepadPressed(GamepadInfo info) {
        }

        public void onGamepadRelepased(GamepadInfo info) {
        }

        public void onMouseDown(MouseInfo info) {
        }

        public void onMouseUp(MouseInfo info) {
        }

        public void onMouseMove(MouseInfo info) {
        }

        public void onMouseScroll(MouseInfo info) {
        }

        public void onTouchDown(TouchInfo info) {
        }

        public void onTouchDrag(TouchInfo info) {
        }

        public void onTouchUp(TouchInfo info) {
        }
    }

    static public void addHandler(Handler handler) {
    }
}
