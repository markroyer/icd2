
package icd2.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.Chart;
import icd2.model.CoreModelConstants;
import icd2.model.DateSession;

public class AddDepthMarker {

	private static final Logger logger = LoggerFactory
			.getLogger(AddDepthMarker.class);

	@Execute
	public void execute(
			@Named(CoreModelConstants.TREE_ITEM_SELECTION) @Optional DateSession session) {
		logger.info("Adding a depth marker to {}.", session.getName());
		
		Chart chart = session.getParent().getChart();
		// TODO finish
	}

}