
package icd2.handlers;

import java.util.List;

import javax.inject.Named;

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
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.Chart;
import icd2.model.CoreModelConstants;
import icd2.model.DateSession;
import icd2.model.DateSession.DateSessionException;
import icd2.model.DepthYear;
import icd2.model.Workspace;

public class RemoveDepthMarkerHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(RemoveDepthMarkerHandler.class);

	@Execute
	public void execute(Shell shell, IEventBroker eventBroker,
			Workspace workspace, IUndoContext ctx,
			@Named(CoreModelConstants.TREE_ITEMS_SELECTED) @Optional List<DepthYear> markers) {

		markers.forEach(m -> removeDepthMarker(eventBroker, ctx,
				m.getParent().getParent().getChart(), m, true));

	}

	public static void removeDepthMarker(IEventBroker eventBroker,
			IUndoContext ctx, Chart chartModel, final DepthYear marker,
			boolean notify) {

		DateSession ds = chartModel.getActiveDateSession();

		AbstractOperation ao = new AbstractOperation(
				"Remove Depth Marker - " + marker) {

			private DepthYear dy = marker;

			@Override
			public IStatus undo(IProgressMonitor monitor, IAdaptable info)
					throws ExecutionException {

				try {

					int index = ds.insertDepth(dy.getDepth());

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
					logger.error("Invalid date depth {}.", dy.getDepth(), e);
				}

				return Status.OK_STATUS;
			}

			@Override
			public IStatus redo(IProgressMonitor monitor, IAdaptable info)
					throws ExecutionException {

				try {
					ds.removeYearDepth(dy.getYear());
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
			logger.error("Failed to execute remove depth marker.", e);
		}
	}

}