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

import jtransc.annotation.JTranscKeep;

import java.util.ArrayList;

public class JTranscInput {
    @JTranscKeep
    static public ListHandler impl = new ListHandler();
    @JTranscKeep
    static public MouseInfo mouseInfo = new MouseInfo();
    @JTranscKeep
    static public KeyInfo keyInfo = new KeyInfo();
    @JTranscKeep
    static public GamepadInfo gamepadInfo = new GamepadInfo ();

    public interface Impl {
        void addHandler(Handler handler);
    }

    @JTranscKeep
    static public class KeyInfo {
        public int keyCode;
    }

    @JTranscKeep
    static public class MouseInfo {
        public int x;
        public int y;
        public int buttons;
    }

    @JTranscKeep
    static public class GamepadInfo {

    }

    @JTranscKeep
    static public class TouchInfo {
    }

    public interface Handler {
        @JTranscKeep
        void onKeyTyped(KeyInfo info);

        @JTranscKeep
        void onKeyDown(KeyInfo info);

        @JTranscKeep
        void onKeyUp(KeyInfo info);

        @JTranscKeep
        void onGamepadPressed(GamepadInfo info);

        @JTranscKeep
        void onGamepadRelepased(GamepadInfo info);

        @JTranscKeep
        void onMouseDown(MouseInfo info);

        @JTranscKeep
        void onMouseUp(MouseInfo info);

        @JTranscKeep
        void onMouseMove(MouseInfo info);

        @JTranscKeep
        void onMouseScroll(MouseInfo info);

        @JTranscKeep
        void onTouchDown(TouchInfo info);

        @JTranscKeep
        void onTouchDrag(TouchInfo info);

        @JTranscKeep
        void onTouchUp(TouchInfo info);
    }

    static public class ListHandler implements Handler, Impl {
        private ArrayList<Handler> handlers = new ArrayList<Handler>();

        public void addHandler(Handler handler) {
            handlers.add(handler);
        }

        @Override
        public void onKeyTyped(KeyInfo info) {
            for (Handler handler : handlers) handler.onKeyTyped(info);
        }

        @Override
        public void onKeyDown(KeyInfo info) {
            for (Handler handler : handlers) handler.onKeyDown(info);
        }

        @Override
        public void onKeyUp(KeyInfo info) {
            for (Handler handler : handlers) handler.onKeyUp(info);
        }

        @Override
        public void onGamepadPressed(GamepadInfo info) {
            for (Handler handler : handlers) handler.onGamepadPressed(info);
        }

        @Override
        public void onGamepadRelepased(GamepadInfo info) {
            for (Handler handler : handlers) handler.onGamepadRelepased(info);
        }

        @Override
        public void onMouseDown(MouseInfo info) {
            for (Handler handler : handlers) handler.onMouseDown(info);
        }

        @Override
        public void onMouseUp(MouseInfo info) {
            for (Handler handler : handlers) handler.onMouseUp(info);
        }

        @Override
        public void onMouseMove(MouseInfo info) {
            for (Handler handler : handlers) handler.onMouseMove(info);
        }

        @Override
        public void onMouseScroll(MouseInfo info) {
            for (Handler handler : handlers) handler.onMouseScroll(info);
        }

        @Override
        public void onTouchDown(TouchInfo info) {
            for (Handler handler : handlers) handler.onTouchDown(info);
        }

        @Override
        public void onTouchDrag(TouchInfo info) {
            for (Handler handler : handlers) handler.onTouchDrag(info);
        }

        @Override
        public void onTouchUp(TouchInfo info) {
            for (Handler handler : handlers) handler.onTouchUp(info);
        }
    }

    static public void addHandler(Handler handler) {
        impl.addHandler(handler);
    }
}
