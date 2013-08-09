package se.kth.kthfsdashboard.util;

import se.kth.kthfsdashboard.utils.ParseUtils;
import junit.framework.TestCase;

/**
 *
 * @author Hamidreza Afzali <afzali@kth.se>
 */
public class ParserTest extends TestCase {

    public ParserTest(String testName) {
        super(testName);
    }

    public void testParseDouble() throws Exception {
        assertEquals(1d, ParseUtils.parseDouble("0.1E1"));
        assertEquals(12d, ParseUtils.parseDouble("1.2E1"));
        assertEquals(10123456789d, ParseUtils.parseDouble("1.0123456789E10"));

        assertEquals(1123456789d, ParseUtils.parseDouble("1.123456789E9"));
        assertEquals(1123456789d, ParseUtils.parseDouble("1.123456789E09"));
        assertEquals(1123456789d, ParseUtils.parseDouble("1.123456789E+09"));
        assertEquals(1123456789d, ParseUtils.parseDouble("1.123456789e+09"));
        
        assertEquals(123456789d, ParseUtils.parseDouble("0.123456789E+09"));

        assertEquals(0.0000000012345d, ParseUtils.parseDouble("1.2345E-09"));
        assertEquals(0.0000000012345d, ParseUtils.parseDouble("1.2345E-9"));
        assertEquals(0.0000000012345d, ParseUtils.parseDouble("1.2345e-09"));
        
        assertEquals(32980475085d, ParseUtils.parseDouble("3.2980475085E+10"));
        
        
    }
    
        public void testParseLong() throws Exception {

        assertEquals(32980475085L, ParseUtils.parseLong("3.2980475085E+10"));
        
        
        
        
    }
}
