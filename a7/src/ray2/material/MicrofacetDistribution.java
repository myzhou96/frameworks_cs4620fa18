package ray2.material;

import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;

/**
 * Microfacet distribution
 *
 * BSDF Component and eval parts come from Zechen's implementation in Ray1.
 *
 *@author mx
 */
public abstract class MicrofacetDistribution {
	/** Microfacet distribution funciton*/
	abstract double D(Vector3d HalfVec, Vector3d SurfaceNormal);
	
    double chi_plus(double a) {
    		if (a > 0)return 1;
    		else return 0;
    };
    
    /** shadow masking fucntion*/
    abstract double G(Vector3d v, Vector3d m, Vector3d n);
    
    /** fresnel function*/
    double fresnel(Vector3d IncomingVec, Vector3d HalfVec)
	{
		double ni = 1.0f;
		double c = (float)Math.abs(IncomingVec.dot(HalfVec));
		double g_sqr = (nt*nt) / (ni*ni) - 1.0 + c*c;
		double F = 1.0f;
		if (g_sqr > 0.0f)
		{
			double g = (float)Math.sqrt(g_sqr);
			F = 0.5f * ((g - c) * (g - c)) / ((g + c)*(g + c));
			F = F * (1.0f + ((c*(g+c) - 1.0f) / (c*(g - c) + 1.0f)) * ((c*(g + c) - 1.0f) / ( c*(g - c) + 1.0f)));
		}
		return F;
	}
    
    /** BSDF evaluation routine*/
	double eval(Vector3d IncomingVec, Vector3d OutgoingVec, Vector3d SurfaceNormal) {
		double result = 0;
		
		double LdotN = IncomingVec.dot(SurfaceNormal);
		double VdotN = OutgoingVec.dot(SurfaceNormal);
		
		if (LdotN > 0 && VdotN > 0){
			Vector3d HalfVec = new Vector3d();
			HalfVec.set(IncomingVec).add(OutgoingVec).normalize();
			double F = fresnel(IncomingVec, HalfVec);
			/*surface normal distribution factor D*/
			double Dvalue = D(HalfVec, SurfaceNormal);
			/*Approximated Smith Masking and shadowing factor G = G_im * G_om */
			double G_im = G(IncomingVec, HalfVec, SurfaceNormal);
			double G_om = G(OutgoingVec, HalfVec, SurfaceNormal);
			double G = G_im * G_om;
			
			result =  F * Dvalue * G / (4 * Math.abs(LdotN) * Math.abs(VdotN));
		}

		return result;
		
	}
	
    
	/** Helper functions for sample half vector*/
	Vector3d SphericalDirection(double sintheta, double costheta, double phi) {
		double sinphi = Math.sin(phi);
		double cosphi = Math.cos(phi);
	    return new Vector3d( sintheta * cosphi, sintheta * sinphi, costheta );

	}
	abstract Vector3d sample_helper(Vector2d seed);
	double SIGN(double a) {
		if (a>0)return 1;
		else if (a==0)return 0;
		else return -1;
	}
	
	static void basisFromW(Vector3d w, Vector3d outU, Vector3d outV) {
		if (Math.abs(w.x) <= Math.abs(w.y) && Math.abs(w.x) <= Math.abs(w.z))
			outV.set(1, 0, 0);
		else if (Math.abs(w.y) <= Math.abs(w.x) && Math.abs(w.y) <= Math.abs(w.z))
			outV.set(0, 1, 0);
		else
			outV.set(0, 0, 1);
		outU.set(outV).cross(w).normalize();
		outV.set(w).cross(outU).normalize();
	}
	
	/** Sample function */
	double sample(BSDFSamplingRecord sampleRecord, Vector2d seed, Colord outValue) {
		double prob = 0;
		Vector3d HalfVecLocal = sample_helper(seed);
		
		// convert half vector from local coordinates to world coordinates
		Vector3d n = new Vector3d();
		n.set(sampleRecord.normal);
		Vector3d u = new Vector3d();
		Vector3d v = new Vector3d();
		basisFromW(n, u, v);
		
		Vector3d HalfVec = new Vector3d();
		HalfVec.set(n).mul(HalfVecLocal.z);
		HalfVec.addMultiple(HalfVecLocal.x, u);
		HalfVec.addMultiple(HalfVecLocal.y, v);
		
		double LdotH = HalfVec.dot(sampleRecord.dir1);
		
		sampleRecord.dir2.set(HalfVec.clone().mul(LdotH).mul(2.0)).addMultiple(-1.0, sampleRecord.dir1);
		sampleRecord.dir2.normalize();
		
		prob = pdf(sampleRecord.dir1, sampleRecord.dir2, sampleRecord.normal);	    
		outValue.set(eval(sampleRecord.dir1, sampleRecord.dir2, sampleRecord.normal)); 
		
		return prob;
	}
    
    /** Calculate probability density for choosing dir2 given dir1 */
    double pdf(Vector3d IncomingVec, Vector3d OutgoingVec, Vector3d SurfaceNormal) {
		
		double prob = 0;
		double LdotN = IncomingVec.dot(SurfaceNormal);
		double VdotN = OutgoingVec.dot(SurfaceNormal);
		
		Vector3d HalfVec = new Vector3d();
		if (LdotN<=0 || VdotN <=0){
			prob = 0.0;
		}else {
			HalfVec.set(IncomingVec).add(OutgoingVec).normalize();
			
			/*surface normal distribution factor D*/
			double Dvalue = D(HalfVec, SurfaceNormal);
			
			double LdotH = IncomingVec.dot(HalfVec);
			double VdotH = OutgoingVec.dot(HalfVec);
			
			if (LdotH==0) return 0.0;
			
			double costheta = Math.abs(HalfVec.dot(SurfaceNormal));
			double jacobian = 1.0 / ( 4.0 * Math.abs(LdotH )); 
			prob = chi_plus(VdotH/VdotN) * chi_plus(LdotH/LdotN) * Dvalue * costheta * jacobian;

		}
		
		return prob;
	}
    
    public abstract String toString();
    
    /** roughness of material */
    protected double alpha;
	public void setAlpha(double t) { alpha = t; }
	public double getAlpha() { return alpha; }
	
	/**refractive index of material */
	protected double nt;
	public void setNt(double nt) {this.nt = nt;}
	public double getNt() {return nt;}	
	
	

}
