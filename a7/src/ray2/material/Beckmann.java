package ray2.material;

import egl.math.Vector2d;
import egl.math.Vector3d;

public class Beckmann extends MicrofacetDistribution {
	
	/** Constructor */
	public Beckmann() {
		this.alpha = 0.1;
		this.nt = 1.5;
	}
	
	public Beckmann(double alpha, double nt) {
		this.alpha = alpha;
		this.nt = nt;
	}

	@Override
	double D(Vector3d HalfVec, Vector3d SurfaceNormal) {
		double alpha_b = getAlpha();
		double Dvalue = 0;
		double cos_thetam = HalfVec.dot(SurfaceNormal);
		
		if (cos_thetam > 0)
		{
			double cos_thetam2 = cos_thetam * cos_thetam;
			double sin_thetam2 = 1.0f - cos_thetam2;
			double cos_thetam4 = cos_thetam2 * cos_thetam2;
			double tan_thetam2 = sin_thetam2 / cos_thetam2;
			Dvalue = (double)Math.exp(-tan_thetam2 / (alpha_b * alpha_b));
			Dvalue = Dvalue / ((double)Math.PI * alpha_b * alpha_b * cos_thetam4);
		}
		return Dvalue;
	}
	
	@Override
	public final double G(Vector3d v, Vector3d m, Vector3d n)
	{
		double alpha_b = getAlpha();
		double ret = 0;
		double vm = v.dot(m);
		double vn = v.dot(n);
		if (vm / vn > 0)
		{
			double cos_thetav = Math.abs(v.normalize().dot(n.normalize()));
			double sin_thetav = (float)Math.sqrt(1.0 - cos_thetav * cos_thetav);
			double tan_thetav = sin_thetav / cos_thetav;
			double a = 1.0 / (alpha_b * tan_thetav);
			ret = G_helper(a);
		}
		return ret;
	}
	
	public final double G_helper(double a)
	{
		double ret = 0.0f;
		if (a>=1.6f)
		{
			ret = 1.0f;
		}
		else
		{
			ret = 3.535f * a + 2.181f * a * a;
			ret = ret / (1.0f + 2.276f * a + 2.577f * a * a);
		}
		return ret;
	}
	
	
	@Override
	Vector3d sample_helper(Vector2d seed) {
		Vector3d HalfVec = new Vector3d();
		
		double tantheta2 = -alpha * alpha * Math.log(1-seed.x);
		double costheta2 = 1.0 / ( 1.0 + tantheta2 );
		double costheta  = Math.sqrt( costheta2 );
		double sintheta  = Math.sqrt( Math.max(0.0, 1.0 - costheta2));
		   
		HalfVec = SphericalDirection( sintheta, costheta, 2 * Math.PI * seed.y );
		return HalfVec;
	}
	
	
	@Override
	public String toString() {
		return "Beckmann" + this.alpha + " " + this.nt;
	}
	
}