/**
 * 
 */
package icd2.widgets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import icd2.FileFormatException;
import icd2.model.DateSession.PlotMethod;
import icd2.model.DatingProject;
import icd2.model.ModelObject;
import icd2.model.Sample;
import icd2.model.Workspace;

/**
 * @author Mark Royer
 *
 */
public class AddNewCoreWizardPage1 extends WizardPage {

	private Text coreNameText;

	private Spinner topDateSpinner;

	private List<Sample> allSamples;

	// BEGIN items that can be enabled ****************************************
	private Label tableLabel;

	private Table coreTable;

	private Label projectNameLabel;

	private Text projectNameText;

	private Button newProjectButton;

	private boolean isNewProject;

	private Label plotTypeLabel;

	private ComboViewer plottingPostionCombo;

	private List<Binding> enableValidateBindings;

	// END items that can be enabled ******************************************

	private List<Sample> selectedSamples;

	private List<Sample> topAndBottom;

	private DataBindingContext dctx;

	class CoreData {

		private String coreName;

		private int topYear;

		private String projectName;

		private PlotMethod plottingPosition;

		public int getTopYear() {
			return topYear;
		}

		public void setTopYear(int topYear) {
			this.topYear = topYear;
		}

		public String getCoreName() {
			return coreName;
		}

		public void setCoreName(String coreName) {
			this.coreName = coreName;
		}

		public String getProjectName() {
			return projectName;
		}

		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}

		public PlotMethod getPlottingPosition() {
			return plottingPosition;
		}

		public void setPlottingPosition(PlotMethod plottingPosition) {
			this.plottingPosition = plottingPosition;
		}

	};

	private CoreData coreData;
	
	private IEclipseContext wctx;

	public AddNewCoreWizardPage1(String initCoreName, List<Sample> allSamples, IEclipseContext wctx)
			throws FileFormatException {
		super("page1");
		this.wctx = wctx;
		coreData = new CoreData();
		coreData.coreName = initCoreName;
		this.allSamples = allSamples;
		setTitle("Add New Core");
		setDescription("Please ensure that the following details are correct.");
		isNewProject = false;
		selectedSamples = new ArrayList<Sample>();
		topAndBottom = getTopAndBottom(allSamples);
		enableValidateBindings = new ArrayList<>();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.
	 * widgets .Composite)
	 */
	@Override
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));

		Label coreNameLabel = new Label(container, SWT.NONE);
		coreNameLabel.setText("Core Name:");

		coreNameText = new Text(container, SWT.SINGLE);
		coreNameText
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label topDateLabel = new Label(container, SWT.NONE);
		topDateLabel.setText("Top Date:");

		topDateSpinner = new Spinner(container, SWT.NONE);
		topDateSpinner
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		newProjectButton = new Button(container, SWT.CHECK);
		newProjectButton.setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		newProjectButton
				.setText("Create a new project with selected core data?");
		newProjectButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				isNewProject = !isNewProject;
				tableLabel.setEnabled(isNewProject);
				coreTable.setEnabled(isNewProject);
				projectNameLabel.setEnabled(isNewProject);
				projectNameText.setEnabled(isNewProject);
				plotTypeLabel.setEnabled(isNewProject);
				plottingPostionCombo.getCombo().setEnabled(isNewProject);

				// If the new project is enabled don't worry about validating
				// it.
				for (Binding b : enableValidateBindings) {
					if (isNewProject) {
						dctx.addBinding(b);
					} else {
						dctx.removeBinding(b);
					}
				}

			}

		});

		tableLabel = new Label(container, SWT.NONE);
		tableLabel.setText("Select values to plot:");
		tableLabel.setEnabled(isNewProject);

		coreTable = new Table(container,
				SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		coreTable.setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		for (Sample s : allSamples) {
			// Top and bottom have already been identified
			if (!s.getName().matches("(?i)(.*top.*)|(.*bot.*)")) {
				TableItem item = new TableItem(coreTable, SWT.NONE);
				item.setText(s.getName());
				item.setData(s);
			}
		}
		coreTable.setEnabled(isNewProject);

		coreTable.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.detail == SWT.CHECK) {
					TableItem ti = (TableItem) event.item;
					Sample samp = (Sample) ti.getData();
					if (selectedSamples.contains(samp))
						selectedSamples.remove(samp);
					else
						selectedSamples.add(samp);
				}

			}
		});

		projectNameLabel = new Label(container, SWT.NONE);
		projectNameLabel.setText("Project Name:");
		projectNameLabel.setEnabled(isNewProject);

		projectNameText = new Text(container, SWT.SINGLE);
		projectNameText
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		projectNameText.setEnabled(isNewProject);

		plotTypeLabel = new Label(container, SWT.NONE);
		plotTypeLabel.setText("Method of plotting:");
		plotTypeLabel.setEnabled(false);

		plottingPostionCombo = new ComboViewer(container, SWT.READ_ONLY);
		plottingPostionCombo
				.setContentProvider(ArrayContentProvider.getInstance());
		plottingPostionCombo.setInput(PlotMethod.values());
		plottingPostionCombo.getCombo().setEnabled(false);

		setControl(container);

		bindValues(); // TODO this needs to happen before setting defaults

		// Default values
		coreNameText.setText(coreData.getCoreName());
		int curYear = Calendar.getInstance().get(Calendar.YEAR);
		topDateSpinner.setValues(curYear, 1900, curYear, 0, 1, 10);
		projectNameText.setText(coreData.getCoreName());
		plottingPostionCombo
				.setSelection(new StructuredSelection(PlotMethod.MIDPOINT));
	}

	private List<Sample> getTopAndBottom(List<Sample> samples)
			throws FileFormatException {

		List<Sample> topAndBottom = new ArrayList<Sample>();

		for (Sample s : samples) {
			if (s.getName().matches("(?i)(.*top.*)|(.*bot.*)"))
				topAndBottom.add(s);
		}

		if (topAndBottom.size() != 2) {
			throw new FileFormatException(
					"Unclear what top and bottom are. Found %d matching types.  "
							+ "Clearly indicate top and bottom columns.",
					topAndBottom.size());
		}

		return topAndBottom;
	}

	private void bindValues() {
		// The DataBindingContext object will manage the data bindings.
		dctx = new DataBindingContext();

		Workspace w = wctx.get(Workspace.class);

		bindAndValidateCoreText(coreNameText, "coreName",
				w.getCoreData().children(),
				"A core with this name already exists.");

		// BEGIN DATA BINDING FOR coreNameText ********************************
		@SuppressWarnings("unchecked")
		IObservableValue<Object> topDateSpinnerValue = (IObservableValue<Object>) WidgetProperties
				.selection().observe(topDateSpinner);
		@SuppressWarnings("unchecked")
		IObservableValue<Object> modelTopDateValue = (IObservableValue<Object>) PojoProperties
				.value("topYear", CoreData.class).observe(coreData);

		dctx.bindValue(topDateSpinnerValue, modelTopDateValue);
		// END DATA BINDING FOR coreNameText **********************************

		List<DatingProject> projs = w.getProjects();
		Binding projectNameBinding = bindAndValidateCoreText(projectNameText,
				"projectName",
				projs.toArray(new ModelObject<?, ?>[projs.size()]),
				"A project with this name already exists.");
		dctx.removeBinding(projectNameBinding); // Will be added when enabled.
		enableValidateBindings.add(projectNameBinding);

		// BEGIN DATA BINDING FOR plottingCombo *******************************
		@SuppressWarnings("unchecked")
		IObservableValue<Object> plottingComboValue = (IObservableValue<Object>) ViewerProperties
				.singleSelection().observe(plottingPostionCombo);
		@SuppressWarnings("unchecked")
		IObservableValue<Object> modelPlottingPositionValue = (IObservableValue<Object>) PojoProperties
				.value("plottingPosition", CoreData.class).observe(coreData);

		dctx.bindValue(plottingComboValue, modelPlottingPositionValue);
		// END DATA BINDING FOR plottingCombo *********************************

		WizardPageSupport.create(this, dctx);

	}

	private Binding bindAndValidateCoreText(Text coreNameText, String property,
			final ModelObject<?, ?>[] namedObjects, final String errorMessage) {

		@SuppressWarnings("unchecked")
		IObservableValue<Object> coreNameTextValue = (IObservableValue<Object>) WidgetProperties
				.text(SWT.Modify).observe(coreNameText);

		IValidator validator = new IValidator() {
			@Override
			public IStatus validate(Object value) {
				for (ModelObject<?, ?> c : namedObjects) {
					if (c.getName().equals(value)) {
						return ValidationStatus.error(errorMessage);
					}
				}
				return ValidationStatus.ok();
			}
		};

		UpdateValueStrategy strategy = new UpdateValueStrategy();
		strategy.setBeforeSetValidator(validator);

		@SuppressWarnings("unchecked")
		IObservableValue<Object> modelCoreNameValue = (IObservableValue<Object>) PojoProperties
				.value(property, CoreData.class).observe(coreData);

		Binding bindValue = dctx.bindValue(coreNameTextValue,
				modelCoreNameValue, strategy, null);

		// add some decorations
		ControlDecorationSupport.create(bindValue, SWT.TOP | SWT.LEFT);

		return bindValue;
	}

	public boolean isCreateNewProject() {
		return isNewProject;
	}

	public String getProjectName() {
		return coreData.getProjectName();
	}

	public List<Sample> getSamplesToPlot() {
		return selectedSamples;
	}

	public PlotMethod getPlotMethod() {
		return coreData.getPlottingPosition();
	}

	public List<Sample> getTopAndBottom() {
		return topAndBottom;
	}

	public int getTopYear() {
		return coreData.getTopYear();
	}

	/**
	 * @return
	 */
	public String getCoreName() {
		return coreData.getCoreName();
	}

	@Override
	public void dispose() {
		dctx.dispose();
		super.dispose();
	}

}
