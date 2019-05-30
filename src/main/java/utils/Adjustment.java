package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/** Class to regulate the the settings of each module.
 * 
 * <p>Used to distribute the configurations which will be always up-to-date.
 *  Furthermore it prevents name space collisions and makes sure that there is
 *  always a default value.</p>
 * 
 * <p>Project       : Software Quality Assignment 1<br>
 * Date             : 24/05/19</p>
 * 
 * @author Nicolas Klenert
 * @see modules.AdjustableModuleInterface
 */
public class Adjustment{
    private final String moduleIdentifier;
    private final ExtendedProperties props;
    
    /** Creates the first Adjustment in the system.
     * 
     *  <p>Should be used if the main system should not have a name.</p>
     */
    public Adjustment(){
        this("");
    }
    
    /** Creates a named Adjustment.
     * 
     * <p>Can be used if the main system, holding onto the first instance of this class,
     * should also be named.</p>
     * 
     * @param moduleIdentifier 
     */
    private Adjustment(String moduleIdentifier){
        this.moduleIdentifier = moduleIdentifier;
        props = new ExtendedProperties();
        loadSettings();
    }
    
    /** Should only be used by {@link setModuleAccess}.
     * 
     * @param props the properties which will be hold on to
     * @param moduleIdentifier 
     * @see setModuleAccess
     */
    private Adjustment(ExtendedProperties props, String moduleIdentifier){
        this.props = props;
        this.moduleIdentifier = moduleIdentifier;
    }
    
    private boolean loadSettings(){
        //inputStream is closed after the try statement 
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            props.load(input);
        }catch(IOException exception){
            utils.Logger.warning("The configuration file could not be read!");
            return false;
        }
        return true;
    }
    
    /** Actualise the configuration.
     * 
     * @return true, if the configuration could be reloaded
     */
    public boolean reloadSettings(){
        props.clear();
        return loadSettings();
    }
    
    /** Creates an Adjustment which can access the settings of a module.
     * 
     * @param moduleIdentifier The identifier of the module used in the configuration file
     * @return A new Adjustment with the same settings but other accessibility.
     */
    public Adjustment setModuleAccess(String moduleIdentifier){
        return new Adjustment(props,moduleIdentifier);
    }
    
    /** Saves default values for it's properties. Name spaces apply. 
     * 
     * @param map (key, default value) pair
     */
    public void addDefaults(Map<String,String> map){
        map.forEach((String key, String value) -> {
            props.addDefault(moduleIdentifier+"_"+key, value);
        });
    }
    
    /** Retrieve user configuration or default value if no other configuration was given.
     * 
     * <p>Important: Only configuration which already have a default value can be retrieved!
     *  This is to reduce bug vulnerability.</p>
     * 
     * @param identifier key to use
     * @return value of user or default value mapped to key
     */
    public String getString(String identifier){
        try{
           return props.getProperty(moduleIdentifier+"_"+identifier);
        }catch(IllegalArgumentException exception){
            utils.Logger.error("Module "+moduleIdentifier+" tried to access a property for which it didn't gave a default value!");
            return null;
        }
    }
    
    /** Retrieve user configuration or default value if no other configuration was given.
     * 
     * <p>Important: Only configuration which already have a default value can be retrieved!
     *  This is to reduce bug vulnerability.</p>
     * 
     * @param identifier key to use
     * @return value of user or default value mapped to key
     */
    public int getInt(String identifier){
        try{
            return Integer.parseInt(props.getProperty(moduleIdentifier+"_"+identifier));
        }catch(NumberFormatException exception){
            String tempModuleString = "".equals(moduleIdentifier) ? "" : (" of module " + moduleIdentifier);
            int defaultNumber = 0;
            try{
                 defaultNumber = Integer.parseInt(props.getDefault(moduleIdentifier+"_"+identifier));
            }catch(NumberFormatException error){
                utils.Logger.error("The default value for "+moduleIdentifier+"_"+identifier+ "can NOT interpreted as int!");
            }
            utils.Logger.log("The Setting" + tempModuleString + " with the name "+identifier+
                    " was not an integer. Default number of "+defaultNumber+ " was used.");
            return defaultNumber;
        }catch(IllegalArgumentException exception){
            utils.Logger.error("Module "+moduleIdentifier+" tried to access a property for which it didn't gave a default value!");
            return 0;
        }
    }
}
