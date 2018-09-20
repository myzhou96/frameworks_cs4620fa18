package ray1.shader;

import egl.math.Colorf;
import egl.math.Vector3d;
import egl.math.Vector3;
import egl.math.Vector2;
import ray1.IntersectionRecord;
import ray1.Light;
import ray1.Ray;
import ray1.RayTracer;
import ray1.Scene;

public abstract class ReflectionShader extends Shader {

	/** BEDF used by this shader. */
	protected BRDF brdf = null;

	/** Coefficient for mirror reflection. */
	protected final Colorf mirrorCoefficient = new Colorf();
	public void setMirrorCoefficient(Colorf mirrorCoefficient) { this.mirrorCoefficient.set(mirrorCoefficient); }
	public Colorf getMirrorCoefficient() {return new Colorf(mirrorCoefficient);}

	public ReflectionShader() {
		super();
	}

	/**
	 * Evaluate the intensity for a given intersection using the Microfacet shading model.
	 *
	 * @param outRadiance The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	@Override
	public void shade(Colorf outRadiance, Scene scene, Ray ray, IntersectionRecord record, int depth) {
		// TODO#A2: Fill in this function.
		// 1) Loop through each light in the scene.
		// 2) If the intersection point is shadowed, skip the calculation for the light.
		//	  See Shader.java for a useful shadowing function.
		// 3) Compute the incoming direction by subtracting
		//    the intersection point from the light's position.
		// 4) Compute the color of the point using the shading model. 
		//	  EvalBRDF method of brdf object should be called to evaluate BRDF value at the shaded surface point.
		// 5) Add the computed color value to the output.
		// 6) If mirrorCoefficient is not zero vector, add recursive mirror reflection
		//		6a) Compute the mirror reflection ray direction by reflecting the direction vector of "ray" about surface normal
		//		6b) Construct mirror reflection ray starting from the intersection point (record.location) and pointing along 
		//			direction computed in 6a) (Hint: remember to call makeOffsetRay to avoid self-intersecting)
		// 		6c) call RayTracer.shadeRay() with the mirror reflection ray and (depth+1)
		// 		6d) add returned color value in 6c) to output
		outRadiance.setZero();
		//used to be in the if scene.getAny statement, see if it changed anything
		Vector3d outgoing = ray.direction.clone().negate().normalize();
		Vector3d surfaceNormal = record.normal.clone().normalize();
		Vector2 texCoords = new Vector2(record.texCoords.clone().normalize());
		for(Light light: scene.getLights()){
			Vector3d incoming = new Vector3d(light.position.clone().sub(new Vector3(record.location)).normalize());
			Ray shadowRay = new Ray(record.location.clone(), incoming);
			//Better way to find scalar????
			double lightT = (light.position.x - shadowRay.origin.x)/shadowRay.direction.x;
			shadowRay.makeOffsetSegment(lightT);

			if(!scene.getAnyIntersection(shadowRay)){	
				Colorf brdfValue = new Colorf(1, 1, 1);
				brdf.evalBRDF(incoming, outgoing, surfaceNormal, texCoords, brdfValue);
				float distLight = light.position.clone().distSq(new Vector3(record.location.clone()));
				
//				System.out.println("brdf value: " + brdfValue);
//				System.out.println("intersection: " + record.location);
//				System.out.println("light position: " + light.position);
//				System.out.println("distance of light: " + distLight);
//				System.out.println("surfaceNormal: " + surfaceNormal);
//				System.out.println("incoming: " + incoming);
				
				Vector3 L_vect = brdfValue.clone().mul((float) Math.max(surfaceNormal.clone().dot(incoming), 0))
						.mul(light.intensity.clone().div(distLight));
				Colorf L = new Colorf(L_vect.x, L_vect.y, L_vect.z);
//				System.out.println("color: " + L);
				outRadiance.add(L);

			}
		}
		if(!mirrorCoefficient.isZero()){
//			Vector3d surfaceNormal = record.normal.clone().normalize();
//			Vector3d outgoing = ray.direction.clone().negate().normalize();
			Vector3d reflectionDir = surfaceNormal.clone().mul(surfaceNormal.clone().dot(outgoing)*2).sub(outgoing).normalize();
			Ray reflection = new Ray(record.location.clone(), reflectionDir);
			reflection.makeOffsetRay();
			Colorf reflectionColor = new Colorf();
			RayTracer.shadeRay(reflectionColor, scene, reflection, depth+1);
			outRadiance.add(reflectionColor.mul(mirrorCoefficient));
		}
	}

}