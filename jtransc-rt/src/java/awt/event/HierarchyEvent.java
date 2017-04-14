package java.awt.event;

import java.awt.*;

@SuppressWarnings({"WeakerAccess", "unused", "PointlessArithmeticExpression"})
public class HierarchyEvent extends AWTEvent {
	public static final int HIERARCHY_FIRST = 1400;
	public static final int HIERARCHY_CHANGED = 0 + HIERARCHY_FIRST;
	public static final int ANCESTOR_MOVED = 1 + HIERARCHY_FIRST;
	public static final int ANCESTOR_RESIZED = 2 + HIERARCHY_FIRST;
	public static final int HIERARCHY_LAST = ANCESTOR_RESIZED;
	public static final int PARENT_CHANGED = 0x1;
	public static final int DISPLAYABILITY_CHANGED = 0x2;
	public static final int SHOWING_CHANGED = 0x4;

	Component changed;
	Container changedParent;
	long changeFlags;

	public HierarchyEvent(Component source, int id, Component changed, Container changedParent) {
		super(source, id);
		this.changed = changed;
		this.changedParent = changedParent;
	}

	public HierarchyEvent(Component source, int id, Component changed, Container changedParent, long changeFlags) {
		super(source, id);
		this.changed = changed;
		this.changedParent = changedParent;
		this.changeFlags = changeFlags;
	}

	public Component getComponent() {
		return (source instanceof Component) ? (Component) source : null;
	}

	public Component getChanged() {
		return changed;
	}

	public Container getChangedParent() {
		return changedParent;
	}

	public long getChangeFlags() {
		return changeFlags;
	}

}
