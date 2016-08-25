/**
 * 
 */
package icd2.model;

import icd2.util.DataFileException;
import icd2.util.HDF5Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * 
 * @author Mark Royer
 *
 */
public class Sample implements ModelObject<Sample, Core> {

	private List<Value> values;

	private String name;

	private Core parent;

	public Sample(String name) {
		this.values = new ArrayList<>();
		this.name = name;
	}

	/**
	 * Adds the given value to the sample.
	 * 
	 * @param value
	 *            (Null allowed)
	 * @return True IFF added to the sample
	 */
	public boolean addValue(Value value) {
		boolean result = values.add(value);
		if (result)
			value.setParent(this);
		return result;
	}

	/**
	 * The parent of this sample must have been previously set.
	 * 
	 * @return Unmodifiable list of values (Never null)
	 * @throws DataFileException
	 *             If the parent has not been set
	 */
	public List<Value> getValues() throws DataFileException {

		if (values.isEmpty()) {

			Core c = this.getParent();

			try {

				if (c == null) {
					throw new DataFileException("The parent for this sample has not been set.");
				}

				File file = c.getFile();

				this.values = HDF5Util.readCoreSample(file, this.name, Value.class);

			} catch (Exception e) {
				throw new DataFileException(e.getMessage());
			}
		}

		return Collections.unmodifiableList(values);
	}

	@Override
	public void setParent(Core parent) {
		this.parent = parent;
	}

	@Override
	public Core getParent() {
		return parent;
	}

	@Override
	public ModelObject<?, ?>[] children() {
		return values.toArray(new Value[values.size()]);
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public double[] getValuesAsDoubleArray() {

		double[] result = new double[values.size()];

		for (int i = 0; i < values.size(); i++) {
			result[i] = values.get(i).doubleValue();
		}

		return result;
	}

}
