package com.dcaiti.traceloader.odometrie;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/** class to easily display and export(.svg) 2DPoints
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public class LineChart extends ApplicationFrame{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    List<LineChartSerie> list;
    JFreeChart chart;
    String pathFolder;
    Range origRange;
    int indexOfClumping;
    ArrayList<Integer> clumpValues;
    boolean clumping;
    JFormattedTextField rangeFieldLower;
    JFormattedTextField rangeFieldUpper;
    
    
    /** Default Constructor (has a default pathFolder)
     *  If you want to change the FolderPath call the other constructor or change it with setPathFolder()
     * 
     * @param title Title of the Chart and Frame
     */
    public LineChart(String title){
        this(title,"results/svg/");
    }
    
    /** Constructor
     * 
     * @param title Title of the Chart and Frame
     * @param pathFolder the default pathFolder for printing svg Files
     */
    public LineChart(String title, String pathFolder){
        super(title);
        list = new ArrayList<LineChartSerie>();
        this.setSize(new Dimension(1500,540));
        this.setPathFolder(pathFolder);
        this.indexOfClumping = -1;
        this.clumpValues = new ArrayList<Integer>();
        this.addClumpValues();
    }
    
    private void addClumpValues(){
        this.clumpValues.add(1);
        this.clumpValues.add(5);
        this.clumpValues.add(10);
        this.clumpValues.add(25);
        this.clumpValues.add(100);
        this.clumpValues.add(500);
    }
    
    public String getPathFolder(){
        return this.pathFolder;
    }
    
    public void setPathFolder(String pathFolder){
        this.pathFolder = pathFolder;
    }

    public void clear(){
        this.list.clear();
    }
    
    /** Add data with equidistant distance of 1. Start is by 0.
     * 
     * @param data the values (y-values) of your data as double[]
     * @param names the captions of your data
     */
    public void addAllLinearData(List<double[]> data, List<String> names){
        this.addAllLinearData(data, 0.0, 1.0, names);
    }
    
    /** Add data with equidistant distance.
     * 
     * @param data data the values (y-values) of your data as double[]
     * @param start the lower bound of your data (x-value)
     * @param tick the distance between your data-points
     * @param names the captions of your data
     */
    public void addAllLinearData(List<double[]> data, double start, double tick, List<String> names){
        for(int i = 0; i < data.size(); ++i){
            XYSeries serie = new XYSeries(names.get(i));
            double[] date = data.get(i);
            for(int j = 0; j < date.length; ++j){
                serie.add(start+tick*j,date[j]);
            }
            this.list.add(new LineChartSerie(serie));
        }
    }       
    
    /** Add arbitrary data
     * 
     * @param date the y_values
     * @param values the x_values
     * @param name the captions of your data
     */
    public void addNonLinearData(double[] date, double[] values, String name){
        XYSeries serie = new XYSeries(name);
        for(int j = 0; j < date.length; ++j){
            serie.add(values[j],date[j]);
        }
        this.list.add(new LineChartSerie(serie));
    }
    
    /** Add arbitrary data
     * 
     * @param data the y_values
     * @param values the x_values -> only one y value for all different dates. If you want more freedom, use addNonLinearData
     * @param names the captions of your data
     */
    public void addAllNonLinearData(List<double[]> data, List<Double> values, List<String> names){
        for(int i = 0; i < data.size(); ++i){
            XYSeries serie = new XYSeries(names.get(i));
            double[] date = data.get(i);
            for(int j = 0; j < date.length; ++j){
                serie.add(values.get(j).doubleValue(),date[j]);
            }
            this.list.add(new LineChartSerie(serie));
        }
    }
    
    /** Add data with equidistant distance.
     * 
     * @param date data the values (y-values) of your data as double[]
     * @param start the lower bound of your data (x-value)
     * @param tick the distance between your data-points
     * @param name the captions of your data
     */
    public void addLinearData(double[] date, double start, double tick, String name){
        XYSeries serie = new XYSeries(name);
        for(int j = 0; j < date.length; ++j){
            serie.add(start+tick*j,date[j]);
        }
        this.list.add(new LineChartSerie(serie));
    }
    
    /** Add data with equidistant distance of 1. Start is by 0.
     * 
     * @param date the values (y-values) of your data as double[]
     * @param name the captions of your data
     */
    public void addLinearData(double[] date, String name){
        this.addLinearData(date,0.0,1.0,name);
    }
    
    /** Set the colors which should be used by the Graph.
     * The List of Colors has to be at least so big as the number of series of DataPoints (SampleSize)
     * 
     * @param color
     */
    public void setAllColor(List<Color> color){
        if(color.size() < this.list.size()){
            return;
        }
        for(int i = 0; i < this.list.size(); ++i){
            this.list.get(i).color = color.get(i);
        }
    }
    
    /** change color for one series of DataPoints
     * 
     * @param color color of the line/shape
     * @param index which DataSerie should change color
     */
    public void setColor(Color color, int index){
        this.list.get(index).color = color;
    }
    
    /** tell if shapes should be used for one series of DataPoints
     * 
     * @param shape if series should use shapes
     * @param index which DataSerie should change shape-behavior
     */
    public void setShape(boolean shape, int index){
        this.list.get(index).shape = shape;
    }
    
    /** tell if lines between points should be used for one series of DataPoints
     * 
     * @param line if series should use line between points
     * @param index which DataSerie should change line-behavior
     */
    public void setLine(boolean line, int index){
        this.list.get(index).line = line;
    }
        
    private JFreeChart createChart(String x_axis, String y_axis){
        XYSeriesCollection set = new XYSeriesCollection();
        for(int i = 0; i < this.list.size(); ++i){
            set.addSeries(this.list.get(i).serie);
        }
        
        JFreeChart chart = ChartFactory.createXYLineChart(
                this.getTitle(),
                x_axis, 
                y_axis, 
                set, 
                PlotOrientation.VERTICAL, 
                true, 
                true, 
                false
        );
        
        chart.setBackgroundPaint(Color.white);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for(int i = 0; i < this.list.size(); ++i){
            renderer.setSeriesLinesVisible(i, this.list.get(i).line);
            renderer.setSeriesShapesVisible(i,this.list.get(i).shape);
            if(this.list.get(i).color != null){
                renderer.setSeriesPaint(i, this.list.get(i).color);
            }
        }
        plot.setRenderer(renderer);
        
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        return chart;
    }
    
    /** Has to be called before you can use any show- or print-related methods!
     * 
     * @param x_axis unit name of the x-values
     * @param y_axis unit name of the y-values
     */
    public void initChart(String x_axis, String y_axis){
        if(this.chart == null){
            this.chart = this.createChart(x_axis, y_axis);
        }
    }
            
    /** Let the Chart display in a Frame. Highly recommended!
     * 
     */
    public void showChart(){
        if(this.testIfInit()){
            return;
        }
        ChartPanel chartPanel = new ChartPanel(this.chart);
        chartPanel.setPreferredSize(this.getSize());
        setContentPane(chartPanel);
                
        chartPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        //add a UI for setRange
        NumberFormat format1 = NumberFormat.getNumberInstance();
        NumberFormat format2 = NumberFormat.getNumberInstance();
        this.rangeFieldLower = new JFormattedTextField(format1);
        this.rangeFieldUpper = new JFormattedTextField(format2);
        
        rangeFieldLower.setColumns(10);
        rangeFieldUpper.setColumns(10);
        
        chartPanel.add(rangeFieldLower);
        chartPanel.add(rangeFieldUpper);
        
        
        JButton rangeButton = new JButton("Set Range");
        rangeButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setRange();
            }
        });
        chartPanel.add(rangeButton);
        
        
        //button UI
        
        JButton printButton = new JButton("Print");
        JButton clumpButton = new JButton("Clumping");
//        JPanel buttonPanel = new JPanel();
//        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
//        buttonPanel.add(printButton);
//        chartPanel.add(buttonPanel);
//        buttonPanel.setPreferredSize(new Dimension(this.getSize().width, this.getSize().height / 10));
        
        chartPanel.add(printButton);
        chartPanel.add(clumpButton);
        printButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt){
//                System.out.println(chartPanel.getScreenDataArea().toString());
//                XYPlot plot = LineChart.this.chart.getXYPlot();
                printChart();
            }
        });
        clumpButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                clumpChart();
            }
        });
        
        //listener if range changes-> for clumping
        XYPlot plot = this.chart.getXYPlot();
        plot.getDomainAxis().addChangeListener(new AxisChangeListener(){
            @Override
            public void axisChanged(AxisChangeEvent event) {
                saveOrigRange(LineChart.this.chart.getXYPlot().getDomainAxis().getRange());              
            }
        });
        this.origRange = plot.getDomainAxis().getRange();
        
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }
    
    /** "clumps" the range of the x_axis. Used primarily in the frame-UI to get nice Borders.
     * 
     */
    public void clumpChart(){
        if(this.testIfInit()){
            return;
        }
        this.clumping = true;
        this.rotateClumping();
        this.clumpChart(this.indexOfClumping);
    }
    
    /** "clumps" the range of the x_axis. Used primarily in the frame-UI to get nice Borders.
     * 
     * To get the list of ClampValues use method getClampValues()
     * 
     * @param index which ClampValue you want to use (e.g.: index 2 is a clampValue of 10 (so the range will be rounded to a 10-step) )
     */
    public void clumpChart(int index){
        if(this.testIfInit()){
            return;
        }
        this.clumping = true;
        XYPlot plot = this.chart.getXYPlot();
        if(index == -1){
            //restore original Range
            plot.getDomainAxis().setRange(origRange);
        }else{
            Range newRange = clump(origRange,clumpValues.get(index));
            if(newRange != null){
                plot.getDomainAxis().setRange(newRange);
            }else{
                setIndexClumping(-1);
                plot.getDomainAxis().setRange(origRange);
            }
        }
    }
    
    /** method to change the range of the x_axis. Has to be called AFTER init_chart() (and after showChart() is recommended)
     * 
     * @param range
     */
    public void setRange(Range range){
        if(this.testIfInit()){
            return;
        }
        this.chart.getXYPlot().getDomainAxis().setRange(range);
    }
    
    public void setRange(double low, double high){
        this.setRange(new Range(low,high));
    }
    
    private void setRange(){
        if(this.rangeFieldLower.getValue() == null || this.rangeFieldUpper.getValue() == null){
            return;
        }
        double rangeFieldLowerValue = ((Number)this.rangeFieldLower.getValue()).doubleValue();
        double rangeFieldUpperValue = ((Number)this.rangeFieldUpper.getValue()).doubleValue();
        if(rangeFieldLowerValue < rangeFieldUpperValue){
            this.setRange(new Range(rangeFieldLowerValue,rangeFieldUpperValue));
        }
    }
    
    public List<Integer> getClampValues(){
        return this.clumpValues;
    }
    
    private void saveOrigRange(Range range){
        if(this.clumping){
//            System.out.println("We clumped");
            this.clumping = false;
        }else{
//            System.out.println("Range saved");
            this.origRange = range;
            this.indexOfClumping = -1;
        }
    }
    
    private void setIndexClumping(int index){
        if(index < this.clumpValues.size() && index > -2){
            this.indexOfClumping = index;
        }
    }
    
    private Range clump(Range range, int round){
        int upper = (int) Math.floor(range.getUpperBound() / round) * round;
        int lower = (int) Math.ceil(range.getLowerBound() / round) * round;
        if(lower < upper){
            return new Range(lower,upper);
        }else{
            return null;
        }
    }
        
    private void rotateClumping(){
        if(indexOfClumping == -1){
            this.indexOfClumping = getMinClumpingIndex();
        }else{
            ++this.indexOfClumping;
        }
        if(this.indexOfClumping == this.clumpValues.size()){
            indexOfClumping = -1;
        }
    }
    
    private int getMinClumpingIndex(){
        double minClumpValue = this.origRange.getLength() /10;
        int i = 0;
        while(this.clumpValues.size() > i && this.clumpValues.get(i) < minClumpValue){
            ++i;
        }
        return i;
//        if(i == this.clumpValues.size()){
//            return -1;
//        }else{
//            return 0;
//        }
    }
       
    /** print the chart on the default location with the default filename
     * 
     * Steps to add these svg-files to an Word-Document:
     * 1. open svg file with inkscape (free software)
     * 2. go to File->Document Settings and put the height and width of the frame there (Default is 1500 and 540)
     * 3. Resize and move the picture to the right place (in the upper Bar: X: 0, Y: 0, W: 1500, H: 540)
     * 4. Save File as: .emf
     * 5. Add the emf-File to the Word-Document
     */
    public void printChart(){
        if(this.testIfInit()){
            return;
        }
        this.printChart(this.getTitle()+this.getSuffixForZoom());
    }
    
    
    private String convertStringForFilename(String string){
        String[] arr = string.split("\\.");
        if(arr.length > 1 && arr[arr.length-1] == "svg"){
            string = StringUtils.join(Arrays.copyOf(arr, arr.length -1), '_');
        }
        string = string.replaceAll("\\W", "_");
        return string.replaceAll("_+","_")+".svg";
    }
    
    private String getSuffixForZoom(){
        XYPlot plot = this.chart.getXYPlot();
        Range x_range = plot.getDomainAxis().getRange();
        Range y_range = plot.getRangeAxis().getRange();
        if(this.inRange(x_range, plot.getDataRange(plot.getDomainAxis())) && this.inRange(y_range, plot.getDataRange(plot.getRangeAxis()))){
            return "";
        }else{
//            String range = (int)x_range.getLowerBound()+"_"+(int)y_range.getLowerBound()+"_"+(int)x_range.getUpperBound()+"_"+(int)y_range.getUpperBound();
            String range = (int)x_range.getLowerBound()+"_"+(int)x_range.getUpperBound();
            return "_"+range;
        }
    }
    
    private boolean inRange(Range outerRange, Range inRange){
        return (outerRange.getLowerBound() <= inRange.getLowerBound() && outerRange.getUpperBound() >= inRange.getUpperBound());
    }
    
    private boolean testIfInit(){
        if(this.chart == null){
            System.out.println("Chart was not initialised. Call initChart before you call this method!");
            return true;
        }
        return false;
    }
    
    /** print the chart as svg in the default location with a specified filename
     * 
     * @param filename filename the file should have. filename will be altered, if it is not a "legal" filename.
     */
    public void printChart(String filename){
        if(this.testIfInit()){
            return;
        }   
        filename = convertStringForFilename(filename);
        File svgFile = new File(this.pathFolder+filename);
        File dir = svgFile.getParentFile();
        if(!dir.exists()){
            if(dir.mkdirs()){
                System.out.println("Directory for printing File could not be created!");
            }
        }
        try {
            this.printChart(svgFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void printChart(File svgFile) throws IOException {
        // Get a DOMImplementation and create an XML document
        DOMImplementation domImpl =
            GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // draw the chart in the SVG generator
        this.chart.draw(svgGenerator, new Rectangle(this.getSize().width,this.getSize().height));                //this.getContentPane().getBounds()

        // Write svg file
        OutputStream outputStream = new FileOutputStream(svgFile);
        Writer out = new OutputStreamWriter(outputStream, "UTF-8");
        svgGenerator.stream(out, true /* use css */);                                           
        outputStream.flush();
        outputStream.close();
    }
    
    protected class LineChartSerie{
        XYSeries serie;
        boolean shape;
        Color color;
        boolean line;
                
        LineChartSerie(XYSeries serie){
            this.serie = serie;
            this.color =  null;
            if(this.serie.getItemCount() > 1000){
                this.line = true;
                this.shape = false;
            }else if(this.serie.getItemCount() < 25){
                this.line = false;
                this.shape = true;
            }else{
                this.line = true;
                this.shape = true;
            }
        }
        
    }
    
}
