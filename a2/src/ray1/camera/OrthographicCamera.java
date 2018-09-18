package ray1.camera;

import egl.math.Vector3;
import egl.math.Vector3d;
import ray1.Ray;

public class OrthographicCamera extends Camera {

    //TODO#A2: create necessary new variables/objects here, including an orthonormal basis
    //          formed by three basis vectors and any other helper variables 
    //          if needed.
    
	protected Vector3 basis_U = new Vector3();
	protected Vector3 basis_V = new Vector3();
	protected Vector3 basis_W = new Vector3();
	protected Vector3 e = new Vector3();
	protected float viewWidth;
	protected float viewHeight;
    
    /**
     * Initialize the derived view variables to prepare for using the camera.
     */
    public void init() {
        // TODO#A2: Fill in this function.
        // 1) Set the 3 basis vectors in the orthonormal basis, 
        //    based on viewDir and viewUp
        // 2) Set up the helper variables if needed
    	Vector3 d = getViewDir();
    	Vector3 up = getViewUp();
    	e = getViewPoint();
    	basis_W = d.clone().negate().normalize();
    	basis_U = up.clone().cross(basis_W).normalize();
    	basis_V = basis_W.clone().cross(basis_U).normalize();
    	
    	viewWidth = getViewWidth();
    	viewHeight = getViewHeight();

    }

    /**
     * Set outRay to be a ray from the camera through a point in the image.
     *
     * @param outRay The output ray (not normalized)
     * @param inU The u coord of the image point (range [0,1])
     * @param inV The v coord of the image point (range [0,1])
     */
    public void getRay(Ray outRay, float inU, float inV) {
        // TODO#A2: Fill in this function.
        // 1) Transform inU so that it lies between [-viewWidth / 2, +viewWidth / 2] 
        //    instead of [0, 1]. Similarly, transform inV so that its range is
        //    [-viewHeight / 2, +viewHeight / 2]
        // 2) Set the origin field of outRay for an orthographic camera. 
        //    In an orthographic camera, the origin should depend on your transformed
        //    inU and inV and your basis vectors u and v.
        // 3) Set the direction field of outRay for an orthographic camera.
    	inU = transform(inU, -viewWidth/2.0f, viewWidth/2.0f);
    	inV = transform(inV, -viewHeight/2.0f, viewHeight/2.0f);
    	Vector3 origin = new Vector3(
    			e.clone().add(this.basis_U.clone().mul(inU)
    					.add(this.basis_V.clone().mul(inV))));
//    	System.out.println("origin");
//    	System.out.print(this.basis_V);
    	Vector3 direction = new Vector3(this.basis_W.clone().negate());
    	outRay.set(new Vector3d(origin), new Vector3d(direction));
    }
    
    public float transform(float x, float rangeLower, float rangeUpper){
    	return x*(rangeUpper-rangeLower) + rangeLower;
    }

}
