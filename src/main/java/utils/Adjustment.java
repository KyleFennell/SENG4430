package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Project          : Software Quality Assignment 1
 * Class name       : Adjustment
 * @Author(s)       : Nicolas Klenert
 * Date             : 27/05/19
 * Purpose          : Class to regulate the the settings of each module.
 */
public class Adjustment{
    private final String moduleIdentifier;
    private final ExtendedProperties props;
    
    public Adjustment(){
        this("");
    }
    
    private Adjustment(String moduleIdentifier){
        this.moduleIdentifier = moduleIdentifier;
        props = new ExtendedProperties();
        try{
            InputStream input = getClass().getResourceAsStream("/config.properties");
            props.load(input);
        }catch(IOException exception){
            utils.Logger.warning("The configuration file could not be read!");
        }
    }
    
    private Adjustment(ExtendedProperties props, String moduleIdentifier){
        this.props = props;
        this.moduleIdentifier = moduleIdentifier;
    }
    
    /** Creates an Adjustment which can access the settings of a module.
     * 
     * @param moduleIdentifier The identifier of the module used in the configuration file
     * @return A new Adjustment with the same settings but other accessibility.
     */
    public Adjustment setModuleAccess(String moduleIdentifier){
        return new Adjustment(props,moduleIdentifier+"_");
    }
    
    public void addDefaults(Map<String,String> map){
        map.forEach((String key, String value) -> {
            props.addDefault(moduleIdentifier+key, value);
        });
    }
    
    public String getString(String identifier){
        try{
           return props.getProperty(moduleIdentifier+identifier);
        }catch(IllegalArgumentException exception){
            utils.Logger.error("Module "+moduleIdentifier+" tried to access a property for which it didn't gave a default value!");
            return null;
        }
    }
    
    public int getInt(String identifier){
        try{
            return Integer.parseInt(props.getProperty(moduleIdentifier+identifier));
        }catch(NumberFormatException exception){
            String tempModuleString = "".equals(moduleIdentifier) ? "" : (" of module " + moduleIdentifier);
            int defaultNumber = 0;
            try{
                 defaultNumber = Integer.parseInt(props.getDefault(moduleIdentifier+identifier));
            }catch(NumberFormatException error){
                utils.Logger.error("The default value for "+moduleIdentifier+identifier+ "can NOT interpreted as int!");
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
