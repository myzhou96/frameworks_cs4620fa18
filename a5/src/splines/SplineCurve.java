package splines;
import java.util.ArrayList;

import mesh.OBJFace;
import mesh.OBJMesh;
import mesh.OBJMesh_Archive;
import egl.NativeMem;
import egl.math.Matrix4;
import egl.math.Matrix3;
import egl.math.Vector2;
import egl.math.Vector3;
import egl.math.Vector3i;
import egl.math.Vector4;


public abstract class SplineCurve {
	private float epsilon;
	
	//Spline Control Points
	private ArrayList<Vector2> controlPoints;
	
	//Bezier Curves that make up this Spline
	private ArrayList<CubicBezier> bezierCurves;
	
	//Whether or not this curve is a closed curve
	private boolean isClosed;
	
	public static final float DIST_THRESH = 0.15f;
	public static final int MIN_OPEN_CTRL_POINTS= 4,
			                           MIN_CLOSED_CTRL_POINTS= 3,
			                           MAX_CTRL_POINTS= 20;

	public SplineCurve(ArrayList<Vector2> controlPoints, boolean isClosed, float epsilon) throws IllegalArgumentException {
		if(isClosed) {
			if(controlPoints.size() < MIN_CLOSED_CTRL_POINTS)
				throw new IllegalArgumentException("Closed Splines must have at least 3 control points.");
		} else {
			if(controlPoints.size() < MIN_OPEN_CTRL_POINTS)
				throw new IllegalArgumentException("Open Splines must have at least 4 control points.");
		}

		this.controlPoints = controlPoints;
		this.isClosed = isClosed;
		this.epsilon = epsilon;
		setBeziers();
	}
	
	public boolean isClosed() {
		return this.isClosed;
	}
	
	public boolean setClosed(boolean closed) {
		if(this.isClosed && this.controlPoints.size() == 3) {
			System.err.println("You must have at least 4 control points to make an open spline.");
			return false;
		}
		this.isClosed= closed;
		setBeziers();
		return true;
	}
	
	public ArrayList<Vector2> getControlPoints() {
		return this.controlPoints;
	}
	
	public void setControlPoint(int index, Vector2 point) {
		this.controlPoints.set(index, point);
		setBeziers();
	}
	
	public boolean addControlPoint(Vector2 point) {
		if(this.controlPoints.size() == MAX_CTRL_POINTS) {
			System.err.println("You can only have "+ SplineCurve.MAX_CTRL_POINTS + " control points per spline.");
			return false;
		}
		/* point= (x0, y0), prev= (x1, y1), curr= (x2,y2)
		 * 
		 * v= [ (y2-y1), -(x2-x1) ]
		 * 
		 * r= [ (x1-x0), (y1-y0) ]
		 * 
		 * distance between point and line prev -> curr is v . r
		 */
		Vector2 curr, prev;
		Vector2 r= new Vector2(), v= new Vector2();
		float distance= Float.POSITIVE_INFINITY;
		int index= -1;
		for(int i= 0; i < controlPoints.size(); i++) {
			curr= controlPoints.get(i);
			if(i == 0) {
				if(isClosed) {
					// add line between first and last ctrl points
					prev= controlPoints.get(controlPoints.size()-1);
				} else {
					continue;
				}
			} else {
				prev= controlPoints.get(i-1);
			}
			v.set(curr.y-prev.y, -(curr.x-prev.x)); v.normalize();
			r.set(prev.x-point.x, prev.y-point.y);
			float newDist = Math.abs(v.dot(r));
			Vector2 v2 = curr.clone().sub(prev);
			v2.mul(1.0f / v2.lenSq());
			float newParam = -v2.dot(r);
			if(newDist < DIST_THRESH && newDist <= distance && 0 < newParam && newParam < 1) {
				distance= newDist;
				index= i;
			}
		}
		
		if (index >= 0) {
			controlPoints.add(index, point);
			setBeziers();
			return true;
		}
		System.err.println("Invalid location, try selecting a point closer to the spline.");
		return false;
	}
	
	public boolean removeControlPoint(int index) {
		if(this.isClosed) {
			if(this.controlPoints.size() == MIN_CLOSED_CTRL_POINTS) {
				System.err.println("You must have at least "+MIN_CLOSED_CTRL_POINTS+" for a closed Spline.");
				return false;
			}
		} else {
			if(this.controlPoints.size() == MIN_OPEN_CTRL_POINTS) {
				System.err.println("You must have at least "+MIN_OPEN_CTRL_POINTS+" for an open Spline.");
				return false;
			}
		}
		this.controlPoints.remove(index);
		setBeziers();
		return true;
	}
	
	public void modifyEpsilon(float newEps) {
		epsilon = newEps;
		setBeziers();
	}
	
	public float getEpsilon() {
		return epsilon;
	}
	
	/**
	 * Returns the sequence of 2D vertices on this Spline specified by the sequence of Bezier curves
	 */
	public ArrayList<Vector2> getPoints() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for(CubicBezier b : bezierCurves)
			for(Vector2 p : b.getPoints())
				returnList.add(p.clone());
		return returnList;
	}
	
	/**
	 * Returns the sequence of normals on this Spline specified by the sequence of Bezier curves
	 */
	public ArrayList<Vector2> getNormals() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for(CubicBezier b : bezierCurves)
			for(Vector2 p : b.getNormals())
				returnList.add(p.clone());
		return returnList;
	}
	
	/**
	 * Returns the sequence of tangents on this Spline specified by the sequence of Bezier curves
	 */
	public ArrayList<Vector2> getTangents() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for(CubicBezier b : bezierCurves)
			for(Vector2 p : b.getTangents())
				returnList.add(p.clone());
		return returnList;
	}
	
	/**
	 * Using this.controlPoints, create the CubicBezier objects that make up this curve and
	 * save them to this.bezierCurves. Assure that the order of the Bezier curves that you
	 * add to bezierCurves is the order in which the overall Spline is chained together.
	 * If the spline is closed, include additional CubicBeziers to account for this.
	 */
	
	/**
	 * The setBeziers() method in SplineCurve.java constructs an ArrayList of Béziers, bezierCurves, 
	 * that make up the SplineCurve. The spline editor hands you a list of control points, numbered from 0 to N-1, 
	 * which are provided in the ArrayList controlPoints. These control points define a chain of curves that we'll 
	 * want to draw. Finding which four control points influence a segment of the Catmull-Rom Spline is mostly 
	 * simple: segment i of the curve is influenced by control points i-1, i, i+1, and i+2. Using this definition 
	 * we can generate N-3 Bézier segments from the N control points without falling off the ends of the sequence, 
	 * which is exactly what we'd like to do in the case of an open curve. However, you may have noticed a 
	 * boolean called isClosed in your SplineCurve. To handle closed splines, you'll need to tack on a few 
	 * additional Bézier curves. We'll let you figure out exactly how to handle this, but know that you'll 
	 * need to "wrap around" to the start of your control point list.
	 */
	private void setBeziers() {
		//TODO A5
		this.bezierCurves = new ArrayList<CubicBezier>();
		
		for(int i = 1; i < this.controlPoints.size()-2; i++){
			CubicBezier curve = toBezier(this.controlPoints.get(i-1), this.controlPoints.get(i),
					this.controlPoints.get(i+1), this.controlPoints.get(i+2), this.epsilon);
			this.bezierCurves.add(curve);
		}
		if (isClosed){
			int n = this.controlPoints.size();
			CubicBezier curve1 = toBezier(this.controlPoints.get(n-1), this.controlPoints.get(0),
					this.controlPoints.get(1), this.controlPoints.get(2), this.epsilon);
			CubicBezier curve2 = toBezier(this.controlPoints.get(n-2), this.controlPoints.get(n-1),
					this.controlPoints.get(0), this.controlPoints.get(1), this.epsilon);
			CubicBezier curve3 = toBezier(this.controlPoints.get(n-3), this.controlPoints.get(n-2),
					this.controlPoints.get(n-1), this.controlPoints.get(0), this.epsilon);
			this.bezierCurves.add(curve3);
			this.bezierCurves.add(curve2);
			this.bezierCurves.add(curve1);
		}
	}
	
	/**
	 * Reverses the tangents and normals associated with this Spline
	 */
	public void reverseNormalsAndTangents() {
		for(CubicBezier b : bezierCurves) {
			for(Vector2 p : b.getNormalReferences())
				p.mul(-1);
			for(Vector2 p : b.getTangentReferences())
				p.mul(-1);
		}
	}
	
	//Debug code
	public double getMaxAngle() {
		ArrayList<Vector2> myPoints = getPoints();
		double max = 0;
		for(int i = 0; i < myPoints.size() - 2; ++i) {
			Vector2 A = myPoints.get(i);
			Vector2 B = myPoints.get(i+1);
			Vector2 C = myPoints.get(i+2);
			
			Vector2 v1 = B.clone().sub(A);
			Vector2 v2 = C.clone().sub(B);
			
			v1.normalize();
			v2.normalize();
			
			double cur = Math.acos(v1.dot(v2));
			if (cur > max) max = cur;
		}
		return max;
	}
	
	
	public abstract CubicBezier toBezier(Vector2 p0, Vector2 p1, Vector2 p2, Vector2 p3, float eps);
	
	
	/**
	 * Given a curve that defines the cross section along the axis, fill the three GLBuffer objects appropriately.
	 * Here, we revolve the crossSection curve about the positive Z-axis.
	 * @param crossSection, the 2D spline for which every point defines the cross section of the surface
	 * @param data, a MeshData where we will output our triangle mesh
	 * @param scale > 0, parameter that controls how much the resulting surface should be scaled
	 * @param sliceTolerance > 0, the maximum angle in radians between adjacent vertical slices.
	 */
	public static void build3DRevolution(SplineCurve crossSection, OBJMesh mesh, float scale, float sliceTolerance) {
		//TODO A5
		Matrix3 rotateX90 = new Matrix3(1f, 0f, 0f, 0f, 0f, -1f, 0f, 1f, 0f);
		ArrayList<Vector3> rotatedPts = new ArrayList<Vector3>();
		
		System.out.println(crossSection.getPoints().size());
		//Iterating through all the pts in all the curves, and rotating them to lie in the yz plane
		for(int i = 0; i < crossSection.getPoints().size(); i++){
			Vector2 pt2 = crossSection.getPoints().get(i);
			Vector3 pt3 = new Vector3(pt2.x, pt2.y, 0f);
			rotatedPts.add(rotateX90.clone().mul(pt3));
		}
		
//		System.out.println(crossSection.bezierCurves.get(0).getPoints().toString());
		ArrayList<Vector3> rotatedZPts = new ArrayList<Vector3>(); //List of points that will store all vertices
		Double d = (2*Math.PI/sliceTolerance);
		int numSlices = d.intValue();
		System.out.println(numSlices);
		float inc = 2*(float)Math.PI/numSlices;

		for(int i = 0; i < rotatedPts.size(); i++){
			for(int j = 0; j < numSlices; j++){
				float rad = j*inc;
				Matrix3 rotateZ = new Matrix3(
						(float) Math.cos((double) rad), (float) -Math.sin((double) rad), 0f, 
						(float) Math.sin((double) rad), (float) Math.cos((double) rad), 0f, 
						0f, 0f, 1f);
				Vector3 p = rotatedPts.get(i);
				rotatedZPts.add(rotateZ.clone().mul(p));
			}
		}
		
		mesh.positions.addAll(rotatedZPts);
		System.out.println(rotatedPts.get(0).toString());
		System.out.println(rotatedPts.get(1).toString());
		System.out.println(rotatedPts.get(2).toString());
		System.out.println("MESH");
		System.out.println(mesh.positions.get(0).toString());
		System.out.println(mesh.positions.get(1).toString());
		System.out.println(mesh.positions.get(2).toString());
		System.out.println(mesh.positions.get(3).toString());
		System.out.println(mesh.positions.get(4).toString());
		System.out.println(mesh.positions.get(20).toString());
		mesh.normals.addAll(rotatedZPts);
		
		for(int i = 0; i < rotatedPts.size()-2; i++){ //vertical
			int top = (i*numSlices) + 1;
            int bottom = top + numSlices;
			for(int j = 0; j < numSlices; j++){ //horizontal
				OBJFace bottomTri = new OBJFace(3, true, true);
                bottomTri.positions[0] = top + j;
                bottomTri.positions[1] = bottom + j;
                bottomTri.positions[2] = bottom + ((1+j)%numSlices);
//                bottomTri.normals = bottomTri.positions;
                
                OBJFace topTri = new OBJFace(3, true, true);
                topTri.positions[0] = bottom + ((1+j)%numSlices);
                topTri.positions[1] = top + ((1+j)%numSlices);
                topTri.positions[2] = top + j;
//                topTri.normals = topTri.positions;
                
                mesh.faces.add(bottomTri);
                mesh.faces.add(topTri);             
			}
		}
		System.out.println(mesh.faces.get(0).positions[0]);
		System.out.println(mesh.faces.get(0).positions[1]);
		System.out.println(mesh.faces.get(0).positions[2]);
		System.out.println(mesh.faces.get(1).positions[0]);
		System.out.println(mesh.faces.get(1).positions[1]);
		System.out.println(mesh.faces.get(1).positions[2]);
		
//		//Rotating curve around z-axis
//		for(int i = 0; i < numSlices; i++){
//			float rad = i*inc;
//			Matrix3 rotateZ = new Matrix3(
//					(float) Math.cos((double) rad), (float) -Math.sin((double) rad), 0f, 
//					(float) Math.sin((double) rad), (float) Math.cos((double) rad), 0f, 
//					0f, 0f, 1f);
//			for(int j = 0; j < rotatedPts.size(); j++){
//				Vector3 p = rotatedPts.get(j);
//				rotateZ.clone().mul(p);
//				rotatedZPts.add(p);
//			}
//		}
//		System.out.println(rotatedZPts.get(0));
//		System.out.println(rotatedZPts.get(1));
//		System.out.println(rotatedZPts.get(2));
		
		
	}
}

