/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2007, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * -------------------------
 * CombinedDomainXYPlot.java
 * -------------------------
 * (C) Copyright 2001-2007, by Bill Kelemen and Contributors.
 *
 * Original Author:  Bill Kelemen;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Anthony Boulestreau;
 *                   David Basten;
 *                   Kevin Frechette (for ISTI);
 *                   Nicolas Brodu;
 *                   Petr Kubanek (bug 1606205);
 *
 *
 */

package icd2.chart;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
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

/**
 * 
 * @author Mark Royer
 * 
 */
public class IceCombinedDomainXYPlot extends CombinedDomainXYPlot
		implements Cloneable, PublicCloneable, Serializable, PlotChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(IceCombinedDomainXYPlot.class);

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
	public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
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

	public void insertYearMarker(int index, Marker marker, boolean notify) {
		super.addDomainMarker(0, marker, Layer.BACKGROUND, false);

		if (marker instanceof YearMarker) {
			YearMarker year = (YearMarker) marker;
			DateSession ds = year.getDateSession();
			yearMarkers.add(index, year);
			for (int i = index + 1; i < ds.getSize() && i < yearMarkers.size(); i++)
				yearMarkers.get(i).setLabel(String.valueOf(ds.getYear(i)), false);
		}

		if (notify) {
			fireChangeEvent();
		}
	}

	public Rectangle2D getDataArea() {
		return dataArea;
	}

}
