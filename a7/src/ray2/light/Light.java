package ray2.light;

import egl.math.Colord;
import egl.math.Vector3d;
import ray2.Ray;
import ray2.Scene;

/**
 * This class represents a basic point light which is infinitely small and emits
 * a constant power in all directions. This is a useful idealization of a small
 * light emitter.
 *
 * @author ags, zechenz, srm
 */
public abstract class Light {
	
	/** How bright the light is. Translates to radiance for area lights and intensity
	 * for point lights. */
	protected final Colord intensity = new Colord(1.0, 1.0, 1.0);
	public void setIntensity(Colord intensity) { this.intensity.set(intensity); }

	// initialization method
	public abstract void init(Scene scene);

	/**
	 * Default constructor.  Produces a unit intensity light.
	 */
	public Light() { }
	
	/**
	 * For point lights, return the intensity of the source.
	 * For area lights, get the radiance of the source along the given ray.  The
	 * Ray points from the surface to the light.  It's assumed that the ray
	 * does hit the light.
	 * 
	 * @param shadowRay The ray along which light exits
	 * @param outRadiance The radiance
	 */
	public abstract void eval(Ray shadowRay, Colord outRadiance);	
	
	/**
	 * Sample the illumination due to this light source at a given shading point.
	 * 
	 * @param record the record where the output is written
	 *   record.attenuation: the 1/r^2 attenuation for this point
	 *   record.direction: the direction from the shading point towards the light point
	 *   record.distance: the distance to the light point
	 *   record.probability: the probability (point lights) or pdf with respect to
	 *     area (for area lights) with which the point was chosen.
	 * @param shadingPoint the surface point where illumination is being computed
	 */
	public abstract void sample(LightSamplingRecord record, Vector3d shadingPoint);
		
	/**
	 * Compute the probability (for point lights) or pdf with respect to area (area 
	 * lights) with which a ray would be sampled.
	 * 
	 * @param shadowRay The ray from the shading point to the light source point
	 * @return The probability of selecting the light source point.
	 */
	public abstract double pdf(Ray shadowRay);
	
	/**
	 * Compute the factor from area to solid angle domain, 
	 * this is equal to attenuation factor
	 * @param shadingPoint
	 * @return The conversion factor
	 */
	public abstract double areaToSolidAnlge(Vector3d shadingPoint);
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		
		return "light: " + intensity;
	}
}