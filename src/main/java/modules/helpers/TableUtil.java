package modules.helpers;

import com.mitchtalmadge.asciidata.table.ASCIITable;
import com.mitchtalmadge.asciidata.table.formats.ASCIITableFormat;

import java.util.ArrayList;
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
	public static String metricTablePrint(List<FileReport> res, String[] headers) {
		String[][] data = new String[res.size()][];

		for (int i = 0; i < res.size(); i++) {
			FileReport f = res.get(i);
			String[] row = new String[f.getAnalyses().length+3]; // +3 for ClassName, Sum, and FileScore
			row[0] = f.getFileName();
			row[1] = String.valueOf(f.getSum());

			// Collecting each file's analysis result. i.e. 'to-do, copyright, l_commentSeg'
			for (int j = 0; j < f.getAnalyses().length; j++) {
				row[j + 2] = String.format("%.2f", (f.getAnalyses()[j].getOptimalValue()));
			}
			row[row.length-1] = String.format("%.2f", f.calcFileScore() * 100) + "%";
			data[i] = row; // Add row to table
		}
		return ASCIITable.fromData(headers, data).withTableFormat(new ASCIITableFormat()).toString();
	}


	public static String printWarningsTable(List<FileReport> res) {
		String[] headers = { "WarningFrom", "File", "Line", "Reason", "Solution" };
		String[] whichHeading = { "todo", "copyright", "l_commentSeg", "javadoc" };

		// TODO This is so messy
		List<String[]> rows = new ArrayList<>();
		for (FileReport f : res) {                                  // Iterate over each file
			for (int i = 0; i < f.getAnalyses().length; i++) {      // Iterate over each Analysis done on a file
				Analysis a = f.getAnalyses()[i];
				for (Warning w : a.getWarnings()) {                 // Iterate over each warning an analysis produced
					rows.add(new String[] { whichHeading[i], f.getFileName(),
							String.valueOf(w.lineOrigin.begin.line),
							w.cause.toString(),
							w.recommendedFix.toString() });
				}
			}
		}
		String[][] data = new String[rows.size()][];
		for (int i = 0; i < rows.size(); i++)
			data[i] = rows.get(i);
		return ASCIITable.fromData(headers, data).withTableFormat(new ASCIITableFormat()).toString();
	}


}
