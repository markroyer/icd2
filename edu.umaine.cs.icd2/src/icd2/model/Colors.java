/**
 * 
 */
package icd2.model;

import java.awt.Color;
import java.awt.Paint;

/**
 * This class is originally based off of the JFreeChart class ChartColor.
 * .
 * 
 * @author Mark Royer
 *
 */
public class Colors {

	/** A very dark red color. */
	public static final Color VERY_DARK_RED = new Color(0x80, 0x00, 0x00);

	/** A dark red color. */
	public static final Color DARK_RED = new Color(0xc0, 0x00, 0x00);

	/** A light red color. */
	public static final Color LIGHT_RED = new Color(0xFF, 0x40, 0x40);

	/** A lighter red color. */
	public static final Color LIGHTER_RED = new Color(0xFF, 0x55, 0x55);

	/** A very light red color. */
	public static final Color VERY_LIGHT_RED = new Color(0xFF, 0x80, 0x80);

	/** A very dark yellow color. */
	public static final Color VERY_DARK_YELLOW = new Color(0x80, 0x80, 0x00);

	/** A dark yellow color. */
	public static final Color DARK_YELLOW = new Color(0xC0, 0xC0, 0x00);

	/** A light yellow color. */
	public static final Color LIGHT_YELLOW = new Color(0xFF, 0xFF, 0x40);

	/** A lighter yellow color. */
	public static final Color LIGHTER_YELLOW = new Color(0xFF, 0xFF, 0x55);

	/** A very light yellow color. */
	public static final Color VERY_LIGHT_YELLOW = new Color(0xFF, 0xFF, 0x80);

	/** A very dark green color. */
	public static final Color VERY_DARK_GREEN = new Color(0x00, 0x80, 0x00);

	/** A dark green color. */
	public static final Color DARK_GREEN = new Color(0x00, 0xC0, 0x00);

	/** A light green color. */
	public static final Color LIGHT_GREEN = new Color(0x40, 0xFF, 0x40);

	/** A lighter green color. */
	public static final Color LIGHTER_GREEN = new Color(0x55, 0xFF, 0x55);

	/** A very light green color. */
	public static final Color VERY_LIGHT_GREEN = new Color(0x80, 0xFF, 0x80);

	/** A very dark cyan color. */
	public static final Color VERY_DARK_CYAN = new Color(0x00, 0x80, 0x80);

	/** A dark cyan color. */
	public static final Color DARK_CYAN = new Color(0x00, 0xC0, 0xC0);

	/** A light cyan color. */
	public static final Color LIGHT_CYAN = new Color(0x40, 0xFF, 0xFF);

	/** A lighter cyan color. */
	public static final Color LIGHTER_CYAN = new Color(0x55, 0xFF, 0xFF);

	/** Aa very light cyan color. */
	public static final Color VERY_LIGHT_CYAN = new Color(0x80, 0xFF, 0xFF);

	/** A very dark blue color. */
	public static final Color VERY_DARK_BLUE = new Color(0x00, 0x00, 0x80);

	/** A dark blue color. */
	public static final Color DARK_BLUE = new Color(0x00, 0x00, 0xC0);

	/** A light blue color. */
	public static final Color LIGHT_BLUE = new Color(0x40, 0x40, 0xFF);

	/** A lighter blue color. */
	public static final Color LIGHTER_BLUE = new Color(0x55, 0x55, 0xFF);

	/** A very light blue color. */
	public static final Color VERY_LIGHT_BLUE = new Color(0x80, 0x80, 0xFF);

	/** A very dark magenta/purple color. */
	public static final Color VERY_DARK_MAGENTA = new Color(0x80, 0x00, 0x80);

	/** A dark magenta color. */
	public static final Color DARK_MAGENTA = new Color(0xC0, 0x00, 0xC0);

	/** A light magenta color. */
	public static final Color LIGHT_MAGENTA = new Color(0xFF, 0x40, 0xFF);

	/** A lighter magenta color. */
	public static final Color LIGHTER_MAGENTA = new Color(0xFF, 0x55, 0xFF);

	/** A very light magenta color. */
	public static final Color VERY_LIGHT_MAGENTA = new Color(0xFF, 0x80, 0xFF);

	private static int lastColor = -1;

	private static Paint[] paints;
	
	public static Paint getNextColor() {
		if (paints == null)
			paints = createDefaultPaintArray();
		
		return paints[++lastColor];
	}
	
	/**
	 * Convenience method to return an array of <code>Paint</code> objects that
	 * represent the pre-defined colors in the <code>Color</code> and
	 * <code>ChartColor</code> objects.
	 *
	 * @return An array of objects with the <code>Paint</code> interface.
	 */
	public static Paint[] createDefaultPaintArray() {

		return new Paint[] { LIGHTER_RED, LIGHTER_BLUE, LIGHTER_GREEN, LIGHTER_YELLOW, LIGHTER_MAGENTA, LIGHTER_CYAN,
				Color.PINK, Color.GRAY, DARK_RED, DARK_BLUE, DARK_GREEN, DARK_YELLOW, DARK_MAGENTA, DARK_CYAN,
				Color.darkGray, LIGHT_RED, LIGHT_BLUE, LIGHT_GREEN, LIGHT_YELLOW, LIGHT_MAGENTA, LIGHT_CYAN,
				Color.lightGray, VERY_DARK_RED, VERY_DARK_BLUE, VERY_DARK_GREEN, VERY_DARK_YELLOW, VERY_DARK_MAGENTA,
				VERY_DARK_CYAN, VERY_LIGHT_RED, VERY_LIGHT_BLUE, VERY_LIGHT_GREEN, VERY_LIGHT_YELLOW,
				VERY_LIGHT_MAGENTA, VERY_LIGHT_CYAN };
	}

}
