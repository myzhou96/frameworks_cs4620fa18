package ray2.material;

import egl.math.Vector3d;

/**
 * A BSDF Record class used for eval, sample and pdf
 *
 * @author mx
 */
public class BSDFSamplingRecord {
	
	  /** The incoming ray. */
	  public Vector3d dir1 = new Vector3d();
	  
	  /** The outgoing ray. */
	  public Vector3d dir2 = new Vector3d();
	  
	  /** The surface normal. */
	  public Vector3d normal = new Vector3d();
	  
	  /** Whether the pdf type is discrete or not*/
	  public boolean isDiscrete = false;
	  
	  public BSDFSamplingRecord() {
		  this.dir1.setZero();
		  this.dir2.setZero();
		  this.normal.setZero();
		  this.isDiscrete = false;
	  }
	  
	  public BSDFSamplingRecord(Vector3d wi, Vector3d normal) {
		  this.dir1.set(wi);
		  this.normal.set(normal);
	  } 
	  
	  public BSDFSamplingRecord(Vector3d wi, Vector3d normal, boolean isDiscrete) {
		  this.dir1.set(wi);
		  this.normal.set(normal);
		  this.isDiscrete = isDiscrete;
	  } 

}