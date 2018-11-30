package ray2.material;

import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;

/**
 * A Bidirectional Scattering Distribution Function.  This class describes the light reflection and transmission
 * through an interface between two materials (typically between a material and air).  For opaque materials this
 * is a bidirectional reflectance distribution function (BRDF) since no light transmits through the interface.
 * 
 * The key method in a BSDF is eval: it evaluates the BSDF for a given incoming and outgoing direction, answering
 * the question "how much light is seen in direction dir1 when the surface is illuminated from direction 
 * dir2?"
 * 
 * Two other functions are crucial for Monte Carlo rendering: sample(), which generates random directions with
 * probability approximately proportional to the BSDF, and pdf(), which gives the probability density with which 
 * sample() generates a given direction.
 *  
 * @author srm
 *
 */
public abstract class BSDF { 

	/**
	 * The material given to all surfaces unless another is specified.
	 */
	public static final BSDF DEFAULT_BSDF = new LambertianBSDF();
	
	/**
	 * Compute the value f_r(dir1, dir2) of the BSDF for a given pair of directions.  
	 * The directions are in world coordinates, so the surface normal is needed to do 
	 * this computation.
	 * 
	 * @param dir1 The first direction (say, the light direction)
	 * @param dir2 The second direction (say, the view direction)
	 * @param normal The surface normal
	 * @param outValue The value of the BSDF
	 */
	public abstract void eval(Vector3d dir1, Vector3d dir2, Vector3d normal, Colord outValue);
	
	/**
	 * Generate a random direction according to this BSDF.  That is, you provide one of the two
	 * directions, dir1, and this method randomly generates the other, using a pdf p(dir2) that is 
	 * approximately proportional to f_r(dir1, dir2).  It also reports the values of
	 * f_r(dir1, dir2) and p(dir2).
	 * 
	 * Samples can come from discrete components, in which a certain direction is generated with
	 * a finite probability, or from continuous components, in which directions are generated over
	 * a continuous range.  For instance, a perfect mirror reflects an incoming ray to a single
	 * outgoing direction, so it would always generate the same direction, and report it as a
	 * discrete direction with probability 1.  On the other hand, a diffuse surface might generate
	 * directions in a cosine distribution, so it would report the continuous (non-discrete) probablity
	 * *density* of cos theta / pi.
	 * 
	 * @param sampleRecord The record encapsulating several of the inputs and outputs:
	 *    sampleRecord.normal: (in) the normal to the surface
	 *    sampleRecord.dir1: (in) the fixed direction in the sampling operation
	 *    sampleRecord.dir2: (out) the varying direction in the sampling operation
	 *    sampleRecord.isDiscrete (out) is this sample from a discrete (mirror-like) component?
	 * @param outValue The BSDF value f(dir1, dir2) for this pair of directions
	 * @return The probability density p(dir2) for choosing dir2 given dir1
	 */
	public abstract double sample(BSDFSamplingRecord sampleRecord, Vector2d seed, Colord outValue);
	
	/**
	 * Compute the probability density with which sample() would choose a direction.  This result
	 * only includes continuous (non-discrete) parts of the distribution, so even if dir2 could
	 * be generated as a discrete selection, this function only returns the density with which it
	 * would be chosen in a continuous way.  
	 * 
	 * This method agrees with sample() in two ways:
	 * first, if we give sample() the direction dir1 and it generates dir2 and returns probability 
	 * density p, then calling pdf() for dir1 and dir2 should also return p.  Second, for any direction
	 * dir2, pdf(dir1, dir2) has to accurately describe the probability density with which sample()
	 * would generate dir2 given dir1.
	 * 
	 * @param dir1 The fixed direction
	 * @param dir2 The direction being asked about
	 * @param normal The surface normal
	 * @return The probability density for choosing dir2 given dir1
	 */
	public abstract double pdf(Vector3d dir1, Vector3d dir2, Vector3d normal);
	
	/**
	 * Return an estimate of the  diffuse reflectance of this surface.
	 * @return the diffuse reflectance of this surface.
	 */
	public abstract Colord getDiffuseReflectance();
	
	
	/**
	 * Utility for computing Fresnel factors
	 * @param normal
	 * @param outgoing
	 * @param refractiveIndex
	 * @return
	 */
	protected double fresnel(Vector3d normal, Vector3d outgoing, double refractiveIndex) {
		double cos_1 = outgoing.dot(normal);
		if (cos_1 < 0)
			return 0;
		
		double cos_2_sq = 1-(1-cos_1*cos_1) / (refractiveIndex*refractiveIndex);
		// Total reflection
		if (cos_2_sq < 0)
			return 1;
		
		double cos_2 = Math.sqrt(cos_2_sq);
		double Fp = (refractiveIndex*cos_1 - cos_2) / (refractiveIndex*cos_1 + cos_2);
		double Fs = (cos_1 - refractiveIndex * cos_2) / (cos_1 + refractiveIndex * cos_2);
		double R = 0.5 * (Fp*Fp + Fs*Fs);

		return R;
	}

}
