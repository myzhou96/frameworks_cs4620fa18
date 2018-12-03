package ray2.integrator;

import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;
import ray2.IntersectionRecord;
import ray2.Ray;
import ray2.RayTracer;
import ray2.Scene;
import ray2.light.Environment;
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
public class LightSamplingIntegrator extends Integrator {

	/*
	 * The illumination algorithm is:
	 *
	 *   0. light source emission:
	 *      if the surface is a light source:
	 *        add the source's radiance
	 *   1. light sources:
	 *      for each light in the scene
	 *        choose a point on the light
	 *        evaluate the BRDF
	 *        do a shadow test
	 *        compute the estimate of this light's contribution
	 *          as (source radiance) * brdf * attenuation * (cos theta) / pdf, and add it
	 *   2. environment:
	 *      choose a direction from the environment
	 *      evaluate the BRDF
	 *      do a shadow test
	 *      compute the estimate of the environment's contribution
	 *        as (env radiance) * brdf * (cos theta) / pdf, and add it
	 *   3. mirror reflections and refractions:
	 *      choose a direction from the BSDF, continuing only if it is discrete
	 *      trace a recursive ray
	 *      add the recursive radiance weighted by (cos theta) * (brdf value) / (probability)
	 *
	 * Step 3 is violating the idea of light source sampling a bit, but it is needed because it's impossible
	 * to choose a light source point exactly in the reflection or refraction direction, and we do like to be
	 * able to see the reflections.  By making the recursive call only for directions chosen discretely
	 * (that is, directions belonging to perfectly sharp reflection and refraction components) we are leaving out
	 * diffuse and glossy interreflections.
	 *
	 * In Step 1 note that the attenuation includes the inverse square law and also the cosine at the source's
	 * end of the ray that is required by the illumination integral for an area source.  This is taken care of
	 * by the Light subclasses: Point light sets its attenuation to 1 / r^2 whereas RectangleLight sets the
	 * attenuation to (cos theta_source) / r^2.
	 *
	 * @see ray2.integrator.Integrator#shade(egl.math.Colord, ray2.Scene, ray2.Ray, ray2.IntersectionRecord, int)
	 */
	@Override
	public void shade(Colord outRadiance, Scene scene, Ray ray, IntersectionRecord iRec, int depth) {
		// TODO#A7: Calculate outRadiance at current shading point.
	    // You need to add contribution from each light,
	    // add contribution from environment light if there is any.
	    // add mirror reflection and refraction.
		for (Surface s : scene.getSurfaces()) {
			if (s != null) {
				Light l = s.getLight();
				if (l != null) {
					// add radiance
					LightSamplingRecord record = new LightSamplingRecord();
					l.sample(record, iRec.location);
					
					if (!isShadowed(scene, record, iRec, ray)) {
						Colord srcRadiance = new Colord();
						Colord brdf = new Colord();
						l.eval(ray, srcRadiance); //sourceRadiance
						s.getBSDF().eval(record.direction, iRec.location.normalize().mul(-1), iRec.normal, brdf);
//						
						//View Direction or referred to as incoming
						Vector3d direction = ((PointLight) l).getPosition().clone().sub(iRec.location).normalize();
						//value of theta
						double nDotL = iRec.normal.dot(direction);
						outRadiance.add(srcRadiance.clone().mul(brdf)
								.mul(record.attenuation).mul(Math.cos(nDotL)/record.probability));
						//(source radiance) * brdf * attenuation * (cos theta) / pdf, and add it
						//continue; // ???
					}
				}
			}
		}
		 
	}

	/**
	 * A utility method to check if there is any surface between the given intersection
	 * point and the given light. shadowRay is set to point from the intersection point
	 * towards the light.
	 *
	 * @param scene The scene in which the surface exists.
	 * @param light A light in the scene.
	 * @param iRec The intersection point on a surface.
	 * @param shadowRay A ray that is set to point from the intersection point towards
	 * the given light.
	 * @return true if there is any surface between the intersection point and the light;
	 * false otherwise.
	 */
	protected boolean isShadowed(Scene scene, LightSamplingRecord lRec, IntersectionRecord iRec, Ray shadowRay) {
		// Setup the shadow ray to start at surface and end at light
		shadowRay.origin.set(iRec.location);
		shadowRay.direction.set(lRec.direction);

		// Set the ray to end at the light
		shadowRay.direction.normalize();
		shadowRay.makeOffsetSegment(lRec.distance);

		return scene.getAnyIntersection(shadowRay);
	}

}
