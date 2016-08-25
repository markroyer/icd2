package icd2.parts;

import static icd2.model.CoreModelConstants.TREE_ITEM_CHANGE_EVENT;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icd2.model.Core;
import icd2.model.Editable;
import icd2.model.EditableObject;
import icd2.model.ModelObject;
import icd2.widgets.StandardEditorCreator;
import icd2.widgets.UnknownTypeException;

public class DetailsView {
	
	private static final Logger logger = LoggerFactory.getLogger(DetailsView.class);
	
	ModelObject<?, ?> modelObject;

	ScrolledComposite scroll;

	Composite contents;

	Composite parent;

	static final String NONESELECTED = "Select an item in the tree to see details.";

	@Inject
	public DetailsView() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		this.parent = parent;
	}

	@Inject
	@Optional
	public void snippetSelectionChanged(
			@UIEventTopic(TREE_ITEM_CHANGE_EVENT) ModelObject<?, ?> item,
			IEventBroker eventBroker, IEclipseContext ctx) {

		this.modelObject = item;

		if (scroll != null)
			scroll.dispose();

		scroll = new ScrolledComposite(parent,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		contents = new Composite(scroll, SWT.NONE);

		scroll.setContent(contents);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		contents.setLayout(layout);

		if (item == null) {
			Label label = new Label(contents, SWT.None);
			label.setText(NONESELECTED);
		} else {

			Class<?> clazz = modelObject.getClass();

			EditableObject[] editableAnnotations = clazz
					.getAnnotationsByType(EditableObject.class);

			// It's been annotated with editable properties.
			if (editableAnnotations.length == 1) {

				// Create the object's label

				Label label = new Label(contents, SWT.None);

				EditableObject editObjectAnn = editableAnnotations[0];

				label.setText(editObjectAnn.name());
				label.setToolTipText(editObjectAnn.description());

				// Create the editable properties

				StandardEditorCreator sec = new StandardEditorCreator();

				for (Field f : getAnnotatedProperties(clazz)) {
					try {
						f.setAccessible(true);
						sec.createEditor(f.getType(), f.get(item),
								f.getAnnotation(Editable.class), item, f,
								contents, eventBroker, ctx.get(IUndoContext.class));
					} catch (UnknownTypeException | IllegalArgumentException
							| IllegalAccessException e) {
						logger.error(e.getMessage(), e);
					}
				}

			} else { // No edit properties annotated

				Label label = new Label(contents, SWT.None);

				// TODO generalize how this works
				String text = modelObject.getName();
				if (modelObject instanceof Core) {
					text += " " + ((Core) modelObject).getTopDate();
				}

				label.setText(text);
				label.setForeground(
						parent.getDisplay().getSystemColor(SWT.COLOR_RED));

			}
		}

		contents.setSize(contents.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		parent.layout();

	}

	private List<Field> getAnnotatedProperties(Class<?> clazz) {

		List<Field> result = new ArrayList<>();

		Class<?> currentType = clazz;
		
		while (Object.class != currentType) {
			for (Field f : clazz.getDeclaredFields()) {
				Editable[] es = f.getAnnotationsByType(Editable.class);
				if (es.length == 1) {
					result.add(f);
				}
			}
			currentType = currentType.getSuperclass();
		}

		return result;
	}

	@Focus
	public void setFocus() {
		logger.debug("Attempting to focus on DetailsView.  Contents is '{}'.", contents);
		if (contents != null)
			contents.setFocus();
	}
}