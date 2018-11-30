package ray2.material;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;

/**
 * Microfacet-based shader
 *
 *@author mx
 */
public class MicrofacetBSDF extends BSDF {
	
	/** The color of the diffuse reflection, if there is no texture. */
	protected final Colord diffuseReflectance = new Colord(Color.White);
	public void setDiffuseColor(Colord diffuseColor) { this.diffuseReflectance.set(diffuseColor); }

	/** The color of the specular reflection. */
	protected final Colord specularColor = new Colord(Color.White);
	public void setSpecularColor(Colord specularColor) { this.specularColor.set(specularColor); }
	
	/** The index of refraction of this material. Used when calculating Fresnel factor. */
	protected double refractiveIndex;
	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }
	
	/** The roughness controlling the roughness of the surface. */
	protected double roughness = 1.0;
	public void setRoughness(double roughness) { this.roughness = roughness; }
	
	/** The distribution type of Microfacet
	 *  0 - Beckmann
	 *  1 - GGX . */
	protected int disttype = 0;
	protected MicrofacetDistribution dist;
	public void setDisttype(int disttype) { 
		this.disttype = disttype; 
		if (disttype == 0) {
			this.dist = new Beckmann(roughness, refractiveIndex);
		}else if(disttype == 1) {
			this.dist = new GGX(roughness, refractiveIndex);
		}
	}
	
	/** Default constructor for Microfacet*/
	public MicrofacetBSDF() { 
		setRefractiveIndex(1.5);
		setRoughness(0.5);
		setDisttype(0);
	}
	public MicrofacetBSDF(Colord diffuseReflectance, Colord specularColor, double roughness, double refractiveIndex, int disttype) { 
		if (disttype == 0) {
			this.dist = new Beckmann(roughness, refractiveIndex);
		}else if(disttype == 1) {
			this.dist = new GGX(roughness, refractiveIndex);
		}else System.err.println("Microfacet distribution indicator should be 'B' or 'G' ");
		
		this.refractiveIndex = refractiveIndex;
		this.diffuseReflectance.set(diffuseReflectance);
		this.specularColor.set(specularColor);
	}

	public String toString() {    
		return "MicrofacetBSDF " + diffuseReflectance + " " + specularColor + " " + this.dist.toString() + " end";
	}

	
	/* (non-Javadoc)
	 * @see ray2.material.BSDF#eval(egl.math.Vector3d, egl.math.Vector3d, egl.math.Vector3d, egl.math.Colord)
	 */
	@Override
	public
	void eval(Vector3d dir1, Vector3d dir2, Vector3d normal, Colord outValue) {
		
		if (dir1.dot(normal) <=0 || dir2.dot(normal)<=0) {
			outValue.set(0);
		} else {
			// add diffuse color
			Colord dcolor = new Colord();
			dcolor.setMultiple(1.0/Math.PI,diffuseReflectance);
			outValue.add(dcolor);
			
			// add specular color
			Colord mcolor = new Colord();
			mcolor.set(specularColor);
			outValue.add(mcolor.mul(this.dist.eval(dir1, dir2, normal)));
		}
	}
	
	/* (non-Javadoc)
	 * @see ray2.material.BSDF#sample(ray2.material.BSDFSamplingRecord, egl.math.Vector2d, egl.math.Colord)
	 */
	@Override
	public
	double sample(BSDFSamplingRecord sampleRecord, Vector2d seed, Colord outValue) {
		
		// add specular color
		Colord bsdf = new Colord();
		Colord mcolor = new Colord();
		mcolor.set(specularColor);
		double prob = dist.sample(sampleRecord, seed, bsdf);
		
		if (sampleRecord.dir1.dot(sampleRecord.normal)>0 && 
				sampleRecord.dir2.dot(sampleRecord.normal)>0) {
			// add diffuse color
			Colord dcolor = new Colord();
			dcolor.setMultiple(1.0/Math.PI,diffuseReflectance);
			outValue.add(dcolor);
			outValue.add(mcolor.mul(bsdf));
		} else {
			prob = 0;
			outValue.set(0);
		}
		
		return prob;
	}
	
	/* (non-Javadoc)
	 * @see ray2.material.BSDF#pdf(egl.math.Vector3d, egl.math.Vector3d, egl.math.Vector3d)
	 */
	@Override
	public
	double pdf(Vector3d dir1, Vector3d dir2, Vector3d normal) {
		double prob = 0;
		if (dir1.dot(normal)>0 && dir2.dot(normal)>0){
			prob = dist.pdf(dir1, dir2, normal);
		}
		return prob;
	}

	@Override
	public Colord getDiffuseReflectance() {
		return diffuseReflectance;
	}
	

}
