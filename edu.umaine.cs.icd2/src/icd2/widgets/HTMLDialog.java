/**
 * 
 */
package icd2.widgets;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Mark Royer
 *
 */
public class HTMLDialog extends Dialog {

	private String html;

	private String title;

	public HTMLDialog(Shell parent, String title, String html) {
		super(parent);
		this.title = title == null ? "" : title;
		this.html = html;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 400;
		data.heightHint = 400;
		composite.setLayoutData(data);

		Browser browser = new Browser(composite, SWT.NONE);
		browser.setText(html);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, Dialog.OK, "OK", true);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	@Override
	public void okPressed() {
		close();
	}

}
