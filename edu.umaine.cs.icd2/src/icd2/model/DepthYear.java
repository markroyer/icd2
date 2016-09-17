/**
 * 
 */
package icd2.model;

/**
 * Represents a single date at a given depth.
 * 
 * @author Mark Royer
 *
 */
public class DepthYear implements ModelObject<DepthYear, DateSession> {

	private double depth;
	
	private int year;
	
	private DateSession parent;

	public DepthYear(DateSession parent, double depth, int year) {
		super();
		this.parent = parent;
		this.depth = depth;
		this.year = year;
	}

	public double getDepth() {
		return depth;
	}

	public void setDepth(double depth) {
		this.depth = depth;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	@Override
	public void setParent(DateSession parent) {
		this.parent = parent;
	}

	@Override
	public DateSession getParent() {
		return this.parent;
	}

	@Override
	public ModelObject<?, ?>[] children() {
		return EMPTYARRAY;
	}

	@Override
	public String getName() {
		return String.format("(%.3f, %d)", depth, year);
	}

	@Override
	public String toString() {
		return getName();
	}
	
}
