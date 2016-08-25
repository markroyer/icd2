package icd2.widgets;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.Setup;
import icd2.model.Workspace;

/**
 * @author Mark Royer
 */
public class PickWorkspaceDialog extends TitleAreaDialog {

	private static final Logger logger = LoggerFactory.getLogger(PickWorkspaceDialog.class);

	public PickWorkspaceDialog(Shell parentShell) {
		super(parentShell);
	}

	Combo workspaceCombo;

	String selectedPath;

	Workspace selection;

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Select a workspace");
		setMessage("Select a workspace to store related dating files.\nThis is simply a folder that will contain projects and core data.");

		try {
			Composite inner = new Composite(parent, SWT.NONE);
			inner.setLayoutData(new GridData(GridData.FILL_BOTH));
			inner.setLayout(new GridLayout(3, false));

			// label on left
			CLabel label = new CLabel(inner, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			label.setText("Workspace Root Path");

			// combo in middle
			workspaceCombo = new Combo(inner, SWT.BORDER);
			workspaceCombo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));

			List<String> _lastUsedWorkspaces = new ArrayList<>(); // TODO come
																	// back to
																	// this
			_lastUsedWorkspaces.add(System.getProperty("user.home") + File.separator + "icdWorkspace");
			for (String last : _lastUsedWorkspaces)
				workspaceCombo.add(last);
			workspaceCombo.select(0);

			// browse button on right
			Button browse = new Button(inner, SWT.PUSH);
			browse.setSize(81, 28);
			browse.setText("Browse...");

			browse.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					DirectoryDialog dd = new DirectoryDialog(getParentShell());
					dd.setText("Select Workspace");
					dd.setMessage("Select a folder for the workspace.");
					String pick = dd.open();
					if (pick != null)
						workspaceCombo.setText(pick);
				}
			});

			// checkbox below
			Button _RememberWorkspaceButton = new Button(inner, SWT.CHECK);
			_RememberWorkspaceButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
			_RememberWorkspaceButton.setText("Remember workspace");

			inner.pack();

			return inner;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	protected void okPressed() {

		selectedPath = workspaceCombo.getText();

		if (isValid(selectedPath)) {
			selection = new Setup().getWorkspace(new File(selectedPath));
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
