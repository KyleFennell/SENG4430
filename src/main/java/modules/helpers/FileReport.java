package modules.helpers;

import java.nio.file.Path;

public class FileReport {
	private final String fileName;
	private final Path fileLocation;
	private Analysis[] analyses;

	public FileReport(String fileName, Path filePath) {
		this.fileName = fileName;
		this.fileLocation = filePath;
	}


	public String getFileName() {
		return fileName;
	}


	public Path getFileLocation() {
		return fileLocation;
	}


	public Analysis[] getAnalyses() {
		return analyses;
	}


	public void setAnalyses(Analysis[] analyses) {
		this.analyses = analyses;
	}

}
