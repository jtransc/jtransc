package android.test.anno;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.AccessibleObject;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.Comparator;

public class TestAnnotations {
    /**
     * Print the annotations in sorted order, so as to avoid
     * any (legitimate) non-determinism with regard to the iteration order.
     */
    static private void printAnnotationArray(String prefix, Annotation[] arr) {
        TreeMap<String, Annotation> sorted =
            new TreeMap<String, Annotation>();

        for (Annotation a : arr) {
            sorted.put(a.annotationType().getName(), a);
        }

        for (Annotation a : sorted.values()) {
            System.out.println(prefix + "  " + annoToString(a));
            System.out.println(prefix + "    " + a.annotationType());
        }
    }

    static void printAnnotations(Class clazz) {
        Annotation[] annos;
        Annotation[][] parAnnos;

        annos = clazz.getAnnotations();
        System.out.println("annotations on TYPE " + clazz +
            "(" + annos.length + "):");
        printAnnotationArray("", annos);
        System.out.println();

        for (Constructor c: sortCtors(clazz.getDeclaredConstructors())) {
            annos = c.getDeclaredAnnotations();
            System.out.println("  annotations on CTOR " + c + ":");
            printAnnotationArray("  ", annos);

            System.out.println("    constructor parameter annotations:");
            for (Annotation[] pannos: c.getParameterAnnotations()) {
                printAnnotationArray("    ", pannos);
            }
        }

        for (Method m: sortMethods(clazz.getDeclaredMethods())) {
            annos = m.getDeclaredAnnotations();
            System.out.println("  annotations on METH " + m + ":");
            printAnnotationArray("  ", annos);

            System.out.println("    method parameter annotations:");
            for (Annotation[] pannos: m.getParameterAnnotations()) {
                printAnnotationArray("    ", pannos);
            }
        }

        for (Field f: sortFields(clazz.getDeclaredFields())) {
            annos = f.getDeclaredAnnotations();
            System.out.println("  annotations on FIELD " + f + ":");
            printAnnotationArray("  ", annos);

            AnnoFancyField aff;
            aff = (AnnoFancyField) f.getAnnotation(AnnoFancyField.class);
            if (aff != null) {
                System.out.println("    aff: " + aff + " / " + aff.getClass());
                System.out.println("    --> nombre is '" + aff.nombre() + "'");
            }
        }
        System.out.println();
    }


    @ExportedProperty(mapping = {
        @IntToString(from = 0, to = "NORMAL_FOCUS"),
        @IntToString(from = 2, to = "WEAK_FOCUS")
    })
    public int getFocusType() {
        return 2;
    }


    @AnnoArrayField
    String thing1;

    @AnnoArrayField(
            zz = {true,false,true},
            bb = {-1,0,1},
            cc = {'Q'},
            ss = {12,13,14,15,16,17},
            ii = {1,2,3,4},
            ff = {1.1f,1.2f,1.3f},
            jj = {-5,0,5},
            dd = {0.3,0.6,0.9},
            str = {"hickory","dickory","dock"}
            )
    String thing2;

    public static void testArrays() {
        TestAnnotations ta = new TestAnnotations();
        Field field;
        Annotation[] annotations;

        try {
           field = TestAnnotations.class.getDeclaredField("thing1");
           annotations = field.getAnnotations();
           System.out.println(field + ": " + annoToString(annotations[0]));
           
           field = TestAnnotations.class.getDeclaredField("thing2");
           annotations = field.getAnnotations();
           System.out.println(field + ": " + annoToString(annotations[0]));
       } catch (NoSuchFieldException nsfe) {
           throw new RuntimeException(nsfe);
       }
    }
    private static Method[] sortMethods(Method[] methods){
   	 Arrays.sort(methods, new Comparator<Method>(){
				public int compare(Method a, Method b){
					return a.getName().compareTo(b.getName());
				}
			});
   	 return methods;
    }
    
    private static Field[] sortFields(Field[] fields){
   	 Arrays.sort(fields, new Comparator<Field>(){
				public int compare(Field a, Field b){
					return a.getName().compareTo(b.getName());
				}
			});
   	 return fields;
    }
    
    private static Constructor[] sortCtors(Constructor[] ctor){
   	 Arrays.sort(ctor, new Comparator<Constructor>(){
				public int compare(Constructor a, Constructor b){
					return a.getName().compareTo(b.getName());
				}
			});
   	 return ctor;
    }
    
	private static String annoToString (Annotation a) {
		StringBuilder b = new StringBuilder();
		try {
			Method[] methods = a.annotationType().getDeclaredMethods();
			sortMethods(methods);
			b.append("@" + a.annotationType().getName() + "(");
			for (int k = 0; k < methods.length; k++) {
				Method m = methods[k];				
				Object o = m.invoke(a, new Object[0]);
				b.append(m.getName() + "=");
				if (o.getClass().isArray()) {
					b.append("[");
					for (int i = 0, length = java.lang.reflect.Array.getLength(o); i < length; i++) {
						b.append(java.lang.reflect.Array.get(o, i));
						if (i != length - 1) {
							b.append(", ");
						}
					}
					b.append("]");
				} else {
					b.append(o);
				}
				if (k != methods.length - 1) {
					b.append(", ");
				}
			}
			b.append(")");

		} catch (Throwable t) {
			t.printStackTrace();
		}
		return b.toString();
	}

    public static void testArrayProblem() {
        Method meth;
        ExportedProperty property;
        final IntToString[] mapping;

        try {
            meth = TestAnnotations.class.getMethod("getFocusType",
                    (Class[])null);
        } catch (NoSuchMethodException nsme) {
            throw new RuntimeException(nsme);
        }
        property = meth.getAnnotation(ExportedProperty.class);
        mapping = property.mapping();

        System.out.println("mapping is " + mapping.getClass() +
            "\n  0='" + mapping[0] + "'\n  1='" + mapping[1] + "'");

        /* while we're here, check isAnnotationPresent on Method */
        System.out.println("present(getFocusType, ExportedProperty): " +
            meth.isAnnotationPresent(ExportedProperty.class));
        System.out.println("present(getFocusType, AnnoSimpleType): " +
            meth.isAnnotationPresent(AnnoSimpleType.class));

        System.out.println("");
    }



    public static void main(String[] args) {
        System.out.println("TestAnnotations...");

        testArrays();
        testArrayProblem();
        //System.exit(0);

        System.out.println(
            "AnnoSimpleField " + AnnoSimpleField.class.isAnnotation() +
            ", SimplyNoted " + SimplyNoted.class.isAnnotation());

        Class clazz;
        clazz = SimplyNoted.class;
        printAnnotations(clazz);
        clazz = INoted.class;
        printAnnotations(clazz);
        clazz = SubNoted.class;
        printAnnotations(clazz);
        clazz = FullyNoted.class;
        printAnnotations(clazz);

        Annotation anno;

        // this is expected to be non-null
        anno = SimplyNoted.class.getAnnotation(AnnoSimpleType.class);
        System.out.println("SimplyNoted.get(AnnoSimpleType) = " + anno);
        // this is non-null if the @Inherited tag is present
        anno = SubNoted.class.getAnnotation(AnnoSimpleType.class);
        System.out.println("SubNoted.get(AnnoSimpleType) = " + anno);

        System.out.println();

        // Package annotations aren't inherited, so getAnnotations and getDeclaredAnnotations are
        // the same.
        System.out.println("Package annotations:");
        printAnnotationArray("    ", TestAnnotations.class.getPackage().getAnnotations());
        System.out.println("Package declared annotations:");
        printAnnotationArray("    ", TestAnnotations.class.getPackage().getDeclaredAnnotations());
    }
}
