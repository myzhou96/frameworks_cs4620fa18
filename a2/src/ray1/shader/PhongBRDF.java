package ray1.shader;

import egl.math.Color;
import egl.math.Colorf;
import egl.math.Vector2;
import egl.math.Vector3d;

public class PhongBRDF extends BRDF {

	/** The color of the specular reflection. */
	protected final Colorf specularColor = new Colorf(Color.White);

	/** The exponent controlling the sharpness of the specular reflection. */
	protected float exponent = 1.0f;

	public String toString() {    
		return "Phong BRDF" + 
				", specularColor = " + specularColor + 
				", exponent = " + exponent + super.toString();
	}
	
	PhongBRDF(Colorf diffuseReflectance, Texture diffuseReflectanceTexture, Colorf specularColor, float exponent) 
	{
		super(diffuseReflectance, diffuseReflectanceTexture);
		this.specularColor.set(specularColor);
		this.exponent = exponent;
	}

	@Override
	public void evalBRDF(Vector3d incoming, Vector3d outgoing, Vector3d surfaceNormal, Vector2 texCoords, Colorf BRDFValue) 
	{	
		// TODO#A2: Evaluate the BRDF value of the modified Blinn-Phong reflection model, 
		//          including both the specular and the diffuse part
//		System.out.println("k_s: " + specularColor);
//		System.out.println("exponent: " + exponent);
		Vector3d diffuseReflectance = new Vector3d(getDiffuseReflectance(texCoords).clone());
		Vector3d diffuseCo = diffuseReflectance.div(Math.PI);
//		System.out.println("diffuseCo: " + diffuseCo);
		
		Vector3d h_top = incoming.clone().add(outgoing);
		Vector3d h = h_top.normalize();
//		System.out.println("h: " + h);
		
//		float max = (float)Math.pow(Math.max(surfaceNormal.clone().dot(h), 0), exponent);
		float specTerm = (float)Math.pow(Math.max(surfaceNormal.clone().dot(h), 0), exponent);
		Vector3d test = new Vector3d(specularColor.clone().mul(specTerm));
		Vector3d vect = diffuseCo.add(test);
//		Vector3d vect = diffuseCo.add(new Vector3d(specularColor.clone().mul(max)));
//		System.out.println("phong val: " + vect);
		BRDFValue.set((float)vect.x, (float)vect.y, (float)vect.z);
		
	}

}
