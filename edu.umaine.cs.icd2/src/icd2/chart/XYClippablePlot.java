/**
 * 
 */
package icd2.chart;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.RendererUtilities;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 * @author Mark Royer
 *
 */
public class XYClippablePlot extends XYPlot {

	/** For serialization. */
	private static final long serialVersionUID = 7044148245716569264L;

	public XYClippablePlot(XYSeriesCollection dataset, ValueAxis domainAxis, NumberAxis rangeAxis,
			XYLineAndShapeRenderer xyLineAndShapeRenderer) {
		super(dataset, domainAxis, rangeAxis, xyLineAndShapeRenderer);
	}

	/**
	 * Trims a rectangle to integer coordinates.
	 *
	 * @param rect
	 *            the incoming rectangle.
	 *
	 * @return A rectangle with integer coordinates.
	 */
	private Rectangle integerise(Rectangle2D rect) {
		int x0 = (int) Math.ceil(rect.getMinX());
		int y0 = (int) Math.ceil(rect.getMinY());
		int x1 = (int) Math.floor(rect.getMaxX());
		int y1 = (int) Math.floor(rect.getMaxY());
		return new Rectangle(x0, y0, (x1 - x0), (y1 - y0));
	}

	/**
	 * Draws the plot within the specified area on a graphics device.
	 *
	 * @param g2
	 *            the graphics device.
	 * @param area
	 *            the plot area (in Java2D space).
	 * @param anchor
	 *            an anchor point in Java2D space (<code>null</code> permitted).
	 * @param parentState
	 *            the state from the parent plot, if there is one (
	 *            <code>null</code> permitted).
	 * @param info
	 *            collects chart drawing information (<code>null</code>
	 *            permitted).
	 */
	@Override
	public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {

		// if the plot area is too small, just return...
		boolean b1 = (area.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
		boolean b2 = (area.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
		if (b1 || b2) {
			return;
		}

		// record the plot area...
		if (info != null) {
			info.setPlotArea(area);
		}

		// adjust the drawing area for the plot insets (if any)...
		RectangleInsets insets = getInsets();
		insets.trim(area);

		AxisSpace space = calculateAxisSpace(g2, area);
		Rectangle2D dataArea = space.shrink(area, null);
		this.getAxisOffset().trim(dataArea);

		dataArea = integerise(dataArea);
		if (dataArea.isEmpty()) {
			return;
		}
		createAndAddEntity((Rectangle2D) dataArea.clone(), info, null, null);
		if (info != null) {
			info.setDataArea(dataArea);
		}

		// draw the plot background and axes...
		drawBackground(g2, dataArea);
		Map<Axis, AxisState> axisStateMap = drawAxes(g2, area, dataArea, info);

		PlotOrientation orient = getOrientation();

		// the anchor point is typically the point where the mouse last
		// clicked - the crosshairs will be driven off this point...
		if (anchor != null && !dataArea.contains(anchor)) {
			anchor = null;
		}
		CrosshairState crosshairState = new CrosshairState();
		crosshairState.setCrosshairDistance(Double.POSITIVE_INFINITY);
		crosshairState.setAnchor(anchor);

		crosshairState.setAnchorX(Double.NaN);
		crosshairState.setAnchorY(Double.NaN);
		if (anchor != null) {
			ValueAxis domainAxis = getDomainAxis();
			if (domainAxis != null) {
				double x;
				if (orient == PlotOrientation.VERTICAL) {
					x = domainAxis.java2DToValue(anchor.getX(), dataArea, getDomainAxisEdge());
				} else {
					x = domainAxis.java2DToValue(anchor.getY(), dataArea, getDomainAxisEdge());
				}
				crosshairState.setAnchorX(x);
			}
			ValueAxis rangeAxis = getRangeAxis();
			if (rangeAxis != null) {
				double y;
				if (orient == PlotOrientation.VERTICAL) {
					y = rangeAxis.java2DToValue(anchor.getY(), dataArea, getRangeAxisEdge());
				} else {
					y = rangeAxis.java2DToValue(anchor.getX(), dataArea, getRangeAxisEdge());
				}
				crosshairState.setAnchorY(y);
			}
		}
		crosshairState.setCrosshairX(getDomainCrosshairValue());
		crosshairState.setCrosshairY(getRangeCrosshairValue());
		Shape originalClip = g2.getClip();
		Composite originalComposite = g2.getComposite();

		Rectangle2D clipArea = dataArea;

		XYItemRenderer r = getRenderer();
		if (r instanceof IceDatingRenderer) {
			IceDatingRenderer idr = (IceDatingRenderer) r;

			if (!idr.isClipTop())
				clipArea = new Rectangle2D.Double(clipArea.getX(), Double.MIN_VALUE, clipArea.getWidth(),
						(dataArea.getY() - Double.MIN_VALUE) + clipArea.getHeight());
			if (!idr.isClipRight())
				clipArea = new Rectangle2D.Double(clipArea.getX(), clipArea.getY(), Double.MAX_VALUE,
						clipArea.getHeight());
			if (!idr.isClipBottom())
				clipArea = new Rectangle2D.Double(clipArea.getX(), clipArea.getY(), clipArea.getWidth(),
						Double.MAX_VALUE);
			if (!idr.isClipLeft())
				clipArea = new Rectangle2D.Double(Double.MIN_VALUE, clipArea.getY(),
						(dataArea.getX() - Double.MIN_VALUE) + clipArea.getWidth(), clipArea.getHeight());

		}

		g2.clip(clipArea);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getForegroundAlpha()));

		AxisState domainAxisState = axisStateMap.get(getDomainAxis());
		if (domainAxisState == null) {
			if (parentState != null) {
				domainAxisState = (AxisState) parentState.getSharedAxisStates().get(getDomainAxis());
			}
		}

		AxisState rangeAxisState = axisStateMap.get(getRangeAxis());
		if (rangeAxisState == null) {
			if (parentState != null) {
				rangeAxisState = (AxisState) parentState.getSharedAxisStates().get(getRangeAxis());
			}
		}
		if (domainAxisState != null) {
			drawDomainTickBands(g2, dataArea, domainAxisState.getTicks());
		}
		if (rangeAxisState != null) {
			drawRangeTickBands(g2, dataArea, rangeAxisState.getTicks());
		}
		if (domainAxisState != null) {
			drawDomainGridlines(g2, dataArea, domainAxisState.getTicks());
			drawZeroDomainBaseline(g2, dataArea);
		}
		if (rangeAxisState != null) {
			drawRangeGridlines(g2, dataArea, rangeAxisState.getTicks());
			drawZeroRangeBaseline(g2, dataArea);
		}

		Graphics2D savedG2 = g2;
		BufferedImage dataImage = null;
		boolean suppressShadow = Boolean.TRUE.equals(g2.getRenderingHint(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION));
		if (this.getShadowGenerator() != null && !suppressShadow) {
			dataImage = new BufferedImage((int) dataArea.getWidth(), (int) dataArea.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			g2 = dataImage.createGraphics();
			g2.translate(-dataArea.getX(), -dataArea.getY());
			g2.setRenderingHints(savedG2.getRenderingHints());
		}

		// draw the markers that are associated with a specific dataset...
		for (int i = 0; i < getDatasetCount(); i++) {
			XYDataset dataset = getDataset(i);
			int datasetIndex = indexOf(dataset);
			drawDomainMarkers(g2, dataArea, datasetIndex, Layer.BACKGROUND);
		}
		for (int i = 0; i < getDatasetCount(); i++) {
			XYDataset dataset = getDataset(i);
			int datasetIndex = indexOf(dataset);
			drawRangeMarkers(g2, dataArea, datasetIndex, Layer.BACKGROUND);
		}

		// now draw annotations and render data items...
		boolean foundData = false;
		DatasetRenderingOrder order = getDatasetRenderingOrder();
		List<Integer> rendererIndices = getRendererIndices(order);
		List<Integer> datasetIndices = getDatasetIndices(order);
		// draw background annotations
		for (int i : rendererIndices) {
			XYItemRenderer renderer = getRenderer(i);
			if (renderer != null) {
				ValueAxis domainAxis = getDomainAxisForDataset(i);
				ValueAxis rangeAxis = getRangeAxisForDataset(i);
				renderer.drawAnnotations(g2, dataArea, domainAxis, rangeAxis, Layer.BACKGROUND, info);
			}
		}

		// render data items...
		for (int datasetIndex : datasetIndices) {
			foundData = render(g2, dataArea, datasetIndex, info, crosshairState) || foundData;
		}

		// draw foreground annotations
		for (int i : rendererIndices) {
			XYItemRenderer renderer = getRenderer(i);
			if (renderer != null) {
				ValueAxis domainAxis = getDomainAxisForDataset(i);
				ValueAxis rangeAxis = getRangeAxisForDataset(i);
				renderer.drawAnnotations(g2, dataArea, domainAxis, rangeAxis, Layer.FOREGROUND, info);
			}
		}

		// draw domain crosshair if required...
		int datasetIndex = crosshairState.getDatasetIndex();
		ValueAxis xAxis = this.getDomainAxisForDataset(datasetIndex);
		RectangleEdge xAxisEdge = getDomainAxisEdge(getDomainAxisIndex(xAxis));
		if (!this.isDomainCrosshairLockedOnData() && anchor != null) {
			double xx;
			if (orient == PlotOrientation.VERTICAL) {
				xx = xAxis.java2DToValue(anchor.getX(), dataArea, xAxisEdge);
			} else {
				xx = xAxis.java2DToValue(anchor.getY(), dataArea, xAxisEdge);
			}
			crosshairState.setCrosshairX(xx);
		}
		setDomainCrosshairValue(crosshairState.getCrosshairX(), false);
		if (isDomainCrosshairVisible()) {
			double x = getDomainCrosshairValue();
			Paint paint = getDomainCrosshairPaint();
			Stroke stroke = getDomainCrosshairStroke();
			drawDomainCrosshair(g2, dataArea, orient, x, xAxis, stroke, paint);
		}

		// draw range crosshair if required...
		ValueAxis yAxis = getRangeAxisForDataset(datasetIndex);
		RectangleEdge yAxisEdge = getRangeAxisEdge(getRangeAxisIndex(yAxis));
		if (!this.isRangeCrosshairLockedOnData() && anchor != null) {
			double yy;
			if (orient == PlotOrientation.VERTICAL) {
				yy = yAxis.java2DToValue(anchor.getY(), dataArea, yAxisEdge);
			} else {
				yy = yAxis.java2DToValue(anchor.getX(), dataArea, yAxisEdge);
			}
			crosshairState.setCrosshairY(yy);
		}
		setRangeCrosshairValue(crosshairState.getCrosshairY(), false);
		if (isRangeCrosshairVisible()) {
			double y = getRangeCrosshairValue();
			Paint paint = getRangeCrosshairPaint();
			Stroke stroke = getRangeCrosshairStroke();
			drawRangeCrosshair(g2, dataArea, orient, y, yAxis, stroke, paint);
		}

		if (!foundData) {
			drawNoDataMessage(g2, dataArea);
		}

		for (int i : rendererIndices) {
			drawDomainMarkers(g2, dataArea, i, Layer.FOREGROUND);
		}
		for (int i : rendererIndices) {
			drawRangeMarkers(g2, dataArea, i, Layer.FOREGROUND);
		}

		drawAnnotations(g2, dataArea, info);
		if (getShadowGenerator() != null && !suppressShadow) {
			BufferedImage shadowImage = getShadowGenerator().createDropShadow(dataImage);
			g2 = savedG2;
			g2.drawImage(shadowImage, (int) dataArea.getX() + getShadowGenerator().calculateOffsetX(),
					(int) dataArea.getY() + getShadowGenerator().calculateOffsetY(), null);
			g2.drawImage(dataImage, (int) dataArea.getX(), (int) dataArea.getY(), null);
		}

		g2.setClip(originalClip);
		g2.setComposite(originalComposite);

		drawOutline(g2, dataArea);

	}

	private List<Integer> getRendererIndices(DatasetRenderingOrder order) {
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < this.getRendererCount(); i++) {
			XYItemRenderer entry = getRenderer(i);
			if (entry != null) {
				result.add(i);
			}
		}
		Collections.sort(result);
		if (order == DatasetRenderingOrder.REVERSE) {
			Collections.reverse(result);
		}
		return result;
	}

	/**
	 * Returns the indices of the non-null datasets in the specified order.
	 * 
	 * @param order
	 *            the order (<code>null</code> not permitted).
	 * 
	 * @return The list of indices.
	 */
	private List<Integer> getDatasetIndices(DatasetRenderingOrder order) {
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < getDatasetCount(); i++) {
			XYDataset entry = getDataset(i);
			if (entry != null) {
				result.add(i);
			}
		}
		Collections.sort(result);
		if (order == DatasetRenderingOrder.REVERSE) {
			Collections.reverse(result);
		}
		return result;
	}

	/**
	 * Draws a representation of the data within the dataArea region, using the
	 * current renderer.
	 * <P>
	 * The <code>info</code> and <code>crosshairState</code> arguments may be
	 * <code>null</code>.
	 *
	 * @param g2
	 *            the graphics device.
	 * @param dataArea
	 *            the region in which the data is to be drawn.
	 * @param index
	 *            the dataset index.
	 * @param info
	 *            an optional object for collection dimension information.
	 * @param crosshairState
	 *            collects crosshair information (<code>null</code> permitted).
	 *
	 * @return A flag that indicates whether any data was actually rendered.
	 */
	public boolean render(Graphics2D g2, Rectangle2D dataArea, int index, PlotRenderingInfo info,
			CrosshairState crosshairState) {

		boolean foundData = false;
		XYDataset dataset = getDataset(index);
		if (!DatasetUtilities.isEmptyOrNull(dataset)) {
			foundData = true;
			ValueAxis xAxis = getDomainAxisForDataset(index);
			ValueAxis yAxis = getRangeAxisForDataset(index);
			if (xAxis == null || yAxis == null) {
				return foundData; // can't render anything without axes
			}
			XYItemRenderer renderer = getRenderer(index);

			if (renderer == null) {
				renderer = getRenderer();
				if (renderer == null) { // no default renderer available
					return foundData;
				}
			}

			IceDatingRenderer idr = null;
			if (renderer instanceof IceDatingRenderer)
				idr = (IceDatingRenderer) renderer;

			XYItemRendererState state = renderer.initialise(g2, dataArea, this, dataset, info);
			int passCount = renderer.getPassCount();

			SeriesRenderingOrder seriesOrder = getSeriesRenderingOrder();
			if (seriesOrder == SeriesRenderingOrder.REVERSE) {
				// render series in reverse order
				for (int pass = 0; pass < passCount; pass++) {
					int seriesCount = dataset.getSeriesCount();
					for (int series = seriesCount - 1; series >= 0; series--) {
						int firstItem = 0;
						int lastItem = dataset.getItemCount(series) - 1;
						if (lastItem == -1) {
							continue;
						}
						if (state.getProcessVisibleItemsOnly()) {
							int[] itemBounds = RendererUtilities.findLiveItems(dataset, series, xAxis.getLowerBound(),
									xAxis.getUpperBound());
							if (idr == null || idr.isClipLeft())
								firstItem = Math.max(itemBounds[0] - 1, 0);
							if (idr == null || idr.isClipRight())
								lastItem = Math.min(itemBounds[1] + 1, lastItem);
						}
						state.startSeriesPass(dataset, series, firstItem, lastItem, pass, passCount);
						for (int item = firstItem; item <= lastItem; item++) {
							renderer.drawItem(g2, state, dataArea, info, this, xAxis, yAxis, dataset, series, item,
									crosshairState, pass);
						}
						state.endSeriesPass(dataset, series, firstItem, lastItem, pass, passCount);
					}
				}
			} else {
				// render series in forward order
				for (int pass = 0; pass < passCount; pass++) {
					int seriesCount = dataset.getSeriesCount();
					for (int series = 0; series < seriesCount; series++) {
						int firstItem = 0;
						int lastItem = dataset.getItemCount(series) - 1;
						if (state.getProcessVisibleItemsOnly()) {
							int[] itemBounds = RendererUtilities.findLiveItems(dataset, series, xAxis.getLowerBound(),
									xAxis.getUpperBound());
							if (idr == null || idr.isClipLeft())
								firstItem = Math.max(itemBounds[0] - 1, 0);
							if (idr == null || idr.isClipRight())
								lastItem = Math.min(itemBounds[1] + 1, lastItem);
						}
						state.startSeriesPass(dataset, series, firstItem, lastItem, pass, passCount);
						for (int item = firstItem; item <= lastItem; item++) {
							renderer.drawItem(g2, state, dataArea, info, this, xAxis, yAxis, dataset, series, item,
									crosshairState, pass);
						}
						state.endSeriesPass(dataset, series, firstItem, lastItem, pass, passCount);
					}
				}
			}
		}
		return foundData;
	}
}
