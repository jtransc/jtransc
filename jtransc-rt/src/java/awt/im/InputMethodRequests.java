package java.awt.im;

import java.awt.*;
import java.awt.font.TextHitInfo;
import java.text.AttributedCharacterIterator;

public interface InputMethodRequests {
	Rectangle getTextLocation(TextHitInfo offset);

	TextHitInfo getLocationOffset(int x, int y);

	int getInsertPositionOffset();

	AttributedCharacterIterator getCommittedText(int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes);

	int getCommittedTextLength();

	AttributedCharacterIterator cancelLatestCommittedText(AttributedCharacterIterator.Attribute[] attributes);

	AttributedCharacterIterator getSelectedText(AttributedCharacterIterator.Attribute[] attributes);
}