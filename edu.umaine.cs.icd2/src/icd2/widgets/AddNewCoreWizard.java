/**
 * 
 */
package icd2.widgets;

import icd2.FileFormatException;
import icd2.model.Sample;
import icd2.model.DateSession.PlotMethod;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.jface.wizard.Wizard;

/**
 * @author Mark Royer
 *
 */
public class AddNewCoreWizard extends Wizard {

	private AddNewCoreWizardPage1 page1;

	@Inject
	public AddNewCoreWizard(String initCoreName, List<Sample> allSamples)
			throws FileFormatException {
		page1 = new AddNewCoreWizardPage1(initCoreName, allSamples);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void addPages() {
		addPage(page1);
	}

	public boolean isCreateNewProject() {
		return page1.isCreateNewProject();
	}

	public String getProjectName() {
		return page1.getProjectName();
	}

	public List<Sample> getSamplesToPlot() {
		return page1.getSamplesToPlot();
	}

	public PlotMethod getPlotMethod() {
		return page1.getPlotMethod();
	}

	public List<Sample> getTopAndBottom() {
		return page1.getTopAndBottom();
	}

	public int getTopYear() {
		return page1.getTopYear();
	}

	/**
	 * @return
	 */
	public String getCoreName() {
		return page1.getCoreName();
	}
}
