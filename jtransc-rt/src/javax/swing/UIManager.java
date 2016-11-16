package javax.swing;

public class UIManager {
	public static String getSystemLookAndFeelClassName() {
		return "default";
	}

	public static void setLookAndFeel(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

	}

	public static class LookAndFeelInfo {
		private String name;
		private String className;

		public LookAndFeelInfo(String name, String className) {
			this.name = name;
			this.className = className;
		}

		public String getName() {
			return name;
		}

		public String getClassName() {
			return className;
		}
	}
}
