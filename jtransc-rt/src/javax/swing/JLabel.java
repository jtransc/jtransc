package javax.swing;

import com.jtransc.widgets.JTranscWidgets;

import java.awt.*;
import java.awt.image.BufferedImage;

public class JLabel extends JComponent {
	protected JTranscWidgets.Widget createJTranscWidget() {
		return JTranscWidgets.impl.createComponent("label");
	}

	class LabelIcon extends Component {
		protected JTranscWidgets.Widget createJTranscWidget() {
			return JTranscWidgets.impl.createComponent("image");
		}

		public void setPixels(int[] rgba, int width, int height) {
			widget.setPixels(rgba, width, height);
		}
	}

	class LabelText extends Component {
		protected JTranscWidgets.Widget createJTranscWidget() {
			return JTranscWidgets.impl.createComponent("image");
		}

		public void setText(String text) {
			widget.setText(text);
		}
	}

	private LabelIcon labelIcon = new LabelIcon();
	private LabelText labelText = new LabelText();

	public JLabel() {
		this("");
	}

	public JLabel(String text) {
		super();
		add(labelIcon);
		add(labelText);
		setText(text);
	}

	public void setText(String text) {
		labelText.setText(text);
	}

	private Icon icon;

	public void setIcon(Icon icon) {
		this.icon = icon;
		updateIcon();
	}

	private void updateIcon() {
		if (icon == null) return;
		int width = icon.getIconWidth();
		int height = icon.getIconHeight();
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		icon.paintIcon(this, bi.getGraphics(), 0, 0);
		int[] rgb = bi.getRGB(0, 0, width, height, null, 0, width);
		labelIcon.setPixels(rgb, width, height);
	}

	@Override
	public void update(Graphics g) {
		updateIcon();
		super.update(g);
	}
}