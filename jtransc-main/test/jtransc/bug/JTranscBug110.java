package jtransc.bug;


public class JTranscBug110 {

	public static interface I {
		String[] tokenImage = {"<EOF>"};
	}

	public static class C implements I{
		public static String[] tokenImage(){
			return tokenImage;
		}
	}

	static public void main(String[] args) {
		C.tokenImage();
	}
}