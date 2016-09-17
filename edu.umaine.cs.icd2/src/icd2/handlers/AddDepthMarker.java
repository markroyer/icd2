
package icd2.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.chart.YearMarker;
import icd2.model.Chart;
import icd2.model.CoreModelConstants;
import icd2.model.DateSession;
import icd2.model.DepthYear;

public class AddDepthMarker {

	private static final Logger logger = LoggerFactory
			.getLogger(AddDepthMarker.class);

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private IUndoContext ctx;

	@Execute
	public void execute(Shell shell, 
			@Named(CoreModelConstants.TREE_ITEM_SELECTION) @Optional DateSession session) {
		logger.info("Adding a depth marker to {}.", session.getName());
		
		Chart chart = session.getParent().getChart();
		
		InputDialog dlg = new InputDialog(shell,
	            "Year depth?", "Please enter the depth of the new marker.", "initVal", new IInputValidator() {
					@Override
					public String isValid(String newText) {
						  try {
					            Double.parseDouble(newText);
					            return null;
					        } catch (NumberFormatException e) {
					            return e.getMessage();
					        }
					}
				});
	        if (dlg.open() == Window.OK) {
	          
	        	double depth = Double.parseDouble(dlg.getValue());
	    			    		
	        	session.getYear(session.getDepthIndex(depth));
	        	
	    		addDepthMarker(chart, depth, year, notify);
	        	
	        }
		
	}

	public static void addDepthMarker(Chart chartModel, double depth, int year,
			boolean notify) {

		DateSession ds = chartModel.getActiveDateSession();
		DepthYear dy = new DepthYear(ds, depth, year);

		YearMarker newYearMarker = new YearMarker(depth, year, ds);

		if (notify) {

		}
	}
}