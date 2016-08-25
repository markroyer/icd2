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
public class DateSessionValueProvider implements ModelValueProvider<DateSession, Chart> {

	@Override
	public List<DateSession> getValues(Class<DateSession> type, Chart referenceObject)
			throws UnknownTypeException {
		return referenceObject.getParent().getSessions();
	}

}
