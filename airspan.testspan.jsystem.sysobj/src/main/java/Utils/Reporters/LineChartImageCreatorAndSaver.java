package Utils.Reporters;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;


public class LineChartImageCreatorAndSaver extends ApplicationFrame {

	private static final long serialVersionUID = 1L;
	
	private JFreeChart chart;
    private XYSeriesCollection dataSet;
    private File file;

    public LineChartImageCreatorAndSaver(String title, ArrayList<Double> uplink, ArrayList<Double> downlink, ArrayList<Long> timeStamp, String uplinkLegend, String downlinkLegend, String axisXTitle, String axisYTitle) throws IOException {
        super(title);
        dataSet = new XYSeriesCollection();
        createDataset(downlink, timeStamp, downlinkLegend);
        createDataset(uplink, timeStamp, uplinkLegend);
        chart = createChart(dataSet, title, axisXTitle, axisYTitle);
    }

    public LineChartImageCreatorAndSaver(String title, ArrayList<Double> values, ArrayList<Long> timeStamp, String valuesLegend, String axisXTitle, String axisYTitle) throws IOException {
        super(title);
        dataSet = new XYSeriesCollection();
        createDataset(values, timeStamp, valuesLegend);
        chart = createChart(dataSet, title, axisXTitle, axisYTitle);

    }

    private void createDataset(ArrayList<Double> rX, ArrayList<Long> timeStamp, String graphLegend) {
        XYSeries series = new XYSeries(graphLegend);
        long firstTime = timeStamp.get(0);
        for (int i = 0; i < rX.size(); i++) {
            series.add((timeStamp.get(i) - firstTime)/1000.0, rX.get(i));
        }
        dataSet.addSeries(series);
    }

    private JFreeChart createChart(XYSeriesCollection dataSet, String title, String axisXTitle, String axisYTitle) throws IOException {
        chart = ChartFactory.createXYLineChart(
                title,
                axisXTitle,
                axisYTitle,
                dataSet,
                PlotOrientation.VERTICAL,
                true, true, false);

        int width = 450; /* Width of the image */

        int height = 450; /* Height of the image */

        Stroke stroke = new BasicStroke(2.5f);
        chart.setBackgroundPaint(Color.white);
        XYPlot plot = chart.getXYPlot();
        plot.setRangeGridlineStroke(new BasicStroke(0.1f));
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinesVisible(false);
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.black);
        plot.setOutlinePaint(Color.WHITE);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRendererForDataset(dataSet);
        renderer.setSeriesPaint(0,Color.RED);
        renderer.setSeriesPaint(1,Color.BLUE);
        plot.setRenderer(renderer);

        plot.getRenderer().setSeriesStroke(0, stroke);
        plot.getRenderer().setSeriesStroke(1, stroke);
        file = new File(title + ".png");
        ChartUtilities.saveChartAsPNG(file, chart, width, height);
        return chart;
    }

    public File getFile() {
        return file;
    }
}
