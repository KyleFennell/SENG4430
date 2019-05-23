package utils;

/**
 * Project      : Software Quality Assignment 1
 * Class name   : utils.Logger
 * Author(s)    : Kyle Fennell
 * Date         : 28/03/19
 * Purpose      : Auto formats logging messages and handles which classes of
 *      message are being shown
 * Example      : utils.Logger.log("this is a logged message");
 */

public class Logger {

    private static boolean LOG_ON = true;
    private static boolean WARN_ON = true;
    private static boolean ERROR_ON = true;
    private static boolean DEBUG_ON = false;

    /**
     * gets information about the context in which the log is called from
     * @return className.lineNumber
     */
    private static String context(){
        StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
        return ste.getClassName() + "." + ste.getLineNumber() + " ";
    }

    /**
     * @param type class of log to change (log|warning|error|debug)
     * @param val the state to change that class to (on|off)
     */
    public static void toggle(String type, String val){
        if (!val.equals("on") && !val.equals("off")){
            Logger.error("Unknown value " + val);
            return;
        }
        switch(type) {
            case "log":
                LOG_ON = val.equals("on");
                break;
            case "warning":
                WARN_ON = val.equals("on");
                break;
            case "error":
                ERROR_ON = val.equals("on");
                break;
            case "debug":
                DEBUG_ON = val.equals("on");
                break;
            default:
                warning("Unknown type " + type);
                return;
        }
        log(type + " logging toggled " + val);
    }

    /**
     * utils.Logger messages take a msg which is prepended with the context and
     * then displayed if that class of message is set to 'on'
     */

    // for general information for the user. e.g. Command not found. try 'help'.
    public static void log(String msg) {
        if (LOG_ON) {
            System.out.println(context() + "Log: " + msg);
        }
    }

    // for warnings in dev. e.g. Command x expects n parameters
    public static void warning(String msg) {
        if (WARN_ON) {
            System.out.println(context() + "Warning: " + msg);
        }
    }

    // for errors in dev. e.g. Command returned null
    public static void error(String msg) {
        if (ERROR_ON) {
            System.out.println(context() + "Error: " + msg);
        }
    }

    // dev debugging. e.g. "length of array " + arr.length
    public static void debug(String msg) {
        if (DEBUG_ON) {
            System.out.println(context() + "Debug: " + msg);
        }
    }
}
