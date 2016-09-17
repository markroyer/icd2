/**
 * 
 */
package icd2.chart;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.DateSession;

/**
 * @author Mark Royer
 *
 */
public class YearMarker extends IntervalMarker {

	private static final Logger logger = LoggerFactory
			.getLogger(YearMarker.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected String label;

	protected DateSession dateSession;

	public YearMarker(double xPos, int label, DateSession ds) {
		super(xPos, xPos);
		dateSession = ds;
		setLabel(String.valueOf(dateSession.getYear(label)));
		setLabelAnchor(RectangleAnchor.TOP_LEFT);
		setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
		logger.info("Created year marker at {} year.",
				dateSession.getYear(label));
		setAlpha(1f);
		setPaint(Color.BLACK);
		setStroke(new BasicStroke(0f));
	}

	public void setLabel(String label, boolean notify) {
		this.label = label;

		if (notify)
			notifyListeners(new MarkerChangeEvent(this));
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof YearMarker) {
			YearMarker other = (YearMarker) obj;

			return super.equals(obj) && (label == null && other.label == null
					|| label.equals(other.label));
		}

		return false;
	}

	public DateSession getDateSession() {
		return dateSession;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{ year: %s }", label);
	}

}
