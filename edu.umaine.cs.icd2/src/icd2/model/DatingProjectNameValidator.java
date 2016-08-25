/**
 * 
 */
package icd2.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Control;

/**
 * @author Mark Royer
 *
 */
public class DatingProjectNameValidator implements ModelValidator<DatingProject> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see icd2.model.ModelValidator#create(icd2.model.ModelObject)
	 */
	@Override
	public IValidator create(DatingProject obj, Control[] widgets) {

		String[] namedObjects = toStrings(obj.getName(), obj.getParent().getProjects());

		return new IValidator() {
			@Override
			public IStatus validate(Object value) {
				for (Object o : namedObjects) {
					if (o.equals(value)) {
						for (Control c : widgets) {
							c.setEnabled(false);
						}
						return ValidationStatus.error("A project already exists with that name.");
					}
				}
				for (Control c : widgets) {
					c.setEnabled(true);
				}
				return ValidationStatus.ok();
			}
		};
	}

	private String[] toStrings(String name, List<DatingProject> projects) {

		List<String> result = new ArrayList<>(projects.size() - 1);

		for (int i = 0; i < projects.size(); i++) {
			if (!projects.get(i).getName().equals(name)) {
				result.add(projects.get(i).getName());
			}
		}

		return result.toArray(new String[result.size()]);
	}
}
