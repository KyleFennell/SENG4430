package modules.helpers;

import com.mitchtalmadge.asciidata.table.ASCIITable;
import com.mitchtalmadge.asciidata.table.formats.ASCIITableFormat;

import java.util.List;

/**
 * Project          :   Software Quality Assignment
 * Class name       :   TableUtil.java
 * Author(s)        :   John Barr
 * Date Created     :   29/05/19
 * Purpose          :   A utility class that helps with outputting data from Analysis in a tabular fashion.
 */
public class TableUtil {

	private TableUtil() { throw new IllegalStateException("Utility class"); }

	/**
	 * Using the library ASCIITable, developed by Mitch Talmadge, this function will output the following in tabular form:
	 * Each {@code Analysis} object in the parameter {@code List<FileReport>} will act a column header.
	 * Each file that was analysed will be the row header containing each file's {@code optimalValue} between 0-1;
	 * rounded to 2 decimal places.
	 *
	 * It should be noted that at this current point in time, each column header will need to be explicitly typed here.
	 * Similarly,the {@code headers} size must match the size of each {@code res.analyses.length},
	 * otherwise an Exception will be thrown by ASCIITable. TODO: Helper that does this automatically.
	 *
	 * @param res A List of FileReport objects that were analysed via the {@code analyse()} function.
	 * @return The entire table containing analysis results as a String
	 */
	public static String fileReportsToTable(List<FileReport> res, String[] headers) {
		String[][] data = new String[res.size()][];

		for (int i = 0; i < res.size(); i++) {
			FileReport f = res.get(i);
			String[] row = new String[f.getAnalyses().length+1];
			row[0] = f.getFileName();

			// Collecting each file's analysis result. i.e. 'to-do, copyright, l_commentSeg'
			for (int j = 0; j < f.getAnalyses().length; j++) {
				row[j+1] = String.format("%.2f", (f.getAnalyses()[j].getOptimalValue()));
			}
			data[i] = row; // Add row to table
		}
		return ASCIITable.fromData(headers, data).withTableFormat(new ASCIITableFormat()).toString();
	}


}
