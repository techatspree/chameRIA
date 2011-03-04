package de.akquinet.gomobile.chameria.services;

/**
 * Helper class to parse arguments.
 * This class supports:
 * <ul>
 * <li>arg value arguments</li>
 * <li>arg=value arguments</li> 
 * </ul>
 * If the argument starts with '-', the given argument must contain it.
 */
public class ActivationUtils {
    
    /**
     * Checks if the arguments contains the argument <code>arg</code>.
     * @param args the arguments
     * @param arg the argument to find
     * @return <code>true</code> if the 
     */
    public static boolean containsArgument(String[] args, String arg) {
         if (args == null || arg == null || arg.length() == 0 || args.length == 0) {
             return false;
         } else {
             for (String a : args) {
                 if (a.equalsIgnoreCase(arg)) {
                     return true;
                 } else if (a.startsWith(arg + "=")) {
                     return true;
                 }
                 
             }
             return false;
         }
    }
    
    /**
     * Gets the argument value.
     * <ul>
     * <li>for 'arg=value' it returns 'value'</li>
     * <li>for 'arg value' it returns 'value'</li>
     * @param args the arguments
     * @param arg the argument to find
     * @return the argument value or <code>null</code> if not found.
     */
    public static String getArgumentValue(String args[], String arg) {
        if (args == null || arg == null || arg.length() == 0 || args.length == 0) {
            return null;
        } else {
            for (int i = 0; i < args.length; i++) {
                String a = args[i];
                if (a.equalsIgnoreCase(arg)) {
                    // Case 'arg value' : Look for arg + 1;
                    if (args.length > i + 1) {
                        return args[i+1];
                    }
                    
                } else if (a.startsWith(arg + "=")) {
                    // Case 'arg=value' : Parse the value
                    int index = a.indexOf('=') + 1;
                    return a.substring(index);
                }
            }
            
            return null;
        }
    }
    
    /**
     * Gets the value of the '-open' argument.
     * @param args the arguments
     * @return the value of the '-open' argument or <code>
     * null</code> if not found.
     */
    public static String getOpenArgument(String args[]) {
        return getArgumentValue(args, "-open");
    }
    

}
