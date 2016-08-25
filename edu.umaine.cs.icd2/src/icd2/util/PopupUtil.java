/**
 * 
 */
package icd2.util;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.handlers.CropPlottedLinesHandler;
import icd2.model.Chart;
import icd2.model.Plot;
import icd2.model.PlotValues;

/**
 * A collection of methods to help create popup menus.
 * 
 * @author Mark Royer
 *
 */
public class PopupUtil {

	private static final Logger logger = LoggerFactory.getLogger(PopupUtil.class);

	/**
	 * The menu must contain the popup menu with the given ID or else it will be silently ignored.
	 * 
	 * @param mPart
	 *            (Not null)
	 * @param menuId
	 *            (Not null)
	 */
	public static void showMenu(MPart mPart, String menuId, Chart chartModel, IEventBroker eventBroker,
			IUndoContext undoContext) {
		MMenu selectedMenu = null;
		for (MMenu menu : mPart.getMenus()) {
			if (menuId.equals(menu.getElementId()))
				selectedMenu = menu;
		}

		if (selectedMenu != null) {
			logger.debug("Selected menu {}.", selectedMenu.getElementId());

			final MMenu theMenu = selectedMenu;
			Runnable r = new Runnable() {
				public void run() {
					showMenu(theMenu, chartModel, eventBroker, undoContext);
				}
			};
			Display.getDefault().asyncExec(r);

		} else {
			logger.debug("Unable to find menu with id = {}.", menuId);
		}
	}

	private static void showMenu(MMenu selectedMenu, Chart parentPlot, IEventBroker eventBroker,
			IUndoContext undoContext) {

		final Display display = Display.getCurrent();

		final Shell active = display.getActiveShell();
		String osName = System.getProperty("os.name").toLowerCase();
		boolean isMacOs = osName.startsWith("mac os x");

		final Shell useForPopups = isMacOs ? active : new Shell(display, SWT.NO_TRIM | SWT.NO_FOCUS | SWT.ON_TOP);

		Point l = display.getCursorLocation();
		// l.x -= 2;
		// l.y -= 2;

		if (!isMacOs) {
			useForPopups.setLocation(l);
			useForPopups.setSize(1, 1);// 4, 4);
			useForPopups.open();
		}

		Runnable r = new Runnable() {
			public void run() {
				useForPopups.setActive();

				final Menu menu = createMenu(parentPlot, useForPopups, eventBroker, undoContext);// create(selectedMenu,
				// useForPopups);
				menu.addListener(SWT.Hide, new Listener() {
					public void handleEvent(Event e) {
						// Dispose only after all other SWT events have been
						// called.
						display.asyncExec(new Runnable() {
							@Override
							public void run() {
								if (!isMacOs)
									useForPopups.dispose();
								if (!active.isDisposed())
									active.setActive();
								logger.debug("Popup menu hidden");
							}
						});
					}
				});
				menu.addListener(SWT.Show, new Listener() {

					private static final int MAX_ATTEMPTS = 200;

					private int attempts = 0;

					public void handleEvent(Event e) {

						// A show event occurred. Make sure that the menu is visible.
						Runnable r = new Runnable() {
							public void run() {
								if (!menu.isVisible() && attempts < MAX_ATTEMPTS) {
									menu.setVisible(true);
									logger.debug("Made popup menu visible");
									attempts++;

									try {
										// Making this sleep briefly greatly reduces the events fired.
										Thread.sleep(10);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								} else if (menu.isVisible()) {
									logger.debug("Menu is already visible. It took {} attempts.", attempts);
								} else {
									logger.debug("Attempted to make popup visible {} times, "
											+ "and it's still not visible. Giving up.", attempts);
									if (!isMacOs)
										useForPopups.dispose();
									if (!active.isDisposed())
										active.setActive();
								}
							}
						};
						display.asyncExec(r);
					}
				});

				menu.setVisible(true);
			}
		};
		display.asyncExec(r);

	}

	private static Menu createMenu(Chart chart, Control parentControl, IEventBroker eventBroker,
			IUndoContext undoContext) {

		Menu menu = new Menu(parentControl);

		// Menu cropItems = new Menu(menu);

		for (Plot[] topPlots : chart.getPlots()) {
			for (Plot topPlot : topPlots) {
				for (Plot[] ps : topPlot.getSubplots()) {
					for (Plot plot : ps) {
						for (PlotValues pv : plot.getRangeValues()) {
							MenuItem item = new MenuItem(menu, SWT.CHECK);
							item.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									new CropPlottedLinesHandler().execute(pv, ((MenuItem) e.getSource()).getSelection(),
											eventBroker, undoContext);
								}

							});

							item.setText("Crop - " + pv.getName());
							item.setSelection(pv.isCropValues());
							logger.debug("Created menu item named '{}'.", item.getText());
						}
					}
				}
			}
		}

		return menu;
	}

	// private static Menu create(MMenu menuSpec, Control parent) {
	// Menu menu = new Menu(parent);
	//
	// for (MMenuElement me : menuSpec.getChildren()) {
	// createMenuElement(menu, me);
	// }
	//
	// return menu;
	// }
	//
	// private static void createMenuElement(Menu menu, MMenuElement me) {
	//
	// MenuItem item = new MenuItem(menu, SWT.PUSH);
	//
	// item.addListener(SWT.Selection, new Listener() {
	// public void handleEvent(Event e) {
	// System.out.println("Select All");
	// }
	// });
	//
	// item.setText(me.getLabel());
	// item.setToolTipText(me.getTooltip());
	// // item.setImage(IconUtils.createImage(me.getIconURI()));
	//
	// }
}
