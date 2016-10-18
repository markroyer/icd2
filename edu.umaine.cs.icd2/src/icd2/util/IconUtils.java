/**
 * 
 */
package icd2.util;

import icd2.model.Chart;
import icd2.model.Core;
import icd2.model.CoreData;
import icd2.model.DatingProject;
import icd2.model.Plot;
import icd2.model.Sample;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Methods related to creating, loading, and manipulating icons.
 * 
 * @author Mark Royer
 *
 */
public class IconUtils {

	private static Map<Class<?>, Image> icons;

	private static Image UNKNOWN = createImage("icons/faenza/unknown16x16.png");

	static {
		icons = new HashMap<Class<?>, Image>();
		icons.put(Core.class, createImage("icons/faenza/visualization16x16.png"));
		icons.put(CoreData.class, createImage("icons/faenza/folder16x16.png"));
		icons.put(DatingProject.class,
				createImage("icons/faenza/folder16x16.png"));
		icons.put(Sample.class, createImage("icons/faenza/folder_tar16x16.png"));
		icons.put(Chart.class, createImage("icons/faenza/emblem-marketing16x16.png"));
		icons.put(Plot.class, createImage("icons/faenza/emblem-marketing16x16.png"));
	}

	public static Image createImage(String path) {
		Bundle bundle = FrameworkUtil.getBundle(IconUtils.class);
		URL url = FileLocator.find(bundle, new Path(path), null); //
		ImageDescriptor imageDcr = ImageDescriptor.createFromURL(url);
		return imageDcr.createImage();
	}

	public static Image getIcon(Class<?> type) {
		Image result = icons.get(type);
		return result != null ? result : UNKNOWN;
	}
}
