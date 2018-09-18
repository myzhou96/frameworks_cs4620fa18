package ray1.surface;

import ray1.IntersectionRecord;
import ray1.Ray;
import egl.math.Vector3;
import egl.math.Vector3d;

/**
 * Represents a sphere as a center and a radius.
 *
 * @author ags
 */
public class Sphere extends Surface {
  
  /** The center of the sphere. */
  protected final Vector3 center = new Vector3();
  public void setCenter(Vector3 center) { this.center.set(center); }
  
  /** The radius of the sphere. */
  protected float radius = 1.0f;
  public void setRadius(float radius) { this.radius = radius; }
  
  protected final double M_2PI = 2 * Math.PI;
  
  public Sphere() { }
  
  /**
   * Tests this surface for intersection with ray. If an intersection is found
   * record is filled out with the information about the intersection and the
   * method returns true. It returns false otherwise and the information in
   * outRecord is not modified.
   *
   * @param outRecord the output IntersectionRecord
   * @param ray the ray to intersect
   * @return true if the surface intersects the ray
   */
  public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
    // TODO#A2: fill in this function.
	  Vector3d d = rayIn.direction.clone();
	  Vector3d e = rayIn.origin.clone();
	  double sqrt1 = (d.clone().dot(e.clone().sub(center)));
	  double sqrt1_2 = sqrt1*sqrt1;
	  double sqrt2 = (d.clone().dot(d))* ((e.clone().sub(center)).dot(e.clone().sub(center))-(radius*radius));
	  
	  if(sqrt1_2-sqrt2 < 0){
		  return false;
	  }
//	  System.out.println("center" + center);
//	  System.out.println("radius" + radius);
	  double outside = d.clone().negate().dot(e.clone().sub(center));
	  double t_sub = (outside-Math.sqrt(sqrt1_2-sqrt2))/(d.clone().dot(d));
	  double t_pos = (outside+Math.sqrt(sqrt1_2-sqrt2))/(d.clone().dot(d));
	 
	  double t;
	  if(t_sub < t_pos){
		  t = t_sub;
		  if(rayIn.start > t || rayIn.end < t){
			  t = t_pos;
			  if(rayIn.start > t || rayIn.end < t){
				  return false;
			  }
		  }
	  }
	  else{
		  t = t_pos;
	  }
	  System.out.println("actual t: " + t + " t_pos: " + t_pos + " t _sub: " + t_sub);
	  
	  
	  IntersectionRecord inRecord = new IntersectionRecord();
	  Vector3d p = new Vector3d(e.clone().add(d.clone().mul(t)));
	  Vector3d normal = new Vector3d(p.clone().sub(center).mul(2)).normalize();
//	  System.out.println("point of intersection: " + p);
//	  System.out.println("normal: " + normal);
	  inRecord.location.set(p);
	  inRecord.t = t;
	  inRecord.normal.set(normal);
	  inRecord.surface = this; 
	  outRecord.set(inRecord);
	  return true;
  }
  
  /**
   * @see Object#toString()
   */
  public String toString() {
    return "sphere " + center + " " + radius + " " + shader + " end";
  }

}