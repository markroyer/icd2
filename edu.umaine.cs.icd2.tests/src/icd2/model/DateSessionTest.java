/**
 * 
 */
package icd2.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

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
	public void testAddYearDepth() {

		dateSession.addYearDepth(2015, .1);
		
		assertEquals(2016, dateSession.getYear(0));
		assertEquals(2015, dateSession.getYear(1));

	}

}
