package icd2.chart;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.event.MouseInputAdapter;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

public class PanningMouseListener extends MouseInputAdapter {

	private Point lastLoc;

	private ChartPanel cp;

	private XYPlot subPlot;

	public PanningMouseListener(ChartPanel cp) {
		this.cp = cp;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (isContained(e.getPoint())) {
			lastLoc = e.getPoint();
		}
	}

	public boolean isContained(Point p) {

		PlotRenderingInfo pInfo = cp.getChartRenderingInfo().getPlotInfo();

		// Actually should be CombinedDomainXYPlot
		AxisLocation dAxisLocation = ((XYPlot) cp.getChart().getPlot())
				.getDomainAxisLocation();

		Rectangle2D dataArea = pInfo.getDataArea();
		Rectangle2D plotArea = pInfo.getPlotArea();

		if (AxisLocation.TOP_OR_RIGHT.equals(dAxisLocation)
				|| AxisLocation.TOP_OR_LEFT.equals(dAxisLocation)) {
			// TODO check this works
			if (new Rectangle2D.Double(dataArea.getMinX(), plotArea.getMinY(),
					dataArea.getWidth(), dataArea.getMinY()).contains(p)) {
				subPlot = null;
				return true;
			}
		}
		if (AxisLocation.BOTTOM_OR_RIGHT.equals(dAxisLocation)
				|| AxisLocation.BOTTOM_OR_LEFT.equals(dAxisLocation)) {

			Rectangle2D r = new Rectangle2D.Double(dataArea.getMinX(),
					dataArea.getMaxY(), dataArea.getWidth(), plotArea.getMaxY());

			if (r.contains(p)) {
				subPlot = null;
				return true;
			}
		}

		// Not domain check the range axes

		@SuppressWarnings("unchecked")
		List<XYPlot> subPlots = (List<XYPlot>) ((CombinedDomainXYPlot) cp
				.getChart().getPlot()).getSubplots();

		for (int i = 0; i < subPlots.size(); i++) {

			XYPlot sp = subPlots.get(i);

			AxisLocation rAxisLocation = sp.getRangeAxisLocation();

			PlotRenderingInfo spInfo = pInfo.getSubplotInfo(i);

			dataArea = spInfo.getDataArea();
			plotArea = spInfo.getPlotArea();

			if (AxisLocation.BOTTOM_OR_LEFT.equals(rAxisLocation)
					|| AxisLocation.TOP_OR_LEFT.equals(rAxisLocation)) {
				if (new Rectangle2D.Double(plotArea.getMinX(),
						dataArea.getMinY(), plotArea.getWidth()
								- dataArea.getWidth(), dataArea.getHeight())
						.contains(p)) {
					subPlot = sp;
					return true;
				}
			}
			if (AxisLocation.BOTTOM_OR_RIGHT.equals(rAxisLocation)
					|| AxisLocation.TOP_OR_RIGHT.equals(rAxisLocation)) {
				if (new Rectangle2D.Double(dataArea.getMaxX(),
						dataArea.getMinY(), dataArea.getMinX(),
						dataArea.getMaxY()).contains(p)) {
					subPlot = sp;
					return true;
				}
			}

		}

		// No match
		return false;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		lastLoc = null;
		subPlot = null;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (lastLoc != null && subPlot == null) {

			Point curPoint = e.getPoint();

			if (Math.abs(curPoint.getX() - lastLoc.getX()) >= 2) {

				CombinedDomainXYPlot cdp = (CombinedDomainXYPlot) cp.getChart()
						.getPlot();

				double l = cdp.getDomainAxis().getLowerBound();
				double u = cdp.getDomainAxis().getUpperBound();

				Double ll = JFreeUtil.getCoordinate(cp, cdp,
						(XYPlot) cdp.getSubplots().get(0), lastLoc).getX();
				Double cur = JFreeUtil.getCoordinate(cp, cdp,
						(XYPlot) cdp.getSubplots().get(0), e.getPoint()).getX();

				double delta = (ll - cur);

				l += delta;
				u += delta;

				cdp.getDomainAxis().setRange(l, u);
				lastLoc = e.getPoint();
			}
		} else if (lastLoc != null && subPlot != null) {

			Point curPoint = e.getPoint();

			if (Math.abs(curPoint.getY() - lastLoc.getY()) >= 2) {

				CombinedDomainXYPlot cdp = (CombinedDomainXYPlot) cp.getChart()
						.getPlot();

				double l = subPlot.getRangeAxis().getLowerBound();
				double u = subPlot.getRangeAxis().getUpperBound();

				Double ll = JFreeUtil.getCoordinate(cp, cdp, subPlot, lastLoc)
						.getY();
				Double cur = JFreeUtil.getCoordinate(cp, cdp, subPlot,
						e.getPoint()).getY();

				double delta = (ll - cur);

				l += delta;
				u += delta;

				subPlot.getRangeAxis().setRange(l, u);
				lastLoc = e.getPoint();
			}

		}
	}
}