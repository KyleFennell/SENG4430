package modules;

import java.util.Map;
import utils.Adjustment;

/**
 * Project          : Software Quality Assignment 1
 * Class name       : AdjustableModuleInterface
 * @Author(s)       : Nicolas Klenert
 * Date             : 27/05/19
 * Purpose          : Template Interface used to discern if a module is adjustable.
 */
public interface AdjustableModuleInterface extends ModuleInterface{
    
    /** Method used to give the module it's settings.
     * 
     * @param setting the object which contains all adjustments of the user.
     */
    void setAdjustments(Adjustment setting);
    
    Map<String,String> getDefaults();
    
}
