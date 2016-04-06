import java.lang.reflect.Field;

public class AstExampleTest2 {
	/*
	int b;

	public void test(int a) {
		int result = (this.b == a) ? 10 : 20;
		System.out.println(result);
	}
	*/

	public void test() {
		try {
			System.out.println("+20: " + Integer.parseInt("+20 ", 16));
			System.out.println("11111111111");
		} catch (NumberFormatException nfe) {
			System.out.println("22222222222");
			System.out.println(nfe.getMessage());
		}
		System.out.println("333333333");
	}

	/*
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// @TODO: This is slow! We could override this at code gen knowing all the fields and with generated code to generate them.
		try {
			Class<?> clazz = this.getClass();
			Object newObject = clazz.newInstance();
			for (Field field : clazz.getDeclaredFields()) {
				//field.getDeclaringClass().isPrimitive()
				field.set(newObject, field.get(this));
			}
			return newObject;
		} catch (Throwable e) {
			throw new CloneNotSupportedException(e.toString());
		}
	}
	*/
}
