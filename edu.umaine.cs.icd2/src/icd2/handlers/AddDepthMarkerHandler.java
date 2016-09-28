
package icd2.handlers;

import javax.inject.Named;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.Chart;
import icd2.model.CoreModelConstants;
import icd2.model.DateSession;
import icd2.model.DateSession.DateSessionException;
import icd2.model.DepthYear;

public class AddDepthMarkerHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(AddDepthMarkerHandler.class);

	@Execute
	public void execute(Shell shell, IEventBroker eventBroker, IUndoContext ctx,
			@Named(CoreModelConstants.TREE_ITEM_SELECTION) @Optional DateSession session) {
		logger.info("Adding a depth marker to {}.", session.getName());

		Chart chart = session.getParent().getChart();

		DescriptiveStatistics ds = new DescriptiveStatistics(
				session.getDepthArray());

		InputDialog dlg = new InputDialog(shell, "Year depth?",
				"Please enter the depth of the new marker.",
				String.format("%.2f",ds.getMean()), new IInputValidator() {
					@Override
					public String isValid(String value) {
						try {
							double dVal = Double.parseDouble(value);
							if (dVal < 0) {
								return "Depth value must be positive.";
							}
							return null;
						} catch (NumberFormatException e) {
							logger.debug("Invalid depth. Message is '{}'",
									e.getMessage());
							return String.format(
									"'%s' is not a valid number.  Please enter a positive depth.",
									value);
						}
					}
				});
		if (dlg.open() == Window.OK) {

			double depth = Double.parseDouble(dlg.getValue());

			int year = session.getYear(session.getDepthIndex(depth));

			addDepthMarker(eventBroker, ctx, chart, depth, year, true);

		}

	}

	public static void addDepthMarker(IEventBroker eventBroker,
			IUndoContext ctx, Chart chartModel, double depth, int year,
			boolean notify) {

		DateSession ds = chartModel.getActiveDateSession();
		DepthYear dy = new DepthYear(ds, depth, year);

		AbstractOperation ao = new AbstractOperation(
				"Add Depth Marker - " + dy) {

			private DepthYear dy;

			@Override
			public IStatus undo(IProgressMonitor monitor, IAdaptable info)
					throws ExecutionException {

				try {

					ds.removeYearDepth(year);
					logger.debug("Depth year removed {}.", dy);

					if (notify) {
						eventBroker.send(
								CoreModelConstants.ICD2_MODEL_DATESESSION_DEPTH_REMOVE,
								dy);
					}

				} catch (DateSessionException e) {
					logger.debug("Unable to remove date session top year.", dy);
				}

				return Status.OK_STATUS;
			}

			@Override
			public IStatus redo(IProgressMonitor monitor, IAdaptable info)
					throws ExecutionException {

				try {

					int index = ds.insertDepth(depth);

					if (index >= 0) {

						dy = ds.getDepthYear(index);

						logger.debug("Depth year added {} at index {}.", dy,
								index);
						logger.debug("Current dates are {}, {}",
								ds.getDepthArray(), ds.getYearArray());

						if (notify) {
							eventBroker.send(
									CoreModelConstants.ICD2_MODEL_DATESESSION_DEPTH_ADD,
									dy);
						}

					}
				} catch (DateSessionException e) {
					logger.error("Invalid date depth {}.", depth, e);
				}

				return Status.OK_STATUS;
			}

			@Override
			public IStatus execute(IProgressMonitor monitor, IAdaptable info)
					throws ExecutionException {
				return redo(monitor, info);
			}

		};

		ao.addContext(ctx);

		IOperationHistory operationHistory = OperationHistoryFactory
				.getOperationHistory();

		try {
			operationHistory.execute(ao, null, null);
		} catch (ExecutionException e) {
			logger.error("Failed to execute add depth marker.", e);
		}
	}
}