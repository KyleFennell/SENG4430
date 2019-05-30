package sudoku;

/*
 * SudokuReader.java
 *
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;

/**
 * Reads Sudokus from a file as described on the exercise sheet.
 */
public class SudokuReader {

    /**
     * Reads all Sudokus and returns a list of them.
     */
    public static LinkedList<Sudoku> readSudokusFromFile(String filename) throws ParseException {
        File file = new File(filename);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Es wurde keine Datei des Namens " + filename + " gefunden!");
        }
        BufferedReader reader = null;
        LinkedList<Sudoku> result = new LinkedList<Sudoku>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                lineNumber++;
                if (line.length() != 81) {
                    throw new ParseException(line + " hat nicht die richtige Länge fuer ein 9x9-Sudoku.", lineNumber);
                }
                int[][] grid = new int[9][9];
                for (int i = 0; i < 81; i++) {
                    if (!Character.isDigit(line.charAt(i))) {
                        throw new ParseException(line + " besteht nicht ausschließlich aus Ziffern.", lineNumber);
                    } else {
                        grid[i / 9][i % 9] = Character.getNumericValue(line.charAt(i));
                    }
                }
                Sudoku sudoku = new Sudoku(grid);
                if (sudoku.isGridValid()) {
                    result.add(sudoku);
                } else {
                    throw new ParseException(line + " enthält ein widerspruechliches Sudoku.", lineNumber);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                }
            }
        }
        return result;
    }
}
