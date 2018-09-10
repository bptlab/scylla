package de.hpi.bpt.scylla.plugin_loader;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Expresses that the execution of a specific plugin class<br>
 * must take place either {@link Execute#AFTER} (default) or {@link Execute#BEFORE} a given other plugin class
 * @author Leon Bein
 *
 */
@Documented
@Repeatable(TemporalDependencies.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TemporalDependent {
	public static enum Execute{BEFORE,AFTER}
	/**The class to be temporally dependent of*/
	Class<?> value();
	/**States if to force execution before or after class dependent of*/
	Execute execute() default Execute.AFTER;
}
