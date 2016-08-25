/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/
package icd2;

import java.util.List;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.CoreModelConstants;
import icd2.model.Workspace;
import icd2.util.WorkspaceUtil;

/**
 * This is a stub implementation containing e4 LifeCycle annotated methods.
 * <br />
 * There is a corresponding entry in <em>plugin.xml</em> (under the
 * <em>org.eclipse.core.runtime.products' extension point</em>) that references
 * this class.
 **/
@SuppressWarnings("restriction")
public class E4LifeCycle {
	
	private static final Logger logger = LoggerFactory.getLogger(E4LifeCycle.class);

	@PostContextCreate
	void postContextCreate(IEclipseContext workbenchContext, IEventBroker eventBroker, IEclipsePreferences prefs,
			Workspace workspace) {

		// Uncomment to create your own shutdown hook
		// eventBroker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE,
		// new AppStartedHandler());

	}

	@PreSave
	void preSave(IEclipseContext workbenchContext, IEclipsePreferences pref) {
		// Set<String> openProjects = (Set<String>)
		// workbenchContext.get(PreferenceKeys.OPEN_PROJECTS);
		//
		// logger.info("Presaving!");
		//
		// ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//
		// try {
		//
		// ObjectOutputStream oos = new ObjectOutputStream(baos);
		//
		// oos.writeObject(openProjects);
		// oos.flush();
		//
		// pref.putByteArray(PreferenceKeys.OPEN_PROJECTS, baos.toByteArray());
		//
		// } catch (IOException e) {
		// logger.error(e.getMessage(), e);
		// }

	}

	@ProcessAdditions
	void processAdditions(IEclipseContext workbenchContext, EModelService modelService, MApplication application,
			IEventBroker eventBroker, Workspace workspace) {

		logger.info("Processing additions...");

		application.getContext().set(IUndoContext.class, new UndoContext());
		
		List<MPartStack> partStackList = modelService.findElements(application, "icd2.partstack.center",
				MPartStack.class, null);

		if (partStackList.size() < 1) {
			logger.debug("Partstack list smaller than 1?");
			return;
		}
		
		// Only 1 center part.
		List<MStackElement> parts = partStackList.get(0).getChildren();

		for (MStackElement el : parts) {
			if (el instanceof MPart) {
				MPart mp = (MPart) el;
				logger.info("Found open project named {}", mp.getLabel());

				// We want to know when a project has been closed so that we can
				// close it in other places.
				eventBroker.subscribe(UIEvents.UIElement.TOPIC_TOBERENDERED, new EventHandler() {

					@Override
					public void handleEvent(Event event) {
						Object part = event.getProperty(UIEvents.EventTags.ELEMENT);
						// boolean tbr = (Boolean) event
						// .getProperty(UIEvents.EventTags.NEW_VALUE);
						if (part instanceof MPart && ((MPart) part).getLabel().equals(mp.getLabel())) {

							MPart mpart = (MPart) part;

							logger.info("Closed mpart named {}", mpart.getLabel());

							eventBroker.send(CoreModelConstants.CLOSE_PROJECT_EVENT,
									WorkspaceUtil.getProject(workspace, mp.getLabel()));
							eventBroker.unsubscribe(this);
						}
					}
				});
			}
		}

	}

	@ProcessRemovals
	void processRemovals(IEclipseContext workbenchContext) {
	}

}
