import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import modules.LengthOfCode;
import modules.ModuleInterface;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Project          : Software Quality Assignment 1
 * Class name       : ConsoleInterface
 * Author(s)        : Kyle Fennell
 * Contributor(s)   : Ben Collins
 * Date             : 28/03/19
 * Purpose          : This is the interface between the terminal and the program.
 *      It is essentially a command line parser but also controls the flow of
 *      the program due to Command objects being executed.
 */

public class ConsoleInterface{

    private List<Command> m_commands = new ArrayList<>();
    private static SourceRoot sourceRoot;

    /**
     * Class constructor
     */
    public ConsoleInterface(){
        initCommands();
    }

    /**
     * Waits for a command to be typed then locates the respective Command object
     * and executes it with the following words as the arguments to the command.
     */
    public void run(){
        registerModules();

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

    /**
     * Register all Modules available to the ModuleManager
     */
    private void registerModules(){
        ModuleInterface lengthOfCode = new LengthOfCode();
        ModuleManager.registerModule(lengthOfCode);
    }

    /**
     * @param command full string containing the command followed by the arguements
     * @return null on failure, "" on success
     */
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
        Logger.log("command not found. try 'help's");
        return null;
    }

    /**
     * @param name of the command requested
     * @return Command object that matches @param name
     *      null of failure to find command
     */
    private Command getCommandByName(String name){
        for (Command c : m_commands){
            if (c.getCommandName().toLowerCase().equals(name.toLowerCase())){
                return c;
            }
        }
        return null;
    }

    /** Lists all commands and their arguement format
     * @return string of all commands
     */
    private String listCommands(){
        String out = "";
        for (Command c : m_commands){
            out += c.getUsage() + "\n";
        }
        return out;
    }

    /**
     * Creates all Command objects and adds them to m_commands
     */
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
                if (args.length < 1 || args.length > 2) {
                    Logger.error("Command 'List' - Expects 1-2 argument. Received " + args.length);
                    return null;
                }

                switch(args[0].toLowerCase()) {
                    case "modules":
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
                "load path",
                "loads the files in the program path",
                "<folderPath>") {
            @Override
            public String execute(String[] args) {
                Logger.debug("Executing Load Path Command");

                if (args.length != 1) {
                    Logger.error("Command 'Load' - Expects 1 argument. Received " + args.length);
                    return null;
                }

                sourceRoot = new SourceRoot(Paths.get(args[0]));

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

        m_commands.add(new Command(
                "run",
                "run the loaded modules",
                "<>"){
            @Override
            public String execute(String[] args){
                if (args.length != 0) {
                    Logger.error("Command 'Run' - Expects no arguments. Received " + args.length);
                    return null;
                }

                if (sourceRoot == null) {
                    Logger.error("Command 'Run' - Requires valid code files.");
                    return null;
                }

                try {
                    List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse();
                } catch (IOException e) {
                    Logger.error("Error occurred while attempting to parse files. " +
                            "Please try again with valid source files");
                }

                List<ModuleInterface> loadedModules = ModuleManager.getLoadedModules();

                List<String[]> allResults = new ArrayList<>();
                List<String> allMetrics = new ArrayList<>();

                for (ModuleInterface module : loadedModules) {
                    allResults.add(module.executeModule(sourceRoot));
                    allMetrics.add(module.printMetrics());
                }

                //TODO: Whatever is to be done with output
                for (String[] result : allResults) {
                    for (String element : result) {
                        System.out.print(element + " ");
                    }
                    System.out.println();
                }

                return "";
            }
        });
    }

    /**
     * Compares 2 string arrays and if one matches all the elements up to its length
     * in the other then they are deemed equal.
     * e.g. arrayEquals([cat, dog, yes], [cat, dog]) returns 2
     * @param in1 first string array
     * @param in2 second string array
     * @return the length of the shorter string on success
     *      -1 on failure
     */
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

    /**
     * toString for String arrays
     * @param arr the array to print
     * @return "[ elem1, elem2, ... ]"
     */
    private String printArray(String[] arr){
        String output = "[ ";
        for (String s : arr){
            output += s + " ";
        }
        return output + "]";
    }

    /**
     * @param in array being split
     * @param index to split on
     * @return the elements of the array after the index,
     *      empty array if index is greater than length
     */
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
    /**
     * Project      : Software Quality Assignment 1
     * Class name   : Command
     * Author(s)    : Kyle Fennell
     * Date         : 28/03/19
     * Purpose      : Container class + execution method
     */
    private abstract class Command{

        private String m_commandName;
        private String m_help;
        private String m_format;

        /**
         * @param commandName Name of the command
         * @param help string explaining command function
         * @param format the argument format expected/required
         */
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

        /**
         * override this command on instantiation with logic that will be
         * executed by the command.
         * @param params arguments to be fed into the command
         * @return null on failure
         */
        //override this on construction
        public abstract String execute(String[] params);
    }
}