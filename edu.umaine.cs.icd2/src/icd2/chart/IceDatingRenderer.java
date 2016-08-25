/**
 * 
 */
package icd2.chart;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.LineUtilities;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.text.TextUtilities;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;

/**
 * @author Mark Royer
 *
 */
public class IceDatingRenderer extends XYLineAndShapeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean clipTop;

	private boolean clipBottom;

	private boolean clipLeft;

	private boolean clipRight;

	public IceDatingRenderer() {
		super(true, false);
		clipBottom = true;
		clipTop = true;
		clipLeft = true;
		clipRight = true;
	}

	@Override
	public void drawDomainMarker(Graphics2D g2, XYPlot plot, ValueAxis domainAxis, Marker marker,
			Rectangle2D dataArea) {

		// Most of this is very similar to AbstractXYItemRenderer
		if (marker instanceof YearMarker) {

			YearMarker im = (YearMarker) marker;
			double start = im.getStartValue();
			double end = im.getEndValue();
			Range range = domainAxis.getRange();
			if (!(range.intersects(start, end))) {
				return;
			}

			double start2d = domainAxis.valueToJava2D(start, dataArea, plot.getDomainAxisEdge());
			double end2d = domainAxis.valueToJava2D(end, dataArea, plot.getDomainAxisEdge());
			double low = Math.min(start2d, end2d);
			double high = Math.max(start2d, end2d);

			PlotOrientation orientation = plot.getOrientation();
			Rectangle2D rect = null;
			if (orientation == PlotOrientation.HORIZONTAL) {
				// clip top and bottom bounds to data area
				low = // clipBottom ?
				Math.max(low, dataArea.getMinY())
				// : Double.MIN_VALUE
				;
				high = // clipTop ?
				Math.min(high, dataArea.getMaxY())
				// : Double.MAX_VALUE
				;
				rect = new Rectangle2D.Double(dataArea.getMinX(), low, dataArea.getWidth(), high - low);
			} else if (orientation == PlotOrientation.VERTICAL) {
				// clip left and right bounds to data area
				low = // clipBottom ?
				Math.max(low, dataArea.getMinX())
				// : Double.MIN_VALUE
				;
				high = // clipTop ?
				Math.min(high, dataArea.getMaxX())
				// : Double.MAX_VALUE
				;
				rect = new Rectangle2D.Double(low, dataArea.getMinY(), high - low, dataArea.getHeight());
			}

			final Composite originalComposite = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, marker.getAlpha()));
			Paint p = marker.getPaint();
			if (p instanceof GradientPaint) {
				GradientPaint gp = (GradientPaint) p;
				GradientPaintTransformer t = im.getGradientPaintTransformer();
				if (t != null) {
					gp = t.transform(gp, rect);
				}
				g2.setPaint(gp);
			} else {
				g2.setPaint(p);
			}
			g2.fill(rect);
			
//			g2.setStroke(new BasicStroke(.1f));
//			g2.setPaint(new Color(1,0,0,1f));
//			g2.draw(new Line2D.Double(rect.getX(), rect.getY(), rect.getX(), rect.getY()+rect.getHeight()));

			// now draw the outlines, if visible...
			if (im.getOutlinePaint() != null && im.getOutlineStroke() != null) {
				if (orientation == PlotOrientation.VERTICAL) {
					Line2D line = new Line2D.Double();
					double y0 = dataArea.getMinY();
					double y1 = dataArea.getMaxY();
					g2.setPaint(im.getOutlinePaint());
					g2.setStroke(im.getOutlineStroke());

					if (range.contains(start)) {
						line.setLine(start2d, y0, start2d, y1);
						g2.draw(line);
					}
					if (range.contains(end)) {
						line.setLine(end2d, y0, end2d, y1);
						g2.draw(line);
					}
				} else { // PlotOrientation.HORIZONTAL
					Line2D line = new Line2D.Double();
					double x0 = dataArea.getMinX();
					double x1 = dataArea.getMaxX();
					g2.setPaint(im.getOutlinePaint());
					g2.setStroke(im.getOutlineStroke());
					if (range.contains(start)) {
						line.setLine(x0, start2d, x1, start2d);
						g2.draw(line);
					}
					if (range.contains(end)) {
						line.setLine(x0, end2d, x1, end2d);
						g2.draw(line);
					}
				}
			}

			String label = marker.getLabel();
			RectangleAnchor anchor = marker.getLabelAnchor();
			if (label != null) {
				Font labelFont = marker.getLabelFont();
				g2.setFont(labelFont);
				g2.setPaint(marker.getLabelPaint());
				Point2D coordinates = calculateDomainMarkerTextAnchorPoint(g2, orientation, dataArea, rect,
						marker.getLabelOffset(), marker.getLabelOffsetType(), anchor);

				// This basically the only modified code section Mark Royer
				TextUtilities.drawRotatedString(label, g2, (float) coordinates.getX(), (float) coordinates.getY(),
						marker.getLabelTextAnchor(), -Math.PI / 4., marker.getLabelTextAnchor());

			}
			g2.setComposite(originalComposite);

		} else {
			super.drawDomainMarker(g2, plot, domainAxis, marker, dataArea);
		}
	}

	/**
	 * Draws the item (first pass). This method draws the lines connecting the
	 * items.
	 *
	 * @param g2
	 *            the graphics device.
	 * @param state
	 *            the renderer state.
	 * @param dataArea
	 *            the area within which the data is being drawn.
	 * @param plot
	 *            the plot (can be used to obtain standard color information
	 *            etc).
	 * @param domainAxis
	 *            the domain axis.
	 * @param rangeAxis
	 *            the range axis.
	 * @param dataset
	 *            the dataset.
	 * @param pass
	 *            the pass.
	 * @param series
	 *            the series index (zero-based).
	 * @param item
	 *            the item index (zero-based).
	 */
	@Override
	protected void drawPrimaryLine(XYItemRendererState state, Graphics2D g2, XYPlot plot, XYDataset dataset, int pass,
			int series, int item, ValueAxis domainAxis, ValueAxis rangeAxis, Rectangle2D dataArea) {
		if (item == 0) {
			return;
		}

		// get the data point...
		double x1 = dataset.getXValue(series, item);
		double y1 = dataset.getYValue(series, item);
		if (Double.isNaN(y1) || Double.isNaN(x1)) {
			return;
		}

		double x0 = dataset.getXValue(series, item - 1);
		double y0 = dataset.getYValue(series, item - 1);
		if (Double.isNaN(y0) || Double.isNaN(x0)) {
			return;
		}

		RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
		RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

		double transX0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
		double transY0 = rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation);

		double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
		double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

		// only draw if we have good values
		if (Double.isNaN(transX0) || Double.isNaN(transY0) || Double.isNaN(transX1) || Double.isNaN(transY1)) {
			return;
		}

		PlotOrientation orientation = plot.getOrientation();
		boolean visible;
		if (orientation == PlotOrientation.HORIZONTAL) {
			state.workingLine.setLine(transY0, transX0, transY1, transX1);
		} else if (orientation == PlotOrientation.VERTICAL) {
			state.workingLine.setLine(transX0, transY0, transX1, transY1);
		}

		Plot topPlot = plot.getParent();
		while (topPlot.getParent() != null) { // Make sure we don't exceed
												// outermost plot
			topPlot = topPlot.getParent();
		}

		double MIN_TOP = Double.MIN_VALUE;
		double MAX_BOTTOM = Double.MAX_VALUE;
		double MIN_LEFT = Double.MIN_VALUE;
		double MAX_RIGHT = Double.MAX_VALUE;

		if (topPlot != null) {
			IceCombinedDomainXYPlot parentPlot = (IceCombinedDomainXYPlot) topPlot;
			Rectangle2D parentDataArea = parentPlot.getDataArea();

			if (parentDataArea != null) {

				MIN_TOP = Math.min(dataArea.getY(), parentDataArea.getY());
				MAX_BOTTOM = Math.max(dataArea.getHeight(), parentDataArea.getHeight());
				MIN_LEFT = Math.min(dataArea.getX(), parentDataArea.getY());
				MAX_RIGHT = Math.min(dataArea.getWidth(), parentDataArea.getWidth());

			} else {
				parentPlot.notifyListeners(new PlotChangeEvent(parentPlot));// Make sure another redraw happens
			}
		}

		// Alter the dataArea based on whether to clip the top or bottom
		// Rectangle2D clipArea = new Rectangle2D.Double(clipLeft ?
		// dataArea.getX() : Double.MIN_VALUE,
		// clipTop ? dataArea.getY() : Double.MIN_VALUE,
		// clipRight ? (clipLeft ? 0 : (dataArea.getX() - Double.MIN_VALUE)) +
		// dataArea.getWidth()
		// : Double.MAX_VALUE,
		// clipBottom ? (clipTop ? 0 : (dataArea.getY() - Double.MIN_VALUE)) +
		// dataArea.getHeight()
		// : Double.MAX_VALUE);

		Rectangle2D clipArea = new Rectangle2D.Double(clipLeft ? dataArea.getX() : MIN_LEFT,
				clipTop ? dataArea.getY() : MIN_TOP,
				clipRight ? (clipLeft ? 0 : (dataArea.getX() - Double.MIN_VALUE)) + dataArea.getWidth() : MAX_RIGHT,
				clipBottom ? (clipTop ? 0 : (dataArea.getY() - Double.MIN_VALUE)) + dataArea.getHeight() : MAX_BOTTOM);

		visible = LineUtilities.clipLine(state.workingLine, clipArea);
		if (visible) {
			drawFirstPassShape(g2, pass, series, item, state.workingLine);
		}
	}

	public boolean isClipTop() {
		return clipTop;
	}

	public void setClipTop(boolean clipTop) {
		this.clipTop = clipTop;
	}

	public boolean isClipBottom() {
		return clipBottom;
	}

	public void setClipBottom(boolean clipBottom) {
		this.clipBottom = clipBottom;
	}

	public boolean isClipLeft() {
		return clipLeft;
	}

	public void setClipLeft(boolean clipLeft) {
		this.clipLeft = clipLeft;
	}

	public boolean isClipRight() {
		return clipRight;
	}

	public void setClipRight(boolean clipRight) {
		this.clipRight = clipRight;
	}

}
