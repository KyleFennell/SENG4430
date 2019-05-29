/**
 * Project      : Software Quality Assignment 1
 * Class name   : FogIndex
 * Author(s)    : Sebastian Wallman
 * Date         : 28/03/19
 * Purpose      : This module will form an estimate on how difficult the comments are to read.
 *                This will be done using the fog index for calculating reading difficulty.
 */
package  modules;

import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.ast.CompilationUnit;

import java.util.List;

public class FogIndex implements ModuleInterface
{
    private String[] metrics = {""};
    private String[] details = {""};
    @Override
    public String[] executeModule(SourceRoot sourceRoot)
    {
        double fogIndexValue = 0;
        int numberOfWords = 0;
        int numberOfSentences = 0;
        int numberComplexWords = 0;
        double wordsPerSentence = 0;
        double complexWordRatio = 0;
        // go through the AST looking for comments
        for (CompilationUnit cu : sourceRoot.getCompilationUnits())
        {
            List<Comment> comments = cu.getComments();
            // work out the fog index of each comment
            for (Comment comment : comments)
            {
                String content = comment.getContent();
                // for each comment block
                String[] splitIntoWords = content.split(" ");
                // for each word
                for(String word: splitIntoWords)
                {

                    // brackets and periods are removed to ensure words don't get concatenated
                    word = word.replaceAll("(\\(-\\)|(\\.)|/)", " ");
                    String[] newSentence = word.split(" ");
                    for(String newWord: newSentence)
                    {
                        newWord = newWord.replaceAll("\\n|\\t|[!-\\-]|/-@|[{-}]|[\\[-`]", "");
                        // remove markers extra characters from words (syllable counter only works on alphabetical characters, special characters count as a simple word)
                        newWord = newWord.replaceAll("\"[^A-Za-z]\"", "");
                        if (!word.equalsIgnoreCase("") && !word.equalsIgnoreCase("\n") && !word.equalsIgnoreCase("*"))
                        {
                            if (count(newWord) >= 3)
                            {
                                numberComplexWords++;
                            }
                            numberOfWords++;
                        }
                    }
                }
                // find the total number of sentences (split on lines ending with a full stop or new line character
                String[] sentences = content.split("(\\.\n)|(\\. )|(\\.\t)|(\\n)");
                for(String e: sentences)
                {
                    // ignore sections that don't have any relevance (asterisks for formatting, single letter comments
                    if(e.length() < 2)
                    {
                        continue;
                    }
//                    System.out.println("New Sentence: " + e);
                    numberOfSentences++;
                }
                // do fog index calculation
                complexWordRatio = ((double)numberComplexWords / (double)numberOfWords) * 100;
                wordsPerSentence = (double) numberOfWords / numberOfSentences;
                fogIndexValue = 0.4 * (wordsPerSentence + complexWordRatio);
            }
        }
        details[0] =   "Average complexity of the comments.\n" +
                    "Fog index of comments: " + fogIndexValue + ".\n" +
                    "There are " + numberOfWords + " words in the comments.\n" +
                    numberComplexWords + " are complex words (made up of more than 3 syllables.)\n" +
                    "There are a total of " + numberOfSentences + " sentences that make up the comments.";


         String highIndex =  "A Fog index of over 10 indicates that the comments are complex in nature. " +
                             "\nOne reason could be that the comments are attached to advanced control logic and a detailed explanation is required."+
                             "\nAnother reason could be that the method is being used to do too much at once." +
                             "Both of these could be resolved by refactoring some logic into a sub-method.";
         String lowIndex =  "A Fog index of 9 or lower indicates that the comments are simple in nature." +
                            "\nThis can indicate that the methods are fairly simple or coded in such a way that little explanation is required" +
                            "\nIt is also possible that the methods have few comments. It is advisable to include comments to clarify complicated logic" +
                            " or for justification of a portion of code.";
         String noWords =   "There were no words detected in the commented sections. This could indicate a trivial class. If the class is not trivial" +
                            "\nit may need to be reviewed.";


        String[] fogIndexSummary = new String[2];
        fogIndexSummary[0] = "Fog Index value is: " + fogIndexValue;
        if(fogIndexValue >=10 && fogIndexValue < 20)
        {
            fogIndexSummary[1] = highIndex;
        }
        else if(fogIndexValue < 10 && fogIndexValue >= 0)
        {
            fogIndexSummary[1] = lowIndex;
        }
        else if(numberOfWords == 0)
        {
            fogIndexSummary[1] = noWords;
        }
        else
        {
            fogIndexSummary[1] = "There was a problem when running the Fog Index module, no words were counted";
        }

        metrics = fogIndexSummary;
        return details;
    }

    // Syllable counter was sourced from <https://gist.github.com/jbertouch/5f19ed775b5064b7a197cb2c9017ce52>
    private String[] addSyllableArray = { "ia", "riet", "dien", "iu", "io", "ii", "[aeiouym]bl$", "[aeiou]{3}", "^mc", "ism$",
            "[^aeiouy][^aeiouy]l$", "[^l]lien", "^coa[dglx].", "[^gq]ua[^auieo]", "dnt$" };
    private String[] subtractSyllableArray = { "cial", "tia", "cius", "cious", "giu", "ion", "iou", "sia$", ".ely$" };

    public int count(String string) {
        string = string.toLowerCase();
        string = string.replaceAll("'", " ");

        if (string.equals("i"))
            return 1;
        if (string.equals("a"))
            return 1;

        if (string.endsWith("e")) {
            string = string.substring(0, string.length() - 1);
        }

        String[] phonemes = string.split("[^aeiouy]+");

        int syllableCount = 0;
        for (int i = 0; i < subtractSyllableArray.length; i++) {
            String syllable = subtractSyllableArray[i];
            if (string.matches(syllable)) {
                syllableCount--;
            }
        }
        for (int i = 0; i < addSyllableArray.length; i++) {
            String syllable = addSyllableArray[i];
            if (string.matches(syllable)) {
                syllableCount++;
            }
        }
        if (string.length() == 1) {
            syllableCount++;
        }

        for (int i = 0; i < phonemes.length; i++) {
            if (phonemes[i].length() > 0)
                syllableCount++;
        }

        if (syllableCount == 0) {
            syllableCount = 1;
        }

        return syllableCount;
    }

    @Override
    public String printMetrics()
    {
        if(metrics[0].equals(""))
        {
            metrics = new String[2];
            metrics[0] = "There was an running the Fog Index module";
            metrics[1] = "";
        }
        return metrics[0].concat("\n" + metrics[1]);
    }

    @Override
    public String getDescription()
    {
        return  "This module will form an estimate on how difficult the comments are to read.\n" +
                "This will be done using the fog index for calculating reading difficulty.";
    }

    @Override
    public String getName()
    {
        return  "FogIndex";
    }
}