package ray2.material;

import egl.math.Vector2d;
import egl.math.Vector3d;

public class GGX extends MicrofacetDistribution{
	
	/** Constructor */
	public GGX() {
		this.alpha = 0.1;
		this.nt = 1.5;
	}
	public GGX(double alpha, double nt) {
		this.alpha = alpha;
		this.nt = nt;
	}
	
	@Override
	double D(Vector3d HalfVec, Vector3d SurfaceNormal) {
		double alpha_g = getAlpha();
		double Dvalue = 0;
		if (HalfVec.dot(SurfaceNormal) > 0)
		{
			double cos_thetam = HalfVec.dot(SurfaceNormal);
			double cos_thetam2 = cos_thetam * cos_thetam;
			double sin_thetam2 = 1.0f - cos_thetam2;
			double cos_thetam4 = cos_thetam2 * cos_thetam2;
			double tan_thetam2 = sin_thetam2 / cos_thetam2;
			double alpha_g2 = alpha_g * alpha_g;
			Dvalue = alpha_g2 / (float)Math.PI / cos_thetam4;
			Dvalue = Dvalue / ((alpha_g2 + tan_thetam2) * (alpha_g2 + tan_thetam2));
		}
		return Dvalue;
	}
	
	@Override
	double G(Vector3d v, Vector3d m, Vector3d n) {
		double alpha_g = getAlpha();
		double ret = 0;
		double vm = v.dot(m);
		double vn = v.dot(n);
		if (vm / vn > 0)
		{
			double cos_thetav = v.normalize().dot(n.normalize());
			double sin_thetav = (float)Math.sqrt(1.0 - cos_thetav * cos_thetav);
			double tan_thetav = sin_thetav / cos_thetav;
			double alpha_g2 = alpha_g * alpha_g;
			double tan_thetav2 = tan_thetav * tan_thetav;
			ret = 2.0f / (1.0f + (float)Math.sqrt(1.0 + alpha_g2 * tan_thetav2));
		}
		return ret;
	}

	@Override
	Vector3d sample_helper(Vector2d seed) {
		Vector3d HalfVec = new Vector3d();
		
		double tantheta2 = alpha*alpha * seed.x / ( 1.0 - seed.x);
		double costheta2 = 1.0 / ( 1.0 + tantheta2 );
		double costheta  = Math.sqrt( costheta2 );
		double sintheta  = Math.sqrt( Math.max( 0.0, 1.0 - costheta2 ) );
		
		HalfVec = SphericalDirection( sintheta, costheta, 2 * Math.PI * seed.y );
		return HalfVec;
	}


	@Override
	public String toString() {
		return "GGX" + this.alpha + " " + this.nt;
	}

	
}