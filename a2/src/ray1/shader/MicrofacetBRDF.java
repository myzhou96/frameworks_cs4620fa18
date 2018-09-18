package ray1.shader;

import egl.math.Colorf;
import egl.math.Vector2;
import egl.math.Vector3;
import egl.math.Vector3d;

/**
 * This interface specifies what is necessary for BRDF function object
 * @author zechen
 */
public abstract class MicrofacetBRDF extends BRDF {
		
	/** refractive index of material, used in Fresnel factor*/
	protected float nt;
	
	/** The roughness parameter of the microfacet model */
	protected float alpha;
	
	/** A scale factor that scales the nondiffuse part. */
	final protected Vector3 scaleFactor = new Vector3();
	
	MicrofacetBRDF(Colorf diffuseReflectance, Texture diffuseReflectanceTexture, Colorf microfacetColor, float nt, float alpha) {
		super(diffuseReflectance, diffuseReflectanceTexture);
		this.scaleFactor.set(microfacetColor);
		this.nt = nt;
		this.alpha = alpha;
	}

	public String toString() {    
		return "Microfacet BRDF, alpha = " + alpha + 
				", nt = " + nt + 
				", scaleFactor = " + scaleFactor + super.toString();
	}

	@Override
	public void evalBRDF(Vector3d incoming, Vector3d outgoing, Vector3d surfaceNormal, Vector2 texCoords, Colorf BRDFValue) 
	{
		// TODO#A2: Evaluate the BRDF value, including both the specular and diffuse part.
		// The specular part is scaled by "scaleFactor" before being combined with diffuse part.
		// Hint: getDiffuseReflectance() method can be helpful.
		// Hint: put the common part here and factor out the parts that are different 
		//       into an abstract method implemented in the subclasses.

	}
	
	public abstract float EvalSpecular(Vector3d incoming, Vector3d outgoing, Vector3d surfaceNormal);
	
	/**
	 * 
	 * @param IncomingVec: direction unit vector of incoming ray
	 * @param HalfVec: direction unit vector of half vector between incoming and outgoing ray
	 * @return evaluated Fresnel factor
	 */
	public final float EvalFresnelFactor(Vector3d incomingVec, Vector3d halfVec)
	{
		// TODO#A2 (extra credit): Implement this helper method to be used by the two subclasses

		return 0.0f;
	}

}