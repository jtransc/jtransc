package java.awt;

public interface MenuContainer {
	Font getFont();

	void remove(MenuComponent comp);

	@Deprecated
	boolean postEvent(Event evt);
}