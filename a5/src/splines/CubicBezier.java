package splines;

import java.util.ArrayList;

import egl.math.Vector2;
/*
 * Cubic Bezier class for the splines assignment
 */

public class CubicBezier {
	
	//This Bezier's control points
	public Vector2 p0, p1, p2, p3;
	
	//Control parameter for curve smoothness
	float epsilon;
	
	//The points on the curve represented by this Bezier
	private ArrayList<Vector2> curvePoints;
	
	//The normals associated with curvePoints
	private ArrayList<Vector2> curveNormals;
	
	//The tangent vectors of this bezier
	private ArrayList<Vector2> curveTangents;
	private static final int maxDepth = 10;
	
	
	/**
	 * 
	 * Cubic Bezier Constructor
	 * 
	 * Given 2-D BSpline Control Points correctly set self.{p0, p1, p2, p3},
	 * self.uVals, self.curvePoints, and self.curveNormals
	 * 
	 * @param bs0 First Bezier Spline Control Point
	 * @param bs1 Second Bezier Spline Control Point
	 * @param bs2 Third Bezier Spline Control Point
	 * @param bs3 Fourth Bezier Spline Control Point
	 * @param eps Maximum angle between line segments
	 */
	public CubicBezier(Vector2 p0, Vector2 p1, Vector2 p2, Vector2 p3, float eps) {
		curvePoints = new ArrayList<Vector2>();
		curveTangents = new ArrayList<Vector2>();
		curveNormals = new ArrayList<Vector2>();
		epsilon = eps;
		
		this.p0 = new Vector2(p0);
		this.p1 = new Vector2(p1);
		this.p2 = new Vector2(p2);
		this.p3 = new Vector2(p3);
		
		tessellate(0);
	}

    /**
     * Approximate a Bezier segment with a number of vertices, according to an appropriate
     * smoothness criterion for how many are needed.  The points on the curve are written into the
     * array self.curvePoints, the tangents into self.curveTangents, and the normals into self.curveNormals.
     * The final point, p3, is not included, because cubic Beziers will be "strung together".
     */
    private void tessellate(int depth) {
    	 // TODO A5
    	Vector2 theta01 = this.p1.clone().sub(this.p0);
    	Vector2 theta12 = this.p2.clone().sub(this.p1);
    	Vector2 theta23 = this.p3.clone().sub(this.p2);
    	float angle012 = theta01.angle(theta12);
    	float angle123 = theta12.angle(theta23);
    	if(depth < this.maxDepth && (angle012 > this.epsilon || angle123 > this.epsilon)){
        	Vector2 p10 = this.p0.clone().mul(0.5f).add(this.p1.clone().mul(0.5f));
        	Vector2 p11 = this.p1.clone().mul(0.5f).add(this.p2.clone().mul(0.5f));
        	Vector2 p12 = this.p2.clone().mul(0.5f).add(this.p3.clone().mul(0.5f));
        	Vector2 p20 = p10.clone().mul(0.5f).add(p11.clone().mul(0.5f));
        	Vector2 p21 = p11.clone().mul(0.5f).add(p12.clone().mul(0.5f));
        	Vector2 p30 = p20.clone().mul(0.5f).add(p21.clone().mul(0.5f));
        	CubicBezier c1 = new CubicBezier(this.p0, p10, p20, p30, this.epsilon);
        	CubicBezier c2 = new CubicBezier(p30, p21, p12, this.p3, this.epsilon);
        	
        	c1.tessellate(depth+1);
        	c2.tessellate(depth+1);
        	this.curvePoints.addAll(c1.curvePoints);
        	this.curvePoints.addAll(c2.curvePoints);
        	this.curveTangents.addAll(c1.curveTangents);
        	this.curveTangents.addAll(c2.curveTangents);
        	this.curveNormals.addAll(c1.curveNormals);
        	this.curveNormals.addAll(c2.curveNormals);
           
    	}
    	else {
    		this.curvePoints.add(this.p0);
    		Vector2 tangent = this.p1.clone().sub(this.p0).normalize();
    		this.curveTangents.add(tangent);
    		Vector2 norm = new Vector2(tangent.y, -tangent.x);
    		this.curveNormals.add(norm);
    	}
    	
    	
    }
	
    
    /**
     * @return The points on this cubic bezier
     */
    public ArrayList<Vector2> getPoints() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curvePoints) returnList.add(p.clone());
    	return returnList;
    }
    
    /**
     * @return The tangents on this cubic bezier
     */
    public ArrayList<Vector2> getTangents() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curveTangents) returnList.add(p.clone());
    	return returnList;
    }
    
    /**
     * @return The normals on this cubic bezier
     */
    public ArrayList<Vector2> getNormals() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curveNormals) returnList.add(p.clone());
    	return returnList;
    }
    
    
    /**
     * @return The references to points on this cubic bezier
     */
    public ArrayList<Vector2> getPointReferences() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curvePoints) returnList.add(p);
    	return returnList;
    }
    
    /**
     * @return The references to tangents on this cubic bezier
     */
    public ArrayList<Vector2> getTangentReferences() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curveTangents) returnList.add(p);
    	return returnList;
    }
    
    /**
     * @return The references to normals on this cubic bezier
     */
    public ArrayList<Vector2> getNormalReferences() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curveNormals) returnList.add(p);
    	return returnList;
    }
    
	public static void main(String[] args) {
		Vector2 p0 = new Vector2(-1.0f, -1.0f);
		Vector2 p1 = new Vector2(-1.0f, 1.0f);
		Vector2 p2 = new Vector2(1.0f, 1.0f);
		Vector2 p3 = new Vector2(1.0f, -1.0f);
		
		CubicBezier test = new CubicBezier(p0, p1, p2, p3, 0.5f);
		System.out.println(test.curvePoints.toString());
	}
}
