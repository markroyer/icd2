/**
 * 
 */
package icd2.model;

import java.lang.reflect.Field;

import org.eclipse.e4.core.services.events.IEventBroker;

/**
 * Make a change to the some model property.
 * 
 * @author Mark Royer
 *
 */
public interface ModelModifier<T extends ModelObject<?, ?>, V> {

	void redo(Field f, T obj, V val);
	
	void undo(Field f, T obj);
	
	void notify(IEventBroker eventBroker, String[] notifications, T obj);
}
