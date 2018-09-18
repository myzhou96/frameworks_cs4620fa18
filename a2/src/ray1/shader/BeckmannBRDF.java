package ray1.shader;

import ray1.shader.MicrofacetBRDF;
import egl.math.Colorf;
import egl.math.Vector3d;

/**
 * Microfacet BRDF function with Beckmann distribution
 * @author zechen
 */
public class BeckmannBRDF extends MicrofacetBRDF
{

	BeckmannBRDF(Colorf diffuseReflectance, Texture diffuseReflectanceTexture, Colorf microfacetColor, float nt, float alpha) {
		super(diffuseReflectance, diffuseReflectanceTexture, microfacetColor, nt, alpha);
	}

	public String toString() {    
		return "Beckmann " + super.toString();
	}
	
	/**
	 * Evaluate the specular part of BRDF function value in microfacet model with Beckmann distribution
	 *
	 * @param IncomingVec Direction vector of the incoming ray.
	 * @param OutgoingVec Direction vector of the outgoing ray.
	 * @param SurfaceNormal Normal vector of the surface at the shaded point.
	 * @return evaluated BRDF function value
	 */
	public float EvalSpecular(Vector3d IncomingVec, Vector3d OutgoingVec, Vector3d SurfaceNormal)
	{
		// TODO#A2: Evaluate the specular part of BRDF function of microfacet-based model with Beckmann distribution
		// Walter, Bruce, et al. 
		// "Microfacet models for refraction through rough surfaces." 
		// Proceedings of the 18th Eurographics conference on Rendering Techniques. Eurographics Association, 2007.
		
		return 0.0f;
	}
	
}
