package icd2.parts;

import java.awt.Frame;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.chart.IceCombinedDomainXYPlot;
import icd2.chart.IceDatingRenderer;
import icd2.chart.JFreeUtil;
import icd2.chart.MouseZoomListener;
import icd2.chart.PanningMouseListener;
import icd2.chart.YearMarker;
import icd2.handlers.AddDepthMarker;
import icd2.model.Chart;
import icd2.model.CoreData;
import icd2.model.CoreModelConstants;
import icd2.model.DateSession;
import icd2.model.DatingProject;
import icd2.model.DepthYear;
import icd2.model.ModelObject;
import icd2.model.ObjectNotFound;
import icd2.model.Plot;
import icd2.model.PlotValues;
import icd2.model.Sample;
import icd2.model.Workspace;
import icd2.util.PopupUtil;
import icd2.util.WorkspaceUtil;

public class DatingProjectView implements ChartMouseListener {

	private static final Logger logger = LoggerFactory
			.getLogger(DatingProjectView.class);

	private Composite chartComposite;

	private Composite bottomChartComposite;

	private JFreeChart bottomChart;

	private ChartPanel topCp;

	private ChartPanel bottomCp;

	private DatingProject project;

	List<YearMarker> yearMarkers;

	@Inject
	private MDirtyable dirty;

	@Inject
	public DatingProjectView() {
		yearMarkers = new ArrayList<>();
	}

	@PostConstruct
	public void postConstruct(Composite parent, final IEclipseContext ctx,
			@Optional DatingProject incomingProject, MPart mpart,
			Workspace workspace, EMenuService menuService,
			EModelService modelService, MApplication application,
			IEventBroker eventBroker, IUndoContext undoContext)
			throws ObjectNotFound {

		if (incomingProject == null) {
			incomingProject = WorkspaceUtil.getProject(workspace,
					mpart.getLabel());
		}

		parent.setLayout(new GridLayout(1, false));

		this.project = incomingProject;

		ctx.set(DatingProject.class, incomingProject);
		ctx.getParent().remove(DatingProject.class);

		final Chart chartModel = incomingProject.getChart();

		JFreeChart topJFreeChart = JFreeUtil.createJFreeChart(chartModel);

		chartComposite = new Composite(parent,
				SWT.EMBEDDED | SWT.BORDER | SWT.NO_BACKGROUND);
		chartComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// menuService.registerContextMenu(chartComposite,
		// "edu.umaine.cs.icd2.popupmenu.chart");

		final Frame frame = SWT_AWT.new_Frame(chartComposite);
		topCp = new ChartPanel(topJFreeChart);
		topCp.addMouseWheelListener(new MouseZoomListener(topCp));

		// Setting the heights makes it so it doesn't stretch text out
		topCp.setMinimumDrawWidth(0);
		topCp.setMinimumDrawHeight(0);
		topCp.setMaximumDrawWidth(1920);
		topCp.setMaximumDrawHeight(1080);

		topCp.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// Ignore
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// Ignore
			}

			@Override
			public void mouseExited(MouseEvent e) {

				CombinedDomainXYPlot cdPlot = (CombinedDomainXYPlot) DatingProjectView.this.topCp
						.getChart().getPlot();

				@SuppressWarnings("unchecked")
				List<XYPlot> subplotsList = cdPlot.getSubplots();

				for (XYPlot p : subplotsList) {
					p.setRangeCrosshairVisible(false);
					p.setDomainCrosshairVisible(false);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// Ignore
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// Ignore
			}
		});

		topCp.addChartMouseListener(this);
		topCp.setPopupMenu(null);
		topCp.addChartMouseListener(new ChartMouseListener() {

			@Override
			public void chartMouseMoved(ChartMouseEvent arg0) {
				// Do nothing
			}

			@Override
			public void chartMouseClicked(ChartMouseEvent event) {

				// Plot combinedPlotModel = chartModel.getPlots()[0][0];

				// if (combinedPlotModel.getRangeValues().size() < 1)
				// return; // Nothing to do

				if (event.getEntity() != null) {
					// logger.info("Entity: " + event.getEntity());
					// logger.info(event.getEntity().getToolTipText());
					ChartEntity ent = event.getEntity();

					if (ent instanceof XYItemEntity) {

						XYItemEntity entity = (XYItemEntity) ent;

						logger.info(
								"XYItemEntity selected. Entity Item: {}, SeriesIndex: {}, Dataset: {}",
								entity.getItem(), entity.getSeriesIndex(),
								entity.getDataset());
					} else {
						logger.info(
								"Chart clicked.  Selected item is of type {}",
								ent.getClass());
					}
				} else {
					logger.info("Chart clicked, but no entity selected.");
				}
			}
		});

		// add Chart Mouse listeners to the chart panel
		topCp.addChartMouseListener(new ChartMouseListener() {
			// user clicked on chart
			@Override
			public void chartMouseClicked(final ChartMouseEvent event) {

				if (MouseEvent.BUTTON1 == event.getTrigger().getButton()) {

					Plot combinedPlotModel = chartModel.getPlots()[0][0];

					if (combinedPlotModel.getRangeValues().size() < 1)
						return; // Nothing to do

					CombinedDomainXYPlot cdp = (CombinedDomainXYPlot) topCp
							.getChart().getPlot();

					double xx = JFreeUtil.getCoordinate(topCp, cdp,
							(XYPlot) cdp.getSubplots().get(0),
							event.getTrigger().getPoint()).getX();
					// year and depth should always have top year already in
					// them
					DateSession ds = chartModel.getActiveDateSession();

					logger.debug("Depth index is {}. New year is {}",
							ds.getDepthIndex(xx),
							ds.getYear(ds.getDepthIndex(xx)));

					// try {
					AddDepthMarker.addDepthMarker(eventBroker, undoContext,
							project.getChart(), xx,
							ds.getYear(ds.getDepthIndex(xx)), true);

					// int insertionSpot = ds.insertDepth(xx);
					//
					// if (insertionSpot >= 0)
					// JFreeUtil.addYearMarker(DatingProjectView.this.project.getChart(),
					// xx, insertionSpot,
					// true);

					dirty.setDirty(true);

					// } catch (IllegalAccessException e) {
					// logger.error(e.getMessage(), e);
					// }

					// updateLines();

				} else if (MouseEvent.BUTTON3 == event.getTrigger()
						.getButton()) { // Right
										// click
					logger.debug("Right clicked");

					List<MPart> projectPartsList = modelService.findElements(
							application, "icd2.partdescriptor.project",
							MPart.class, null);

					PopupUtil.showMenu(projectPartsList.get(0),
							"edu.umaine.cs.icd2.popupmenu.chart", chartModel,
							eventBroker, undoContext);

				}

				// TODO Find out why I need this really stupid hack to make
				// the cross hairs be in the right place.
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						DatingProjectView.this.chartMouseMoved(event);
					}
				});

			}

			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				// Do nothing
			}

		});

		PanningMouseListener mouser = new PanningMouseListener(topCp);

		topCp.addMouseMotionListener(mouser);
		topCp.addMouseListener(mouser);

		frame.add(topCp);

		bottomChartComposite = new Composite(parent,
				SWT.EMBEDDED | SWT.BORDER | SWT.NO_BACKGROUND);
		bottomChartComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		bottomChart = createBottomChart(chartModel);

		final Frame bottomFrame = SWT_AWT.new_Frame(bottomChartComposite);

		bottomCp = new ChartPanel(bottomChart);
		bottomCp.addMouseWheelListener(new MouseZoomListener(bottomCp));
		bottomCp.setPopupMenu(null);
		// Setting the heights makes it so it doesn't stretch text out
		bottomCp.setMinimumDrawWidth(0);
		bottomCp.setMinimumDrawHeight(0);
		bottomCp.setMaximumDrawWidth(1920);
		bottomCp.setMaximumDrawHeight(1080);

		PanningMouseListener mouser2 = new PanningMouseListener(bottomCp);

		bottomCp.addMouseMotionListener(mouser2);
		bottomCp.addMouseListener(mouser2);

		bottomFrame.add(bottomCp);

		CombinedDomainXYPlot tp = (CombinedDomainXYPlot) topJFreeChart
				.getPlot();
		tp.setDomainPannable(true);

		updateLines();

	}

	public void chartMouseMoved(ChartMouseEvent event) {
		int mouseX = event.getTrigger().getX();
		int mouseY = event.getTrigger().getY();
		Point mousePoint = new Point(mouseX, mouseY);

		// convert the Java2D coordinate to axis coordinates...
		CombinedDomainXYPlot cdPlot = (CombinedDomainXYPlot) this.topCp
				.getChart().getPlot();
		ChartRenderingInfo chartInfo = this.topCp.getChartRenderingInfo();
		Point2D java2DPoint = this.topCp.translateScreenToJava2D(mousePoint);
		PlotRenderingInfo plotInfo = chartInfo.getPlotInfo();

		// see if the point is in one of the subplots; this is the
		// intersection of the range and domain crosshairs
		int subplotIndex = plotInfo.getSubplotIndex(java2DPoint);

		@SuppressWarnings("unchecked")
		List<XYPlot> subplotsList = (List<XYPlot>) cdPlot.getSubplots();

		// Over a plot draw crosshairs
		if (subplotIndex >= 0) {
			// all subplots have the domain crosshair
			// the x coordinate is the same for all subplots
			Rectangle2D dataArea = plotInfo.getDataArea();
			double xx = cdPlot.getDomainAxis().java2DToValue(java2DPoint.getX(),
					dataArea, cdPlot.getDomainAxisEdge());

			Rectangle2D panelArea = this.topCp.getScreenDataArea(mouseX,
					mouseY);

			for (int i = 0; i < subplotsList.size(); i++) {
				XYPlot subplot = subplotsList.get(i);

				// set domain crosshair for each plot
				subplot.setDomainCrosshairValue(xx, true);
				subplot.setDomainCrosshairVisible(true);

				if (subplotIndex == i) {
					// this subplot has the range crosshair
					// get the y axis positon
					double yy = subplot.getRangeAxis().java2DToValue(
							mousePoint.getY(), panelArea,
							subplot.getRangeAxisEdge());
					// make sure the range crosshair is on
					subplot.setRangeCrosshairVisible(true);

					// and plot it
					subplot.setRangeCrosshairValue(yy, true);

				} else {
					subplot.setRangeCrosshairVisible(false);
				}

			}
		} else {
			for (XYPlot p : subplotsList) {
				p.setRangeCrosshairVisible(false);
				p.setDomainCrosshairVisible(false);
			}
		}
	}

	private JFreeChart createBottomChart(Chart chartModel)
			throws ObjectNotFound {

		XYSeriesCollection dataset = new org.jfree.data.xy.XYSeriesCollection();
		// Create the X Axis and customize it
		NumberAxis numberAxis = new NumberAxis("Year (C.E.)");
		numberAxis.setInverted(true);
		numberAxis.setAutoRange(true);
		numberAxis.setAutoRangeIncludesZero(false);

		/*
		 * Our chart is an instance of CombinedDomainXYPlot, because it contains
		 * more than one chart (subplots) that share the same X axis but each
		 * has it's own Y axis
		 */
		IceCombinedDomainXYPlot combinedPlot = new IceCombinedDomainXYPlot(
				numberAxis);

		Plot combinedPlotModel = chartModel.getPlots()[0][0];

		List<PlotValues> samples = combinedPlotModel.getRangeValues();

		CoreData cd = chartModel.getParent().getParent().getCoreData();

		for (int i = 0; i < samples.size(); i++) {

			PlotValues pv = samples.get(i);
			Sample sample = cd.lookupSample(samples.get(i).getValuesKey());

			// create a new XYSeries for each element selected
			XYSeries series = new XYSeries(pv.getName());

			// elementNumber++;
			// create a new XYSeriesCollection for the element series
			dataset = new XYSeriesCollection(series);
			// each element has its own range (Y) axis
			final NumberAxis rangeAxis = new NumberAxis(sample.getName());
			rangeAxis.setAutoRange(true);

			// if (i == 0) {
			// rangeAxis.addChangeListener(new AxisChangeListener() {
			//
			// @Override
			// public void axisChanged(AxisChangeEvent event) {
			// updateLines();
			// }
			// });
			// }

			XYPlot subplot = new XYPlot(dataset, combinedPlot.getDomainAxis(),
					rangeAxis, new XYLineAndShapeRenderer(true, false));
			subplot.setBackgroundPaint(null);

			// customization
			// subplot.setWeight(elementNumber);
			// subplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
			// now when you are done customizing the subPlot, add it to
			// your combined plot. Repeat
			// the process for each element (subPlot) you have
			combinedPlot.add(subplot, 1);

			NumberAxis na = (NumberAxis) subplot.getDomainAxis();
			NumberAxis ra = (NumberAxis) subplot.getRangeAxis();

			na.setAutoRange(true);
			ra.setAutoRange(true);

		}

		// customize the plot
		// combinedPlot.setFixedLegendItems(items);
		combinedPlot.setOrientation(PlotOrientation.VERTICAL);

		// create your chart from the one big combined plot
		final JFreeChart chart = new JFreeChart(
				chartModel.getTitle() + " Dated", JFreeChart.DEFAULT_TITLE_FONT,
				combinedPlot, true);

		// customize your chart
		chart.setBackgroundPaint(java.awt.Color.white);

		chart.getLegend().setBorder(0, 0, 0, 0);

		// further customization of the plot
		final XYPlot plot = chart.getXYPlot();
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

		updateLines();

		return chart;
	}

	private void updateLines() {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				final CombinedDomainXYPlot plot = ((CombinedDomainXYPlot) topCp
						.getChart().getPlot());

				@SuppressWarnings("unchecked")
				List<XYPlot> subPlots = plot.getSubplots();
				if (subPlots.size() < 1)
					return; // There is nothing to update.

				DateSession ds = project.getChart().getActiveDateSession();

				@SuppressWarnings("unchecked")
				List<XYPlot> bSubPlots = ((IceCombinedDomainXYPlot) bottomChart
						.getXYPlot()).getSubplots();
				for (int i = 0; i < subPlots.size(); i++) {

					XYPlot sp = subPlots.get(i);
					XYPlot bsp = bSubPlots.get(i);

					XYSeries s = (XYSeries) ((XYSeriesCollection) sp
							.getDataset()).getSeries().get(0);
					XYSeries bs = (XYSeries) ((XYSeriesCollection) bsp
							.getDataset()).getSeries().get(0);

					bs.clear();

					// Require at least 3 points for interpolation.
					if (ds.getSize() >= 3) {

						double[] dr = ds.getDepthArray();
						double[] yr = ds.getYearArray();
						SplineInterpolator si = new SplineInterpolator();
						PolynomialSplineFunction spf = si.interpolate(dr, yr);

						for (int j = 0; j < s.getItemCount() && spf
								.isValidPoint(s.getX(j).doubleValue()); j++) {
							bs.add(spf.value(s.getX(j).doubleValue()),
									s.getY(j), false);
						}
					}
					bs.fireSeriesChanged();
				}

				CombinedDomainXYPlot bp = ((CombinedDomainXYPlot) bottomChart
						.getPlot());

				@SuppressWarnings("unchecked")
				List<XYPlot> subplots = bp.getSubplots();

				// This is a stupid hack to fix the range axes
				for (XYPlot xyPlot : subplots) {
					xyPlot.getRangeAxis().setAutoRange(false);
					xyPlot.getRangeAxis().setAutoRange(true);
				}

			}
		});

	}

	@Focus
	public void setFocus() {
		chartComposite.setFocus();
	}

	@Persist
	public void save() {
		logger.info("Saving dating project named {}", project.getName());

		WorkspaceUtil.saveDatingProject(project);

		dirty.setDirty(false);
	}

	/**
	 * @param application
	 * @param modelService
	 * @param chart
	 *            Whatever chart had its title changed. This may or may not be
	 *            the chart associated with {@link DatingProjectView}.
	 */
	@Inject
	@Optional
	public void onChartTitleChange(MApplication application,
			EModelService modelService,
			@UIEventTopic(CoreModelConstants.ICD2_MODEL_CHART_TITLE_CHANGE) Chart chart) {
		topCp.getChart().setTitle(this.project.getChart().getTitle());
		bottomCp.getChart()
				.setTitle(this.project.getChart().getTitle() + " Dated");
	}

	@Inject
	@Optional
	public void onChange(MApplication application, EModelService modelService,
			@UIEventTopic(CoreModelConstants.ICD2_MODEL_CHART_CHANGE) Chart chart) {
		if (chart.equals(project.getChart()))
			dirty.setDirty(true);
	}

	@Inject
	@Optional
	public void onModelObjectNameChange(MApplication application,
			EModelService modelService,
			@UIEventTopic(CoreModelConstants.ICD2_MODEL_MODELOBJECT_CHANGE) ModelObject<?, ?> mo) {

		ModelObject<?, ?> cur = mo;

		while (cur != null && !(cur instanceof DatingProject)) {
			cur = cur.getParent();
		}

		if (cur != null && cur.equals(project))
			dirty.setDirty(true);
	}

	@Inject
	@Optional
	public void onDatingProjectChange(MApplication application,
			EModelService modelService,
			@UIEventTopic(CoreModelConstants.ICD2_MODEL_DATINGPROJECT_CHANGE) DatingProject project) {

		if (this.project.equals(project))
			dirty.setDirty(true);
	}

	@Inject
	@Optional
	public void onDepthMarkerRemove(MApplication application,
			EModelService modelService,
			@UIEventTopic(CoreModelConstants.ICD2_MODEL_DATESESSION_DEPTH_REMOVE) DepthYear marker) {

		logger.debug("Remove marker occurred {}. Refreshing charts.", marker);

		IceCombinedDomainXYPlot plot = (IceCombinedDomainXYPlot) topCp
				.getChart().getPlot();

		plot.removeYearMarker(marker);

		topCp.getChart().fireChartChanged();
		updateLines();

		dirty.setDirty(true);
	}

	@Inject
	@Optional
	public void onDepthMarkerAdd(MApplication application,
			EModelService modelService,
			@UIEventTopic(CoreModelConstants.ICD2_MODEL_DATESESSION_DEPTH_ADD) DepthYear marker) {

		logger.debug("Add marker occurred {}. Refreshing charts.", marker);

		IceCombinedDomainXYPlot topPlot = (IceCombinedDomainXYPlot) topCp
				.getChart().getPlot();

		DateSession ds = this.project.getChart().getActiveDateSession();

		int index = ds.getDepthIndex(marker.getDepth());

		logger.debug("Marker info {}", marker);

		JFreeUtil.addYearMarker(this.project.getChart(), topCp.getChart(),
				marker.getDepth(), index, true);

		updateLines();

		// topCp.getChart().fireChartChanged();

		dirty.setDirty(true);
	}

	@Inject
	@Optional
	public void onModelChartPlotCropChange(MApplication application,
			EModelService modelService,
			@UIEventTopic(CoreModelConstants.ICD2_MODEL_CHART_PLOT_CROPLINES_CHANGE) PlotValues pv) {

		if (pv.getParent().getParent().getParent() instanceof Chart) {
			if (this.project
					.equals(((Chart) pv.getParent().getParent().getParent())
							.getParent()))
				dirty.setDirty(true);
			else
				return;
		}

		Plot plot = pv.getParent();
		Plot superPlot = (Plot) plot.getParent();

		@SuppressWarnings("unchecked")
		List<XYPlot> plots = (List<XYPlot>) ((CombinedDomainXYPlot) topCp
				.getChart().getPlot()).getSubplots();

		int index = 0;
		for (int i = 0; i < superPlot.getSubplots().length; i++) {
			for (int j = 0; j < superPlot.getSubplots()[i].length; j++) {
				for (PlotValues p : superPlot.getSubplots()[i][j]
						.getRangeValues()) {
					if (p.equals(pv)) {
						index = i * superPlot.getSubplots()[i].length + j;
						break;
					}
				}
			}
		}

		List<IceDatingRenderer> renderers = new ArrayList<>();
		for (XYPlot p : plots) {
			for (int i = 0; i < p.getRendererCount(); i++) {
				renderers.add((IceDatingRenderer) p.getRenderer(i));
			}
		}
		renderers.get(index).setClipTop(pv.isCropValues());
		renderers.get(index).setClipBottom(pv.isCropValues());
		topCp.getChart().fireChartChanged();
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		// Do nothing
	}

}