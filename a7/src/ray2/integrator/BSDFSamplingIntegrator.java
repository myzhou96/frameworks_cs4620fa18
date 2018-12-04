package ray2.integrator;

import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;
import ray2.IntersectionRecord;
import ray2.Ray;
import ray2.RayTracer;
import ray2.Scene;
import ray2.light.Light;
import ray2.light.LightSamplingRecord;
import ray2.light.PointLight;
import ray2.material.BSDF;
import ray2.material.BSDFSamplingRecord;
import ray2.surface.Surface;

/**
 * An Integrator that works by sampling light sources.  It accounts for light that illuminates all surfaces
 * directly from point or area sources, and from the environment.  It also includes recursive reflections
 * for polished surfaces (Glass and Glazed), but not for other surfaces.
 *
 * @author srm
 */
public class BSDFSamplingIntegrator extends Integrator {

	/*
	 * The illumination algorithm is:
	 *
	 *   0. light source emission:
	 *      if the surface is a light source:
	 *        add the source's radiance
	 *   1. reflected radiance:
	 *      generate a sample from the BSDF
	 *      trace a ray in that direction
	 *      if you hit nothing:
	 *        look up incident radiance from the environment
	 *      if you hit a surface:
	 *        for discrete directions, shade the ray recursively to get incident radiance
	 *        for non-discrete, incident radiance is source radiance if you hit a source (else 0)
	 *      compute the estimate for reflected radiance as incident radiance * brdf * cos theta / pdf
	 *   2. point light source:
	 *      for each point light in the scene:
	 *        compute the light direction and distance
	 *        evaluate the BRDF
	 *        add a contribution to the reflected radiance due to that source
	 *
	 * For this integrator, step 1 automatically includes light from all sources (area sources and
	 * the environment) but step 2 is needed because point lights can't be hit by rays.
	 *
	 * In step 1, by making the recursive call only for directions chosen discretely (that is,
	 * directions belonging to perfectly sharp reflection and refraction components) we are leaving
	 * out diffuse and glossy interreflections.
	 *
	 * @see ray2.integrator.Integrator#shade(egl.math.Colord, ray2.Scene, ray2.Ray, ray2.IntersectionRecord, int)
	 */
	@Override
	public void shade(Colord outRadiance, Scene scene, Ray ray, IntersectionRecord iRec, int depth) {
	   // TODO#A7: Calculate outRadiance at current shading point
       // You need to add contribution from source emission if the current surface has a light source,
       // generate a sample from the BSDF,
       // look up lighting in that direction and get incident radiance.
       // Before you calculate the reflected radiance, you need to check whether the probability value
       // from bsdf sample is 0.
		
		//incoming/light: light - intersection (going from light towards the intersection)
		//outgoing/view: ray.direction.clone().negate() 
		//ray is from point to the camera
		
		/* 0. light source emission:
    	 * if the surface is a light source:
    	 * add the source's radiance*/
		Light lightSource = iRec.surface.getLight();
		if(lightSource != null){
			//add source radiance, check which ray to put into eval
			Colord sourceRadiance = new Colord();
			lightSource.eval(ray, sourceRadiance); 
			outRadiance.add(sourceRadiance);
		}
		
		 /* 1. reflected radiance:
	      * generate a sample from the BSDF
		  * trace a ray in that direction
		  * if you hit nothing:
		  * look up incident radiance from the environment
		  * if you hit a surface:
	      *        for discrete directions, shade the ray recursively to get incident radiance
		  *        for non-discrete, incident radiance is source radiance if you hit a source (else 0)
	      * compute the estimate for reflected radiance as incident radiance * brdf * cos theta / pdf */
		
		// generating sample from BSDF, should definitely be view ray + surface normal
		BSDFSamplingRecord bsdfRecord = new BSDFSamplingRecord(ray.direction.clone().negate().normalize(), iRec.normal); 
		Colord outColor = new Colord();
		Vector2d seed = new Vector2d(Math.random(), Math.random());
		double brdfPdf = iRec.surface.getBSDF().sample(bsdfRecord, seed, outColor); //outColor here is our brdf
		
		//trace the ray, is this the right origin???
		Ray bsdfRay = new Ray(iRec.location.clone(), bsdfRecord.dir2.clone());
		bsdfRay.makeOffsetRay();
		Colord incidentRad = new Colord();
		
		//If you hit a surface
		IntersectionRecord iRec2 = new IntersectionRecord();
		if(scene.getFirstIntersection(iRec2, bsdfRay)){
			//for discrete directions, shade the ray recursively to get incident radiance
			if(bsdfRecord.isDiscrete){
				RayTracer.shadeRay(incidentRad, scene, bsdfRay, depth + 1);
//				this.shade(incidentRad, scene, ray, iRec2, depth);
			}
			else{
				//for non-discrete, incident radiance is source radiance if you hit a source (else 0)
				if(iRec2.surface.getLight() != null){
					iRec2.surface.getLight().eval(bsdfRay, incidentRad);
				}
				else{
					incidentRad = new Colord();
				}
			}
		}
		else{
			//hit nothing, look up incident radiance from the environment, not sure if direction is correct
			if(scene.getEnvironment() != null){
				scene.getEnvironment().eval(bsdfRecord.dir2, incidentRad);
			}
			
		}
		//compute the estimate for reflected radiance as incident radiance * brdf * cos theta / pdf
		//Vector3d direction = ((PointLight) l).getPosition().clone().sub(iRec.location).normalize();
		//cosTheta is incorrect
		double cosTheta = Math.abs(iRec.normal.clone().normalize().dot(bsdfRecord.dir2.clone().normalize()));
		outRadiance.add(incidentRad.clone().mul(outColor).mul(cosTheta).div(brdfPdf));
		
		
		 /* 2. point light source:
		  *      for each point light in the scene:
		  *        compute the light direction and distance
		  *        evaluate the BRDF
		  *        add a contribution to the reflected radiance due to that source */
		for (Light l : scene.getLights()) {
			if (l instanceof PointLight) {
				if (!isShadowed(scene, iRec.location, ((PointLight) l).getPosition())) {
					double distanceSq = ((PointLight) l).getPosition().distSq(iRec.location);
					//View Direction or referred to as incoming
					Vector3d direction = ((PointLight) l).getPosition().clone().sub(iRec.location).normalize();
					double nDotL = iRec.normal.clone().normalize().dot(direction);
					if (nDotL <= 0.0) {
						outRadiance.set(Colord.BLACK);
					}
					else {
						Colord outBSDF = new Colord();
						iRec.surface.getBSDF().eval(direction, ray.direction.clone().negate(), iRec.normal.clone(), outBSDF);
						Colord intensity = new Colord();
						l.eval(ray, intensity); // intensity
						Colord L = new Colord(intensity.mul(outBSDF).mul(nDotL).div(distanceSq));
						outRadiance.add(L);
					}
				}
			}
		}
				
	}
		


	/**
	 * A utility method to check if there is any surface between the given intersection
	 * point and the given light.
	 *
	 * @param scene The scene in which the surface exists.
	 * @param light A light in the scene.
	 * @param iRec The intersection point on a surface.
	 * @param shadowRay A ray that is set to point from the intersection point towards
	 * the given light.
	 * @return true if there is any surface between the intersection point and the light;
	 * false otherwise.
	 */
	protected boolean isShadowed(Scene scene, Vector3d shadingPoint, Vector3d lightPosition) {

		Ray shadowRay = new Ray();

		// Setup the shadow ray to start at surface and end at light
		shadowRay.origin.set(shadingPoint);
		shadowRay.direction.set(lightPosition).sub(shadingPoint);

		// Set the ray to end at the light
		shadowRay.makeOffsetSegment(shadowRay.direction.len());
		shadowRay.direction.normalize();

		return scene.getAnyIntersection(shadowRay);
	}

}
