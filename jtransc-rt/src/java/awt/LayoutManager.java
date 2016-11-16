package java.awt;

public interface LayoutManager {
	void addLayoutComponent(String name, Component comp);

	void removeLayoutComponent(Component comp);

	Dimension preferredLayoutSize(Container parent);

	Dimension minimumLayoutSize(Container parent);

	void layoutContainer(Container parent);
}
