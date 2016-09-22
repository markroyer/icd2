package icd2.parts;

import static icd2.model.CoreModelConstants.ITEM_AT_MOUSE_CLICK;
import static icd2.model.CoreModelConstants.TREE_ITEMS_SELECTED;
import static icd2.model.CoreModelConstants.TREE_ITEM_CHANGE_EVENT;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.Core;
import icd2.model.CoreModelConstants;
import icd2.model.DatingProject;
import icd2.model.DepthYear;
import icd2.model.ModelObject;
import icd2.model.Workspace;
import icd2.util.HDF5Util;
import icd2.util.WorkspaceUtil;

public class WorkspaceView {

	private TreeViewer viewer;

	private static final Logger logger = LoggerFactory
			.getLogger(WorkspaceView.class);

	@PostConstruct
	public void createControls(Composite parent, Workspace workspace,
			EMenuService menuService, final IEclipseContext ctx,
			final IEventBroker eventBroker, EModelService modelService,
			MApplication application) {
		viewer = new TreeViewer(parent,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection thisSelection = (IStructuredSelection) event
						.getSelection();
				Object selection = thisSelection.getFirstElement();

				if (selection instanceof DatingProject) {
					List<MPart> projectPartsList = modelService.findElements(
							application, "icd2.partdescriptor.project",
							MPart.class, null);

					DatingProject project = (DatingProject) selection;
					boolean isOpen = false;
					for (MPart mPart : projectPartsList) {
						if (mPart.getContext() != null) {
							if (mPart.getContext().get(DatingProject.class)
									.equals(project)) {
								isOpen = true;
							}
						}
					}
					if (!isOpen) { // Make sure to reload project
						project = WorkspaceUtil.refreshDatingProject(workspace,
								project);
						viewer.refresh();
					}

					// @SuppressWarnings("unchecked")
					// Set<String> openProjects = (Set<String>)
					// ctx.get(PreferenceKeys.OPEN_PROJECTS);
					//
					// openProjects.add(project.getName());

					logger.info("Opening project '{}'", project.getName());

					eventBroker.send(CoreModelConstants.DISPLAY_PROJECT_EVENT,
							project);
					eventBroker.send(CoreModelConstants.OPEN_PROJECT_EVENT,
							project);

				}
			}
		});

		// @SuppressWarnings("unchecked")
		// Set<String> openProjects = (Set<String>)
		// ctx.get(PreferenceKeys.OPEN_PROJECTS);

		List<MPartStack> partStackList = modelService.findElements(application,
				"icd2.partstack.center", MPartStack.class, null);

		viewer.setContentProvider(
				new ViewContentProvider(partStackList.get(0)));// openProjects));
		viewer.setLabelProvider(new FolderDisplayLabelProvider());
		viewer.setInput(workspace);

		menuService.registerContextMenu(viewer.getTree(),
				"icd2.popupmenu.leftTree");

		viewer.getTree().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				ctx.remove(ITEM_AT_MOUSE_CLICK);
				ctx.remove(TREE_ITEMS_SELECTED);

				if (e.button == 3) { // Right click?

					IStructuredSelection selected = viewer
							.getStructuredSelection();

					logger.debug("selected {}",
							viewer.getStructuredSelection().size());

					ctx.set(TREE_ITEMS_SELECTED, selected.toList());

					TreeItem itemAtClick = viewer.getTree()
							.getItem(new Point(e.x, e.y));

					if (itemAtClick != null) {
						ctx.set(ITEM_AT_MOUSE_CLICK, itemAtClick.getData());
					} else {
						ctx.remove(ITEM_AT_MOUSE_CLICK);
					}
				}
			}

		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event
						.getSelection();
				ModelObject<?, ?> item = (ModelObject<?, ?>) selection
						.getFirstElement();
				ctx.set(CoreModelConstants.TREE_ITEM_SELECTION, item);
				eventBroker.send(TREE_ITEM_CHANGE_EVENT, item);
			}
		});
	}

	class ViewContentProvider implements ITreeContentProvider {

		// Set<String> openProjects;

		MPartStack partStack;

		public ViewContentProvider() {// Set<String> openProjects) {
			// this.openProjects = openProjects;
		}

		public ViewContentProvider(MPartStack partStack) {
			this.partStack = partStack;
		}

		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return ((Workspace) inputElement).children(); // Don't display the
															// very root element
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return ((ModelObject<?, ?>) parentElement).children();
		}

		@Override
		public Object getParent(Object element) {
			return ((ModelObject<?, ?>) element).getParent();
		}

		@Override
		public boolean hasChildren(Object element) {

			if (element instanceof DatingProject) {

				DatingProject dp = (DatingProject) element;

				List<MStackElement> parts = partStack.getChildren();

				logger.info(
						"Checking if dating project tree node named {} has children",
						dp.getName());

				for (MStackElement mStackElement : parts) {
					if (mStackElement instanceof MPart) {
						MPart mp = (MPart) mStackElement;
						if (mp.getLabel().equals(dp.getName()))
							return mp.isToBeRendered();
					}
				}

				return false; // This dating project is not open

				// return openProjects.contains(dp.getName()) &&
				// dp.children().length > 0;
			} else
				return ((ModelObject<?, ?>) element).children().length > 0;
		}

	}

	@Focus
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Inject
	@Optional
	public void onAddNewCore(
			@UIEventTopic(CoreModelConstants.ON_ADD_NEW_CORE) Core newCore,
			Workspace workspace) {

		try {

			// Create the HDF5 core file
			HDF5Util.saveCoreHDF5(newCore);

			workspace.addCore(newCore);

			viewer.refresh(workspace.getCoreData());
			viewer.setSelection(new StructuredSelection(newCore));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Inject
	@Optional
	public void onRemovedCore(
			@UIEventTopic(CoreModelConstants.REMOVED_CORE) Core core,
			Workspace workspace) {
		viewer.refresh(workspace.getCoreData());
		logger.debug("Refreshed workspace viewer after core '{}' removed.",
				core.getName());
	}

	@Inject
	@Optional
	public void onRemoveDepthMarker(
			@UIEventTopic(CoreModelConstants.ICD2_MODEL_DATESESSION_DEPTH_REMOVE) DepthYear marker) {
		viewer.refresh(marker.getParent());
		logger.debug("Refreshed date session after depth marker '{}' removed.",
				marker.getName());
	}

	@Inject
	@Optional
	public void onDatingProjectNameChange(Shell shell,
			@UIEventTopic(CoreModelConstants.ICD2_MODEL_MODELOBJECT_NAME_CHANGE) ModelObject<?, ?> mo) {

		// URL workspaceLocation = datingProject.getParent().getLocation();
		//
		// IEclipseContext ctx = E4Workbench.getServiceContext();
		//
		// String oldName =
		// (String)ctx.get(CoreModelConstants.ICD2_MODEL_DATINGPROJECT_NAME_CHANGE
		// + "old");
		//
		// String oldPath = workspaceLocation.getPath() + File.separator +
		// oldName;
		// String newPath = workspaceLocation.getPath() + File.separator +
		// datingProject.getName();
		//
		// try {
		// WorkspaceUtil.moveDatingProject(new File(oldPath), new
		// File(newPath));
		// } catch (IOException e) {
		// ErrorDialogUtil.showErrorDialog(shell, e);
		// datingProject.setName(oldName);
		// }

		logger.info("Model folder named changed to {}", mo.getName());

		viewer.refresh(mo);
	}

	@Inject
	@Optional
	public void onNewDatingProject(
			@UIEventTopic(CoreModelConstants.ON_NEW_DATING_PROJECT) DatingProject project,
			Workspace workspace) {

		viewer.refresh(workspace);
		viewer.setSelection(new StructuredSelection(project));

	}

	@Inject
	@Optional
	public void onRemovedDatingProject(
			@UIEventTopic(CoreModelConstants.REMOVED_DATING_PROJECT) DatingProject project,
			Workspace workspace) {
		viewer.refresh(workspace);
		logger.debug(
				"Refreshed workspace viewer after dating project '{}' removed.",
				project.getName());
	}

	@Inject
	@Optional
	public void onDatingProjectOpened(
			@UIEventTopic(CoreModelConstants.OPEN_PROJECT_EVENT) DatingProject project,
			Workspace workspace) {
		logger.info("Dating project opened. Refreshing folder {}",
				project.getName());
		viewer.expandToLevel(project, 1);
		viewer.refresh(project);
	}

	@Inject
	@Optional
	public void onDatingProjectClosed(
			@UIEventTopic(CoreModelConstants.CLOSE_PROJECT_EVENT) DatingProject project,
			Workspace workspace) {
		logger.info("Dating project opened. Refreshing folder {}",
				project.getName());
		viewer.collapseToLevel(project, TreeViewer.ALL_LEVELS);
		viewer.refresh(project);
	}
	
	@Inject
	@Optional
	public void onAddDateMarker(
			@UIEventTopic(CoreModelConstants.ICD2_MODEL_DATESESSION_DEPTH_ADD) DepthYear marker) {
		viewer.refresh(marker.getParent());
	}
	
	@Inject
	@Optional
	public void onRemoveDateMarker(
			@UIEventTopic(CoreModelConstants.ICD2_MODEL_DATESESSION_DEPTH_REMOVE) DepthYear marker) {
		viewer.refresh(marker.getParent());
	}
}