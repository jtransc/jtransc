package javax.swing;

import java.awt.*;

@SuppressWarnings("unused")
public class GroupLayout implements LayoutManager2 {
	public static final int DEFAULT_SIZE = -1;
	public static final int PREFERRED_SIZE = -2;

	private Container host;

	public GroupLayout(Container host) {
		this.host = host;
	}

	public void setHonorsVisibility(boolean honorsVisibility) {
	}

	public boolean getHonorsVisibility() {
		return false;
	}

	public void setAutoCreateGaps(boolean autoCreatePadding) {
	}

	public boolean getAutoCreateGaps() {
		return true;
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {

	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		return null;
	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0;
	}

	@Override
	public void invalidateLayout(Container target) {

	}

	@Override
	public void addLayoutComponent(String name, Component comp) {

	}

	@Override
	public void removeLayoutComponent(Component comp) {

	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return null;
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return null;
	}

	@Override
	public void layoutContainer(Container parent) {

	}

	public enum Alignment {
		LEADING, TRAILING, CENTER, BASELINE
	}

	public abstract class Group {
	}

	public class SequentialGroup extends GroupLayout.Group {
	}

	public class ParallelGroup extends GroupLayout.Group {
	}
}
