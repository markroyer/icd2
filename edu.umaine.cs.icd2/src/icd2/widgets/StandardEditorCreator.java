/**
 * 
 */
package icd2.widgets;

import java.lang.reflect.Field;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.internal.commands.operations.GlobalUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.WorkbenchLogger;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.Editable;
import icd2.model.ModelModifier;
import icd2.model.ModelObject;
import icd2.model.ModelValidator;
import icd2.model.ModelValueProvider;

/**
 * @author Mark Royer
 *
 */
public class StandardEditorCreator {

	private static final Logger logger = LoggerFactory.getLogger(StandardEditorCreator.class);

	/**
	 * 
	 * @param type
	 *            The type of the editable property (Not null)
	 * @param value
	 *            The current value of the editable property (Null possible)
	 * @param editable
	 *            The editable annotation for the property (Not null)
	 * @param p
	 *            The model object in the tree that was selected (Not null)
	 * @param field
	 *            The field for the editable property (Not null)
	 * @param parent
	 *            The composite that the result will be placed in (Not null)
	 * @param eventBroker
	 *            Broker to be notified when changes occur (Not null)
	 * @param ctx
	 *            Context to help looking up or storing objects (Not null)
	 * @return The editing container that was created.
	 * @throws UnknownTypeException
	 *             Thrown if the type of object isn't known
	 */
	public Composite createEditor(Class<?> type, Object value, Editable editable, ModelObject<?, ?> p, Field field,
			Composite parent, IEventBroker eventBroker, IUndoContext ctx) throws UnknownTypeException {

		if (String.class == type) {

			switch (editable.editType()) {
			case CONFIRM:
				return createConfirmStringEditor(value, editable, p, field, parent, eventBroker, ctx);
			case IMMEDIATE:
				return createImmediateStringEditor(value, editable, p, field, parent, eventBroker, ctx);
			}

		} else if (type.isEnum()) {
			return createImmediateEnumEditor(value, editable, p, field, parent, eventBroker, ctx);
		} else if (p instanceof ModelObject<?, ?>) {
			return createImmediateListEditor(value, editable, p, field, parent, eventBroker, ctx);

		}

		throw new UnknownTypeException(type);
	}

	private <T extends ModelObject<?, ?>> Composite createImmediateListEditor(Object value, Editable editable, T p,
			Field field, Composite parent, IEventBroker eventBroker, IUndoContext ctx) {

		Composite container = new Composite(parent, SWT.BORDER);
		container.setLayout(new GridLayout(3, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Label label = new Label(container, SWT.NONE);
		label.setText(editable.name());
		label.setToolTipText(editable.description());

		ComboViewer combo = new ComboViewer(container, SWT.READ_ONLY);
		combo.setContentProvider(ArrayContentProvider.getInstance());
		try {

			@SuppressWarnings("unchecked")
			ModelValueProvider<Object, Object> mvp = (ModelValueProvider<Object, Object>) editable.valueProvider()
					.newInstance();
			
			@SuppressWarnings("unchecked")
			Class<Object> clazz = (Class<Object>) value.getClass();
			
			combo.setInput((mvp).getValues(clazz, p));

			combo.setLabelProvider(new LabelProvider() {

				@Override
				public String getText(Object element) {

					if (element instanceof ModelObject<?, ?>) {
						ModelObject<?, ?> mo = (ModelObject<?, ?>) element;
						return mo.getName();
					}
					return element != null ? element.toString() : null;
				}

				@Override
				public Image getImage(Object element) {
					// Don't worry about images for now
					return null;
				}
			});

		} catch (InstantiationException | IllegalAccessException | UnknownTypeException e) {
			logger.error(e.getMessage(), e);
		}

		// Default selection
		combo.setSelection(new StructuredSelection(value));

		// combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();

		for (String event : editable.onchange()) {
			EventHandler eh = new EventHandler() {
				@Override
				public void handleEvent(org.osgi.service.event.Event event) {
					try {
						Object obj = field.get(p);
						combo.setSelection(new StructuredSelection(obj));

					} catch (IllegalArgumentException | IllegalAccessException e) {
						logger.error(e.getMessage(), e);
					}
				}
			};
			eventBroker.subscribe(event, eh);
		}

		combo.getCombo().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				try {

					@SuppressWarnings("unchecked")
					ModelModifier<ModelObject<?, ?>, Object> mm = (ModelModifier<ModelObject<?, ?>, Object>) editable
							.method().newInstance();

					AbstractOperation ao = new AbstractOperation("Combo select" + value) {

						private Object val;

						private boolean isSet;

						@Override
						public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
							mm.undo(field, p);
							mm.notify(eventBroker, editable.onchange(), p);
							return Status.OK_STATUS;
						}

						@Override
						public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {

							if (!isSet) {
								ISelection selection = combo.getSelection();
								if (!selection.isEmpty()) {
									IStructuredSelection structuredSelection = (IStructuredSelection) selection;
									// cast the object to an terminal object
									val = structuredSelection.getFirstElement();
									isSet = true;
								}
							}

							mm.redo(field, p, val);
							mm.notify(eventBroker, editable.onchange(), p);
							return Status.OK_STATUS;
						}

						@Override
						public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
							return redo(monitor, info);
						}
					};

					ao.addContext(ctx);

					IStatus is = operationHistory.execute(ao, null, null);

					eventBroker.send(UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, UIEvents.ALL_ELEMENT_ID);

				} catch (InstantiationException | IllegalAccessException | ExecutionException e1) {
					logger.error(e1.getMessage(), e1);
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

		});

		return container;
	}

	private <T extends ModelObject<?, ?>> Composite createImmediateEnumEditor(Object value, Editable editable, T p,
			Field field, Composite parent, IEventBroker eventBroker, IUndoContext ctx) {

		Composite container = new Composite(parent, SWT.BORDER);
		container.setLayout(new GridLayout(3, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Label label = new Label(container, SWT.NONE);
		label.setText(editable.name());
		label.setToolTipText(editable.description());

		ComboViewer combo = new ComboViewer(container, SWT.READ_ONLY);
		combo.setContentProvider(ArrayContentProvider.getInstance());
		combo.setInput(value.getClass().getEnumConstants());

		// Default selection
		combo.setSelection(new StructuredSelection(value));

		// combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();

		for (String event : editable.onchange()) {
			EventHandler eh = new EventHandler() {
				@Override
				public void handleEvent(org.osgi.service.event.Event event) {
					try {
						Object obj = field.get(p);
						combo.setSelection(new StructuredSelection(obj));

					} catch (IllegalArgumentException | IllegalAccessException e) {
						logger.error(e.getMessage(), e);
					}
				}
			};
			eventBroker.subscribe(event, eh);
			// combo.addDisposeListener(new DisposeListener() {
			// @Override
			// public void widgetDisposed(DisposeEvent e) {
			// eventBroker.unsubscribe(eh);
			// }
			// });
		}

		combo.getCombo().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				try {

					@SuppressWarnings("unchecked")
					ModelModifier<ModelObject<?, ?>, Object> mm = (ModelModifier<ModelObject<?, ?>, Object>) editable
							.method().newInstance();

					AbstractOperation ao = new AbstractOperation("Combo select" + value) {

						private Object val;

						private boolean isSet;

						@Override
						public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
							mm.undo(field, p);
							mm.notify(eventBroker, editable.onchange(), p);
							return Status.OK_STATUS;
						}

						@Override
						public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {

							if (!isSet) {
								ISelection selection = combo.getSelection();
								if (!selection.isEmpty()) {
									IStructuredSelection structuredSelection = (IStructuredSelection) selection;
									// cast the object to an terminal object
									val = structuredSelection.getFirstElement();
									isSet = true;
								}
							}

							mm.redo(field, p, val);
							mm.notify(eventBroker, editable.onchange(), p);
							return Status.OK_STATUS;
						}

						@Override
						public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
							return redo(monitor, info);
						}
					};

					ao.addContext(ctx);

					IStatus is = operationHistory.execute(ao, null, null);

					eventBroker.send(UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, UIEvents.ALL_ELEMENT_ID);

				} catch (InstantiationException | IllegalAccessException | ExecutionException e1) {
					logger.error(e1.getMessage(), e1);
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

		});

		return container;
	}

	private <T extends ModelObject<?, ?>> Composite createImmediateStringEditor(Object value, Editable editable, T p,
			Field field, Composite parent, IEventBroker eventBroker, IUndoContext ctx) {

		Composite container = new Composite(parent, SWT.BORDER);
		container.setLayout(new GridLayout(3, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Label label = new Label(container, SWT.NONE);
		label.setText(editable.name());
		label.setToolTipText(editable.description());

		Text text = new Text(container, SWT.SINGLE);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		IObservableMap attributesMap = new WritableMap();
		DataBindingContext dbc = new DataBindingContext();

		IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();

		try {

			@SuppressWarnings("unchecked")
			ModelValidator<T> mv = (ModelValidator<T>) editable.validator().newInstance();

			bindAndValidateCoreText(p, text, mv, attributesMap, dbc, new Control[] {});

			text.setText((String) value);

			for (String event : editable.onchange()) {
				EventHandler eh = new EventHandler() {
					@Override
					public void handleEvent(org.osgi.service.event.Event event) {
						try {
							int pos = text.getCaretPosition();
							text.setText((String) field.get(p));
							text.setSelection(pos);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							logger.error(e.getMessage(), e);
						}
					}
				};
				eventBroker.subscribe(event, eh);
				text.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						eventBroker.unsubscribe(eh);
					}
				});
			}

			text.addKeyListener(new TextChangeListener(editable, p, field, eventBroker, attributesMap, operationHistory,
					ctx, text));

		} catch (InstantiationException | IllegalAccessException e1) {
			logger.error(e1.getMessage(), e1);
		}

		return container;

	}

	private class TextChangeListener implements KeyListener {

		Editable editable;
		ModelObject<?, ?> p;
		Field field;
		IEventBroker eventBroker;
		IObservableMap attributesMap;
		IOperationHistory operationHistory;
		IUndoContext undoContext;
		Text text;

		public TextChangeListener(Editable editable, ModelObject<?, ?> p, Field field, IEventBroker eventBroker,
				IObservableMap attributesMap, IOperationHistory operationHistory, final IUndoContext undoContext,
				Text text) {
			this.editable = editable;
			this.p = p;
			this.field = field;
			this.eventBroker = eventBroker;
			this.attributesMap = attributesMap;
			this.operationHistory = operationHistory;
			this.undoContext = undoContext;
			this.text = text;
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// Do nothing
		}

		@Override
		public void keyReleased(KeyEvent e) {
			Text source = (Text) e.getSource();

			try {

				String oldValue = (String) field.get(p);

				if (!oldValue.equals(source.getText())) {

					@SuppressWarnings("unchecked")
					ModelModifier<ModelObject<?, ?>, Object> mm = (ModelModifier<ModelObject<?, ?>, Object>) editable
							.method().newInstance();

					executeStringConfirm(editable, p, field, eventBroker, attributesMap, operationHistory, undoContext,
							mm, text);
					oldValue = source.getText();

				}
			} catch (InstantiationException | IllegalArgumentException | IllegalAccessException e1) {
				logger.error(e1.getMessage(), e1);
			}
		}

	}

	private <T extends ModelObject<?, ?>> Composite createConfirmStringEditor(Object value, Editable editable, T p,
			Field field, Composite parent, IEventBroker eventBroker, IUndoContext ctx) {

		Composite container = new Composite(parent, SWT.BORDER);
		container.setLayout(new GridLayout(3, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Label label = new Label(container, SWT.NONE);
		label.setText(editable.name());
		label.setToolTipText(editable.description());

		Text text = new Text(container, SWT.SINGLE);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		Button button = new Button(container, SWT.PUSH);
		button.setText("Apply");

		IObservableMap attributesMap = new WritableMap();
		DataBindingContext dbc = new DataBindingContext();

		IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();

		try {

			@SuppressWarnings("unchecked")
			ModelValidator<T> mv = (ModelValidator<T>) editable.validator().newInstance();

			bindAndValidateCoreText(p, text, mv, attributesMap, dbc, new Control[] { button });

			text.setText((String) value);

			for (String event : editable.onchange()) {
				EventHandler eh = new EventHandler() {
					@Override
					public void handleEvent(org.osgi.service.event.Event event) {
						try {
							text.setText((String) field.get(p));
						} catch (IllegalArgumentException | IllegalAccessException e) {
							logger.error(e.getMessage(), e);
						}
					}
				};
				eventBroker.subscribe(event, eh);
				text.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						eventBroker.unsubscribe(eh);
					}
				});
			}

			text.addListener(SWT.DefaultSelection, new Listener() {

				public void handleEvent(Event event) {

					try {

						@SuppressWarnings("unchecked")
						ModelModifier<ModelObject<?, ?>, Object> mm = (ModelModifier<ModelObject<?, ?>, Object>) editable
								.method().newInstance();

						executeStringConfirm(editable, p, field, eventBroker, attributesMap, operationHistory,
								ctx, mm, text);
					} catch (InstantiationException | IllegalAccessException e) {
						logger.error(e.getMessage(), e);
					}
				}
			});

			button.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					switch (e.type) {
					case SWT.Selection:

						try {

							@SuppressWarnings("unchecked")
							ModelModifier<ModelObject<?, ?>, Object> mm = (ModelModifier<ModelObject<?, ?>, Object>) editable
									.method().newInstance();
							executeStringConfirm(editable, p, field, eventBroker, attributesMap, operationHistory,
									ctx, mm, text);

						} catch (InstantiationException | IllegalAccessException e1) {
							logger.error(e1.getMessage(), e1);
						}

						break;
					}
				}

			});

		} catch (InstantiationException | IllegalAccessException e1) {
			logger.error(e1.getMessage(), e1);
		}

		return container;

	}

	private void executeStringConfirm(Editable editable, ModelObject<?, ?> p, Field field, IEventBroker eventBroker,
			IObservableMap attributesMap, IOperationHistory operationHistory, final IUndoContext undoContext,
			ModelModifier<ModelObject<?, ?>, Object> mm, Text text) {

		logger.info("Using {} as value to set on model object", attributesMap.get(text));

		try {

			AbstractOperation ao = new AbstractOperation("String change " + attributesMap.get(text)) {

				private Object val;

				private boolean isSet;

				@Override
				public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
					mm.undo(field, p);
					mm.notify(eventBroker, editable.onchange(), p);
					return Status.OK_STATUS;
				}

				@Override
				public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {

					if (!isSet) {
						val = attributesMap.get(text);
						isSet = true;
					}

					mm.redo(field, p, val);
					mm.notify(eventBroker, editable.onchange(), p);
					return Status.OK_STATUS;
				}

				@Override
				public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
					return redo(monitor, info);
				}
			};

			ao.addContext(undoContext);

			IStatus is = operationHistory.execute(ao, null, null);

			eventBroker.send(UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, UIEvents.ALL_ELEMENT_ID);
		} catch (ExecutionException e1) {
			logger.error(e1.getMessage(), e1);
		}
	}

//	private IUndoContext getUndoContext(IEclipseContext ctx) {
//		IUndoContext undoContext = ctx.get();//IUndoContext.class);
//
//		IEclipseContext cur = ctx;
//		while(cur != null) {
//			System.out.println(cur.getClass() + " " + cur);
//			cur = cur.getParent();
//		}
//		
//		if (undoContext == null) {
//			logger.debug("Context was null.  Creating a new GlobalUndoContext...");
//			undoContext = new GlobalUndoContext();
//			ctx.set(IUndoContext.class, undoContext);
//		}
//
//		return undoContext;
//	}

	private <T extends ModelObject<?, ?>> Binding bindAndValidateCoreText(T p, Text coreNameText, ModelValidator<T> mv,
			IObservableMap attributesMap, DataBindingContext dctx, Control[] widgets) {

		IObservableValue coreNameTextValue = WidgetProperties.text(SWT.Modify).observe(coreNameText);

		IValidator validator = mv.create(p, widgets);

		UpdateValueStrategy strategy = new UpdateValueStrategy();
		strategy.setBeforeSetValidator(validator);

		IObservableValue modelCoreNameValue = Observables.observeMapEntry(attributesMap, coreNameText);

		Binding bindValue = dctx.bindValue(coreNameTextValue, modelCoreNameValue, strategy, null);

		// add some decorations
		ControlDecorationSupport.create(bindValue, SWT.TOP | SWT.LEFT);

		return bindValue;
	}

}