public interface ModuleInterface {

    String getName();

    // returns any error information
    String[] executeModule();

    // description of what the module does
    String getDescription();

    // prints the report generated by running the module
    String printMetrics();
}
