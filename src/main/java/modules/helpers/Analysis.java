package modules.helpers;

import com.github.javaparser.Range;
import com.mitchtalmadge.asciidata.table.ASCIITable;
import com.mitchtalmadge.asciidata.table.formats.ASCIITableFormat;

import java.util.ArrayList;
import java.util.List;

public class Analysis {
	private List<Warning> warnings;
	private double optimalValue;

	public Analysis() {
		setWarnings(new ArrayList<>());
	}

	public String asPercentage() {
		return String.format("%.2f", getOptimalValue() *100) + "%";
	}

	public String printWarnings() {
		if (!getWarnings().isEmpty()) {
			String[] headers = { "Line", "Cause", "Fix" };
			String[][] data = new String[getWarnings().size()][];

			for (int i = 0; i < getWarnings().size(); i++) {
				Warning w = getWarnings().get(i);
				String[] row = { String.valueOf(w.lineOrigin.begin.line), w.cause.toString(), w.recommendedFix.toString() };
				data[i] = row;
			}

			return ASCIITable.fromData(headers, data).withTableFormat(new ASCIITableFormat()).toString();
		}
		return "No Warnings";
	}

	public void addWarning(Warning warning) {
		warnings.add(warning);
	}

	public void addWarning(String cause, String recommendation, Range origin) {
		warnings.add(new Warning<>(cause, recommendation, origin));
	}

	public List<Warning> getWarnings() { return warnings; }
	public void setWarnings(List<Warning> warnings) { this.warnings = warnings; }

	public double getOptimalValue() { return optimalValue; }
	public void setOptimalValue(double optimalValue) { this.optimalValue = optimalValue; }

}
