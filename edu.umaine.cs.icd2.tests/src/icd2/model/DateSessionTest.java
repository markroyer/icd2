/**
 * 
 */
package icd2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import icd2.model.DateSession.DateSessionException;

/**
 * @author Mark Royer
 *
 */
public class DateSessionTest {

	DateSession dateSession;

	private static final int TOPYEAR = 2016;
	
	@Before
	public void setUp() {
		dateSession = new DateSession(null, "Test", TOPYEAR);
	}

	@Test
	public void testAddYearDepth() throws DateSessionException {

		int insertSpot = dateSession.insertDepth(.1);
		
		assertEquals(1, insertSpot);
		assertEquals(2016, dateSession.getYear(0));
		assertEquals(2015, dateSession.getYear(1));
		assertEquals(2, dateSession.getSize());
		
		insertSpot = dateSession.insertDepth(1);
		
		assertEquals(2, insertSpot);
		assertEquals(2016, dateSession.getYear(0));
		assertEquals(2015, dateSession.getYear(1));
		assertEquals(2014, dateSession.getYear(2));
		assertEquals(3, dateSession.getSize());
		
		
		// 0 is already dated, so should be ignored
		insertSpot = dateSession.insertDepth(0);
		
		assertEquals(0, insertSpot);
		assertEquals(3, dateSession.getSize());
		
	}
	
	@Test(expected=DateSessionException.class)
	public void testNegativeDepth() throws DateSessionException {
		// Should throw exception since negative depths do not exist in cores
		dateSession.insertDepth(-0.1);
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
	
	@Test
	public void testGetDepthIndex() throws DateSessionException {
		
		assertEquals(0, dateSession.getDepthIndex(0));
		
		dateSession.insertDepth(.1);
		
		assertEquals(0, dateSession.getDepthIndex(0));
		assertEquals(1, dateSession.getDepthIndex(.1));
		
		dateSession.insertDepth(.2);
		
		assertEquals(0, dateSession.getDepthIndex(0));
		assertEquals(1, dateSession.getDepthIndex(.1));
		assertEquals(2, dateSession.getDepthIndex(.2));
		
	}
	
	@Test
	public void testYearIndex() throws DateSessionException {
		
		assertEquals(0, dateSession.yearIndex(TOPYEAR));
		
		dateSession.insertDepth(.1);
		
		assertEquals(0, dateSession.yearIndex(TOPYEAR));
		assertEquals(1, dateSession.yearIndex(TOPYEAR-1));
		
		// Test one that doesn't exist in the date session
		assertEquals(-3, dateSession.yearIndex(TOPYEAR-2));
		
	}

}
