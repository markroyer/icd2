/**
 * 
 */
package icd2.parts;

import icd2.model.ModelObject;
import icd2.util.IconUtils;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * @author Mark Royer
 *
 */
public class FolderDisplayLabelProvider extends StyledCellLabelProvider {

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		StyledString text = new StyledString();
		ModelObject<?, ?> to = (ModelObject<?, ?>) element;
		ModelObject<?, ?>[] kids = to.children();
		text.append(to.getName());
		cell.setImage(IconUtils.getIcon(element.getClass()));
		if (kids.length > 0) {
			text.append(" (" + kids.length + ") ", StyledString.COUNTER_STYLER);
		}

		cell.setText(text.toString());
		cell.setStyleRanges(text.getStyleRanges());
		super.update(cell);

	}
}