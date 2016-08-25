/**
 * 
 */
package icd2.model;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mark Royer
 *
 */
public class StandardModelModifier extends AbstractModelModifier<ModelObject<?, ?>, Object> {

	private static final Logger logger = LoggerFactory.getLogger(StandardModelModifier.class);

	private Object oldValue;

	@Override
	public void redo(Field f, ModelObject<?, ?> obj, Object val) {
		try {
			// ctx.set(editable.onchange()+"old", oldValue);
			Object tmpOld = f.get(obj);
			f.set(obj, val);
			oldValue = tmpOld; // Don't set old value until we're sure we've set
								// it to something new
			logger.info("Model object change.  Old value = {}, New value = {}", oldValue, val);

		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void undo(Field f, ModelObject<?, ?> obj) {
		logger.info("Performing undo.  Setting value to {}", oldValue);
		redo(f, obj, oldValue);
	}

}
