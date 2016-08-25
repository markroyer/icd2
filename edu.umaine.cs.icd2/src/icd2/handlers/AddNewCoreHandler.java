package icd2.handlers;

import static icd2.model.DateSession.PlotMethod.MIDPOINT;
import static icd2.util.PreferenceKeys.LAST_DIR_PATH;
import icd2.FileFormatException;
import icd2.model.Core;
import icd2.model.CoreModelConstants;
import icd2.model.ModelKey;
import icd2.model.Sample;
import icd2.model.Workspace;
import icd2.model.DateSession.PlotMethod;
import icd2.util.CSVFileReader;
import icd2.util.DataFileException;
import icd2.util.Validate;
import icd2.widgets.AddNewCoreWizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.ValidationException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mark Royer
 *
 */
public class AddNewCoreHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(AddNewCoreHandler.class);

	@Inject
	@Preference(value = LAST_DIR_PATH)
	String lastPath;

	@Execute
	public void execute(Shell shell, IEventBroker eventBroker,
			@Preference IEclipsePreferences prefs, Workspace workspace) {

		boolean done = false;
		while (!done) {

			try {
				attemptToAddNewCore(shell, eventBroker, prefs, workspace);
				done = true;
			} catch (FileFormatException e) {
				MessageDialog dialog = new MessageDialog(shell, "File Error",
						null, e.getMessage(), MessageDialog.ERROR,
						new String[] { "Cancel", "Try again" }, 1);
				int result = dialog.open();
				if (result == 0) // Cancel selected
					done = true;
			}

		}
	}

	private void attemptToAddNewCore(Shell shell, IEventBroker eventBroker,
			IEclipsePreferences prefs, Workspace workspace)
			throws FileFormatException {
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);

		if (lastPath != null)
			dialog.setFilterPath(lastPath);

		String path = dialog.open();

		prefs.put(LAST_DIR_PATH, dialog.getFilterPath());

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			logger.error(e.getMessage(), e);
		}

		if (path != null) {
			try {
				File file = new File(path);
				List<Sample> samples = CSVFileReader.read(file);

				// Remove the file extension
				String coreName = file.getName();
				if (coreName.contains("."))
					coreName = coreName.substring(0, coreName.lastIndexOf('.'));

				AddNewCoreWizard wiz = new AddNewCoreWizard(coreName, samples);
				WizardDialog wDialog = new WizardDialog(shell, wiz);

				if (wDialog.open() == WizardDialog.OK) {

					Core newCore = new Core(workspace.getCoreData()
							.createCoreFile(wiz.getCoreName()),
							wiz.getCoreName(), wiz.getTopYear(), samples);

					Validate.validate(newCore);

					for (Sample s : samples) {
						s.setParent(newCore);
					}

					eventBroker.send(CoreModelConstants.ON_ADD_NEW_CORE,
							newCore);

					if (wiz.isCreateNewProject()) {

						eventBroker.post(
								CoreModelConstants.CREATE_NEW_DATING_PROJECT,
								new NewProjectObject(wiz.getProjectName(),
										toKeys(wiz.getSamplesToPlot()),
										toKeys(wiz.getTopAndBottom()), wiz
												.getPlotMethod(), wiz
												.getTopYear()));
					}

				}
			} catch (DataFileException | ValidationException e) {
				throw new FileFormatException(e.getMessage(), (Object[]) null);
			}
		}
	}

	List<ModelKey<String>> toKeys(List<Sample> samples) {

		List<ModelKey<String>> sampleKeys = new ArrayList<>();

		for (Sample s : samples) {
			sampleKeys.add(new ModelKey<String>(s.getParent().getName() + "/"
					+ s.getName()));
		}

		return sampleKeys;
	}

	public static class NewProjectObject {

		String projectName;

		List<ModelKey<String>> samples;

		List<ModelKey<String>> topAndBottom;

		PlotMethod plotMethod;

		int topYear;

		public NewProjectObject(String projectName) {
			this(projectName, new ArrayList<ModelKey<String>>(),
					new ArrayList<ModelKey<String>>(), MIDPOINT, Calendar
							.getInstance().get(Calendar.YEAR));
		}

		public NewProjectObject(String projectName,
				List<ModelKey<String>> samples,
				List<ModelKey<String>> topAndBottom, PlotMethod plotMethod,
				int topYear) {
			super();
			this.projectName = projectName;
			this.samples = samples;
			this.topAndBottom = topAndBottom;
			this.plotMethod = plotMethod;
			this.topYear = topYear;
		}

		public String getProjectName() {
			return projectName;
		}

		public List<ModelKey<String>> getSamples() {
			return samples;
		}

		public PlotMethod getPlotMethod() {
			return plotMethod;
		}

		public List<ModelKey<String>> getTopAndBottom() {
			return topAndBottom;
		}

		public int getTopYear() {
			return topYear;
		}
	}
}
