package testsNG.Actions.Utils;

import java.io.File;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

/**
 * StringTools
 * Date: 26.11.18
 * Created: Paz
 */
public class StringTools {

    /**
     * join the strings with File Separetor "\" between.
     * Input: A.B.C
     * Output: A/B/C or A\B\C , depends on the OS
     *
     * @param strings - old path
     * @return - new path
     */
    public static String getStringFileSeperator(String... strings) {
        return StringUtils.join(strings, File.separator);
    }


    /**
     * Replace the "/" and the "\" with a file separator string compatible both to Windows and Linux
     *
     * @param path - old path
     * @return - new path
     */
    public static String getStringWithUnifiedFileSeperator(String path) {
        String newPath = path.replaceAll("/", File.separator);
        newPath =  newPath.replaceAll("\\\\+", Matcher.quoteReplacement(File.separator));
        return newPath;
    }
}