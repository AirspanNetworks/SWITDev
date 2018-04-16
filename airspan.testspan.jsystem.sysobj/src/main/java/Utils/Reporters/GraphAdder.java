package Utils.Reporters;


import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;
import java.io.*;
import java.util.ArrayList;


public class GraphAdder {
	public static String imagesPropertyValue = "";
	
    public enum GraphType {
        DATETIME("", ""),
        MONTH("valueFormatString: \"MMM\"},", "axisY: {valueFormatString: \"0.0#\"},"),
        HOUR(",gridThickness: 2,interval:2,intervalType: \"hour\",valueFormatString: \"hh TT Y\",labelAngle: -20", ""),
        STRING(),
        INT();

        private String valueX;
        private String valueY;

        GraphType() {
            this("", "");
        }

        GraphType(String valueX, String valueY) {
            this.valueX = valueX;
            this.valueY = valueY;
        }

        public String getValueX() {
            return valueX;
        }

        public String getValueY() {
            return valueY;
        }

    }

    public static Reporter report = ListenerstManager.getInstance();

    public static int oneGraphNo = 0;
    public static int twoGraphNo = 0;

    public static void AddGraph(String title, String titleX, String titleY, ArrayList<Double> upLink, ArrayList<Double> downLink, ArrayList<Long> timeStamp, String series1, String series2,boolean isQci9) throws Exception {
        //title += ":Total_RunTime=" + (timeStamp.get(timeStamp.size()-1)-timeStamp.get(0)/60000.0)+ "Min" ;
        String html = "<div id=\"" + title + "\" style=\"height: 300px; width: 95%;\"></div>"
                + "<script type=\"text/javascript\">"
                + "function twoGraphs"+twoGraphNo+"(){"
                + "var x = " + timeStamp
                + ";var up = " + upLink
                + ";var down = " + downLink
                + ";var data = [];"
                + "var dataSeries0 = {type: \"line\",showInLegend: true,name: \""+series1+"\"};"
                + "var dataPoints0 = [];"
                + "var dataSeries1 = {type: \"line\",showInLegend: true,name: \""+series2+"\"};"
                + "var dataPoints1 = [];"
                + "for (var i=0;i<x.length;i++){"
                +     "dataPoints0.push({x: new Date(x[i]), y: up[i]});"
                +     "dataPoints1.push({x: new Date(x[i]), y: down[i]});"
                + "}"
                + "dataSeries0.dataPoints = dataPoints0;"
                + "dataSeries1.dataPoints = dataPoints1;"
                + "data.push(dataSeries0);"
                + "data.push(dataSeries1);"
                + "var chart = new CanvasJS.Chart(\"" + title + "\",{"
                + "exportEnabled: true,"
                + "zoomEnabled: true,"
                + "panEnabled: true,"
                + "colorSet: [ \"#3359F2\",\"#FF5151\"],"
                + "title :{"
                + "text: \"" + title + "\""
                + "},"
                + "axisX: {"
                + "title: \"" + titleX + "\""
                + "},"
                + "axisY: {"
                + "title: \"" + titleY + "\""
                + "},"
                + "data: data"
                + "});"
                + "chart.render();"
                + "}"
                + "twoGraphs"+twoGraphNo++ + "();"
                + "</script>";
        report.reportHtml(title, html, true);
        if(isQci9){
            File f = getPictureAsFile(title, titleX, titleY, upLink, downLink, timeStamp, series1, series2);
            createGraphPicture(title, f);
        }
    }

    private static void createGraphPicture(String title, File f) throws Exception {
        report.startLevel("Graph Picture");
        ReporterHelper.copyFileToReporterAndAddLink(report,f,"Image_" + title);
        //ReporterHelper.copyFileToReporterAndAddLinkProperty(report,f,"Image_"+ System.currentTimeMillis()+ "_" + title,f.getName());
        //report.addProperty("Img_" + title,f.getName());
        String newField = title + "," + f.getName() + ";";
        imagesPropertyValue = newField;
        report.addProperty("Images", imagesPropertyValue);
        //Property e.g: "Images", "title1,imageName1.png;title2,imageName2.png
        report.stopLevel();
    }

    public static void AddGraph(String title, String titleX, String titleY, ArrayList<Double> upLink, ArrayList<Double> downLink, ArrayList<Long> axisX, boolean isQci9) throws Exception {
        AddGraph(title, titleX, titleY, upLink, downLink, axisX, "Uplink", "Downlink",isQci9);
    }

    public static void AddGraph(String title,String titleX, String titleY, ArrayList<Double> values, ArrayList<Long> timeStamp, String legend, boolean isQci9) throws Exception {
        //title += ":Total_RunTime=" + (timeStamp.get(timeStamp.size()-1)-timeStamp.get(0)/60000.0)+ "Min";
        String html = "<div id=\"" + title + "\" style=\"height: 300px; width: 95%;\"></div>"
                + "<script type=\"text/javascript\">"
                + "function oneGraphs"+oneGraphNo+"(){"
                + "var x = " + timeStamp
                + ";var up = " + values
                + ";var data = [];"
                + "var dataSeries = {type: \"line\",showInLegend: true,name: \"" + legend +"\"};"
                + "var dataPoints = [];"
                + "for (var i=0;i<x.length;i++){"
                +     "dataPoints.push({x: new Date(x[i]), y: up[i]});"
                + "}"
                + "dataSeries.dataPoints = dataPoints;"
                + "data.push(dataSeries);"
                + "var chart = new CanvasJS.Chart(\"" + title + "\",{"
                + "exportEnabled: true,"
                + "zoomEnabled: true,"
                + "panEnabled: true,"
                + "colorSet: [ \"#3359F2\"],"
                + "title :{"
                + "text: \"" + title + "\""
                + "},"
                + "axisX: {"
                + "title: \"" + titleX + "\""
                + "},"
                + "axisY: {"
                + "title: \"" + titleY + "\""
                + "},"
                + "data: data"
                + "});"
                + "chart.render();"
                + "}"
                + "oneGraphs"+oneGraphNo++ + "();"
                + "</script>";
        report.reportHtml(title, html, true);
        if(isQci9){
            File f = getPictureAsFile(title, titleX, titleY,values,timeStamp,legend);
        	createGraphPicture(title, f);
        }
    }



	private static File getPictureAsFile(String title, String titleX, String titleY, ArrayList<Double> upLink, ArrayList<Double> downLink, ArrayList<Long> timeStamp, String series1, String series2) throws IOException {
        LineChartImageCreatorAndSaver lineChartImageCreatorAndSaver = new LineChartImageCreatorAndSaver(title, upLink, downLink, timeStamp, series1, series2, titleX, titleY);
        return lineChartImageCreatorAndSaver.getFile();
    }

    private static File getPictureAsFile(String title, String titleX, String titleY, ArrayList<Double> graph, ArrayList<Long> timeStamp, String series) throws IOException {
        LineChartImageCreatorAndSaver lineChartImageCreatorAndSaver = new LineChartImageCreatorAndSaver(title, graph, timeStamp, series, titleX, titleY);
        return lineChartImageCreatorAndSaver.getFile();
    }


}
