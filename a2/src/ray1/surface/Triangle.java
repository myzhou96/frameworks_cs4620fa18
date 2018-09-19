package ray1.surface;

import ray1.IntersectionRecord;
import ray1.Ray;
import egl.math.Vector3;
import egl.math.Vector3d;
import egl.math.Vector2d;
import egl.math.Matrix3d;
import ray1.shader.Shader;
import ray1.OBJFace;

/**
 * Represents a single triangle, part of a triangle mesh
 *
 * @author ags
 */
public class Triangle extends Surface {
  /** The normal vector of this triangle, if vertex normals are not specified */
  Vector3 norm;
  
  /** The mesh that contains this triangle */
  Mesh owner;
  
  /** The face that contains this triangle */
  OBJFace face = null;
  
  double a, b, c, d, e, f;
  
  Vector3 v0_mine;
 
  public Triangle(Mesh owner, OBJFace face, Shader shader) {
    this.owner = owner;
    this.face = face;

    Vector3 v0 = owner.getMesh().getPosition(face,0);
    Vector3 v1 = owner.getMesh().getPosition(face,1);
    Vector3 v2 = owner.getMesh().getPosition(face,2);
    
    if (!face.hasNormals()) {
      Vector3 e0 = new Vector3(), e1 = new Vector3();
      e0.set(v1).sub(v0);
      e1.set(v2).sub(v0);
      norm = new Vector3();
      norm.set(e0).cross(e1).normalize();
    }

    a = v0.x-v1.x;
    b = v0.y-v1.y;
    c = v0.z-v1.z;
    
    d = v0.x-v2.x;
    e = v0.y-v2.y;
    f = v0.z-v2.z;
    
    //get rid
	v0_mine = v0.clone();
    //
    
    this.setShader(shader);
  }

  /**
   * Tests this surface for intersection with ray. If an intersection is found
   * record is filled out with the information about the intersection and the
   * method returns true. It returns false otherwise and the information in
   * outRecord is not modified.
   *
   * @param outRecord the output IntersectionRecord
   * @param rayIn the ray to intersect
   * @return true if the surface intersects the ray
   */
  public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
    // TODO#A2: fill in this function.

	Vector3d v0 = new Vector3d(owner.getMesh().getPosition(face,0));
	Vector3d v1 = new Vector3d(owner.getMesh().getPosition(face,1));
	Vector3d v2 = new Vector3d(owner.getMesh().getPosition(face,2));
	    
    Matrix3d A_mat = new Matrix3d(new double[]{a, b, c, d, e, f, rayIn.direction.x, rayIn.direction.y, rayIn.direction.z});
	double ADeterminant = A_mat.determinant();
    Matrix3d beta_mat = new Matrix3d(
			new Vector3d(v0.clone().sub(rayIn.origin)),
			new Vector3d(v0.clone().sub(v2)),
			new Vector3d(rayIn.direction)
	);
	beta_mat.transpose();
	Matrix3d gamma_mat = new Matrix3d(
			new Vector3d(v0.clone().sub(v1)),
			new Vector3d(v0.clone().sub(rayIn.origin)),
			new Vector3d(rayIn.direction)
	);
	gamma_mat.transpose();
	Matrix3d t_mat = new Matrix3d(
			new Vector3d(v0.clone().sub(v1)),
			new Vector3d(v0.clone().sub(v2)),
			new Vector3d(v0.clone().sub(rayIn.origin))
	);
	t_mat.transpose();
	
	double beta = beta_mat.determinant()/ADeterminant;
	double gamma = gamma_mat.determinant()/ADeterminant;
	double t = t_mat.determinant()/ADeterminant;
	if(t < rayIn.start || t > rayIn.end) return false;
	if(gamma < 0 || gamma > 1) return false;
	if(beta < 0 || beta > 1-gamma) return false;
//	System.out.println("gamma: " + gamma + " beta: " + beta);
	IntersectionRecord inRecord = new IntersectionRecord();
	inRecord.t = t;
	Vector3d location = rayIn.origin.clone().add(rayIn.direction.clone().mul(t));
	inRecord.location.set(location);
//	System.out.println("location: " + location);
//	System.out.println("time: " + t);
	
	if(norm == null){

		Vector3d normA = new Vector3d(owner.getMesh().getNormal(face, 0));
		Vector3d normB = new Vector3d(owner.getMesh().getNormal(face, 1));
		Vector3d normC = new Vector3d(owner.getMesh().getNormal(face, 2));
		
//		System.out.println("normA: " + normA + " normB: " + normB + " normC: " + normC);
		
		Vector3d posA = new Vector3d(owner.getMesh().getPosition(face, 0));
		Vector3d posB = new Vector3d(owner.getMesh().getPosition(face, 1));
		Vector3d posC = new Vector3d(owner.getMesh().getPosition(face, 2));
		
//		System.out.println("posA: " + posA + " posB: " + posB + " posC: " + posC);
		
		Vector3d n = normA.clone().mul(1-beta-gamma)
				.add(normB.clone().mul(beta))
				.add(normC.clone().mul(gamma));
		inRecord.normal.set(n.clone().normalize());
		
		
	}
	else{
		inRecord.normal.set(norm);
	}
	
	if(face.hasUVs()){
		Vector2d uvA = new Vector2d(owner.getMesh().getUV(face, 0));
		Vector2d uvB = new Vector2d(owner.getMesh().getUV(face, 1));
		Vector2d uvC = new Vector2d(owner.getMesh().getUV(face, 2));
		
		Vector2d uv = uvA.clone().mul(1-beta-gamma)
				.add(uvB.clone().mul(beta))
				.add(uvC.clone().mul(gamma));
		 inRecord.texCoords.set(uv);
	}
	
	inRecord.surface = this;
	outRecord.set(inRecord);
    return true;
  }
  

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "Triangle ";
  }
}