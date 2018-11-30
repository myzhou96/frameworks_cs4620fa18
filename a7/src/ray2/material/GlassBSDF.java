package ray2.material;

import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;

/**
 * Glass BSDF
 *
 *@author mx
 */
public class GlassBSDF extends BSDF {
	
	/** The index of refraction of this material. Used when calculating Fresnel factor. */
	protected double refractiveIndex = 1.5;
	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }
		
	public String toString() {    
		return "glass " + refractiveIndex + " end";
	}
	
	public GlassBSDF() {
	}
	
	public GlassBSDF(double refractiveIndex) {
		this.refractiveIndex = refractiveIndex;
	}
	
	/* (non-Javadoc)
	 * @see ray2.material.BSDF#eval(egl.math.Vector3d, egl.math.Vector3d, egl.math.Vector3d, egl.math.Colord)
	 */
	@Override
	public
	void eval(Vector3d dir1, Vector3d dir2, Vector3d normal, Colord outValue) {
		outValue.setZero();
	}
	
	/* (non-Javadoc)
	 * @see ray2.material.BSDF#sample(ray2.material.BSDFSamplingRecord, egl.math.Vector2d, egl.math.Colord)
	 */
	@Override
	public
	double sample(BSDFSamplingRecord sampleRecord, Vector2d seed, Colord outValue) {
		
		double prob = 0.0;
		
		Vector3d IncomingVec = sampleRecord.dir1;
		outValue.set(0);
		
		double R = 0;
		
		// Determine whether the ray is coming from the inside of the
		// surface or from the outside.
		boolean inside = IncomingVec.dot(sampleRecord.normal) <= 0;
		Vector3d normal = sampleRecord.normal;
		
		double n1, n2;
		// Decides n1, n2
		if(inside) {
			n1 = refractiveIndex;
			n2 = 1;
			normal.negate();
		} else {
			n1 = 1;
			n2 = refractiveIndex;
		}

		double cos_1 = IncomingVec.dot(normal);
		double cos_2 = 0;
		// Total internal reflection
		boolean totalInternalReflection = 1-n1*n1*(1-cos_1*cos_1) / (n2*n2)< 0; 
		if (totalInternalReflection) {
			R = 1;
		} else {
			cos_2 = Math.sqrt(1-n1*n1*(1-cos_1*cos_1) / (n2*n2));
			R = fresnel(normal, IncomingVec, n2/n1);
		}
		
		if (seed.x <= R) {
			// Compute reflected ray direction
			sampleRecord.dir2.addMultiple(2*cos_1, normal).sub(IncomingVec).normalize();
			outValue.set(R).div(cos_1);
			// probability R if refracted 
			prob = R;
		} else {
			// Compute transmitted ray if not total internal reflection.
			if (!totalInternalReflection)
			{
				sampleRecord.dir2.set(normal).mul(cos_1)
										  .sub(IncomingVec)
										  .mul(n1/n2)
										  .addMultiple(-cos_2, normal)
										  .normalize();
				outValue.set(1 - R).div(cos_2);	
				// probability (1-R) if refracted 
				prob = (1 - R);
			}
		}
		
		sampleRecord.isDiscrete = true;
		return prob;
	}
	
	/* (non-Javadoc)
	 * @see ray2.material.BSDF#pdf(egl.math.Vector3d, egl.math.Vector3d, egl.math.Vector3d)
	 */
	@Override
	public
	double pdf(Vector3d dir1, Vector3d dir2, Vector3d normal) {
		return 0.0;
	}

	@Override
	public Colord getDiffuseReflectance() {
		return Colord.BLACK;
	}
	

}
