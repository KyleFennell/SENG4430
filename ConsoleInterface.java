public class ConsoleInterface{
	
	private List<CommandFunction> functions = new ArrayList<>();

	public ConsoleInterface(){



	}

	public void parseCommand(String command){

		List<String> words = command.split(' ');
		

	}

	private class Command{
		
		private String name;
		private CommandFunction function;

		public Command(String commandName, CommandFunction function){
			this.commandName = commandName;
			this.function = function;
		}
		
		public getCommandName(){
			return this.name;
		}

		public void execute(String params){
			function(params);
		}
	}

	// functional interface (lambda)
	public interface CommandFunction{
		public abstract execute(String args);
	}


}