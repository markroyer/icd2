
package icd2.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UndoHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(UndoHandler.class);
	
	@Execute
	public void execute(IEclipseContext ectx, IEventBroker eventBroker) {

		IUndoContext context = ectx.get(IUndoContext.class);
		
		IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();

		try {
			
			operationHistory.undo(context, null, null);

			eventBroker.send(UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, UIEvents.ALL_ELEMENT_ID);
		} catch (ExecutionException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@CanExecute
	public boolean canExecute(IEclipseContext ectx) {
		
		IUndoContext context = ectx.get(IUndoContext.class);

		IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();

		return operationHistory.canUndo(context);
	}

}