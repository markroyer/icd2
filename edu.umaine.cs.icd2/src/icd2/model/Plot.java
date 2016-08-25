/**
 * 
 */
package icd2.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.DateSession.PlotMethod;
import icd2.util.DataFileException;

/**
 * @author Mark Royer
 *
 */
public class Plot implements PlotParent<Plot, PlotParent<?, ?>> {

	private static final Logger logger = LoggerFactory.getLogger(Plot.class);

	private PlotParent<?, ?> parent;

	private String name;

	private Axis rangeAxis;

	private Axis domainAxis;

	private List<PlotValues> domainValues;

	private List<PlotValues> rangeValues;

	private Plot[][] subplots;

	@Editable(name = "Plot Method", description = "Top, Bottom, or Mid",
			onchange = CoreModelConstants.ICD2_MODEL_CHART_PLOTMETHOD_CHANGE)
	private PlotMethod plotMethod;

	public Plot(List<PlotValues> domainValues, Plot[][] subplots, PlotMethod plotMethod) {
		this.domainValues = domainValues;
		if (subplots != null) {
			this.subplots = subplots;
			for (Plot[] p1 : this.subplots) {
				for (Plot p : p1) {
					p.setParent(this);
				}
			}
			this.name = "Shared Domain Plot";
		} 
		this.plotMethod = plotMethod;
		this.domainAxis = new Axis(this, plotMethod.toString());
	}

	public Plot(List<PlotValues> rangeValues) {
		this.rangeValues = rangeValues;
		PlotValues firstPV = rangeValues.get(0);
		this.rangeAxis = new Axis(this, firstPV.getName());
		this.name = "Plot";
	}

	@Override
	public void setParent(PlotParent<?, ?> parent) {
		this.parent = parent;
	}

	@Override
	public PlotParent<?, ?> getParent() {
		return parent;
	}

	@Override
	public ModelObject<?, ?>[] children() {

		List<ModelObject<?, ?>> children = new ArrayList<>();
		if (rangeAxis != null) {
			children.add(rangeAxis);
		}
		if (domainAxis != null) {
			children.add(domainAxis);
		}
		if (subplots != null) {
			for (int i = 0; i < subplots.length; i++) {
				for (int j = 0; j < subplots[i].length; j++) {
					children.add(subplots[i][j]);
				}
			}
		}

		return children.toArray(new ModelObject<?, ?>[children.size()]);
	}

	public List<Double> getXData() throws ObjectNotFound {

		List<Double> result = new ArrayList<Double>(domainValues.size());

		Chart chart = getChart();

		if (domainValues.size() == 2) {

			CoreData cd = chart.getParent().getParent().getCoreData();

			try {

				List<Value> top = cd.lookupSample(domainValues.get(0).getValuesKey()).getValues();
				List<Value> bottom = cd.lookupSample(domainValues.get(1).getValuesKey()).getValues();

				switch (plotMethod) {
				case MIDPOINT:
					for (int i = 0; i < top.size(); i++) {
						result.add((top.get(i).doubleValue() + bottom.get(i).doubleValue()) / 2.);
					}
					break;
				case TOP:
					for (int i = 0; i < top.size(); i++) {
						result.add(top.get(i).doubleValue());
					}
					break;
				case BOTTOM:
					for (int i = 0; i < bottom.size(); i++) {
						result.add(bottom.get(i).doubleValue());
					}
					break;

				default:
					break;
				}

			} catch (DataFileException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return result;
	}

	private Chart getChart() throws ObjectNotFound {

		Chart chart = null;

		if (this.getParent() instanceof Chart) { // Combined plot
			chart = (Chart) this.getParent();
		} else { // Child of combined plot
			if (!(this.getParent().getParent() instanceof Chart)) {
				throw new ObjectNotFound("Plot is not a child of a plot or a chart?");
			}

			chart = (Chart) this.getParent().getParent();
		}
		return chart;
	}

//	public List<Sample> getYValues() throws ObjectNotFound {
//
//		Chart chart = getChart();
//
//		CoreData cd = chart.getParent().getParent().getCoreData();
//
//		List<Sample> result = new ArrayList<>();
//		
//		if (rangeValues == null) { // Combined plot get it from children
//			for (int i = 0; i < subplots.length; i++) {
//				for (int j = 0; j < subplots[i].length; j++) {
//					result.addAll(subplots[i][j].getYValues());
//				}
//			}
//		} else {
//			for (PlotValues p : rangeValues) {
//				result.add(cd.lookupSample(p.getValuesKey()));
//			}
//		}
//
//		return result;
//	}

	public PlotMethod getPlotMethod() {
		return plotMethod;
	}

	@Override
	public String getName() {
		return name;
	}

	public Axis getRangeAxis() {
		return rangeAxis;
	}

	public Axis getDomainAxis() {
		return domainAxis;
	}

	public List<PlotValues> getDomainValues() {
//		
//		List<ModelKey<String>> result = new ArrayList<>(domainValues.size());
//		
//		for (PlotValues v : domainValues) {
//			result.add(v.getValuesKey());
//		}
		return domainValues;
	}

	public List<PlotValues> getRangeValues() {
		
		List<PlotValues> result = new ArrayList<>();
		
		if (rangeValues == null) { // Combined plot get it from children
			for (int i = 0; i < subplots.length; i++) {
				for (int j = 0; j < subplots[i].length; j++) {
					result.addAll(subplots[i][j].getRangeValues());
				}
			}
		} else {
			result.addAll(rangeValues);
		}
		
		return result;
	}

	public Plot[][] getSubplots() {
		return subplots;
	}

}
