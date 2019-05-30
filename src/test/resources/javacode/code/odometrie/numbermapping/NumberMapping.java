package com.dcaiti.traceloader.odometrie.numbermapping;

import com.dcaiti.traceloader.odometrie.LineChart;

/** class to easily map some number interval to another.
 * <p>
 * It takes a arbitrary UnivariateBorderFunction to get the information how the distribution should be.
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public class NumberMapping implements UnivariateBorderFunction{
    
    protected LinearMapping input;
    protected LinearMapping output;
    protected UnivariateBorderFunction func;
    
    public NumberMapping(UnivariateBorderFunction func){
        this(0,1,func);
    }
    
    public NumberMapping(double minValue, double maxValue, UnivariateBorderFunction func){
        this(minValue, maxValue, 0, 1, func);
    }
    
    public NumberMapping(double minValue, double maxValue,  double loAsym, double hiAsym, UnivariateBorderFunction func){
        double[] funcFrom = func.getFromBorder();
        this.input = new LinearMapping(minValue, maxValue, funcFrom[0], funcFrom[1]);
        double[] funcTo = func.getToBorder();
        this.output = new LinearMapping(funcTo[0],funcTo[1],loAsym, hiAsym);
        this.func = func;
    }
    
    public double value(double x){
        x = this.input.value(x);
        x = this.func.value(x);
        x = this.output.value(x);
        return x;
    }

    @Override
    public double[] getFromBorder() {
        return this.input.getFromBorder();
    }

    @Override
    public double[] getToBorder() {
        return this.output.getToBorder();
    }
    
    public void visualize(){
        int steps = 100;
        LineChart chart = new LineChart("NumberMapping with function "+this.func.getClass().getSimpleName());
        double[] values = new double[steps];
        double[] border = this.getFromBorder();
        double tick = (border[1] - border[0]) / steps;
        for(int i = 0; i < steps; ++i){
            values[i] = this.value(border[0]+i*tick);
        }
        chart.addLinearData(values, border[0], tick, "NumberMapping");
        chart.initChart("X Values","Y Values");
        chart.showChart();
        chart.setRange(border[0], border[1]);
    }
    
}
