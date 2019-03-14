public class Logger {

    private static boolean LOG_ON = true;
    private static boolean WARN_ON = true;
    private static boolean ERROR_ON = true;
    private static boolean DEBUG_ON = true;

    private static String context(){
        StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
        return ste.getClassName() + "." + ste.getLineNumber() + " ";
    }

    public static void toggle(String type, String val){
        if (!val.equals("on") && !val.equals("off")){
            Logger.error("Unknown value " + val);
            return;
        }
        switch(type) {
            case "log":
                LOG_ON = val.equals("on");
                break;
            case "warn":
                WARN_ON = val.equals("on");
                break;
            case "error":
                ERROR_ON = val.equals("on");
                break;
            case "debug":
                DEBUG_ON = val.equals("on");
                break;
            default:
                warn("Unknown type " + type);
                return;
        }
        log(type + " logging toggled " + val);
    }

    public static void log(String msg) {
        if (LOG_ON) {
            System.out.println(context() + "Log: " + msg);
        }
    }

    public static void warn(String msg) {
        if (WARN_ON) {
            System.out.println(context() + "Warning: " + msg);
        }
    }

    public static void error(String msg) {
        if (ERROR_ON) {
            System.out.println(context() + "Error: " + msg);
        }
    }

    public static void debug(String msg) {
        if (DEBUG_ON) {
            System.out.println(context() + "Debug: " + msg);
        }
    }
}
