package utils;

import java.util.Map;
import java.util.Properties;

/**
 * Project          : Software Quality Assignment 1
 * Class name       : ExtendedProperties
 * @Author(s)       : Nicolas Klenert
 * Date             : 27/05/19
 * Purpose          : Used to extend the functionality of Properties and to ensure default settings.
 */
public class ExtendedProperties extends Properties{
    
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
    
}
