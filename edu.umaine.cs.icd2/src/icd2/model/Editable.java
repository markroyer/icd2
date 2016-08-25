/**
 * 
 */
package icd2.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this to specify editable properties.
 * 
 * @author Mark Royer
 *
 */
@Target(value = { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Editable {

	String name();

	String description();

	String[]onchange();

	Class<? extends ModelModifier<?, ?>>method() default StandardModelModifier.class;

	Class<? extends ModelValidator<?>>validator() default StandardModelValidator.class;

	Class<? extends ModelValueProvider<?, ?>>valueProvider() default StandardModelValueProvider.class;

	EditType editType() default EditType.IMMEDIATE;
}
