/**
 * 
 */
package icd2.model;

/**
 * @author Mark Royer
 *
 */
public class Value extends Number implements ModelObject<Value, Sample> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private double value;

	private Sample parent;

	public Value(double value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#intValue()
	 */
	@Override
	public int intValue() {
		return (int) value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#longValue()
	 */
	@Override
	public long longValue() {
		return (long) value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#floatValue()
	 */
	@Override
	public float floatValue() {
		return (float) value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#doubleValue()
	 */
	@Override
	public double doubleValue() {
		return value;
	}

	@Override
	public void setParent(Sample parent) {
		this.parent = parent;
	}

	@Override
	public Sample getParent() {
		return parent;
	}

	@Override
	public ModelObject<?, ?>[] children() {
		return ModelObject.EMPTYARRAY;
	}

	@Override
	public String getName() {
		return String.valueOf(value);
	}
}
