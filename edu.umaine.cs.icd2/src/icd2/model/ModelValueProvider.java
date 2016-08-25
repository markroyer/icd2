/**
 * 
 */
package icd2.model;

import java.util.List;

import icd2.widgets.UnknownTypeException;

/**
 * @author Mark Royer
 *
 */
public interface ModelValueProvider<T, R> {

	List<T> getValues(Class<T> type, R referenceObject) throws UnknownTypeException;
	
}
