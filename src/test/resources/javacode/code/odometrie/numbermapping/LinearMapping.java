package com.dcaiti.traceloader.odometrie.numbermapping;

/** A linear mapping from one interval to another.
 * <p>
 * Used in {@link com.dcaiti.traceloader.odometrie.numbermappingNumberMapping NumberMapping}
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public class LinearMapping implements UnivariateBorderFunction{

    double fromMin;
    double fromMax;
    double toMin;
    double toMax;
    
    double fromDiff;
    double toDiff;
    double diff;
    
    boolean identity = false;
    
    public LinearMapping(double fromMin, double fromMax){
        this(fromMin, fromMax,0,1);
    }
    
    public LinearMapping(double fromMin, double fromMax, double toMin, double toMax){
        this.fromMin = fromMin;
        this.fromMax = fromMax;
        this.toMin = toMin;
        this.toMax = toMax;
        this.init();
    }
    
    private void init(){
        if(this.fromMin == this.toMin && this.fromMax == this.toMax){
            this.identity = true;
            return;
        }
        this.fromDiff = this.fromMax - this.fromMin;
        this.toDiff = this.toMax - this.toMin;
        this.diff = fromDiff / toDiff;
    }
    
    private double stickyBorder(double x){
        return Math.min(this.fromMax, Math.max(this.fromMin, x));
    }
    
    @Override
    public double value(double x) {
        x = this.stickyBorder(x);
        if(this.identity){
            return x;
        }
        x -= this.fromMin;
        x /= this.diff;
        x += this.toMin;
        return x;
    }

    @Override
    public double[] getFromBorder() {
        return new double[]{this.fromMin, this.fromMax};
    }

    @Override
    public double[] getToBorder() {
        return new double[]{this.toMin, this.toMax};
    }
    
}
