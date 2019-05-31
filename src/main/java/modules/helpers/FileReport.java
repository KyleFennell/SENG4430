package modules.helpers;

import java.nio.file.Path;

/**
 * Project          :   Software Quality Assignment
 * Class name       :   FileReport.java
 * Author(s)        :   John Barr
 * Date Created     :   23/05/19
 * Purpose          :   This class is a Wrapper for required information about a file.
 */
public class FileReport {
	private final String fileName;
	private final Path fileLocation;
	private double fileScore;
	private int sum;
	private Analysis[] analyses;

	public FileReport(String fileName, Path filePath) {
		this.fileName = fileName;
		this.fileLocation = filePath;
		this.fileScore = 0;
		this.sum = 0;
	}


	public double calcFileScore() {
		double score = 0.0;
		for (Analysis a : analyses) {
			score += a.getOptimalValue();
		}
		this.fileScore = score;
		return score / analyses.length;
	}


	public double getFileScore()                    { return fileScore; }
	public void setFileScore(double fileScore)      { this.fileScore = fileScore; }

	public int getSum()                             { return sum; }
	public void setSum(int sum)                     { this.sum = sum; }

	public String getFileName()                     { return fileName; }
	public Path getFileLocation()                   { return fileLocation; }

	public Analysis[] getAnalyses()                 { return analyses; }
	public void setAnalyses(Analysis[] analyses)    { this.analyses = analyses; }

}
