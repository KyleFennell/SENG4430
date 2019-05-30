package com.dcaiti.traceloader.odometrie.onepartition;

import java.util.Arrays;

/** A class which represents a distribution of unity with the help of the basis function of splines.
 * <p>
 * The knot array of the imaginary splines and the degree can be changed.
 * The knot array can be any valid knot array or a clipped knot array which has the 0s and 1s in the array omitted
 * 
 * @author nkl- Nicolas Klenert
 *
 */
public class SplineOnePartition extends OnePartition{

    private double[] knots;
    private int degree;
    private boolean pinned;
    
    /** Constructor for OnePartition with Splines.
     * Because of the the Definition with Splines, maximum of degree can only be size-1
     * 
     * @param size
     * @param degree degree of the basis function.
     * 
     *  SplineOnePartition(x,1) get the same results as LinearOnePartition(x)
     * 
     */
    public SplineOnePartition(int size, int degree) {
        super(size);
        degree = Math.min(this.size -1, degree);
        this.degree = degree;
        this.knots = createKnotVectorPinnedUniform(this.size,this.degree,null);
        this.pinned = true;
    }
    
    /**
     * 
     * @param knots can be one of two types: the full knotvector OR the knotvector with all 0s and 1s omitted if it is a pinned vector.
     * size is "1+knots.length+degree" if you want to use the second knotvector type
     * @param degree
     */
    public SplineOnePartition(double[] knots, int degree){
        //method filter knots
        super(getSizeWithKnotVector(knots, degree));
        this.degree = degree;
        if(knots.length == this.degree + 1 + this.size){
            //knots is full array -> just look if it is pinned
            this.knots = knots;
            this.pinned = this.isPinned();
        }else{
            //because our method filter, we know the border 0s and 1s are just emitted
            this.knots = new double[this.degree + 1 + this.size];
            for(int i = 0; i < this.degree +1 ; ++i){
                this.knots[i] = 0;
                this.knots[this.knots.length - i -1] = 1;
            }
            for(int i = this.degree +1; i < this.knots.length - (this.degree + 1); i++){
                this.knots[i] = knots[i - this.degree -1];
            }
            this.pinned = true;
        }
    }
    
    /** helper method which caluclate the size the onePartition with the knotvector and the degree
     * 
     * @param knots
     * @param degree
     * @return
     */
    private static int getSizeWithKnotVector(double knots[], int degree){
        Arrays.sort(knots);
        if(knots[knots.length -1] > 1 || knots[0] < 0){
            normalizeKnotVector(knots);
        }
        //get the mid ones
        int counter = 0;
        for(int i = 0; i < knots.length; ++i){
            if(knots[i] != 0 && knots[i] != 1){
                ++counter;
            }
        }
        if(knots.length != counter && knots.length != 2*degree+2+counter){
            //array is not pinned!
            return knots.length-degree-1;
//            System.out.println("__SplineOnePartition__: Input data does not match!");
//            int size = degree+1;
//            createKnotVectorPinnedUniform(size,degree,knots);
//            return size;
        }
        return counter+degree+1;
    }

    @Override
    public double[] getDistribution(double value) {
        double[] w = new double[this.size];
        for(int i = 0; i < this.size; ++i){
            w[i] = basis(value,i,this.degree,this.knots);
        }
        if(this.pinned){
            return w;
        }else{
            return OnePartition.normalize(w);
        }
    } 
    
    /** method to check it the knots are pinned/clipped or not
     * 
     * @return
     */
    private boolean isPinned(){
        for(int i = 0; i < this.degree +1; ++i){
            if(this.knots[i] != 0 || this.knots[this.knots.length - i -1] != 1){
                return false;
            }
        }
        return true;
    }
    
    /**
     * compute value of basis function for u value
     * 
     * @param u
     *            u value to get the basis function value for. Valid range is
     *            from uMin to uMax
     * @param i
     *            control point parameter. 0: first control point basis
     *            function, 1: second ...
     * @param p
     *            degree of the curve
     * @param knots
     *            the knot vector of the B-spline
     * @return
     */
    private static double basis(double u, int i, int p, double[] knots) {
            if (p == 0) {
                    if (knots[i] <= u && u <= knots[i + 1]) {
                            return 1d;
                    } else {
                            return 0d;
                    }
            } else {
                    double a1 = u - knots[i];
                    double a2 = knots[i + p] - knots[i];

                    double b1 = knots[i + p + 1] - u;
                    double b2 = knots[i + p + 1] - knots[i + 1];

                    double a = a1 / a2;
                    if (a1 == 0 && a2 == 0) {
                            a = 1;
                    } else if (Double.isNaN(a1) || Double.isNaN(a2) || Double.isNaN(a) || Double.isInfinite(a)) {
                            a = 0;
                    }

                    double b = b1 / b2;
                    if (b1 == 0 && b2 == 0) {
                            b = 1;
                    } else if (Double.isNaN(b1) || Double.isNaN(b2) || Double.isNaN(b) || Double.isInfinite(b)) {
                            b = 0;
                    }
                    double aBasis = basis(u, i, p - 1, knots);
                    double bBasis = basis(u, i + 1, p - 1, knots);
                    double result = (a * aBasis) + (b * bBasis);
                    return Math.max(0, Math.min(1, result));
            }
    }
    
    /**
     * create a pinned uniform knot vector for a b-spline with specified number
     * of control points and a specified curve degree
     * 
     * @param n_controlPoints
     *            number of control points
     * @param degree
     *            degree of the curve
     * @return a array with a normalized knot vector that is pinned uniform
     */
    private static double[] createKnotVectorPinnedUniform(int n_controlPoints, int degree, double[] knots) {
            if(knots == null){
                knots = new double[degree + 1 + n_controlPoints];
            }

            // create pinned uniform knot vector
            // create mutliplicity values
            for (int i = 0; i < degree + 1; i++) {
                    knots[i] = 0;
                    knots[knots.length - 1 - i] = n_controlPoints - (degree);
            }

            // fill in values in the middle
            int cnt = 1;
            for (int i = degree + 1; i < knots.length - (degree + 1); i++) {
                    knots[i] = cnt++;

            }
            normalizeKnotVector(knots);
            return knots;
    }
    
    /**
     * normalize knot vector to values from 0..1
     * 
     * @param knotVector
     *            the array defining the knot vector that is to be normalized
     * 
     */
    private static void normalizeKnotVector(double[] knotVector) {
            double max = knotVector[0];
            double min = knotVector[0];

            for (int i = 1; i < knotVector.length; i++) {
                    double value = knotVector[i];
                    if (value < min) {
                            min = value;
                    }
                    if (value > max) {
                            max = value;
                    }
            }
            double delta = max - min;
            for (int i = 0; i < knotVector.length; i++) {
                    double value = knotVector[i];
                    knotVector[i] = (value - min) / delta;
            }
    }
    
}
