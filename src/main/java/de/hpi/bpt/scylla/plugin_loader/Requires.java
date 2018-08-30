package de.hpi.bpt.scylla.plugin_loader;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Expresses that a plugin class depends on another class 
 * and cannot be executed without that class being loaded
 * @author Leon Bein
 *
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Requires {
	/**The class to be required*/
	Class<?> value();
}
