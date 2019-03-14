import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

    private static List<ModuleInterface> modules = new ArrayList<>();

    public static boolean registerModule(ModuleInterface module){
        for (ModuleInterface m : modules){
            if (m.getName().toLowerCase().equals(module.getName().toLowerCase())){
                return false;
            }
        }
        modules.add(module);
        return true;
    }

    public static String listModules(){
        String list = "";

        for (ModuleInterface m : modules){
            list += m.getName() + ", ";
        }
        list = list.substring(0, list.lastIndexOf(','));

        return list;
    }

    public static ModuleInterface getModuleByName(String name){
        for (ModuleInterface m : modules){
            if (m.getName().toLowerCase().equals(name.toLowerCase())){
                return m;
            }
        }
        return null;
    }
}
