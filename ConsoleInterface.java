import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleInterface{

    private List<Command> m_commands = new ArrayList<>();

    public ConsoleInterface(){
        initCommands();
    }

    public void run(){
        Scanner input = new Scanner(System.in);
        String line = "";
        while (true){
            System.out.println("Enter next command: ");
            line = input.nextLine();
            String retVal = parseCommand(line);
            if (retVal == null){
                Logger.warning("command returned null");
            }
        }
    }

    private String parseCommand(String command){
        Logger.debug("parse command: '" + command + "'");
        if (command.equals("")){
            return command;
        }
        String[] inputWords = command.split(" ");
        for (Command c : m_commands){
            int argsIndex = arrayEquals(c.getCommandName().split(" "), inputWords);
            if (argsIndex != -1){
                String[] args = splitArray(inputWords, argsIndex);
                c.execute(args);
                return "";
            }
        }
        Logger.warning("command not found");
        return null;
    }

    private Command getCommandByName(String name){
        for (Command c : m_commands){
            if (c.getCommandName().toLowerCase().equals(name.toLowerCase())){
                return c;
            }
        }
        return null;
    }

    private String listCommands(){
        String out = "";
        for (Command c : m_commands){
            out += c.getUsage() + "\n";
        }
        return out;
    }

    private void initCommands(){

        m_commands.add(new Command(
                "exit",
                "terminates the program",
                "N\\A"){
            @Override
            public String execute(String[] args){
                System.exit(0);
                return null;
            }
        });

        m_commands.add(new Command(
                "help",
                "displays help for a command",
                "<command>"){
            @Override
            public String execute(String[] args){
                if (args.length == 0){
                    for (Command c : m_commands){
                        System.out.println(c.getHelp());
                    }
                    return "";
                }
                for (Command c : m_commands){
                    int argsIndex = arrayEquals(c.getCommandName().split(" "), args);
                    if (argsIndex == -1){
                        Logger.warning("command not found");
                        return null;
                    }
                    System.out.println(c.getHelp());
                    return "";
                }
                return null;	//unreachable
            }
        });

        m_commands.add(new Command(
                "list",
                "lists all the available modules/commands",
                "<modules|commands|loaded modules>") {
            @Override
            public String execute(String[] args) {
                Logger.debug("Executing List command");
                if (args.length > 2) {
                    Logger.error("Command 'List' - Expects 1-2 argument.");
                    return null;
                }
                switch(args[0].toLowerCase()) {
                    case "module":
                        System.out.println(ModuleManager.listModules());
                        return ModuleManager.listModules();
                    case "commands":
                        System.out.println(listCommands());
                        return listCommands();
                    case "loaded":
                        System.out.println(ModuleManager.listLoadedModules());
                        return ModuleManager.listLoadedModules();
                    default: return null;
                }
            }
        });

        m_commands.add(new Command(
                "load file",
                "loads a file to the program",
                "<filePath>") {
            @Override
            public String execute(String[] args) {
                Logger.debug("Executing Load File Command");
                String fileContents = null;

                if (args.length != 1) {
                    Logger.error("Command 'Load' - Expects 1 argument. Received " + args.length);
                    return null;
                }

                try {
                    Scanner file = new Scanner(new File(args[0]));
                    while (file.hasNextLine()) {
                        fileContents += file.nextLine() + "\n";
                    }
                } catch (FileNotFoundException e) {
                    Logger.error("Command 'Load' - File not found.");
                    return null;
                }
                if (fileContents == null) {
                    Logger.warning("Command 'Load' - The file has no contents.");
                }
                return "";
            }
        });

        m_commands.add(new Command(
                "load modules",
                "loads modules to be run",
                "<module1, module2, ...>"){
            @Override
            public String execute(String[] args){
                String modulesLoaded = "";
                for (String m : args){
                    if (ModuleManager.loadModule(m)) {
                        modulesLoaded += m + " ";
                        Logger.log(" Module " + m + " successfully loaded");
                    }
                }
                return modulesLoaded;
            }
        });

        m_commands.add(new Command(
                "toggle",
                "toggles the log type to the value",
                "<'log'|'warning'|'error'|'debug'> <'on'|'off'>"){
            @Override
            public String execute(String[] args){
                if (args.length != 2) {
                    Logger.error("Command 'Toggle' - Expects 2 arguments. Received " + args.length);
                    return null;
                }
                Logger.toggle(args[0], args[1]);
                return "";
            }
        });
    }

    private int arrayEquals(String[] in1, String[] in2){
        Logger.debug("comparing " + printArray(in1) + " " + printArray(in2));
        String[] arr1 = in1.length <= in2.length? in1 : in2;
        String[] arr2 = in1.length > in2.length? in1 : in2;
        for (int i = 0; i < arr1.length; i++){
            if (!arr1[i].equals(arr2[i])) {
//				Logger.debug(arr1[i] + " != " + arr2[i]);
                return -1;
            }
        }
        return arr1.length;
    }

    private String printArray(String[] arr){
        String output = "[ ";
        for (String s : arr){
            output += s + " ";
        }
        return output + "]";
    }

    private String[] splitArray(String[] in, int index){
        if (in.length-index < 0){
            Logger.error("index greater than array length");
            return new String[0];
        }
        String[] out = new String[in.length-index];
        for (int i = 0; i < out.length; i++){
            out[i] = in[index+i];
        }
        return out;
    }

    private abstract class Command{

        private String m_commandName;
        private String m_help;
        private String m_format;

        public Command(String commandName, String help, String format){
            m_commandName = commandName;
            m_help = help;
            m_format = format;
        }

        public String getCommandName(){
            return m_commandName;
        }

        // returns 'commandName': 'help'
        public String getHelp(){ return m_commandName + ": " + m_help; }

        // returns 'commandName' 'format'
        public String getUsage(){
            return m_commandName + " " + m_format;
        }

        //override this on construction
        public abstract String execute(String[] params);
    }
}