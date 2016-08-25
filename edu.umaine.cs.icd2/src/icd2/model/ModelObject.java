package icd2.model;


public interface ModelObject<T extends ModelObject<? super T, ?>, P extends ModelObject<?, ?>> {

	/**
	 * Empty array for model objects that don't have any children.
	 */
	public static final ModelObject<?, ?>[] EMPTYARRAY = new ModelObject<?, ?>[0];

	/**
	 * @param parent The parent of this object (Null allowed)
	 */
	public void setParent(P parent);

	/**
	 * @return Parent of this object (Null possible)
	 */
	public P getParent();

	/**
	 * Children of this object. 
	 * 
	 * If you need to return no children return the EMPTYARRAY.
	 * 
	 * @return Children of this object (Never null)
	 */
	public ModelObject<?, ?>[] children();

	/**
	 * @return A simple name for this object. (Never null)
	 */
	public String getName();

	/**
	 * @return Typical items for editing (Never null)
	 */
//	public Editable[] getEditables();
}
