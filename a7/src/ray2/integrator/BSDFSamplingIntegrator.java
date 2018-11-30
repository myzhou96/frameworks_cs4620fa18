package ray2.integrator;

import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;
import ray2.IntersectionRecord;
import ray2.Ray;
import ray2.RayTracer;
import ray2.Scene;
import ray2.light.Light;
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
