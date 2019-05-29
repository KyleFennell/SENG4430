package modules.helpers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;

/**
 * Project          : Software Quality Assignment
 * Class name       : LengthHelperTest
 * Author(s)        : Ben Collins
 * Date Created     : 15/05/19
 * Purpose          : This class is used to test the LengthHelper class
 *                    of the helpers package
 */
public class LengthHelperTest {

    @Test
    public void limitMessageExtensionLow() {
        String[] results = LengthHelper.limitMessageExtension(15, 10, 20, 30);
        String[] expected = new String[] {"10", "may need reviewing"};
        assertArrayEquals(expected, results);
    }

    @Test
    public void limitMessageExtensionMed() {
        String[] results = LengthHelper.limitMessageExtension(25, 10, 20, 30);
        String[] expected = new String[] {"20", "should be reviewed"};
        assertArrayEquals(expected, results);
    }

    @Test
    public void limitMessageExtensionHigh() {
        String[] results = LengthHelper.limitMessageExtension(35, 10, 20, 30);
        String[] expected = new String[] {"30", "should be refactored"};
        assertArrayEquals(expected, results);
    }
}