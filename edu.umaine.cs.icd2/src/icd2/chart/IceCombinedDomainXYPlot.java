/**
 * 
 */
package icd2.chart;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.ui.Layer;
import org.jfree.util.PublicCloneable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.DateSession;
import icd2.model.DepthYear;

/**
 * 
 * @author Mark Royer
 * 
 */
public class IceCombinedDomainXYPlot extends CombinedDomainXYPlot implements
		Cloneable, PublicCloneable, Serializable, PlotChangeListener {

	private static final Logger logger = LoggerFactory
			.getLogger(IceCombinedDomainXYPlot.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected List<YearMarker> yearMarkers;

	protected Rectangle2D dataArea;

	/**
	 * Default constructor.
	 */
	public IceCombinedDomainXYPlot() {
		this(new NumberAxis());
	}

	public IceCombinedDomainXYPlot(NumberAxis domainAxis) {
		super(domainAxis);
		yearMarkers = new ArrayList<>();
		setGap(0);
		setRenderer(new IceDatingRenderer());
	}

	@Override
	public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
			PlotState parentState, PlotRenderingInfo info) {
		try {
			super.draw(g2, area, anchor, parentState, info);
		} catch (Exception e) {
			logger.error("Unable to draw with current axis. Resetting axis.");
			this.getDomainAxis().setAutoRange(true);
			return;
		}
		AxisSpace space = calculateAxisSpace(g2, area);
		dataArea = space.shrink(area, null);

		drawDomainMarkers(g2, dataArea, 0, Layer.BACKGROUND);

	}

	public void insertYearMarker(int index, YearMarker yearMarker,
			boolean notify) {
		super.addDomainMarker(0, yearMarker, Layer.BACKGROUND, false);

		DateSession ds = yearMarker.getDateSession();

		yearMarkers.add(index, yearMarker);
		for (int i = index + 1; i < ds.getSize() && i < yearMarkers.size(); i++)
			yearMarkers.get(i).setLabel(String.valueOf(ds.getYear(i)), false);

		if (notify) {
			fireChangeEvent();
		}
	}

	public Rectangle2D getDataArea() {
		return dataArea;
	}

	public void removeYearMarker(DepthYear depthMarker) {

		logger.debug("Removing year marker {} from {}.", depthMarker,
				yearMarkers);

		int index = Collections.binarySearch(yearMarkers,
				new YearMarker(depthMarker.getDepth(),
						String.valueOf(depthMarker.getYear()),
						depthMarker.getParent()),
				(o1, o2) -> o2.getLabel().compareTo(o1.getLabel()));

		Marker marker = yearMarkers.get(index);
		removeDomainMarker(0, marker, Layer.BACKGROUND);
		yearMarkers.remove(index);

	}

}
