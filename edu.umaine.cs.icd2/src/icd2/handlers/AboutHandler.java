/*******************************************************************************
 * Copyright (c) 2010 - 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <lars.Vogel@gmail.com> - Bug 419770
 *******************************************************************************/
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
