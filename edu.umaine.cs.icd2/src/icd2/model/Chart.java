/**
 * 
 */
package icd2.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mark Royer
 *
 */
@EditableObject(name = "Chart", description = "Graphically displays data.")
public class Chart implements PlotParent<Chart, DatingProject> {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(Chart.class);

	private DatingProject parent;

	@Editable(name = "Chart Title", description = "The title of the chart.", onchange = {
			CoreModelConstants.ICD2_MODEL_MODELOBJECT_NAME_CHANGE, CoreModelConstants.ICD2_MODEL_CHART_TITLE_CHANGE })
	private String title;

	private Plot[][] plots;

	@Editable(name = "Active Date Session", description = "The core session used for finding the curve", onchange = CoreModelConstants.ICD2_MODEL_DATESESSION_CHANGE, valueProvider = DateSessionValueProvider.class)
	private DateSession activeDateSession;

	public Chart(DatingProject parent, String title, Plot plot) {
		this.parent = parent;
		this.title = title;
		this.plots = new Plot[1][1];
		plots[0][0] = plot;
		plot.setParent(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icd2.model.ModelObject#setParent(icd2.model.ModelObject)
	 */
	@Override
	public void setParent(DatingProject parent) {
		this.parent = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icd2.model.ModelObject#getParent()
	 */
	@Override
	public DatingProject getParent() {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icd2.model.ModelObject#children()
	 */
	@Override
	public ModelObject<?, ?>[] children() {

		Plot[] result = new Plot[plots.length * plots[0].length];

		for (int i = 0; i < plots.length; i++) {
			for (int j = 0; j < plots[i].length; j++) {
				result[i * plots.length + j] = plots[i][j];
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icd2.model.ModelObject#getName()
	 */
	@Override
	public String getName() {
		return title;
	}

	/**
	 * @return The chart's displayed title (Never null)
	 */
	public String getTitle() {
		return title;
	}

	public void setActiveDateSession(DateSession session) {
		this.activeDateSession = session;
	}

	/**
	 * @return May be null if no active session set.
	 */
	public DateSession getActiveDateSession() {
		return activeDateSession;
	}

	public Plot[][] getPlots() {
		return plots;
	}

}
