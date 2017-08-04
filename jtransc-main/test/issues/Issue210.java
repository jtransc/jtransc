package issues;

public class Issue210 {
    public static int count = -2;

    public static void main(String[] args) {
        Counter c = new Counter();
        Thread t1 = new Thread(c, "c1");
        Thread t2 = new Thread(c, "c2");
        t1.start();
        t2.start();
        while (count != 0) { // wait the two threads
        }
    }

    public static class Counter implements Runnable {
        public void run() {
            synchronized (this) {
                for (int i = 0; i < 5; i++) {
                    System.out.println("Counting: " + i); // This should be synchronized.
                }
                count++;
            }
        }
    }

}
