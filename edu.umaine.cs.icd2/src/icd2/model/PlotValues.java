/**
 * 
 */
package icd2.model;

import java.awt.Paint;

/**
 * @author Mark Royer
 *
 */
public class PlotValues implements ModelObject<PlotValues, Plot>{

	private Plot parent;
	
	private String name;
	
	private ModelKey<String> valuesKey;
	
	private Paint color;
	
	/**
	 * Crop the rendered values to the range axis? Default is true.
	 */
	private boolean cropValues; 
	
	public PlotValues(String name, ModelKey<String> valuesKey, Paint color) {
		this.name = name;
		this.valuesKey = valuesKey;
		this.color = color;
		this.cropValues = true;
	}
	
	@Override
	public void setParent(Plot parent) {
		this.parent = parent;
	}

	@Override
	public Plot getParent() {
		return parent;
	}

	@Override
	public ModelObject<?, ?>[] children() {
		return ModelObject.EMPTYARRAY;
	}

	@Override
	public String getName() {
		return name;
	}

	public ModelKey<String> getValuesKey() {
		return valuesKey;
	}

	public Paint getColor() {
		return color;
	}

	public boolean isCropValues() {
		return cropValues;
	}

	public void setCropValues(boolean cropValues) {
		this.cropValues = cropValues;
	}

}
