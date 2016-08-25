/**
 * 
 */
package icd2.chart;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.Chart;
import icd2.model.CoreData;
import icd2.model.DateSession;
import icd2.model.ObjectNotFound;
import icd2.model.Plot;
import icd2.model.PlotValues;
import icd2.model.Sample;
import icd2.model.Value;
import icd2.util.DataFileException;

/**
 * @author Mark Royer
 *
 */
public class JFreeUtil {

	private static final Logger logger = LoggerFactory.getLogger(JFreeUtil.class);

	public static JFreeChart createJFreeChart(Chart chartModel) throws ObjectNotFound {

		Plot combinedPlotModel = chartModel.getPlots()[0][0];

		XYSeriesCollection dataset = new XYSeriesCollection();
		// Create the X Axis and customize it
		NumberAxis domainAxis = new NumberAxis(combinedPlotModel.getPlotMethod().toString());

		/*
		 * Our chart is an instance of CombinedDomainXYPlot, because it contains more than one chart (subplots) that
		 * share the same X axis but each has it's own Y axis
		 */
		IceCombinedDomainXYPlot combinedPlot = new IceCombinedDomainXYPlot(domainAxis);

		List<Double> xData = combinedPlotModel.getXData();

		List<PlotValues> plotValues = combinedPlotModel.getRangeValues(); // getYValues();

		CoreData cd = chartModel.getParent().getParent().getCoreData();

		for (int i = 0; i < plotValues.size(); i++) {

			PlotValues pv = plotValues.get(i);
			Sample sample = cd.lookupSample(pv.getValuesKey());

			// create a new XYSeries for each element selected
			XYSeries series = new XYSeries(pv.getName());

			List<Value> sVals = null;
			try {
				sVals = sample.getValues();
			} catch (DataFileException e) {
				logger.error(e.getMessage(), e);
			}
			for (int j = 0; j < sVals.size(); j++) {
				series.add(xData.get(j), sVals.get(j));
			}
			// elementNumber++;
			// create a new XYSeriesCollection for the element series
			dataset = new XYSeriesCollection(series);
			// each element has its own range (Y) axis
			final NumberAxis rangeAxis = new NumberAxis(sample.getName());

			IceDatingRenderer idr = new IceDatingRenderer();
			idr.setSeriesFillPaint(0, pv.getColor());
			idr.setSeriesOutlinePaint(0, pv.getColor());
			idr.setClipTop(pv.isCropValues());
			idr.setClipBottom(pv.isCropValues());

			XYClippablePlot subplot = new XYClippablePlot(dataset, combinedPlot.getDomainAxis(), rangeAxis, idr);// new
																													// XYLineAndShapeRenderer(true,
																													// false));

			// idr.setSeriesShape(0,ShapeUtilities.createDiamond(3f));
			// idr.setSeriesShapesVisible(0, true);

			subplot.setDomainCrosshairVisible(true);
			subplot.setRangeCrosshairVisible(true);
			// {
			//
			// private static final long serialVersionUID =
			// 1809391328166489312L;
			//
			// @Override
			// public void draw(Graphics2D g2, Rectangle2D area,
			// Point2D anchor, PlotState parentState,
			// PlotRenderingInfo info) {
			// super.draw(g2, area, anchor, parentState, info);
			// dataArea = area;
			// }
			//
			// };
			subplot.setBackgroundPaint(null);

			// customization
			// subplot.setWeight(elementNumber);
			// subplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
			// now when you are done customizing the subPlot, add it to
			// your combined plot. Repeat
			// the process for each element (subPlot) you have
			combinedPlot.add(subplot, 1);

		}
		combinedPlot.setDomainCrosshairVisible(true);
		combinedPlot.setRangeCrosshairVisible(true);

		// customize the plot
		// combinedPlot.setFixedLegendItems(items);
		combinedPlot.setOrientation(PlotOrientation.VERTICAL);

		// create your chart from the one big combined plot
		final JFreeChart chart = new JFreeChart(chartModel.getTitle(), JFreeChart.DEFAULT_TITLE_FONT, combinedPlot,
				true);

		// customize your chart
		chart.setBackgroundPaint(java.awt.Color.white);

		chart.getLegend().setBorder(0, 0, 0, 0);

		// further customization of the plot
		final XYPlot plot = chart.getXYPlot();
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

		RectangleInsets m = chart.getTitle().getMargin();
		chart.getTitle().setMargin(m.getTop(), m.getLeft(), m.getBottom() + 20, m.getRight());

		// Add the markers

		DateSession ds = chartModel.getActiveDateSession();

		for (int i = 0; i < ds.getSize(); i++) {
			addYearMarker(chartModel, chart, ds.getDepth(i), i, i == ds.getSize() -1);
		}

		return chart;
	}

	public static Point2D getCoordinate(ChartPanel cp, CombinedDomainXYPlot cdp, XYPlot sp, Point point) {
		final Point2D p = cp.translateScreenToJava2D(new Point(point.x, point.y));
		final Rectangle2D dataArea = cp.getChartRenderingInfo().getPlotInfo().getSubplotInfo(0).getDataArea();

		double xx = cdp.getDomainAxis().java2DToValue(p.getX(), dataArea, cdp.getDomainAxisEdge());
		double yy = sp.getRangeAxis().java2DToValue(p.getY(), dataArea, sp.getRangeAxisEdge());
		return new Point2D.Double(xx, yy);
	}

	public static void addYearMarker(Chart chartModel, JFreeChart chart, double xx, int index, boolean notify) {

		IceCombinedDomainXYPlot plot = ((IceCombinedDomainXYPlot) chart.getPlot());

		DateSession ds = chartModel.getActiveDateSession();
		
		YearMarker newMarker = new YearMarker(xx, index, ds);

		plot.insertYearMarker(index, newMarker, notify);
//		yearMarkers.add(index, newMarker);

//		for (int i = index + 1; i < ds.getSize() && i < yearMarkers.size(); i++)
//			yearMarkers.get(i).setLabel(String.valueOf(ds.getYear(i)), false);

	}
	
}
