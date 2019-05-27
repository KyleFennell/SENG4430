import modules.ModuleInterface;
import utils.Logger;

import java.util.ArrayList;
import java.util.List;
import modules.AdjustableModuleInterface;
import utils.Adjustment;

/**
 * Project          : Software Quality Assignment 1
 * Class name       : ModuleManager
 * Author(s)        : Kyle Fennell
 * Contributor(s)   : Ben Collins, Nicolas Klenert
 * Date             : 28/03/19
 * Purpose          : Manages all modules in the system including getting,
 *                  registering and running them.
 */

public class ModuleManager {

    private static List<ModuleInterface> m_modules = new ArrayList<>();
    private static List<ModuleInterface> m_loadedModules = new ArrayList<>();
    private static Adjustment adjustments = new Adjustment();

    /**
     * Get the list of loaded modules as modules.ModuleInterface Objects
     * @return ArrayList of loaded modules
     */
    public static List<ModuleInterface> getLoadedModules() {
        return m_loadedModules;
    }

    /**
     * Notifies the Manager about the module
     * @param module instance of the module to be managed
     * @return
     *      true on success
     *      false if module is already found
     */
    public static boolean registerModule(ModuleInterface module){
        for (ModuleInterface m : m_modules){
            if (m.getName().toLowerCase().equals(module.getName().toLowerCase())){
                Logger.warning("Module " + module.getName() + " is already registered");
                return false;
            }
        }
        m_modules.add(module);
        Logger.log("Module " + module.getName() + " successfully registered");
        return true;
    }

    /**
     * @return list of all modules currently available in the manager
     */
    public static String listModules(){
        String list = "";

        for (ModuleInterface m : m_modules){
            list += m.getName() + ", ";
        }
        list = list.substring(0, list.lastIndexOf(','));

        return list;
    }

    /**
     * @return lists all the active (loaded) modules that will be executed
     *      when run
     */
    public static String listLoadedModules(){
        String list = "";

        if (m_loadedModules.size() == 0) {
            return "No Modules Loaded";
        }

        for (ModuleInterface m : m_loadedModules){
            list += m.getName() + ", ";
        }
        list = list.substring(0, list.lastIndexOf(','));

        return list;
    }

    /**
     * @param name
     * @return the module that matches the name
     */
    public static ModuleInterface getModuleByName(String name){
        for (ModuleInterface m : m_modules){
            if (m.getName().toLowerCase().equals(name.toLowerCase())){
                return m;
            }
        }
        Logger.warning("No module found matching " + name);
        return null;
    }

    /**
     * @param m name of the module to load
     * @return true if successfully added
     */
    public static boolean loadModule(String m){
        ModuleInterface module = getModuleByName(m);
        if (m_loadedModules.contains(module)){
            Logger.log("module " + m + " already loaded");
            return false;
        }
        if (m_loadedModules.add(module)){
            if(module instanceof AdjustableModuleInterface){
                //load default settings
                adjustments.addDefaults(((AdjustableModuleInterface) module).getDefaults());
                //give the module the adjustments
                ((AdjustableModuleInterface) module).setAdjustments(adjustments.setModuleAccess(m));
            }
            return true;
        }
        Logger.error("failed to add module " +  m + ". Reason unknown.");
        return false;
    }

    /**
     * Unloads all modules
     * @return true
     */
    public static boolean clearModules(){
        if (m_loadedModules.isEmpty()){
            Logger.warning("no modules loaded.");
        }
        m_loadedModules.clear();
        Logger.log("all modules unloaded");
        return true;
    }

    /**
     * @param m name of module to unload
     * @return true on success
     *      false if module was not found.
     */
    public static boolean unloadModule(String m){
        if (m_loadedModules.contains(getModuleByName(m))){
            m_loadedModules.remove(getModuleByName(m));
            Logger.log("module " + m + " successfully unloaded");
            return true;
        }
        Logger.warning("module " + m + " not loaded");
        return false;
    }
}
