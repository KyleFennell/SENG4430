import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

    private static List<ModuleInterface> m_modules = new ArrayList<>();
    private static List<ModuleInterface> m_loadedModules = new ArrayList<>();

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

    public static String listModules(){
        String list = "";

        for (ModuleInterface m : m_modules){
            list += m.getName() + ", ";
        }
        list = list.substring(0, list.lastIndexOf(','));

        return list;
    }

    public static String listLoadedModules(){
        String list = "";

        for (ModuleInterface m : m_loadedModules){
            list += m.getName() + ", ";
        }
        list = list.substring(0, list.lastIndexOf(','));

        return list;
    }

    public static ModuleInterface getModuleByName(String name){
        for (ModuleInterface m : m_modules){
            if (m.getName().toLowerCase().equals(name.toLowerCase())){
                return m;
            }
            Logger.warning("No module found matching " + name);
        }
        return null;
    }

    public static boolean loadModule(String m){
        if (m_loadedModules.contains(getModuleByName(m))){
            Logger.log("module " + m + " already loaded");
            return false;
        }
        if (m_loadedModules.add(getModuleByName(m))){
            return true;
        }
        Logger.error("failed to add module " +  m + ". Reason unknown.");
        return false;
    }

    public static boolean clearModules(){
        if (m_loadedModules.isEmpty()){
            Logger.warning("no modules loaded.");
        }
        m_loadedModules.clear();
        Logger.log("all modules unloaded");
        return true;
    }

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
