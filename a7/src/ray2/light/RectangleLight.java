package ray2.light;


import java.io.IOException;

import egl.math.Colord;
import egl.math.Matrix4d;
import egl.math.Vector3d;
import ray2.Ray;
import ray2.Scene;
import ray2.material.LambertianBSDF;
import ray2.mesh.OBJMesh;
import ray2.surface.Mesh;

/**
 * This class represents an area source that is rectangular, specified by a
 * frame in the same way as a camera.  It has constant radiance across the
 * whole surface.
 *
 * @author srm, zechenz
 */
public class RectangleLight extends Light {
	
	/** Where the light is located in space. */
	public final Vector3d position = new Vector3d();
	public void setPosition(Vector3d position) { this.position.set(position); }

	/** The direction the light is facing. */
	protected final Vector3d normalDir = new Vector3d(0, 0, -1);
	public void setNormalDir(Vector3d normalDir) { this.normalDir.set(normalDir); }
	
	/** The upwards direction, which is aligned with the light's height axis. */
	protected final Vector3d upDir = new Vector3d(0, 1, 0);
	public void setUpDir(Vector3d upDir) { this.upDir.set(upDir); }
	
	/** The height of the source, in world units. */
	protected double height = 1.0;
	public void setHeight(double height) { this.height = height; }
	
	/** The width of the source, in world units. */
	protected double width = 1.0;
	public void setWidth(double width) { this.width = width; }
	
	/*
	 * Derived values that are computed at initialization time.
	 * basisU, basisV, and basisW form an orthonormal basis.
	 * basisW is parallel to normalDir.
	 */
	protected final Vector3d basisU = new Vector3d();
	protected final Vector3d basisV = new Vector3d();
	protected final Vector3d basisW = new Vector3d();

	/**
	 * Perform one-time setup for this light source: precompute a basis, and add linked
	 * geometry to the scene.
	 */
	public void init(Scene scene) {

		// Set the 3 basis vectors in the orthonormal basis, 
        //    based on normalDir and upDir
		basisW.set(normalDir).negate().normalize();
	    basisU.set(upDir).cross(basisW).normalize();
	    basisV.set(basisW).cross(basisU).normalize();
	    
	    // Add geometry to the scene so that this area source can be hit by rays
	    OBJMesh rect = new OBJMesh();
	    try {
			rect.parseOBJFromString(
					"v -0.5 -0.5 0\n" +
					"v  0.5 -0.5 0\n" +
					"v  0.5  0.5 0\n" +
					"v -0.5  0.5 0\n" +
					"f 1 2 3\n" + 
					"f 3 1 4\n"
					);
		} catch (IOException e) {
			// Will not happen if the above string is valid OBJ
			e.printStackTrace();
		}
	    
	    Mesh geom = new Mesh(rect);
	    Matrix4d tMat = Matrix4d.createLookAt(position, position.clone().add(normalDir), upDir).invert();
	    tMat.mulBefore(Matrix4d.createScale(width, height, 1.0));
	    Matrix4d tMatInv = new Matrix4d(tMat).invert();
	    Matrix4d tMatTInv = new Matrix4d(tMat).transpose().invert();
	    
	    geom.setTransformation(tMat, tMatInv, tMatTInv);
	    
	    geom.setBSDF(new LambertianBSDF(new Colord(0.0, 0.0, 0.0)));
	    geom.setLight(this);
	    
	    scene.addSurface(geom);
	}

	@Override
	public void eval(Ray shadowRay, Colord outRadiance) {
		if (shadowRay.direction.dot(normalDir) < 0.0)
			outRadiance.set(intensity);
		else
			outRadiance.set(0.0);
	}

	/*
	 * Sample the illumination due to this light source at a given shading point.
	 * A rectangle light provides illumination from a range of directions; a direction
	 * is chosen by selecting a point on the source.  A rectangle source is one-sided;
	 * it provides no illumination to points that are behind it.
	 *
	 *    lRec.direction is the direction from the shading point to the source
	 *    lRec.distance is the distance between the shading point and the source
	 *    lRec.attenuation includes both the inverse squared distance and the cosine with the source's normal
	 *    lRec.probability is a probability density over the source's area.
	 *
	 * @see ray2.light.Light#sample(ray2.light.LightSamplingRecord, egl.math.Vector3d)
	 */
	@Override
	public void sample(LightSamplingRecord lRec, Vector3d shadingPoint) {
		Vector3d lightPoint = position.clone()
			.addMultiple(width * (Math.random() - 0.5), basisU)
			.addMultiple(height * (Math.random() - 0.5), basisV);
		lRec.direction.set(lightPoint).sub(shadingPoint);
		lRec.distance = lRec.direction.len();
		lRec.direction.normalize();
		lRec.attenuation = Math.max(0, lRec.direction.dot(basisW)) / shadingPoint.distSq(lightPoint);
		lRec.probability = 1.0 / (width * height);
	}

	@Override
	public double pdf(Ray shadowRay) {
		return 1.0 / (width * height);
	}
	
	@Override
	public double areaToSolidAnlge(Vector3d shadingPoint) {
		Vector3d lightPoint = position.clone()
				.addMultiple(width * (Math.random() - 0.5), basisU)
				.addMultiple(height * (Math.random() - 0.5), basisV);
		Vector3d direction = lightPoint.sub(shadingPoint);
		double factor = Math.max(0, direction.dot(basisW)) / shadingPoint.distSq(lightPoint);
		return 1.0 / factor;
	}
	

	/**
	 * Default constructor.  Produces a unit square light at the origin facing -z.
	 */
	public RectangleLight() { }
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		
		return "RectangleLight: " + width + "x" + height + " @ " + position + " " + intensity + "; normal " + normalDir + "; up " + upDir;
	}
}