package ray2.material;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;

public class LambertianBSDF extends BSDF {
	
	/** The color of the diffuse reflection, if there is no texture. */
	protected final Colord diffuseReflectance = new Colord(Color.White);
	public void setDiffuseColor(Colord diffuseColor) { this.diffuseReflectance.set(diffuseColor); }
	
	public LambertianBSDF() { }
	
	public LambertianBSDF(Colord diffuseReflectance) {
		this.diffuseReflectance.set(diffuseReflectance);
	}

	public String toString() {
		return "lambertianBSDF: " + diffuseReflectance;
	}

	@Override
	public void eval(Vector3d dir1, Vector3d dir2, Vector3d normal, Colord outValue) {
		if (dir1.dot(normal) >= 0 && dir2.dot(normal) >= 0)
			outValue.set(diffuseReflectance).div(Math.PI);
		else
			outValue.set(0);
	}
	
	static void cosineHemisphere(Vector2d seed, Vector3d outDir) {
		double r = Math.sqrt(seed.x);
		outDir.z = Math.sqrt(1 - seed.x);
		outDir.x = r * Math.cos(2 * Math.PI * seed.y);
		outDir.y = r * Math.sin(2 * Math.PI * seed.y);		
	}
	
	static void uniformHemisphere(Vector2d seed, Vector3d outDir) {
		outDir.z = seed.x;
		double r = Math.sqrt(1 - seed.x*seed.x);
		outDir.x = r * Math.cos(2 * Math.PI * seed.y);
		outDir.y = r * Math.sin(2 * Math.PI * seed.y);		
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

	/* (non-Javadoc)
	 * @see ray2.material.BSDF#sample(ray2.material.BSDFSamplingRecord, egl.math.Vector2d, egl.math.Colord)
	 */
	@Override
	public double sample(BSDFSamplingRecord sampleRecord, Vector2d seed, Colord outValue) {
		Vector3d outDirLocal = new Vector3d();
		cosineHemisphere(seed, outDirLocal);
		Vector3d u = new Vector3d();
		Vector3d v = new Vector3d();
		basisFromW(sampleRecord.normal, u, v);
		sampleRecord.dir2.set(sampleRecord.normal).mul(outDirLocal.z);
		sampleRecord.dir2.addMultiple(outDirLocal.x, u);
		sampleRecord.dir2.addMultiple(outDirLocal.y, v);
		outValue.set(diffuseReflectance).div(Math.PI);
		return sampleRecord.dir2.dot(sampleRecord.normal) / Math.PI;
	}

	/* (non-Javadoc)
	 * @see ray2.material.BSDF#pdf(egl.math.Vector3d, egl.math.Vector3d, egl.math.Vector3d)
	 */
	@Override
	public double pdf(Vector3d dir1, Vector3d dir2, Vector3d normal) {
		if (dir2.dot(normal) >= 0)
			return dir2.dot(normal) / Math.PI;
		else
			return 0;
	}

	/* (non-Javadoc)
	 * @see ray2.material.BSDF#getDiffuseReflectance()
	 */
	@Override
	public Colord getDiffuseReflectance() {
		return diffuseReflectance;
	}
	

}
