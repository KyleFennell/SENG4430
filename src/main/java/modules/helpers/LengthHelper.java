package modules.helpers;

/**
 * Project          : Software Quality Assignment
 * Class name       : LengthHelper
 * Author(s)        : Ben Collins
 * Date Created     : 15/05/19
 * Purpose          : This is a helper class for the "LengthOf" classes
 */
public class LengthHelper {

    public static String[] limitMessageExtension(int lineCount, int low, int med, int high) {
        String limit = Integer.toString(low);
        String messageExtension = "may need reviewing";
        if (lineCount > med) {
            limit = Integer.toString(med);
            messageExtension = "should be reviewed";
        }
        if (lineCount > high) {
            limit = Integer.toString(high);
            messageExtension = "should be refactored";
        }
        return new String[] {limit, messageExtension};
    }
}
