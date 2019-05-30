package com.dcaiti.traceloader.odometrie.blender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TracePoint;
import com.google.common.collect.MinMaxPriorityQueue;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.math.Vector2D;

public class TraceBlender {

    MathBlender blend;
    List<MathBlenderTrigger> triggers;
    int error_counter = 0;
    
    public TraceBlender(){
        this.blend = new MathBlender();
        this.blend.subscribe(new GenericMathBlenderModule());
        this.blend.subscribe(new MathBlenderDetermenisticModel());
    }
    
    //function should be finished
    public Trace optimiseTrace(Trace orig){
        getMathBlenderTrigger();
        Trace blended = new Trace();
        for(int i = 0; i < orig.size(); ++i){
            TracePoint tp = orig.getTracePointAtIndex(i);
            MathBlenderTrigger trigger = this.getTriggerForIndex(i);
            if(trigger != null){
                //look if a module has to be optimised. If so do that before you use the MathBlender
                //for now: i know that the second module "MathBlenderDetermenisticModel" has to be optimised
//                double[] init = tp.getSensorVehicleModel().getMathState();
                this.optimiseModule(1,i,orig);
                this.blend.startWorking(trigger);
            }
            //predict the next blended tp
            try {
                double[] pred = this.blend.predict(tp.getSensorVehicleModel().getMathState()).toArray();
                blended.add(createTracePoint(tp,pred));
            } catch (Exception e) {
                // dürfte nie passieren, da TraceBlender IMMER MathBlender Module gibt
                e.printStackTrace();
            }
        }
        return blended;
    }
    
    public void test(int moduleIndex,int index, Trace trace){
        MathBlenderModule module = this.blend.modules.get(moduleIndex);
        Mapper mapper = new Mapper() {
            @Override
            public double f(double arg0, double arg1) {
                double[] d = new double[] {arg0*0.001, arg1*0.001};
                return TraceBlender.error(module, index, trace, d, 50);
            }
        };
        
//        Range Yrange = new Range(-40,40);
//        Range Xrange = new Range(0,40);
//        int Ysteps = 500;
//        int Xsteps = 100;
        
        Range Yrange = new Range(-1000,1000);
//        Range Yrange = new Range(0,50);
//        Range Yrange = new Range(350,400);
        Range Xrange = new Range(0,40);
        int Ysteps = 500;
        int Xsteps = 100;
        
        // Create the object to represent the function over the given range.
        final Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(Xrange, Xsteps, Yrange, Ysteps), mapper);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);

//        System.out.println(TraceBlender.error(module, index, trace, new double[]{0,0.37}, 50));
//        System.out.println(TraceBlender.error(module, index, trace, new double[]{0,0.009}, 50));
        
        // Create a chart
        Chart chart = AWTChartComponentFactory.chart(Quality.Advanced);
        chart.getScene().getGraph().add(surface);
        ChartLauncher.openChart(chart);     
                
    }
    
    public List<double[]> optimiseModule(int moduleIndex, int index, Trace trace){
        MathBlenderModule module = this.blend.modules.get(moduleIndex);
        int length = 50;
        //ignore threshold and everthing -> just optimise the deterministic module with drift
        double threshold = 0.000001;
        int limit = 10000;
        int counter = 0;
        int dir = 0;
        int switchCounter = 1;
        int switches = 0;
        double origStep = 0.005;
        double step = origStep;
        double[] param = new double[]{0,0};
        
        List<double[]> path = new ArrayList<double[]>();
        
        while(counter < limit && step > threshold){
            path.add(new double[]{param[1],counter,this.error(moduleIndex, index, trace, param, length)});
            //look to the left and right
            double error_left = this.error(moduleIndex, index, trace, new double[]{0,param[1]-step}, length);
            double error_right = this.error(moduleIndex, index, trace, new double[]{0,param[1]+step}, length);
            
            if(error_right < error_left){
//                System.out.println("Es geht nach rechts, da "+error_right +" < "+error_left);
                //go to the right
                param[1] += step;
                if(dir == -1){
                    ++switches;
                    switchCounter *= 2;
                    step = origStep / switchCounter;
                }
                dir = 1;
            }
            
            if(error_left < error_right){
//                System.out.println("Es geht nach links, da "+error_left +" < "+error_right);
                //go to the left
                param[1] -= step;
                if(dir == 1){
                    ++switches;
                    switchCounter *= 2;
                    step = origStep / switchCounter;
                }
                dir = -1;
            }
            ++counter;
        }
        
        System.out.println("Algo ist "+counter+" oft mal durchgelaufen! Param ist "+Arrays.toString(param));
        System.out.println("switchCounter: "+switches);
        module.correct(param);
        return path;
    }
    
    /** function to optimise a module! -> https://de.wikipedia.org/wiki/Downhill-Simplex-Verfahren
     * 
     * TODO: get the Trigger and determine how long the length of the optimisation trace should be
     * -> easy if trigger has an endpoint or something
     * 
     * @param index
     * @param trace
     */
    public void optimiseModule2(int moduleIndex,int index, Trace trace){
        //optimise the module with the downhill-simplex-iteration
        MathBlenderModule module = this.blend.modules.get(moduleIndex);
        int length = 50;
        double[][] paramSpace = module.optimisedParamSpace();
        int numberOfParam = paramSpace[0].length;
        int numberOfPoints = numberOfParam +1;
        
        //create #numberOfPoints random params to use the algorithm and calculate their error
        double[] diff = new double[numberOfParam];
        for(int i = 0; i < numberOfParam; ++i){
            diff[i] = Math.min(Math.abs(paramSpace[0][i] - paramSpace[1][i]), Math.abs(paramSpace[1][i] - paramSpace[2][i]));
        }
        
        ArrayList<ArrayRealVector> points = new ArrayList<ArrayRealVector>();
        final Map<ArrayRealVector,Double> map = new HashMap<ArrayRealVector,Double>();
        MinMaxPriorityQueue<ArrayRealVector> dequeue = MinMaxPriorityQueue
                .orderedBy(new Comparator<ArrayRealVector>(){
                    public int compare(ArrayRealVector param1, ArrayRealVector param2){
                        return (int)Math.signum((map.get(param2) - map.get(param1)));
                    }
                })
                .maximumSize(numberOfPoints)
                .create();
        
        for(int i = 0; i < numberOfPoints; ++i){
            double[] point = new double[numberOfParam];
            for(int j = 0; j < numberOfParam; ++j){
                point[j] = Math.random()*diff[j]*0.5+paramSpace[1][j];
            }
            //calculate error of point
            double err = this.error(moduleIndex, index, trace, point, length);
            ArrayRealVector vector = new ArrayRealVector(point);
            map.put(vector, err);
            dequeue.add(vector);
            points.add(vector);
        }
        
        //here comes the algo: first_element in dequeue ist element with highest error-value
        long tStart = System.currentTimeMillis();
        
        double alpha = 1.3;       //reflection          (1)
        double gamma = 5;       //expansion             (2)
        double beta = 0.7;      //contraction           (0.5)
        double sigma = 0.9;     //compression of simplex(0.5)
        
        int counter = 0;
        double threshold = 0.1;
        int limit = 1000000;
        
        int counterForCompression = 0;
        int counterForReflection = 0;
        int counterForExpansion = 0;
        int counterForContraction = 0;
        
        ArrayRealVector Xn = dequeue.poll();
        points.remove(Xn);
        ArrayRealVector Xn_1 = dequeue.peek();
        ArrayRealVector X0 = dequeue.peekLast();
        
        double error_Xn = map.get(Xn);
        double error_Xn_1 = map.get(Xn_1);
        double error_X0 = map.get(X0);
        
        System.out.println("Fehler vom besten param "+X0.toString()+" ist: "+map.get(X0));
        System.out.println("Fehler vom param "+Xn_1.toString()+" ist: "+map.get(Xn_1));
        System.out.println("Fehler vom param "+Xn.toString()+" ist: "+map.get(Xn));
        
        //while schleife mit gutem Konvergenzkriterium
        //->X0 und X1 sollen sehr nah beieinander sein(?)
        while(Math.abs(error_X0-error_Xn) > threshold && counter < limit){
            ++counter;
            
            //IMPORTANT: if you poll something out of dequeue, you have to remove the point in the list
            //IMPORTANT: if you add something in dequeue, you have to add it to the points AND to the map
            
            //midpoint of X0 to Xn_1
            ArrayRealVector mid = new ArrayRealVector(numberOfParam);
            for(int i = 0; i < points.size(); ++i){
                mid = mid.add(points.get(i));
            }
            
            //reflection
            ArrayRealVector r = (ArrayRealVector) mid.mapMultiply(1+alpha).subtract(Xn.mapMultiply(alpha));
            //get error of r
            double error_r = this.error(moduleIndex, index, trace, r.toArray(), length);
            if(error_r < error_X0){
                //expansion
                ArrayRealVector e = (ArrayRealVector) mid.mapMultiply(1+gamma).subtract(Xn.mapMultiply(gamma));
                double error_e = this.error(moduleIndex, index, trace, e.toArray(), length);
                //switch Xn for e or r -> Xn is already gone (except in the map)
                if(error_r > error_e){
                    ++counterForExpansion;
                }else{
                    ++counterForReflection;
                }
                ArrayRealVector better = error_r > error_e ? e : r;
                double error_better = error_r > error_e ? error_e : error_r;
                map.put(better, error_better);
                dequeue.add(better);
                points.add(better);
                //----------------------->end it here
            }else if(error_r < error_Xn_1){
                ++counterForReflection;
                //switch Xn with r
                map.put(r, error_r);
                dequeue.add(r);
                points.add(r);
                //------------------------>end it here
            }else{
                //h should be the better one of Xn or r
                ArrayRealVector h = error_r > error_Xn ? Xn : r;
                //contraction
                ArrayRealVector c = (ArrayRealVector) mid.mapMultiply(beta).add(h.mapMultiply(1-beta));
                double error_c = this.error(moduleIndex, index, trace, c.toArray(), length);
                if(error_c < error_Xn){
                    ++counterForContraction;
                    //switch c with Xn
                    map.put(c, error_c);
                    dequeue.add(c);
                    points.add(c);
                    //------------------------>end it here
                }else{
                  //contract the whole simplex
                    ++counterForCompression;
                    //list of points and Xn are together all points
                    ArrayList<ArrayRealVector> points_new = new ArrayList<ArrayRealVector>();
                    for(int i = 0; i < points.size(); ++i){
                        ArrayRealVector x = points.get(i);
                        x = (ArrayRealVector) X0.mapMultiply(sigma).add(x.mapMultiply(1-sigma));
                        points_new.add(x);
                    }
                    points_new.add((ArrayRealVector) X0.mapMultiply(sigma).add(Xn.mapMultiply(1-sigma)));
                    //reset dequeue and map
                    dequeue.clear();
                    map.clear();
                    
                    //X0 does not have to be computed again!
                    points_new.remove(X0);//-> remove it for the for-loop and add it after that
                    map.put(X0, error_X0);
                    dequeue.add(X0);
                    
                    for(int i = 0; i < points_new.size(); ++i){
                        map.put(points_new.get(i),this.error(moduleIndex, index, trace, points_new.get(i).toArray(), length));
                        dequeue.add(points_new.get(i));
                    }
                    
                    points_new.add(X0);
                    //------------------------>end it here
                }
            }
            
            //at the end get the new elements -> have to be called every iteration!
            Xn = dequeue.poll();
            points.remove(Xn);
            Xn_1 = dequeue.peek();
            X0 = dequeue.peekLast();
            
            error_Xn = map.get(Xn);
            error_Xn_1 = map.get(Xn_1);
            error_X0 = map.get(X0);
        }
        //END OF ALGO

        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("Der Algo hat "+elapsedSeconds+" Sekunden gebraucht!");
        System.out.println("Der Algo ist "+counter+" mal durchgelaufen");
        System.out.println("Die Fehlerfunction wurde "+this.error_counter+" mal oft aufgerufen!");
        System.out.println("Der Simplex wurde "+counterForCompression+" mal komprimiert!");
        
        //just to test if heap does his job:
        System.out.println("Am Ende des Algos sehen die Parameter so aus: ");
        System.out.println("Fehler von param "+Xn.toString()+" ist: "+map.get(Xn));
        while(!dequeue.isEmpty()){
            ArrayRealVector param = dequeue.poll();
            System.out.println("Fehler von param "+param.toString()+" ist: "+map.get(param));
        }
        
        try {
            PrintWriter pw = new PrintWriter(new File("results/deterministic/TraceBlender/stats.csv"));
            StringBuilder sb = new StringBuilder();
            char enter = '\n';
            char comma = ',';
            
            String[] heading = {"Dauer","Iterationen","Aufrufe Fehlerfunktion",
                    "Kompressionsanzahl","Kontraktionsanzahl","Reflektionsanzahl","Expansionsanzahl"};
            
            for(int i = 0; i < heading.length - 1; ++i){
                sb.append(heading[i]);
                sb.append(comma);
            }
            sb.append(heading[heading.length-1]);
            sb.append(enter);
            
            sb.append(elapsedSeconds); sb.append(comma);
            sb.append(counter); sb.append(comma);
            sb.append(this.error_counter); sb.append(comma);
            sb.append(counterForCompression); sb.append(comma);
            sb.append(counterForContraction); sb.append(comma);
            sb.append(counterForReflection); sb.append(comma);
            sb.append(counterForExpansion); sb.append(enter);
            
            pw.write(sb.toString()); pw.close();
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    /** function to get the value for a parameterset <-> point
     * 
     * @param index
     * @param trace
     * @param params
     * @param length
     * @return
     */
    public double error(int moduleIndex,int index,Trace trace, double[] params, int length){
        ++this.error_counter;
        //take params for module, calculate Trace and return the errorBetweenTraces
        MathBlenderModule module = this.blend.modules.get(moduleIndex);
        module.correct(params);
        //iterate through the trace and build a deterministic trace -> only iterate trough #length TracePoints
        ListIterator<TracePoint> iter = trace.listIterator(index);
        TracePoint tp = iter.next();
        double[] lastPrediction = tp.getSensorVehicleModel().getMathState();
        Trace det = new Trace();
        Trace orig = new Trace();
        det.add(tp);
        orig.add(tp);
        while(iter.hasNext() && length > 0){
            tp = iter.next();
            --length;
            orig.add(tp);
            lastPrediction = module.predict(lastPrediction, tp.getSensorVehicleModel().getMathState());
            det.add(createTracePoint(tp,lastPrediction));
        }
        //throw both traces in errorBetweenTraces and return the result
        return errorBetweenTraces(orig,det,new int[]{2,1,0});
    }
    
    public static double error(MathBlenderModule module,int index,Trace trace, double[] params, int length, int[] mult){
        //take params for module, calculate Trace and return the errorBetweenTraces
        module.correct(params);
        //iterate through the trace and build a deterministic trace -> only iterate trough #length TracePoints
        ListIterator<TracePoint> iter = trace.listIterator(index);
        TracePoint tp = iter.next();
        double[] lastPrediction = tp.getSensorVehicleModel().getMathState();
        Trace det = new Trace();
        Trace orig = new Trace();
        det.add(tp);
        orig.add(tp);
        while(iter.hasNext() && length > 0){
            tp = iter.next();
            --length;
            orig.add(tp);
            lastPrediction = module.predict(lastPrediction, tp.getSensorVehicleModel().getMathState());
            det.add(createTracePoint(tp,lastPrediction));
        }
        //throw both traces in errorBetweenTraces and return the result
        return errorBetweenTraces(orig,det,mult);
    }
    
    public static double error(MathBlenderModule module,int index,Trace trace, double[] params, int length){
        return error(module,index,trace,params,length,new int[]{2,1,0} );
    }
    
    //copy of error -> just to debug some things
    public Trace getPredTrace(int moduleIndex,int index,Trace trace, double[] params, int length){
        //take params for module, calculate Trace and return the errorBetweenTraces
        MathBlenderModule module = this.blend.modules.get(moduleIndex);
        module.correct(params);
        //iterate through the trace and build a deterministic trace -> only iterate trough #length TracePoints
        ListIterator<TracePoint> iter = trace.listIterator(index);
        TracePoint tp = iter.next();
        double[] lastPrediction = tp.getSensorVehicleModel().getMathState();
        Trace det = new Trace();
        Trace orig = new Trace();
        det.add(tp);
        orig.add(tp);
        while(iter.hasNext() && length > 0){
            tp = iter.next();
            --length;
            orig.add(tp);
            lastPrediction = module.predict(lastPrediction, tp.getSensorVehicleModel().getMathState());
            det.add(createTracePoint(tp,lastPrediction));
        }
        //throw both traces in errorBetweenTraces and return the result
//        return errorBetweenTraces(orig,det);
        return det;
    }
    
    private MathBlenderTrigger getTriggerForIndex(int index){
        MathBlenderTrigger trigger = null;
        for(MathBlenderTrigger trig : this.triggers){
            if(trig.index == index){
                trigger = trig;
            }
        }
        return trigger;
    }
    
    private void getMathBlenderTrigger(){
        this.triggers = new ArrayList<MathBlenderTrigger>();
        this.triggers.add(new MathBlenderTrigger(0,null,0));
    }
    
    public static double errorBetweenTraces(Trace traceA,Trace traceB,int[] mult){
        //improvement: identify "bad" places (where gps and other sensor-data dont make sense)
        //improvement: the first TracePoint should be more important than the last one
        if(traceA.size() != traceB.size()){
            System.out.println("Fehler bei dem Vergleich von Traces. -> Unterschiedlich lang!");
        }
        GeometryFactory fac = new GeometryFactory();
        Coordinate[] coor = new Coordinate[traceA.size()];
        for(int i = 0; i < coor.length; ++i){
            coor[i] = traceA.getTracePointAtIndex(i).getCoor();
        }
        LineString string = fac.createLineString(coor);
        LengthIndexedLine line = new LengthIndexedLine(string);
        double error = 0;
        for(int i = 0; i < traceB.size(); ++i){
            double index_nearestA = line.project(traceB.getTracePointAtIndex(i).getCoor());
            Coordinate nearestA = line.extractPoint(index_nearestA);
            double error_line = traceB.getTracePointAtIndex(i).getCoor().distance(nearestA);
            double error_point = traceA.getTracePointAtIndex(i).getCoor().distance(traceB.getTracePointAtIndex(i).getCoor());
//            double error_line_point = traceA.getTracePointAtIndex(i).getCoor().distance(nearestA);
            double index_pointA = line.indexOf(traceA.getTracePointAtIndex(i).getCoor());
            double error_line_point =Math.abs(index_pointA - index_nearestA); 
            error += Math.pow(error_point,mult[0]) * Math.pow(error_line,mult[1])* Math.pow(error_line_point,mult[2]);
        }
        return error/traceA.size();
    }
    
    public static TracePoint createTracePoint(TracePoint tp, double[] status){
        return new TracePoint.Builder(tp)
                .coordinate(new Coordinate(status[0], status[1]))
                .speed(status[2] * 3.6)
                .heading(new Vector2D(status[3], status[4]).angle())
                .yawRate(status[5]).build();
    }
    
}
