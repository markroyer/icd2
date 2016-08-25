/**
 * 
 */
package icd2.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import icd2.widgets.UnknownTypeException;

/**
 * @author Mark Royer
 *
 */
public class StandardModelValueProvider implements ModelValueProvider<Object, Object> {

	/* (non-Javadoc)
	 * @see icd2.model.ModelValueProvider#getValues(java.lang.Class)
	 */
	@Override
	public List<Object> getValues(Class<Object> type, Object referenceObject) throws UnknownTypeException {

		List<Object> result = new ArrayList<>();
		
		if (type.isEnum()) {
			
			result = Arrays.asList(type.getEnumConstants());
			
		} else {
			throw new UnknownTypeException(type);
		}
		
		return result;
	}

}
