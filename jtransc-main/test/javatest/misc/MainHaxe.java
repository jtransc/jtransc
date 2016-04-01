package javatest.misc;

import jtransc.annotation.JTranscKeep;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

public class MainHaxe {
    static public int MY_GREAT_CONSTANT = 10;

    static public void main(String[] args) throws Throwable {
        new MainHaxe().main2(args);
    }

    void main2(String[] args) throws Throwable {
        System.out.println("STARTED");
        System.out.println("args:" + Arrays.toString(args));
        systemPropertiesTest();
        testRegex();
        mapTest();
        arrayListTest();
        stringConstructTest();
        inheritanceTest();
        accessStaticConstantTest();
        returnDistinctTypeTest();
        instanceOfTest();
        abstractInterfaceTest();
        operatorTest();
        simpleReflection();
        toStringInterface();
        shortCalc();
        fieldReflection();
        accessInterfaceStaticFields();
        tryCatchTest();
        testRandom();
        testCloneArray();
        //testMd5();
        //testSha1();
        testCharset();
        testDynamicInstanceof();
        testGenericStuff();
        testAnnotations();
        testArrays();

	    System.out.println(StaticCall1.a);
	    StaticCall1.a = 20;
	    System.out.println(StaticCall1.a);

	    System.out.println("COMPLETED");
        //stage.getStage3Ds()[0].requestContext3D(Context3DRenderMode.AUTO, "baselineConstrained");
    }

    static private void testArrays() {
        byte[] bytes = new byte[16];
        short[] shorts = new short[16];
        for (int n = 0; n < 16; n++) {
            bytes[n] = (byte)(n - 6);
            shorts[n] = (short)(n - 6);
        }
        for (int n = 0; n < 16; n++) System.out.print((int)bytes[n]);
        System.out.println();
        for (int n = 0; n < 16; n++) System.out.print((int)shorts[n]);
        System.out.println();
    }

    static private void testAnnotations() {
        new ExampleClass().demo();
        System.out.println("Annotations:");
        for (Annotation a : ExampleClass.class.getDeclaredAnnotations()) {
            System.out.println("Annotation: " + a);
        }
    }

    static private void testDynamicInstanceof() throws ClassNotFoundException {
        Class<?> integerClass = Class.forName("java.lang.Integer");
        Class<?> numberClass = Class.forName("java.lang.Number");
        System.out.println("integerClass.isInstance(Integer.valueOf(1)):" + integerClass.isInstance(Integer.valueOf(1)));
        System.out.println("Integer.TYPE.isInstance(Integer.valueOf(1)):" + Integer.TYPE.isInstance(Integer.valueOf(1)));
        System.out.println("numberClass.isAssignableFrom(integerClass):" + numberClass.isAssignableFrom(integerClass));
        System.out.println("numberClass.isAssignableFrom(integerClass):" + integerClass.isAssignableFrom(numberClass));
    }

    static private void testGenericStuff() {
        System.out.println("Generic:");

        new GenericTest().map = null; // @TODO: Remove this line and it should work because of the @AllKeep!

        for (Field f : GenericTest.class.getDeclaredFields()) {
            // sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
            // java.lang.reflect.ParameterizedType
            System.out.println("Field: " + f.toString());
            Type genericType = f.getGenericType();
            if (genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                System.out.println("  type args:" + Arrays.toString(pt.getActualTypeArguments()));
                System.out.println("  owner type:" + pt.getOwnerType());
                System.out.println("  raw type:" + pt.getRawType());
            }
        }

        for (Method m : GenericTest.class.getDeclaredMethods()) {
            // sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
            // java.lang.reflect.ParameterizedType
            System.out.println("Method: " + m.toString());
            System.out.println("  ret: " + m.getReturnType());
            System.out.println("  args count: " + m.getParameterCount());
            System.out.println("  args: " + Arrays.toString(m.getParameterTypes()));
            Type[] genericTypes = m.getGenericParameterTypes();
            System.out.println("  ret generic: " + m.getGenericReturnType());
            for (Type genericType : genericTypes) {
                System.out.println("  param generic: " + genericType);
            }
        }
    }

    static private void testRegex() {
        System.out.println("regex.numbers[true]:" + Pattern.matches("^\\d+$", "10000"));
        System.out.println("regex.numbers[false]:" + Pattern.matches("^\\d+$", "a"));
        System.out.println("regex.split:" + Arrays.toString(Pattern.compile(",+").split("hello,,,world,,b,,c,,d")));
    }

    static private void shortCalc() {
        System.out.println("short:-1536==" + doShortCalc((short) 32000, (short) 32000));
    }

    static private int doShortCalc(short a, short b) {
        return (int) (short) (a + b);
    }

    static private void toStringInterface() {
        _toStringInterface(new ClassImplementingMyInterface());
    }

    static private void _toStringInterface(MyInterface a) {
        System.out.println(a.toString());
    }

    static private void operatorTest() {
        boolean a = true;
        boolean result = a ^ false;
        System.out.println("operatorTest:" + result);
    }

    static private void abstractInterfaceTest() {
        System.out.println("abstractInterfaceTest[1]");
        for (Character c : new Iterable<Character>() {
            public Iterator<Character> iterator() {
                return new DummyCharIterator();
            }
        }) {
            System.out.println("abstractInterfaceTest[c]:" + c);
        }
        System.out.println("abstractInterfaceTest[2]");
    }

    static private void accessStaticConstantTest() {
        System.out.println("ACCESS_STATIC_CONSTANT[10]:" + MY_GREAT_CONSTANT);
    }

    static private void systemPropertiesTest() {
        System.out.println("java.runtime.name:" + System.getProperty("java.runtime.name"));
        System.out.println("path.separator:" + System.getProperty("path.separator"));
    }

    static private void arrayListTest() {
        ArrayList<String> list = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();

        sb.append(list.size());
        list.add("A");
        sb.append(list.get(0));
        sb.append(list.size());
        list.add("B");
        list.add("C");
        sb.append(list.size());
        Collections.reverse(list);
        for (String item : list) sb.append(item);

        System.out.println("ArrayList:" + sb.toString());
    }

    static private void mapTest() {
        Map<String, String> map = new HashMap<String, String>();
        StringBuilder sb = new StringBuilder();

        sb.append(map.size());
        map.put("hello", "world");
        sb.append(map.get("hello"));
        sb.append(map.size());
        map.put("hello", "world");
        sb.append(map.size());
        map.put("hello2", "world");
        sb.append(map.size());
        map.clear();
        sb.append(map.size());

        System.out.println("Map:" + sb.toString());
    }

    static private void instanceOfTest() {
        Object str = "test";
        A a = new A();
        B b = new B();
        System.out.println("INSTANCEOF[true]:" + (str instanceof String));
        System.out.println("INSTANCEOF[false]:" + (str instanceof A));
        System.out.println("INSTANCEOF[true]:" + (a instanceof A));
        System.out.println("INSTANCEOF[false]:" + (a instanceof B));
        System.out.println("INSTANCEOF[true]:" + (b instanceof A));
        System.out.println("INSTANCEOF[true]:" + (b instanceof B));
    }

    static private void inheritanceTest() {
        System.out.println("INHERITANCE[BA]:" + new B().test());
    }

    static private void returnDistinctTypeTest() {
        BB bb = new BB();
        BB bb2 = bb.test();
        AA aa2 = bb.test();
        Object demo = aa2;
        System.out.println("RETURN_DISTINCT_TYPE(true):" + (demo instanceof BB));
        System.out.println("RETURN_DISTINCT_TYPE(false):" + (demo instanceof String));
    }

    static private void stringConstructTest() {
        System.out.println("STRING[]:" + new String());
        System.out.println("STRING[other]:" + new String("other"));
        System.out.println("STRING[test]:" + new String(new StringBuilder("test")));
        System.out.println("STRING[abcd]:" + new String(new char[]{'a', 'b', 'c', 'd'}));
        System.out.println("STRING[bc]:" + new String(new char[]{'a', 'b', 'c', 'd'}, 1, 2));
        System.out.println("STRING[ABC]:" + new String(new int[]{65, 66, 67}, 0, 3));
        System.out.println("STRING[abc]:" + new String(new byte[]{'a', 'b', 'c'}));
        System.out.println("STRING[demo]:" + "demo".toString());
        System.out.println("STRING[0]:" + "demo".hashCode());

	    System.out.println("STRING[bc]:" + new String(new char[]{'a', 'b', 'c', 'd'}, 1, 2));

	    System.out.println("STRING[bug]:" + new String(new char[] { '(',')','[','L','j','a','v','a','.','l','a','n','g','.','r','e','f','l','e','c','t','.','F','i','e','l','d',';' }, 4, 23));
    }

    private int FIELD = 10;

    static private void simpleReflection() {
        System.out.println("simpleReflection:" + MainHaxe.class.getName());
    }

    private void fieldReflection() throws NoSuchFieldException, IllegalAccessException {
        Field field = MainHaxe.class.getDeclaredField("FIELD");
        System.out.println("fieldReflection:10:" + field.getName() + "," + field.get(this));
    }

    private void accessInterfaceStaticFields() {
        System.out.println("accessInterfaceStaticFields:10:Test:demo = " + InterfaceFields.a + ":" + InterfaceFields.b + ":" + InterfaceFields.cc.value);
    }

    private void tryCatchTest() {
        System.out.println("try:[0]");
        try {
            System.out.println("try:[1]");
            tryCatchTest2();
            System.out.println("try:[FAIL]");
        } catch (Exception e) {
            System.out.println("try:[2]");
        } finally {
            System.out.println("try:[3]");
        }
        System.out.println("try:[4]");
    }

    private void tryCatchTest2() {
        throw new RuntimeException("hello");
    }

    private void testRandom() {
        Random random = new Random(0L);
        System.out.println("Random:" + random.nextInt());
        System.out.println("Random:" + random.nextInt());
        System.out.println("Random:" + random.nextInt());
        System.out.println("Random:" + random.nextInt(10));
        System.out.println("Random:" + random.nextInt(10));
        System.out.println("Random:" + random.nextInt(10));
    }

    private void testCloneArray() {
        byte[] bytes = new byte[]{1, 2, 3, 4};
        byte[] clonedBytes = bytes.clone();
        bytes[0] = -1;
        clonedBytes[1] = -2;
        System.out.println("bytes:" + bytes[0] + "," + bytes[1] + "," + bytes[2] + "," + bytes[3]);
        System.out.println("clonedBytes:" + clonedBytes[0] + "," + clonedBytes[1] + "," + clonedBytes[2] + "," + clonedBytes[3]);
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        for (byte b : bytes) {
            String part = ("00" + Integer.toHexString((b & 0xFF)));
            out.append(part.substring(part.length() - 2));
        }
        return out.toString();
    }

    private void testMd5() {
        try {
            byte[] message = new byte[] { 'h', 'e', 'l', 'l', 'o' };
            String digest = bytesToHexString(MessageDigest.getInstance("MD5").digest(message));
            System.out.println("MD5:" + digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

		//try {
		//    String digest = bytesToHexString(MessageDigest.getInstance("MD5").digest(message));
		//    System.out.println("MD5:" + digest);
		//} catch (NoSuchAlgorithmException e) {
		//    e.printStackTrace();
		//}
    }

    private void testSha1() {
        try {
            byte[] message = new byte[] { 'h', 'e', 'l', 'l', 'o' };
            String digest = bytesToHexString(MessageDigest.getInstance("SHA-1").digest(message));
            System.out.println("SHA1:" + digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void testCharset() {
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuffer data = utf8.encode("hello");
        //System.out.println("charset capacity:" + data.capacity());
        System.out.println("charset limit:" + data.limit());
        System.out.println("charset position:" + data.position());
        System.out.println("charset arrayOffset:" + data.arrayOffset());

        //System.out.println("charset array length:" + data.array().length);
        byte[] message = Arrays.copyOf(data.array(), data.limit());
        System.out.println("charset message length:" + message.length);

    }
}

class StaticCall1 extends StaticCall2 {

}

class StaticCall2 {
	static public int a = 10;
}

interface InterfaceFields {
    int a = 10;
    String b = "Test";
    CC cc = new CC("demo");
}

class CC {
    public String value;

    public CC(String value) {
        this.value = value;
    }
}

class A {
    public String test() {
        return "A";
    }
}

class B extends A {
    public String test() {
        return "B" + super.test();
    }
}

class AA {
    public AA test() {
        return this;
    }
}

class BB extends AA {
    public BB test() {
        return this;
    }
}

@JTranscKeep
class GenericTest {
    @JTranscKeep
    public Map<String, Map<Integer, Double>> map;

    @JTranscKeep
    public List<Integer> method1(List<String> a, boolean b, Map<String, List<Integer>> c, int d) {
        throw new Error("Not supported calling!");
    }
}

abstract class CharIterator implements Iterator<Character> {
    public Character next() {
        //System.out.println("Called CharIterator.next()");
        return nextChar();
    }

    public abstract char nextChar();
}

class DummyCharIterator extends CharIterator {
    @Override
    public char nextChar() {
        return 0;
    }

    public boolean hasNext() {
        return false;
    }
}

class DisplayObject {
    public void setX(int value) {

    }
}

interface MyInterface {
    public void sample();
}

class ClassImplementingMyInterface implements MyInterface {
    public void sample() {
    }

    @Override
    public String toString() {
        return "MyInterface";
    }
}

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@interface ExampleAnnotation {
    String value();
}

@ExampleAnnotation("hello!")
class ExampleClass {
    public void demo() {

    }
}