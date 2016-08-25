/**
 * 
 */
package icd2.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Mark Royer
 *
 */
public class ErrorDialogUtil {

	public static void showErrorDialog(final Shell shell, Throwable t) {

		// build the error message and include the current stack trace
		MultiStatus status = createMultiStatus(t.getLocalizedMessage(), t);
		// show error dialog
		ErrorDialog.openError(shell, "Error", "This is an error", status);
	}

	private static MultiStatus createMultiStatus(String msg, Throwable t) {

		List<Status> childStatuses = new ArrayList<Status>();
		StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

		for (StackTraceElement stackTrace : stackTraces) {
			Status status = new Status(IStatus.ERROR, "com.example.e4.rcp.todo", stackTrace.toString());
			childStatuses.add(status);
		}

		MultiStatus ms = new MultiStatus("com.example.e4.rcp.todo", IStatus.ERROR,
				childStatuses.toArray(new Status[] {}), t.toString(), t);
		return ms;
	}

}
