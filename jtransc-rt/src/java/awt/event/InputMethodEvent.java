package java.awt.event;

import java.awt.*;
import java.awt.font.TextHitInfo;
import java.text.AttributedCharacterIterator;

@SuppressWarnings("WeakerAccess")
public class InputMethodEvent extends AWTEvent {
	public static final int INPUT_METHOD_FIRST = 1100;
	public static final int INPUT_METHOD_TEXT_CHANGED = INPUT_METHOD_FIRST;
	public static final int CARET_POSITION_CHANGED = INPUT_METHOD_FIRST + 1;
	public static final int INPUT_METHOD_LAST = INPUT_METHOD_FIRST + 1;

	long when;
	private boolean consumed;
	private AttributedCharacterIterator text;
	private int committedCharacterCount;
	private TextHitInfo caret;
	private TextHitInfo visiblePosition;

	public InputMethodEvent(Component source, int id, long when, AttributedCharacterIterator text, int committedCharacterCount, TextHitInfo caret, TextHitInfo visiblePosition) {
		super(source, id);
		this.when = when;
		this.text = text;
		this.committedCharacterCount = committedCharacterCount;
		this.caret = caret;
		this.visiblePosition = visiblePosition;
	}

	public InputMethodEvent(Component source, int id, AttributedCharacterIterator text, int committedCharacterCount, TextHitInfo caret, TextHitInfo visiblePosition) {
		this(source, id, 0L, text, committedCharacterCount, caret, visiblePosition);
	}

	public InputMethodEvent(Component source, int id, TextHitInfo caret, TextHitInfo visiblePosition) {
		this(source, id, 0L, null, 0, caret, visiblePosition);
	}

	public AttributedCharacterIterator getText() {
		return text;
	}

	public int getCommittedCharacterCount() {
		return committedCharacterCount;
	}

	public TextHitInfo getCaret() {
		return caret;
	}

	public TextHitInfo getVisiblePosition() {
		return visiblePosition;
	}

	public void consume() {
		consumed = true;
	}

	public boolean isConsumed() {
		return consumed;
	}

	public long getWhen() {
		return when;
	}
}
