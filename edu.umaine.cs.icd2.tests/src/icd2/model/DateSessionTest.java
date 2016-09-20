/**
 * 
 */
package icd2.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import icd2.model.DateSession.DateSessionException;

/**
 * @author Mark Royer
 *
 */
public class DateSessionTest {

	DateSession dateSession;

	@Before
	public void setUp() {
		dateSession = new DateSession(null, "Test", 2016);
	}

	@Test
	public void testAddYearDepth() throws IllegalAccessException {

		dateSession.insertDepth(.1);
		
		assertEquals(2016, dateSession.getYear(0));
		assertEquals(2015, dateSession.getYear(1));

	}
	
	@Test(expected=DateSessionException.class)
	public void testRemoveYearDepthTopYear() throws DateSessionException {
		// Remove the core top depth aka the first depth
		dateSession.removeYearDepth(2016);
	}
	
	@Test
	public void testRemoveYearDepth() throws Exception {
		
		// Insert and remove 1
		dateSession.insertDepth(.1);
		dateSession.removeYearDepth(2015);
		assertEquals(2016, dateSession.getYear(0));
	}

}
