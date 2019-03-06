package Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import Netspan.API.Enums.EnbStates;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;
import jsystem.utils.FileUtils;

/**
 * @author mgoldenberg
 */
public class GeneralUtils {

    public static Reporter report = ListenerstManager.getInstance();

    private static int levelCounter = 0;

    public static final int ERROR_VALUE = -999;

    public static final float ERROR_VALUE_FLOAT = -999;

    public static void clearLevelCounter() {
        levelCounter = 0;
    }

    /**
     * hex2decimal changes string in HEX to decimal
     *
     * @param s
     * @return
     */
    public static int hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16 * val + d;
        }
        return val;
    }

    /**
     * decimal2hex changes int decimal to HEX
     *
     * @param d
     * @return
     */
    public static String decimal2hex(int d) {
        String digits = "0123456789ABCDEF";
        if (d == 0)
            return "0";
        String hex = "";
        while (d > 0) {
            int digit = d % 16; // rightmost digit
            hex = digits.charAt(digit) + hex; // string concatenation
            d = d / 16;
        }
        return hex;
    }

    public static void reportHtmlLink(String title, String text) {
        String html = String.format("<pre style=\"margin-left: 60px; max-height:500px; overflow: scroll;\">%s</pre>", text.replace("\n", "<br>"));
        report.reportHtml(title, html, true);
    }

    public static boolean unSafeSleep(long timeInMS) {
        try {
            Thread.sleep(timeInMS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static int milisToMinutes(long milis) {
        return (int) TimeUnit.MILLISECONDS.toMinutes(milis);
    }

    public static String fractureToPercent(double value) {
        return new DecimalFormat("##.##").format(value * 100);
    }

    public static float tryParseStringToFloat(String s) {
        float ret;
        try {
            ret = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ERROR_VALUE;
        }
        return ret;
    }

    public static int tryParseStringToInt(String s) {
        int ret;
        try {
            ret = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ERROR_VALUE;
        }
        return ret;
    }

    /**
     * @return
     * @author sshahaf on 19/10/16 makes prefix with clock time return a 5
     * letters String.
     */
    public static String getPrefixAutomation() {
        Long time = System.currentTimeMillis();
        String strTime = String.valueOf(time);
        return strTime.substring(strTime.length() - 5, strTime.length());
    }

    /**
     * creates a file with the name of the name parameter and a content
     *
     * @param name
     * @param content
     * @return File object with the given Name filled with given content
     * @author Shahaf Shuhamy
     */
    public static File createFileWithContent(String name, String content) {
        File file;
        try {
            file = new File(name);
            FileOutputStream os = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(os);
            Writer w = new BufferedWriter(osw);
            w.write(content);
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    /**
     * return true if file is not there OR if it was and deleted!
     *
     * @param pathToFile
     * @return
     * @author sshahaf
     */
    public static boolean deleteFileIfExists(String pathToFile) {
        boolean result = false;
        File temp = new File(pathToFile);
        if (temp.exists()) {
            {
                result = temp.delete();
            }
        } else {
            result = true;
        }
        return result;
    }

    /**
     * delete a folder if path is a folder and exists
     */
    public static void deleteFolder(String pathToFolder) {
        File folder = new File(pathToFolder);
        if (folder.exists() && folder.isDirectory()) {
            FileUtils.deleteDirectory(folder);
        }
    }

    /**
     * return 1 if value equals true, else return 0
     */
    public static int booleanToInteger(boolean value) {
        return value ? 1 : 0;
    }

    /**
     * return true if value equals "1" or "true", else return 0
     */
    public static boolean stringToBoolean(String value) {
        return value.equals("1") || value.toLowerCase().equals("true");
    }

    /**
     * return true if value equals "1" or "true", else return 0
     */
    public static boolean intToBoolean(int value) {
        return value == 1;
    }

    /**
     * stopping level with reporter
     *
     * @author Shahaf Shuhamy
     */
    public static boolean stopLevel() {
        try {
            report.stopLevel();
            levelCounter--;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * starting level with reporter and get message
     *
     * @param message
     * @author Shahaf Shuhamy
     */
    public static boolean startLevel(String message) {
        try {
            GeneralUtils.printToConsole(message);
            report.startLevel(message);
            levelCounter++;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /*
     * Logging to console and to reporter
     * @author Dmitry Oguz
     * @param message
     * @param status
     */
    public static void logToLevel(String message, boolean status) {
    	GeneralUtils.printToConsole(message);
        report.report(message, status);
    }
    
    public static void stopAllLevels() {
        while (levelCounter > 0) {
            if (!stopLevel()) {
                printToConsole("failed closing a level, stopping stopAllLevels, current level = " + levelCounter);
                break;
            }
        }
    }

    /**
     * return string starting with prefix until the end of the string.<br>
     * return "" if prefix is not in buffer.
     *
     * @param string
     */
    public static String getStringWithPreFix(String string, String preFix) {
        int prefixIndex = string.lastIndexOf(preFix);
        String returnStr = "";
        GeneralUtils.printToConsole("buffer before cutting: \n" + string);
        if (prefixIndex < 0) {
            GeneralUtils.printToConsole("string have no such prefix return error value - null");
            return "";
        }
        returnStr = string.substring(prefixIndex);
        GeneralUtils.printToConsole("string Return : \n" + returnStr);
        return returnStr;
    }

    /**
     * gets Long number (currentTimeMillis)
     *
     * @param rxRate
     * @return date with the format MMM dd,yyyy HH:mm:ss
     * @author Shahaf Shuhamy
     */
    public static String timeFormat(long rxRate) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        Date resultDate = new Date(rxRate);
        return sdf.format(resultDate);
    }

    /**
     * @return simple Date time format : dd/MM/yyyy as String using Date
     * objects.
     * @author sshahaf
     */
    public static String simpleTimeFormat() {
        Date date = Calendar.getInstance().getTime();
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String today = formatter.format(date);
        return today.replaceAll("\\/", "_");
    }
    
    /**
     * gets Long number (currentTimeMillis)
     *
     * @author Moran Goldenberg
     * @prints the message with time stamp
     */
    public static void printToConsole(Object message) {
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date resultDate = new Date(currentTime);
        String formatedTime = sdf.format(resultDate);
        System.out.println(formatedTime + ": " + message);

    }

    public static void printToConsole() {
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date resultDate = new Date(currentTime);
        String formatedTime = sdf.format(resultDate);
        System.out.println(formatedTime);

    }

    public static File makeFileWithTheName(String fileName) {
        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch (Exception d) {
            d.printStackTrace();
        }
        return file;
    }

    /**
     * writing string to file useing PrintStream object
     *
     * @param file
     * @param fileCont
     */
    public static void writeToFile(File file, String fileCont) {
        try {
            PrintStream ps = new PrintStream(file);
            ps.print(fileCont);
            ps.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * remove all Non Digits from string
     *
     * @param str
     * @return
     */
    public static String removeNonDigitsFromString(String str) {
        String result = "";
        try {
            result = str.replaceAll("[^\\d.]", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public enum CellIndex {
        FORTY(40), // Cell 1
        FORTY_ONE(41), // Cell 2
        ENB(-1); // All Cells

        public int value;

        CellIndex(int index) {
            value = index;
        }
    }

    public enum AntennaIndex {
        ZERO(0), //Antenna 0
        ONE(1); //Antenna 1

        public int value;

        AntennaIndex(int index) {
            value = index;
        }
    }

    public static int serviceStateToInt(EnbStates serviceState) {
        if (serviceState == EnbStates.IN_SERVICE) {
            return 1;
        } else {
            return 0;
        }
    }

    public enum RebootType {
        COLD_REBOOT(3), WARM_REBOOT(2);

        public int value;

        RebootType(int index) {
            value = index;
        }
    }

    public enum RebootTypesNetspan{
    	Reset_Node, Cold_Reset_Node, Forced_Reset_Node, Forced_Cold_Reset_Node; 
    }
    
    /**
     * extract gz file to a new file with the given prefix
     *
     * @param gzFile          - the file with "gz" prefix
     * @param newFileLocation - the new file location with name : "C:\\Users\\lte1.pcap"
     * @return
     * @author sshahaf
     */
    public static File extractGZFile(File gzFile, String newFileLocation) {
        File outFile = new File(newFileLocation);
        byte[] buffer = new byte[1024];
        try {
            GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(gzFile));
            FileOutputStream out = new FileOutputStream(outFile);
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            gzis.close();
            out.close();

            printToConsole("Extract File from - " + gzFile.getPath());
            printToConsole("to location - " + newFileLocation);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return outFile;
    }

    /**
     * extract tar.gz file to a new file with the given prefix
     *
     * @param tarGzFile       - the file with "gz" prefix
     * @param newFileLocation - Destination folder "C:\\Users\\"
     * @return
     * @author dshalom
     */
    public static void extractTarFile(File tarFile, String newFileLocation) {
        try {
            FileInputStream inputStream;
            FileOutputStream out;
            inputStream = new FileInputStream(tarFile);
            byte[] content;
            TarArchiveInputStream tarInput = new TarArchiveInputStream(inputStream);
            TarArchiveEntry currentEntry;
            while ((currentEntry = tarInput.getNextTarEntry()) != null) {
                out = new FileOutputStream(newFileLocation + "/" + currentEntry.getName());
                content = new byte[(int) currentEntry.getSize()];
                System.out.println("For File = " + currentEntry.getName());
                tarInput.read(content, 0, content.length);
                out.write(content, 0, content.length);
                out.close();
            }
            tarInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void boldReportLine(String message) {
        startLevel(message);
        stopLevel();
    }

    public enum HtmlFieldColor {
        WHITE("#ffffff"), GREEN("#70db70"), YELLOW("#ffff00"), RED("#ff5c33"), BLUE("#3366ff"), GRAY("#A9A9A9"), BLACK("#000000");

        public String value;

        HtmlFieldColor(String v) {
            value = v;
        }
    }

    public enum StyleTagName {
        MARK1, MARK2, MARK3, MARK4, MARK5;
    }

    public static class HtmlTable {
        public static String markLine(StyleTagName styleTagName, String line) {
            return "<" + styleTagName + ">" + line + "</" + styleTagName + ">";
        }

        private Vector<Vector<Pair<HtmlFieldColor, String>>> table;
        private Vector<Triple<StyleTagName, HtmlFieldColor, HtmlFieldColor>> styles;

        public HtmlTable() {
            this.table = new Vector<Vector<Pair<HtmlFieldColor, String>>>();
            this.table.add(new Vector<Pair<HtmlFieldColor, String>>());//Columns headlines.
            this.table.get(0).add(new Pair<HtmlFieldColor, String>(HtmlFieldColor.WHITE, ""));//first field empty.

            this.styles = new Vector<Triple<StyleTagName, HtmlFieldColor, HtmlFieldColor>>();
        }

        public Pair<Boolean, String> addStyle(StyleTagName styleTagName, HtmlFieldColor backgroundColor, HtmlFieldColor color) {
            boolean result = true;
            String resultMassage = "New Style Added Successfully.";
            if (!isStyleTagNameAlreadyInUse(styleTagName)) {
                this.styles.addElement(new Triple<StyleTagName, HtmlFieldColor, HtmlFieldColor>(styleTagName, backgroundColor, color));
            } else {
                result = false;
                resultMassage = "Style Already Added(" + styleTagName + ").";
            }
            return new Pair<Boolean, String>(result, resultMassage);
        }

        private boolean isStyleTagNameAlreadyInUse(StyleTagName styleTagName) {
            boolean result = false;
            for (Triple<StyleTagName, HtmlFieldColor, HtmlFieldColor> style : this.styles) {
                if (style.getLeftElement() == styleTagName) {
                    result = true;
                    break;
                }
            }
            return result;
        }

        public void addNewColumn(String columnHeadline) {
            table.get(0).add(new Pair<GeneralUtils.HtmlFieldColor, String>(HtmlFieldColor.WHITE, columnHeadline));
        }

        public void addNewRow(String rowCriterion) {
            table.add(new Vector<Pair<HtmlFieldColor, String>>());
            table.lastElement().add(new Pair<HtmlFieldColor, String>(HtmlFieldColor.WHITE, rowCriterion));
        }

        public boolean addField(HtmlFieldColor severity, String fieldContent) {
            boolean result = false;
            if (table.lastElement().size() < table.get(0).size()) {
                if (table.indexOf(table.lastElement()) > 0) {
                    table.lastElement().add(new Pair<HtmlFieldColor, String>(severity, fieldContent));//add field to the last row that was added.
                    result = true;
                } else {
                    printToConsole("GeneralUtils.HtmlTable.addField: Row doesn't exist!");
                }
            } else {
                printToConsole("GeneralUtils.HtmlTable.addField: Column doesn't exist!");
            }
            return result;
        }

        public void reportTable(String headline) {
            String htmlTable = "<style>";
            for (Triple<StyleTagName, HtmlFieldColor, HtmlFieldColor> style : this.styles) {
                htmlTable += (style.getLeftElement() + "{");
                htmlTable += ("background-color: " + style.getMiddleElement().value + ";");
                htmlTable += ("color: " + style.getRightElement().value + ";");
                htmlTable += "}";
            }
            htmlTable += "</style>";
            htmlTable += "<table border='1px solid black'>";
            int tableSize = table.size();
            for (int i = 0; i < tableSize; i++) {
                htmlTable += "<tr>";
                int rowSize = table.get(i).size();
                for (int j = 0; j < rowSize; j++) {
                    if (i == 0 || j == 0) {
                        htmlTable += "<th>";
                    } else {
                        htmlTable += "<td bgcolor=" + table.get(i).get(j).getElement0().value + " align='middle'>";
                    }
                    htmlTable += table.get(i).get(j).getElement1();
                    if (i == 0 || j == 0) {
                        htmlTable += "</th>";
                    } else {
                        htmlTable += "</td>";
                    }
                }
                htmlTable += "</tr>";
            }
            htmlTable += "</table>";
            printToConsole("Html Table String: " + htmlTable);
            if (headline == "")
                report.reportHtml(htmlTable, "", true);
            else
                report.reportHtml(headline, htmlTable, true);
        }
    }

    public static void printAList(List<?> listOf) {
        for (Object element : listOf) {
            printToConsole(element);
        }
    }

    public static boolean checkPingResponse(String response) {
        return response.contains("0% packet loss");
    }

    public static List<String> convertStringToArrayList(String strToSplit, String separator) {
        List<String> res = new ArrayList<String>();

        for (String str : strToSplit.split(separator)) {
            res.add(str);
        }
        return res;
    }

    /**
     * checks if the given string is an integer.
     *
     * @param arrStrings - array of strings
     * @return - true if integer
     */
    public static boolean isArrayMadeOfIntegers(String[] arrStrings) {
        String oneString = arrayToString(arrStrings);
        try {
            Integer.parseInt(oneString);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * convert an array into one string
     *
     * @param array - array
     * @return one string
     */
    public static String arrayToString(String[] array) {
        StringBuilder builder = new StringBuilder();
        for (String s : array) {
            builder.append(s);
        }
        return builder.toString();
    }

    /**
     * is Linux OS
     *
     * @return - true if it's linux
     */
    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains(CommonConstants.LINUX_OS);
    }

    /**
     * get OS
     *
     * @return - Stirng - return CommonConstants.LINUX_OS if Linux, else return CommonConstants.WINDOWS_OS
     */
    public static String getOS() {
        if (System.getProperty("os.name").toLowerCase().contains(CommonConstants.LINUX_OS)) {
            return CommonConstants.LINUX_OS;
        } else return CommonConstants.WINDOWS_OS;
    }

    /**
     * Try to parse String to Int
     *
     * @param userInput - userInput
     * @return true is succeeded
     */
    private static  boolean tryParseInt(String userInput) {
        try {
            Integer.parseInt(userInput);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Converting the user's input String to Int, if typed wrong value, takes the default value.
     *
     * @param userInput    - user Input
     * @param defaultValue - defaultValue
     * @return - Integer
     */
    public static Integer parseInt(String userInput, Integer defaultValue) {
        if (tryParseInt(userInput)) {
            return Integer.parseInt(userInput);
        } else {
            return defaultValue;
        }
    }
}