/**
 * Project      : Software Quality Assignment 1
 * Class name   : FogIndex
 * Author(s)    : Sebastian Wallman
 * Date         : 28/03/19
 * Purpose      : This module will form an estimate on how difficult the comments are to read.
 *                This will be done using the fog index for calculating reading difficulty.
 *                Possible expansions ideas:
 *                - Evaluate how each type of comment affects the fog index.
 *                      Do block comments help? or do they indicate a complex section that could be simplified?
 *                      How do Orphan comments (defined by javaparser) affect the code?
 *                      do more single line comments mean clearer code, or are they an indicator that it needs to be clarified?
 *                - accounting for acronyms as they are often few syllables but may cause confusion.
 *                - accounting for some technical jargon going to adversely affect the fog index calculation
 *                - accounting for common multiple syllable words that are easy to read register as false positive difficult words (eg: Watermelon, Helicopter)
*                //todo: move the
 *                -//todo: potential dictionary of familiar words that can be ignored in future runs because everyone know what they are (add the to dictionary that ignores them)
 * Example      : Logger.log("this is a logged message");
 */
package  modules;

import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.ast.CompilationUnit;

import java.util.List;

public class FogIndex implements ModuleInterface
{

    @Override
    public String[] executeModule(SourceRoot sourceRoot)
    {
        // TODO: test whether this works
        double fogIndexValue = 0;
        // go through the AST looking for comments
        for (CompilationUnit cu : sourceRoot.getCompilationUnits())
        {
            List<Comment> comments = cu.getComments();
            // work out the fog index of each comment
            for (Comment comment : comments)
            {
                String content = comment.getContent();
                System.out.println(content);
                fogIndexValue += count(content);
            }
        }

        String[] fixThisLater = {"Fog Index value is: " + fogIndexValue};
        return fixThisLater;
    }

    // TODO: integrate this
    // https://gist.github.com/jbertouch/5f19ed775b5064b7a197cb2c9017ce52
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
        return "";
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