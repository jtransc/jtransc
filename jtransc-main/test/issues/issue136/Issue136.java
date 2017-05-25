package issues.issue136;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Issue136 {
	static public void main(String[] args) throws Throwable {
		// To keep class from optimization, probably it could be replaced by annotation
		SomeClass<String, Float> f = new SomeClass<>();
        f.point = 0.3f;
        f.map = new HashMap<>();
        f.mass = new String[] {"dd", "d"};

        for (Field field : SomeClass.class.getDeclaredFields()) {
            field.getGenericType();
        }
	}
}
