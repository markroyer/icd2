package icd2.handlers;

import icd2.widgets.HTMLDialog;

import java.util.Scanner;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;

public class AboutHandler {
	@Execute
	public void execute(Shell shell) {

		Color gray = shell.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND);

		String hex = String.format("#%02x%02x%02x", gray.getRed(),
				gray.getGreen(), gray.getBlue());

		Scanner scanner = new Scanner(
				AboutHandler.class.getResourceAsStream("about.html"));
		String text = scanner.useDelimiter("\\Z").next();
		scanner.close();

		new HTMLDialog(shell, "About ICD", "<body bgcolor='" + hex + "'>"
				+ text + "</body>").open();
	}
}
