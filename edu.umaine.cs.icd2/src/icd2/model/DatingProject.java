/**
 * 
 */
package icd2.model;

import icd2.model.DateSession.PlotMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Royer
 *
 */
@EditableObject(name = "Dating Project", description = "A project contains a plot that dating sessions can be shown on.")
public class DatingProject implements ModelObject<DatingProject, Workspace> {

	private transient Workspace parent;

	@Editable(name = "Project Name", 
			description = "The name of the project.", 
			onchange = CoreModelConstants.ICD2_MODEL_MODELOBJECT_NAME_CHANGE,
			editType = EditType.CONFIRM,
			method = DatingProjectNameModifier.class,
			validator = DatingProjectNameValidator.class)
	private String name;

	private Chart chart;

	private List<DateSession> sessions;

	public DatingProject(Workspace parent, String name) {
		this(parent, name, null, null, null);
	}

	public DatingProject(Workspace parent, String name, List<ModelKey<String>> samples,
			List<ModelKey<String>> topAndBottom, PlotMethod plotMethod) {
		this.parent = parent;
		this.name = name;
		
		Plot[][] subplots = new Plot[1][samples.size()];
		
		for (int i=0; i < samples.size(); i++) {
			ModelKey<String> s = samples.get(i);
			List<PlotValues> sList = new ArrayList<PlotValues>();
			PlotValues pv = new PlotValues(s.getName(), s, Colors.getNextColor());
			sList.add(pv);
			Plot p = new Plot(sList);
			pv.setParent(p);
			subplots[0][i] = p;
			
		}
		
		List<PlotValues> topAndBot = new ArrayList<>(topAndBottom.size());
		for (ModelKey<String> modelKey : topAndBottom) {
			topAndBot.add(new PlotValues(modelKey.getName(), modelKey, null));
		}
		
		
		Plot plot = new Plot(topAndBot, subplots, plotMethod);
		
		chart = new Chart(this, name, plot);
		sessions = new ArrayList<DateSession>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icd2.model.TreeObject#setParent(icd2.model.TreeObject)
	 */
	@Override
	public void setParent(Workspace parent) {
		this.parent = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icd2.model.TreeObject#getParent()
	 */
	@Override
	public Workspace getParent() {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icd2.model.TreeObject#children()
	 */
	@Override
	public ModelObject<?, ?>[] children() {

		List<ModelObject<?, ?>> result = new ArrayList<ModelObject<?, ?>>(sessions.size() + 1);

		result.add(chart);
		result.addAll(sessions);

		return result.toArray(new ModelObject<?, ?>[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icd2.model.TreeObject#treeText()
	 */
	@Override
	public String getName() {
		return name;
	}

	public Chart getChart() {
		return chart;
	}

	public List<DateSession> getSessions() {
		return sessions;
	}

	public void addSession(DateSession dateSession) {
		sessions.add(dateSession);
		dateSession.setParent(this);
	}

}
