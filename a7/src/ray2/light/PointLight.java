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
 * @author ags, zechenz
 */
public class PointLight extends Light {
	
	/** Where the light is located in space. */
	public final Vector3d position = new Vector3d();
	public void setPosition(Vector3d position) { this.position.set(position); }
	
	/** Point lights make their position and intensity available. */
	public Vector3d getPosition() { return position; }
	public Vector3d getIntensity() { return intensity; }

	public void init(Scene scene) {
		// do nothing
	}

	@Override
	public void eval(Ray shadowRay, Colord outRadiance) {
		outRadiance.set(intensity);
	}

	/**
	 * Sample the illumination due to this light source at a given shading point.
	 * A point light provides illumination from a single direction; the outputs are:
	 *
	 *    lRec.direction is the direction from the shading point to the source
	 *    lRec.distance is the distance between the shading point and the source
	 *    lRec.attenuation is the inverse square of the distance to the source
	 *    lRec.probability is 1.0 because the same direction is always chosen
	 *
	 * @param record the record where the output is written:
	 * @param shadingPoint the surface point where illumination is being computed
	 */
	@Override
	public void sample(LightSamplingRecord lRec, Vector3d shadingPoint) {
		lRec.direction.set(position).sub(shadingPoint);
		lRec.attenuation = 1.0 / shadingPoint.distSq(this.position);
		lRec.distance = lRec.direction.len();
		lRec.probability = 1.0;
	}
	
	@Override
	public double pdf(Ray shadowRay) {
		// always 1 for a point source.
		return 1.0;
	}
	
	@Override
	public double areaToSolidAnlge(Vector3d shadingPoint) {
		return shadingPoint.distSq(this.position);
	}
	
	/**
	 * Default constructor.  Produces a unit intensity light at the origin.
	 */
	public PointLight() { }
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		
		return "PointLight: " + position + " " + intensity;
	}

}