 
package icd2.handlers;

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
import org.eclipse.e4.core.services.events.IEventBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.CoreModelConstants;
import icd2.model.PlotValues;

public class CropPlottedLinesHandler {

	private static final Logger logger = LoggerFactory.getLogger(CropPlottedLinesHandler.class);
	
	@Execute
	public void execute(PlotValues plotValues, boolean isCropped, IEventBroker eventBroker, IUndoContext ctx) {
		
		AbstractOperation ao = new AbstractOperation("Crop - " + plotValues.getName() + " " + isCropped) {

			private boolean isSet = isCropped;

			@Override
			public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				return redo(monitor, info);
			}

			@Override
			public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				plotValues.setCropValues(isSet);
				logger.debug("Plotted lines have been toggled for '{}' to {}.", plotValues.getName(), isSet);
				eventBroker.send(CoreModelConstants.ICD2_MODEL_CHART_PLOT_CROPLINES_CHANGE, plotValues);
				isSet = !isSet;
				return Status.OK_STATUS;
			}

			@Override
			public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				return redo(monitor, info);
			}
		};

		ao.addContext(ctx);

		IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
		
		try {
			operationHistory.execute(ao, null, null);
		} catch (ExecutionException e) {
			logger.error("Failed to execute cropp plot.", e);
		}

	}
		
}