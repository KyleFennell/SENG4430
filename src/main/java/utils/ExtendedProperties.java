package utils;

import java.util.Properties;

/** Used to extend the functionality of Properties and to ensure default settings.
 * 
 * <p>Do not use this class directly. Instead use {@link Adjustment}! This
 *  class only exist to get access on the default values of {@code Properties}</p>
 * 
 * <p>Project       : Software Quality Assignment 1<br>
 * Date             : 24/05/19</p>
 * 
 * @author Nicolas Klenert
 * @see Adjustment
 */
public class ExtendedProperties extends Properties{
    
    private static final long serialVersionUID = 122793759L;
    
    public ExtendedProperties(){
        super();
        this.defaults = new Properties();
    }
    
    public void addDefault(String key,String value){
        this.defaults.setProperty(key, value);
    }

    @Override
    public String getProperty(String key) throws IllegalArgumentException{
        if(!this.defaults.containsKey(key)){
            throw new IllegalArgumentException("Only Properties which have defaults are allowed!");
        }
        return super.getProperty(key);
    }
    
    public String getDefault(String key){
        return this.defaults.getProperty(key);
    }
    
    protected String getDefaultKeys(){
        return this.defaults.keySet().toString();
    }
    
}
