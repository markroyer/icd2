/**
 * 
 */
package icd2.model;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Control;

/**
 * @author Mark Royer
 *
 */
public class StandardModelValidator implements ModelValidator<ModelObject<?, ?>> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see icd2.model.ModelValidator#create(icd2.model.ModelObject)
	 */
	@Override
	public IValidator create(ModelObject<?, ?> obj, Control[] widgets) {
		return  new IValidator() {
			@Override
			public IStatus validate(Object value) {
				return ValidationStatus.ok();
			}
		};
	}

}
