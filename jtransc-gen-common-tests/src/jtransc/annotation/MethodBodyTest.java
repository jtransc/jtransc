package jtransc.annotation;



public class MethodBodyTest {
    static public void main(String[] args) {
        System.out.println(mymethod(777));
    }


    static public native String mymethod(int arg);
}
