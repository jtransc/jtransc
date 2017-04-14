package java.awt.event;

import java.util.EventListener;

public interface InputMethodListener extends EventListener {
	void inputMethodTextChanged(InputMethodEvent event);

	void caretPositionChanged(InputMethodEvent event);
}
