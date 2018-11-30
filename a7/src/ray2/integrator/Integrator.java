package ray2.integrator;

import egl.math.Colord;
import ray2.IntersectionRecord;
import ray2.Ray;
import ray2.Scene;

/**
 * An Integrator encapsulates an algorithm for shading surfaces. Different integrators
 * will have different strategies for estimating reflected radiance, and they may also
 * have different policies about what illumination to include (such as only paying attention
 * to certain kinds of light sources, or omitting certain types of interreflections.
 * 
 * @author srm
 */
public abstract class Integrator {
	
	public static final Integrator DEFAULT_INTEGRATOR = new LightSamplingIntegrator(); 
	
	/**
	 * Compute the reflected radiance for a ray intersection.  Implementations can do 
	 * pretty much whatever they want, but to compute realistic results they will make
	 * use of the BSDF associated with the surface (iRec.surface.getBSDF()) and will
	 * use the scene to do ray intersections for shadow and reflection rays, and to 
	 * find out about light sources in the scene.  Most integrators will use the
	 * depth parameter to track and limit recursion depth.
	 * 
	 * @param outRadiance The radiance reflected to the ray
	 * @param scene The scene containing sources and geometry
	 * @param ray The ray being shaded
	 * @param iRec Record with information about the ray intersection
	 * @param depth The recursion depth of the ray
	 */
	public abstract void shade(Colord outRadiance, Scene scene, Ray ray, 
			IntersectionRecord iRec, int depth);

	public void init() { }
}
