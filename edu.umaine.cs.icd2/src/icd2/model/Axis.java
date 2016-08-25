/**
 * 
 */
package icd2.model;

/**
 * @author Mark Royer
 *
 */
public class Axis implements ModelObject<Axis, Plot> {

	private Plot parent;
	
	private String name;
	
	public Axis(Plot parent, String name) {
		this.parent = parent;
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see icd2.model.ModelObject#setParent(icd2.model.ModelObject)
	 */
	@Override
	public void setParent(Plot parent) {
		this.parent = parent;
	}

	/* (non-Javadoc)
	 * @see icd2.model.ModelObject#getParent()
	 */
	@Override
	public Plot getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see icd2.model.ModelObject#children()
	 */
	@Override
	public ModelObject<?, ?>[] children() {
		return ModelObject.EMPTYARRAY;
	}

	/* (non-Javadoc)
	 * @see icd2.model.ModelObject#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

}
