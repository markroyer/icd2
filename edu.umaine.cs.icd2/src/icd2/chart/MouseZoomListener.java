package icd2.chart;

import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MouseZoomListener implements MouseWheelListener {

	private static final Logger logger = LoggerFactory.getLogger(MouseZoomListener.class);

	private ChartPanel cp;
	
	public MouseZoomListener(ChartPanel cp) {
		this.cp = cp;
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {
		
		CombinedDomainXYPlot cdPlot = (CombinedDomainXYPlot) cp.getChart().getXYPlot();
		
		Point mousePoint = event.getPoint(); // Mouse location on screen
		
		ChartRenderingInfo chartInfo = cp.getChartRenderingInfo();
		Point2D java2DPoint = cp.translateScreenToJava2D(mousePoint); // Take into account insets and scale
		
		PlotRenderingInfo plotInfo = chartInfo.getPlotInfo();
		Rectangle2D dataArea = plotInfo.getDataArea();

		double xp = cdPlot.getDomainAxis().java2DToValue(java2DPoint.getX(), dataArea,
				cdPlot.getDomainAxisEdge()); // Finally get the location of the mouse pointer on the plot

		if (event.getWheelRotation() > 0) {
			ValueAxis dAxis = cdPlot.getDomainAxis();
			double x3 = dAxis.getLowerBound();
			double x4 = dAxis.getUpperBound();
			double x1 = 2 * x3 - xp;
			double x2 = 2 * x4 - xp;

			dAxis.setRange(x1, x2);
			
			logger.debug("Attempting to zoom out. x1={}, x2={}, xp={}, x3={}, x4={}", x1, x2, xp, x3, x4);
		} else if (event.getWheelRotation() < 0) {
			ValueAxis dAxis = cdPlot.getDomainAxis();
			double x1 = dAxis.getLowerBound();
			double x2 = dAxis.getUpperBound();
			double x3 = (xp + x1) / 2.;
			double x4 = (xp + x2) / 2.;

			dAxis.setRange(x3, x4);

			logger.debug("Attempting to zoom in. x1={}, x2={}, xp={}, x3={}, x4={}", x1, x2, xp, x3, x4);
		}
		
	}
	
}
