/**
 * 
 */
package icd2.model;

/**
 * @author Mark Royer
 *
 */
public interface PlotParent<T extends ModelObject<? super T, ?>, P extends ModelObject<?, ?>>
		extends ModelObject<T, P> {

}
