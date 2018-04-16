package Utils;

import org.joda.time.DateTime;


/**
 * Used for retrieve stack trace information. In order to get location information inside the source
 * code.</br>
 * Class contains static functions 1. at() 2.print2console(String message).</br>
 * The <b>at()</b> can be used with standard "System.out.println" in following form:</br>
 * Example:</br>
 * <b>"GeneralUtils.printToConsole(Here.at()+" end of loop"); " </b><br>
 * and the result will be as follow</br>
 * <b>" [2016-01-27 11:09:05.314][jodaTime.JodaTimeExample][main][62]:  end of loop "</b></br>
 * 
 */
public class StdOut {
	
	/**
	 * The <b>at()</b> can be used with standard "System.out.println" in following form:</br>
	 * Example:</br>
	 * <b>"GeneralUtils.printToConsole(Here.at()+" end of loop"); " </b><br>
	 * and the result will be as follow</br>
	 * <b>" [2016-01-27 11:09:05.314][jodaTime.JodaTimeExample][main][62]:  end of loop "</b></br>
	 * @return - String that contains following information:
	 * <b>[time][package.class][function][line]</b>
	 */
    public static String at () {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
        String time = DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS");
        String clazz = ste.getClassName();
        String method = ste.getMethodName();
        int line = ste.getLineNumber();
        return String.format("[%s][%s][%s][%d]: ", time,clazz,method,line);   
    }
    
    /**
     * Can replace standard <b>"System.out.println"</b> command.
     * Function prints message with additional information in following form:</b>
     * <b>[time][package.class][function][line] MESSAGE </b>
     * Example:</br>
     * <b>" [2016-01-27 11:09:05.314][jodaTime.JodaTimeExample][main][62]:  end of loop "</b></br>
     * @param message -String message to print.
     */
    public static void print2console(String message){
        StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
        String time = DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS");
        String clazz = ste.getClassName();
        String method = ste.getMethodName();
        int line = ste.getLineNumber();
        System.out.println(String.format("[%s][%s][%s][%d]: %s", time,clazz,method,line,message)); 	
    }
    
    
	/**
	 * Publish String Array to standard output
	 * 
	 * @param String[] - Array to publish
	 * @param String - description
	 */
	public static void printStringArray(String[] array,String description){
		print2console(description);
		if(array == null || array.length == 0){
			print2console("Nothing to print array is: null or his size = 0");
			return;
		}
		print2console("---------------");
		for (String string : array) {
			print2console(string);
		}
	}
}
