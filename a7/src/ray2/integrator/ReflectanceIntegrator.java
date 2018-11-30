package ray2.integrator;

import egl.math.Colord;
import ray2.IntersectionRecord;
import ray2.Ray;
import ray2.Scene;

public class ReflectanceIntegrator extends Integrator {

	@Override
	public void shade(Colord outIntensity, Scene scene, Ray ray, IntersectionRecord iRec, int depth) {
		outIntensity.set(iRec.surface.getBSDF().getDiffuseReflectance());
	}

}
