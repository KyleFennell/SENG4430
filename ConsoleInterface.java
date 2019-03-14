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
				Logger.warn("command returned null");
			}
			else if(retVal.equals("exit")){
				System.exit(0);
			}
		}
	}

	private String parseCommand(String command){
		Logger.debug("parse command: '" + command + "'");
		if (command.equals("exit") || command.equals("")){
			return command;
		}
		String commandName = command;
		String[] params = new String[0];
		if (command.indexOf(' ') != -1){
//			System.out.println("second parameter found");
			commandName = command.substring(0, command.indexOf(' '));
			params = command.substring(command.indexOf(' ')+1).split(" ");
		}

		for (Command c : m_commands){
			if (c.getCommandName().equals(commandName)){
				return c.execute(params);
			}
		}
		Logger.warn("Unknown Command: " + commandName);
		return "";
	}

	private Command getCommandByName(String name){
		for (Command c : m_commands){
			if (c.getCommandName().toLowerCase().equals(name.toLowerCase())){
				return c;
			}
		}
		return null;
	}

	private String listComands(){
		String out = "";
		for (Command c : m_commands){
			out += c.getCommandName() + ": " + c.getUsage() + "\n";
		}
		return out;
	}

	private void initCommands(){
		m_commands.add(new Command(
				"load",
				"loads a file to the program",
				"<filePath>") {
			@Override
			public String execute(String[] args) {
				Logger.debug("Executing Load Command");
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
					Logger.warn("Command 'Load' - The file has no contents.");
				}
				return fileContents;
			}
		});

		m_commands.add(new Command(
				"list",
				"lists all the available modules/commands",
				"<modules|commands>") {
			@Override
			public String execute(String[] args) {
				Logger.debug("Executing List command");
				if (args.length != 1) {
					Logger.error("Command 'List' - Expects 1 argument.");
					return null;
				}
				switch(args[0].toLowerCase()) {
					case "module":
						System.out.println(ModuleManager.listModules());
						return ModuleManager.listModules();
					case "commands":
						System.out.println(listComands());
						return listComands();
					default: return null;
				}
			}
		});

		m_commands.add(new Command(
				"toggle",
				"toggles the log type to the value",
				"<log|warn|error|debug> <on|off>"){
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
				"help",
				"displays help for a command",
				"<command>"){
			@Override
			public String execute(String[] args){
				if (args.length != 1){
					Logger.error("Command 'Help' - Expects 1 argument. Recieved " + args.length);
					return null;
				}
				System.out.println(getCommandByName(args[0]).getHelp());
				return "";
			}
		});
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

		public String getHelp(){
			return m_commandName + ": " + m_help;
		}

		public String getUsage(){
			return m_commandName + " " + m_format;
		}

		//override this on construction
		public abstract String execute(String[] params);
	}

}