package icd2;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.Workspace;
import icd2.widgets.PickWorkspaceDialog;

@SuppressWarnings("restriction")
public class Activator implements BundleActivator {

	private static BundleContext context;

	private static final Logger logger = LoggerFactory
			.getLogger(Activator.class);

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {

		Activator.context = bundleContext;

		IEclipseContext ctx = E4Workbench.getServiceContext();

		Location instanceLoc = Platform.getInstanceLocation();
		// instanceLoc.set(new URL("file", null, System.getProperty("user.home")
		// + "/icdWorkspace"), false);

		Shell shell = Display.getDefault().getActiveShell();

		String ecs = System.getProperty("eclipse.commands");

		boolean found = false;
		for (String s : ecs.split("\\s+")) {
			if ("-testproperties".equals(s)) {
				found = true;
				break;
			}
		}

		PreferenceUtils pus = new PreferenceUtils();

		if (!found) {

			PickWorkspaceDialog pwd = new PickWorkspaceDialog(shell,
					pus.getWorkspacesAccessTimes());
			int pick = pwd.open();

			Workspace workspace = null;

			while (!pwd.isValid() && pick != Window.CANCEL) {
				pick = pwd.open();
			}

			if (pick == Window.CANCEL) {
				MessageDialog.openError(shell, "Error",
						"The application can not start without a workspace root and will now exit.");
				System.exit(0);
			} else {
				workspace = pwd.getSelectedWorkspace();
				instanceLoc.set(workspace.getFile().toURI().toURL(), false);

				// Remember the location for next time
				pus.updateWorkspaceAccessTime(workspace.getFile().getAbsolutePath(),
						String.valueOf(System.currentTimeMillis()));

			}

			ctx.set(Workspace.class, workspace);

		} else {
			logger.info(
					"testproperties flag found, so assume testing and don't allow workspace to be chosen...");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {

		// IEclipseContext ctx = E4Workbench.getServiceContext();
		// IEclipsePreferences prefs =
		// InstanceScope.INSTANCE.getNode("edu.umaine.cs.icd2");
		//
		// logger.info("Finishing open projects are {}",
		// ctx.get(PreferenceKeys.OPEN_PROJECTS));

		Activator.context = null;
	}

}
