/**
 * Project      : Software Quality Assignment 1
 * Class name   : ModuleInterface
 * Author(s)    : Kyle Fennell
 * Date         : 28/03/19
 * Purpose      : template interface that all modules will implement
 */

public interface ModuleInterface {

    String getName();

    /**
     * @return results of the module being run
     */
    String[] executeModule();

    /**
     * @return a description of what the module is testing
     */
    String getDescription();

    /**
     * @return a formatted string that represents the metrics determined
     *      by the module
     */
    String printMetrics();
}
