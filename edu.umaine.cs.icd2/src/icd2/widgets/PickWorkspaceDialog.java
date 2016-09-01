package icd2.widgets;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.util.Pair;
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

			// combo in middle
			workspaceCombo = new ComboViewer(inner,
					SWT.READ_ONLY);
			// workspaceCombo = new Combo(inner, SWT.BORDER);
			// workspaceCombo.setLayoutData(new
			// GridData(GridData.GRAB_HORIZONTAL));

			// for (Pair<String,String> last : wsAccessTimes)
			// workspaceCombo.add(last.getFirst() + " (" + last.getSecond() +
			// ")");
			// workspaceCombo.select(0);

			workspaceCombo
					.setContentProvider(ArrayContentProvider.getInstance());
			workspaceCombo.setInput(wsAccessTimes);

			workspaceCombo.setLabelProvider(new LabelProvider() {
				SimpleDateFormat df = new SimpleDateFormat();
				@Override
				public String getText(Object element) {
					Pair<String,String> p = (Pair)element;
					String date = p.getSecond();
					try {
						date = df.parse(date).toString();
					} catch (ParseException e) {
						// Unable to format
					}
					
					return p.getFirst() + " (" + date + ")";
					
					
				}
			});
			
			workspaceCombo.setSelection(new StructuredSelection(wsAccessTimes.get(0)), true);

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
					if (pick != null) {
//						workspaceCombo.setText(pick);
						Optional<Pair<String, String>> match = wsAccessTimes.stream().filter(a-> a.getFirst().equals(pick)).findAny();
						ISelection selection;
						if (match.isPresent())
							selection = new StructuredSelection(match.get());
						else {
							Pair<String, String> newPair = new Pair<>(pick, "never used");
//							wsAccessTimes.add(0, newPair);
							workspaceCombo.insert(newPair,0);
							selection = new StructuredSelection(newPair);
						}
							
						workspaceCombo.setSelection(selection);
					}
				}
			});

			// checkbox below
			Button rememberWorkspaceButton = new Button(inner, SWT.CHECK);
			rememberWorkspaceButton.setLayoutData(
					new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
			rememberWorkspaceButton.setText("Remember workspace");
			rememberWorkspaceButton.setToolTipText(
					"Selecting this option will make it so that you are not prompted for the workspace location again");

			inner.pack();

			return inner;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	protected void okPressed() {

		ISelection iSelect = workspaceCombo.getSelection();

		if (iSelect != null) {
		
			selectedPath = ((Pair<String,String>)((StructuredSelection)iSelect).getFirstElement()).getFirst();
			
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