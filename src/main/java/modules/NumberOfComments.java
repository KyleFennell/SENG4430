package modules;

import com.github.javaparser.utils.SourceRoot;

public class NumberOfComments implements ModuleInterface {

	@Override
	public String getName() {
		return "Number of Comments";
	}
	@Override
	public String getDescription() {
		return "This module has been split into the following sub-modules:\n" +
					   "\t - To-do\n" +
					   "\t - Copyright\n" +
					   "\t - Trivial and Unnecessary\n" +
					   "\t - Surrounded By\n" +
					   "\t - Compliance of Conformance\n" +
					   "\n" +
					   "Each analysis will factor in several different ‘what if’ conditions and edge cases to " +
					   "return a single optimal value";
	}

	@Override
	public String[] executeModule(SourceRoot sourceRoot) {
		return new String[0];
	}


	private double todoSubmetric() {
		return 0.0;
	}
	private double copyrightSubmetric() {
		return 0.0;
	}
	private double TrivialAndUnnecessarySubmetric() {
		return 0.0;
	}
	private double SurroundedBySubmetric() {
		return 0.0;
	}
	private double ComplianceOfConformanceSubmetric() {
		return 0.0;
	}

	@Override
	public String printMetrics() {
		return null;
	}

}
