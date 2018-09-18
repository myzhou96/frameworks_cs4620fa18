package ray1.shader;

import ray1.IntersectionRecord;
import ray1.Ray;
import ray1.Scene;
import egl.math.Colorf;

public class RGBNormals extends Shader {

	public RGBNormals() { }

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "RGB surface normal encoding shader";
	}

	/**
	 * XYZ component to RGB encoding of the normal vector at the intersection point.
	 * 
	 * @param outRadiance The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 */
	public void shade(Colorf outRadiance, Scene scene, Ray ray, IntersectionRecord record, int depth) {
		outRadiance.x = (float) (record.normal.x + 1) / 2;
		outRadiance.y = (float) (record.normal.y + 1) / 2;
		outRadiance.z = (float) (record.normal.z + 1) / 2;
	}
}