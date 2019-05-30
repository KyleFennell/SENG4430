package modules;

import java.util.Map;
import utils.Adjustment;

/** Template Interface used to discern if a module is adjustable.
 * 
 * <p>Project          : Software Quality Assignment 1<br>
 *    Date             : 27/05/19</p>
 * 
 * @author Nicolas Klenert
 * @see utils.Adjustment
 * @see CyclomaticComplexity
 * @see NumberOfPaths
 * 
 */
public interface AdjustableModuleInterface extends ModuleInterface{
    
    /** Method used to give the module it's settings.
     * 
     * @param setting the object which contains all adjustments of the user.
     */
    void setAdjustments(Adjustment setting);
    
    /** Returns a map signalising both, the only keys the module will ask for and their default value.
     * 
     * @return (key,default value) pairs
     */
    Map<String,String> getDefaults();
    
}
