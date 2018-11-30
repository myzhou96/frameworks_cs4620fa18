package ray2.light;

import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;

/**
 * This class represents a lighting environment -- that is, a function that maps directions of rays
 * leaving the scene to radiance values.  The environment defines the colors of rays that miss all
 * geometry, and it can act as a light source that illuminates objects.
 * 
 * @author srm
 *
 */
public interface Environment {

	/**
	 * Evaluate the environment.  That is, for a given direction, find the radiance
	 * that is seen in that direction.
	 * 
	 * @param dir Lookup direction
	 * @param outRadiance The radiance in direction dir
	 */
	void eval(Vector3d dir, Colord outRadiance);

	/**
	 * Sample the environment.  That is, generate a random direction with a probability
	 * proportional to the environment's radiance in that direction.  Since the environment
	 * radiance is a color, the pdf value has to be derived by somehow combining the three
	 * channels into one.
	 * 
	 * @param seed A pair of uniform random numbers
	 * @param outDirection The selected direction
	 * @param outRadiance The radiance in this direction
	 * @return The pdf (with respect to solid angle) for choosing this direction
	 */
	double sample(Vector2d seed, Vector3d outDirection, Colord outRadiance);

	/**
	 * Compute the probability density of the sample() method for a particular direction.
	 * The result describes the probability of selecting a direction in a small neighborhood
	 * of _dir_, and it is the same value that sample() would return if it selected _dir_.
	 * 
	 * @param dir The direction for which the pdf value is needed
	 * @return The pdf value
	 */
	double pdf(Vector3d dir);

}