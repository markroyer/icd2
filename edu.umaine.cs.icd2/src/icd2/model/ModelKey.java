/**
 * 
 */
package icd2.model;

/**
 * {@link ModelKey}s represent a path to information about a model object. Most
 * commonly these keys are used to refer to the location of core data. For
 * example, the model key "COREID/ELEMENTID" would uniquely identify a element
 * data for the ELEMENTID of the core COREID.
 * 
 * @author Mark Royer
 *
 */
public class ModelKey<T>
		implements ModelObject<ModelKey<T>, ModelObject<?, ?>> {

	private T key;

	private ModelObject<?, ?> parent;

	/**
	 * @param key
	 *            (Not null)
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
