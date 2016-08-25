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
@EditableObject(name="Dating Session", description="Year values to use for resampling")
public class DateSession implements ModelObject<DateSession, DatingProject> {
	
	private static final Logger logger = LoggerFactory.getLogger(DateSession.class);
	
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

	private DatingProject parent;

	@Editable(name = "Session Name", description = "A label for the dating session.",
			onchange = CoreModelConstants.ICD2_MODEL_MODELOBJECT_NAME_CHANGE)
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

	public void addYearDepth(int year, double depth) {
		datedDepths.add(new DepthYear(this, depth, year));
	}

	public void removeYearDepth(int index) {
		datedDepths.remove(index);
	}

	/**
	 * This will insert a new year value based on the proper position of xx.
	 * 
	 * @param depth
	 * @return The index it was inserted at or a negative number if it was not inserted.
	 * @throws IllegalAccessException 
	 */
	public int insertDepth(double depth) throws IllegalAccessException {
		int i = datedDepths.size();
		
		if (depth <= datedDepths.get(i-1).getDepth()) // Already has a top depth of 0 
			i = Collections.binarySearch(datedDepths, new DepthYear(this, depth,0), new Comparator<DepthYear>(){
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
			
		if (0 <= i && i < datedDepths.size()) {
			logger.info("A depth at position {} has already been dated.", depth);
			return -1;
		} else if (i < 0){
			i = -1 * (i + 1); // Insertion point is (-(insertion point) - 1)
		} // Otherwise it's at position depths.size() - the last depth
		
		if (i < 1) {
			logger.info("Depth position {} is above top year and will be ignored.", depth);
			return -1;
		}
			
		// There is at least a top year already.
		datedDepths.add(i, new DepthYear(this, depth, datedDepths.get(i-1).getYear() - 1)); 
		
		// Shift the rest of the years by one
		for (int j = i + 1; j < datedDepths.size(); j++) {
			datedDepths.get(j).setYear(datedDepths.get(j).getYear()-1);
		}
		
		return i;
	}

	public int getYear(int i) {
		return datedDepths.get(i).getYear();
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

}
