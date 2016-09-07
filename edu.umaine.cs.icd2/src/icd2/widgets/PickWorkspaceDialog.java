package icd2.widgets;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.PreferenceUtils.Pair;
import icd2.Setup;
import icd2.model.Workspace;

/**
 * @author Mark Royer
 */
public class PickWorkspaceDialog extends TitleAreaDialog {

	private static final Logger logger = LoggerFactory
			.getLogger(PickWorkspaceDialog.class);

	private List<Pair<String, String>> wsAccessTimes;

	public PickWorkspaceDialog(Shell parentShell,
			List<Pair<String, String>> wsAccessTimes) {
		super(parentShell);
		this.wsAccessTimes = wsAccessTimes;
	}

	ComboViewer workspaceCombo;

	String selectedPath;

	Workspace selection;

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Select a workspace");
		setMessage(
				"Select a workspace to store related dating files.\nThis is simply a folder that will contain projects and core data.");

		try {
			Composite inner = new Composite(parent, SWT.NONE);
			inner.setLayoutData(new GridData(GridData.FILL_BOTH));
			inner.setLayout(new GridLayout(3, false));

			// label on left
			CLabel label = new CLabel(inner, SWT.NONE);
			label.setLayoutData(
					new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			label.setText("Workspace Root Path");

			workspaceCombo = new ComboViewer(inner, SWT.READ_ONLY);

			workspaceCombo
					.setContentProvider(ArrayContentProvider.getInstance());
			workspaceCombo.setInput(wsAccessTimes);

			workspaceCombo.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					@SuppressWarnings("unchecked")
					Pair<String, String> p = (Pair<String, String>) element;
					String date = p.getValue();
					try {
						date = new Date(Long.parseLong(date)).toString();
					} catch (NumberFormatException e) {
						logger.debug(
								"'{}' is not a valid long, so it will not be converted to a date.",
								date);
					}

					return p.getKey() + " (" + date + ")";

				}
			});

			workspaceCombo.setSelection(
					new StructuredSelection(wsAccessTimes.get(0)), true);

			Button browse = new Button(inner, SWT.PUSH);
			browse.setSize(81, 28);
			browse.setText("Browse...");

			// Insert new selection into the combo
			browse.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					DirectoryDialog dd = new DirectoryDialog(getParentShell());
					dd.setText("Select Workspace");
					dd.setMessage("Select a folder for the workspace.");
					String pick = dd.open();
					if (pick != null) {
						Optional<Pair<String, String>> match = wsAccessTimes
								.stream().filter(a -> a.getKey().equals(pick))
								.findAny();
						ISelection selection;
						if (match.isPresent())
							selection = new StructuredSelection(match.get());
						else {
							Pair<String, String> newPair = new Pair<>(pick,
									"never used");
							workspaceCombo.insert(newPair, 0);
							selection = new StructuredSelection(newPair);
						}

						workspaceCombo.setSelection(selection);
					}
				}
			});
			
			// For now we'll just ignore this option
			// Button rememberWorkspaceButton = new Button(inner, SWT.CHECK);
			// rememberWorkspaceButton.setLayoutData(
			// new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
			// rememberWorkspaceButton.setText("Remember workspace");
			// rememberWorkspaceButton.setToolTipText(
			// "Selecting this option will make it so that you are not prompted for the workspace location again");

			inner.pack();

			return inner;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {

		ISelection iSelect = workspaceCombo.getSelection();

		if (iSelect != null) {

			selectedPath = ((Pair<String, String>) ((StructuredSelection) iSelect)
					.getFirstElement()).getKey();

			if (isValid(selectedPath)) {
				selection = new Setup().getWorkspace(new File(selectedPath));
			}
		}

		super.okPressed();
	}

	private boolean isValid(String selection) {
		return true; // TODO need to actually validate.
	}

	public Workspace getSelectedWorkspace() {
		return selection;
	}

	public boolean isValid() {
		return selection != null;
	}

}
