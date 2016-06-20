package regexodus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for the GWT compiler.
 *
 * @author smelC from the SquidLib project
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
public @interface GwtIncompatible {

}
