/**
 * 
 */
package icd2.model;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.swt.widgets.Control;

/**
 * @author Mark Royer
 *
 */
public interface ModelValidator<T extends ModelObject<?,?>> {

	IValidator create(T p, Control[] widgets);
	
}
