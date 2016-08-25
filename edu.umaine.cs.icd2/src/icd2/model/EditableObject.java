/**
 * 
 */
package icd2.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Mark Royer
 *
 */
@Target(value={ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EditableObject {

	/**
	 * @return The display name of the object.
	 */
	String name();
	
	/**
	 * @return Object description.
	 */
	String description();

}
