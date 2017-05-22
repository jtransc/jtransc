package jtransc.bug;

public class JTranscBug127 {
	static public void main(String[] args) {
		Object test = "hello";
		try {
			Integer t = (Integer) test;
			System.out.println("BAD. Expected exception.");
			System.out.println(t);
		} catch (ClassCastException e) {
			System.out.println("Good. Exception catched.");
		}
	}
}
