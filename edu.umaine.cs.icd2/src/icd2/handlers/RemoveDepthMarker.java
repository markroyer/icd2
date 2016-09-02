 
package icd2.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.CoreModelConstants;
import icd2.model.DepthYear;
import icd2.model.Workspace;

public class RemoveDepthMarker {
	
	private static final Logger logger = LoggerFactory.getLogger(RemoveDepthMarker.class);
	
	@Execute
	public void execute(Shell shell, IEventBroker eventBroker, Workspace workspace,
			@Named(CoreModelConstants.TREE_ITEM_SELECTION) @Optional DepthYear marker) {
	
			eventBroker.post(CoreModelConstants.ICD2_MODEL_DATESESSION_DEPTH_REMOVE, marker);

			logger.debug("Removed the depth {}.", marker.getName());
		
			// TODO finish
	}
	
}