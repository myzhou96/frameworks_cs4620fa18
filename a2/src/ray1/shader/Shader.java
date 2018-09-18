package ray1.shader;

import ray1.IntersectionRecord;
import ray1.Light;
import ray1.Ray;
import ray1.Scene;
import egl.math.Colorf;

/**
 * This interface specifies what is necessary for an object to be a shader.
 * @author ags, pramook, zechenz
 */
public abstract class Shader {
	
	/**
	 * The material given to all surfaces unless another is specified.
	 */
	public static final Shader DEFAULT_SHADER = new Lambertian();
	
	protected Texture texture = null;
	public void setTexture(Texture t) { texture = t; }
	public Texture getTexture() { return texture; }
	
	/**	
	 * Calculate the intensity (color) for this material at the intersection described in
	 * the record contained in workspace.
	 * 	 
	 * @param outRadiance The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	public abstract void shade(Colorf outRadiance, Scene scene, Ray ray, IntersectionRecord record, int depth);
	
	/**
	* Initialize method
	*/
	public void init() {
		// do nothing
	};
	
}