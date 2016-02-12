import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import jtransc.JTranscInput;

public class LibgdxInput {
    static public void config() {
        Gdx.input.setInputProcessor(new InputProcessor() {
            private JTranscInput.MouseInfo mouseInfo = new JTranscInput.MouseInfo();
            private JTranscInput.KeyInfo keyInfo = new JTranscInput.KeyInfo();

            @Override
            public boolean keyDown(int keyCode) {
                keyInfo.keyCode = keyCode;
                JTranscInput.impl.onKeyDown(keyInfo);
                return false;
            }

            @Override
            public boolean keyUp(int keyCode) {
                keyInfo.keyCode = keyCode;
                JTranscInput.impl.onKeyUp(keyInfo);
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                mouseInfo.x = screenX;
                mouseInfo.y = screenY;
                mouseInfo.buttons = 1;
                JTranscInput.impl.onMouseDown(mouseInfo);
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                mouseInfo.x = screenX;
                mouseInfo.y = screenY;
                mouseInfo.buttons = 0;
                JTranscInput.impl.onMouseUp(mouseInfo);
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                mouseInfo.x = screenX;
                mouseInfo.y = screenY;
                JTranscInput.impl.onMouseMove(mouseInfo);
                return false;
            }

            @Override
            public boolean scrolled(int amount) {
                return false;
            }
        });

    }
}
