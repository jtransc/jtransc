package jtransc.bug;


public class JTranscBug110 {

	public static interface I {
		// This doesn't happen with String for example because if it's a String, it gets inlined
		// So happens just for things not calculated at compile-time.
		String[] tokenImage = {};
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