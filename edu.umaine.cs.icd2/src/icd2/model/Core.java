package icd2.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Core implements ModelObject<Core, ModelObject<?, ?>> {

	/**
	 * Some identifier. By default this will be the name of the file excluding
	 * the extension.
	 */
	private String name;

	/**
	 * When the core was drilled.
	 */
	private float topDate;

	/**
	 * A sample is a column of data.
	 */
	private List<Sample> samples;

	/**
	 * The file that the core data came from.
	 */
	private File file;

	private ModelObject<?, ?> parent;

	public Core(File file, String name, float topDate, List<Sample> samples) {
		this.file = file;
		this.name = name;
		this.topDate = topDate;
		this.samples = samples;
		for (Sample sample : samples) {
			sample.setParent(this);
		}
	}

	@Override
	public void setParent(ModelObject<?, ?> parent) {
		this.parent = parent;
	}

	@Override
	public ModelObject<?, ?> getParent() {
		return parent;
	}

	@Override
	public ModelObject<?, ?>[] children() {
		return samples.toArray(new Sample[samples.size()]);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getTopDate() {
		return topDate;
	}

	public void setTopDate(float topDate) {
		this.topDate = topDate;
	}

	public List<Sample> getSamples() {
		return Collections.unmodifiableList(samples);
	}

	public File getFile() {
		return file;
	}

	/**
	 * @return The labels for each sample.
	 */
	public List<String> getHeaders() {

//		String[] result = new String[samples.size()];
		
		List<String> result = new ArrayList<>();
		for (int i = 0; i < samples.size(); i++) {
//			result[i] = samples.get(i).getName();
			result.add(samples.get(i).getName());
		}

		return result;
	}

}
