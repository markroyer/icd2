/**
 * 
 */
package icd2.model;

/**
 * @author Mark Royer
 *
 */
public class ModelKey<T> implements ModelObject<ModelKey<T>, ModelObject<?,?>> {

	private T key;
	
	private ModelObject<?, ?> parent;
	
	/**
	 * @param key (Not null)
	 */
	public ModelKey(T key) {
		this.key = key;
	}
	
	@Override
	public void setParent(ModelObject<?, ?> parent) {
		this.parent = parent;
	}

	@Override
	public ModelObject<?, ?> getParent() {
		return parent;
	}

	@Override
	public ModelObject<?, ?>[] children() {
		return EMPTYARRAY; // No children for a key
	}

	@Override
	public String getName() {
		return key.toString();
	}

	@Override
	public String toString() {
		return getName();
	}

	
	
}
