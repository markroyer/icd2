/**
 * 
 */
package icd2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mark Royer
 *
 */
@EditableObject(name = "Dating Session", description = "Year values to use for resampling")
public class DateSession implements ModelObject<DateSession, DatingProject> {

	private static final Logger logger = LoggerFactory
			.getLogger(DateSession.class);

	public enum PlotMethod {
		TOP, BOTTOM, MIDPOINT;

		@Override
		public String toString() {
			switch (this) {
			case TOP:
				return "Top";
			case BOTTOM:
				return "Bottom";
			case MIDPOINT:
				return "Midpoint";
			default:
				return "ERROR";
			}
		}
	};

	public class DateSessionException extends Exception {
		/**
		 * For serialization
		 */
		private static final long serialVersionUID = 1L;
	}

	private DatingProject parent;

	@Editable(name = "Session Name", description = "A label for the dating session.", onchange = CoreModelConstants.ICD2_MODEL_MODELOBJECT_NAME_CHANGE)
	private String name;

	/**
	 * A list that holds depth for a specific year
	 */
	private List<DepthYear> datedDepths;

	public DateSession(DatingProject parent, String name, int topYear) {
		this.parent = parent;
		this.name = name;
		this.datedDepths = new ArrayList<DepthYear>();
		datedDepths.add(new DepthYear(this, 0.0, topYear));
	}

	@Override
	public void setParent(DatingProject parent) {
		this.parent = parent;
	}

	@Override
	public DatingProject getParent() {
		return parent;
	}

	@Override
	public ModelObject<?, ?>[] children() {
		return datedDepths.toArray(new DepthYear[datedDepths.size()]);
	}

	@Override
	public String getName() {
		return "Session: " + name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Removes the given year from the date session. Increases the remaining
	 * dates that are lower in the core by +1 year.
	 * 
	 * @param year
	 *            The year to remove
	 * @throws DateSessionException
	 *             Thrown if attempting to remove the top date or if the year
	 *             doesn't exist
	 */
	public void removeYearDepth(int year) throws DateSessionException {
		if (datedDepths.get(0).getYear() == year) {
			throw new DateSessionException();
		}

		int index = yearIndex(year);
		// Year didn't exist or top year
		if (index < 1)
			throw new DateSessionException();

		datedDepths.remove(index);

		adjustYearDates(index, datedDepths.size(), +1);
	}

	/**
	 * @param year
	 *            Year to locate
	 * @return The index of the year in the date session or (-(insertion point)
	 *         - 1) if not in the date session
	 */
	public int yearIndex(int year) {
		return Collections.binarySearch(datedDepths,
				new DepthYear(this, 0, year),
				(d1, d2) -> new Integer(d2.getYear()).compareTo(d1.getYear()));
	}

	/**
	 * This will insert a new year value based on the proper position of xx. If
	 * a date already exists at the same location, then the insertion of the new
	 * date is ignored, and the index of the existing date is returned.
	 * 
	 * @param depth
	 * @return The index it was inserted at
	 * @throws DateSessionException
	 *             Thrown if depth is negative
	 */
	public int insertDepth(double depth) throws DateSessionException {
		if (depth < 0) {
			throw new DateSessionException();
		}
		int i = datedDepths.size();

		// All sessions start with a top depth of 0
		if (depth <= datedDepths.get(i - 1).getDepth())
			i = getDepthIndex(depth);

		if (0 <= i && i < datedDepths.size()) {
			logger.info("A depth at position {} has already been dated.",
					depth);
			return i;
		} else if (i < 0) {
			i = -1 * (i + 1); // Insertion point is (-(insertion point) - 1)
		} // Otherwise it's at position depths.size() - the last depth

		if (i < 1) {
			logger.info(
					"Depth position {} is above top year and will be ignored.",
					depth);
			return -1;
		}

		// There is at least a top year already.
		datedDepths.add(i, new DepthYear(this, depth,
				datedDepths.get(i - 1).getYear() - 1));

		adjustYearDates(i + 1, datedDepths.size(), -1);

		return i;
	}

	/**
	 * Adds the given amount to the dates in range of start <= i < end.
	 * 
	 * @param start
	 *            The index to start at
	 * @param end
	 *            The ending index
	 * @param amnt
	 *            The amount to adjust the dates by
	 */
	private void adjustYearDates(int start, int end, int amnt) {
		// Shift the rest of the years by one
		for (int j = start; j < end; j++) {
			datedDepths.get(j).setYear(datedDepths.get(j).getYear() + amnt);
		}
	}

	/**
	 * Returns the location of the given depth. If the given depth does not
	 * exist in this date session, then (-(insertion point) - 1) is returned.
	 * 
	 * @param depth
	 *            A non-negative distance
	 * @return The index the depth occurs or (-(insertion point) - 1)
	 */
	public int getDepthIndex(double depth) {
		return Collections.binarySearch(datedDepths,
				new DepthYear(this, depth, 0), new Comparator<DepthYear>() {
					@Override
					public int compare(DepthYear o1, DepthYear o2) {
						double d1 = o1.getDepth();
						double d2 = o2.getDepth();
						if (d1 < d2)
							return -1;
						else if (d1 > d2)
							return 1;
						else
							return 0;
					}
				});
	}

	/**
	 * @param index
	 * @return The year at the given index or the year that would be at that
	 *         index
	 */
	public int getYear(int index) {

		if (0 <= index && index < datedDepths.size()) {
			return datedDepths.get(index).getYear();
		} else {
			// Outside of the existing index return what would be the date
			if (index <= datedDepths.size() * -1
					|| datedDepths.size() <= index) {
				// Return last date -1
				return datedDepths.get(datedDepths.size() - 1).getYear() - 1;
			} else { // index < 0
				return datedDepths.get((index + 1) * -1).getYear();
			}
		}
	}

	public double getDepth(int i) {
		return datedDepths.get(i).getDepth();
	}

	public int getSize() {
		return datedDepths.size();
	}

	public double[] getDepthArray() {
		double[] result = new double[datedDepths.size()];
		for (int i = 0; i < result.length; i++) {
			// Nulls are not allowed in depths array. So OK
			result[i] = datedDepths.get(i).getDepth();
		}

		return result;
	}

	public double[] getYearArray() {
		double[] result = new double[datedDepths.size()];
		for (int i = 0; i < result.length; i++) {
			// Nulls are not allowed in years array. So OK
			result[i] = datedDepths.get(i).getYear();
		}

		return result;
	}

	public DepthYear getDepthYear(int index) {
		return this.datedDepths.get(index);
	}

}
