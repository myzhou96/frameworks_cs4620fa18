package manip;

import egl.math.*;
import gl.RenderObject;

public class TranslationManipulator extends Manipulator {

	public TranslationManipulator (ManipulatorAxis axis) {
		super();
		this.axis = axis;
	}

	public TranslationManipulator (RenderObject reference, ManipulatorAxis axis) {
		super(reference);
		this.axis = axis;
	}

	@Override
	protected Matrix4 getReferencedTransform () {
		if (this.reference == null) {
			throw new RuntimeException ("Manipulator has no controlled object!");
		}
		return new Matrix4().set(reference.translation);
	}

	@Override
	public void applyTransformation(Vector2 lastMousePos, Vector2 curMousePos, Matrix4 viewProjection) {
		// TODO#A3: Modify this.reference.translation given the mouse input.
		// Use this.axis to determine the axis of the transformation.
		// Note that the mouse positions are given in coordinates that are normalized to the range [-1, 1]
		//   for both X and Y. That is, the origin is the center of the screen, (-1,-1) is the bottom left
		//   corner of the screen, and (1, 1) is the top right corner of the screen.
//		Vector2 dir = new Vector2(curMousePos);
//		dir.sub(lastMousePos);
//		switch (this.axis) {
//			case X:
//				this.reference.translation.set(0, 3, this.reference.translation.get(0, 3) + dir.x);
//				break;
//			case Y:
//				this.reference.translation.set(1, 3, this.reference.translation.get(1, 3) + dir.y);
//				break;
//			case Z:
//				this.reference.translation.set(2, 3, this.reference.translation.get(2, 3) );
//				break;
//		}
		
		//view projection matrix: world -> canonical view volume
		//getReferencedTransform: object -> world
		if(lastMousePos == curMousePos) return;
		Vector3 lastMCanF = new Vector3(lastMousePos.x, lastMousePos.y, -1);
		Vector3 lastMCanN = new Vector3(lastMousePos.x, lastMousePos.y, 1);
		Vector3 curMCanF = new Vector3(curMousePos.x, curMousePos.y, -1);
		Vector3 curMCanN = new Vector3(curMousePos.x, curMousePos.y, 1);
		
		Vector3 lastMWorldF = viewProjection.clone().invert().mulPos(lastMCanF);
		Vector3 lastMWorldN = viewProjection.clone().invert().mulPos(lastMCanN);
		Vector3 curMWorldF = viewProjection.clone().invert().mulPos(curMCanF);
		Vector3 curMWorldN = viewProjection.clone().invert().mulPos(curMCanN);
		
		Vector3 curMDir = curMCanF.clone().sub(curMCanN);
		Vector3 lastMDir = lastMCanF.clone().sub(lastMCanN);
		curMDir.set(viewProjection.clone().invert().mulDir(curMDir).normalize());
		lastMDir.set(viewProjection.clone().invert().mulDir(lastMDir).normalize());
		
		//Manipulator is all in world space
		//A plane can be defined by a normal vector and an origin, now need to find normal 
		Vector3 manipOrigin = new Vector3(0, 0, 0);
		manipOrigin.set(this.getReferencedTransform().clone().mulPos(manipOrigin));//in object space to world space
		Vector3 manipAxis = new Vector3(); //1 vector in plane is manipulator axis
		if(this.axis == ManipulatorAxis.X){
			System.out.println("X");
			manipAxis.set(this.getReferencedTransform().clone().mulDir(new Vector3(1, 0, 0)));
		}
		else if(this.axis == ManipulatorAxis.Y){
			System.out.println("Y");
			manipAxis.set(this.getReferencedTransform().clone().mulDir(new Vector3(0, 1, 0)));
		}
		else{
			System.out.println("Z");
			manipAxis.set(this.getReferencedTransform().clone().mulDir(new Vector3(0, 0, 1)));
		}
		manipAxis.normalize();
		
		//Another vector is perpendicular to this axis and parallel to the view plane. 
		//A vector is parallel to the view plane if it is perpendicular to its plane's normal, and 
		//you can take the mouse ray's direction to be the normal of the view plane.
		Vector3 imgNormalC = new Vector3(curMDir.negate()); //or should this be (0, 0, 1)
		imgNormalC.set(viewProjection.clone().invert().mulDir(imgNormalC)).normalize();		
		Vector3 imgNormalL = new Vector3(lastMDir.negate()); 
		imgNormalL.set(viewProjection.clone().invert().mulDir(imgNormalL)).normalize();
		
		Vector3 perpToImgNormalC = imgNormalC.clone().cross(manipAxis).normalize();
		Vector3 perpToImgNormalL = imgNormalL.clone().cross(manipAxis).normalize();
		
		System.out.println("img normal C: " + imgNormalC);
		System.out.println("img normal L: " + imgNormalL);
		
		//You can do this by taking two non-parallel vectors within the plane and taking their cross product.
		Vector3 manipNormalC = manipAxis.clone().cross(perpToImgNormalC).normalize();
		Vector3 manipNormalL = manipAxis.clone().cross(perpToImgNormalL).normalize();
		
		//find the world coordinates of the points where these mouse rays intersect 
		//the plane defined by the translation manipulator's origin and direction
		float topC = manipOrigin.clone().sub(curMWorldN).dot(manipNormalC);
		float tCurr = topC/(curMDir.clone().dot(manipNormalC));
		float topL = manipOrigin.clone().sub(lastMWorldN).dot(manipNormalL);
		float tLast = topL/(lastMDir.clone().dot(manipNormalL));
		
		
		Vector3 intersectionC = curMWorldN.clone().add(curMDir.clone().mul(tCurr));
		Vector3 intersectionL = lastMWorldN.clone().add(lastMDir.clone().mul(tLast));
		
		//Now we need to find the points on the manipulator ray that are closest to these points
		float t1 = intersectionC.clone().dot(manipAxis);
		float t2 = intersectionL.clone().dot(manipAxis);
		t2 = intersectionC.clone().dot(manipAxis)/(manipAxis.clone().lenSq())/11;
		t1 = intersectionL.clone().dot(manipAxis)/(manipAxis.clone().lenSq())/11;
//		System.out.println("t1: " + t1);
//		System.out.println("t2: " + t2);
		System.out.println("final t:" + (t2-t1));
		System.out.println("manipAxis: " + manipAxis);
		if(Double.isNaN(t2-t1)) return;
		Matrix4 T = new Matrix4();
		if(this.axis == ManipulatorAxis.X){
			T = T.createTranslation(new Vector3(t2-t1, 0, 0));
			this.reference.translation.mulAfter(T);
		}
		else if(this.axis == ManipulatorAxis.Y){
			T = T.createTranslation(new Vector3(0, t2-t1, 0));
			this.reference.translation.mulAfter(T);
		}
		else if(this.axis == ManipulatorAxis.Z){
			T = T.createTranslation(new Vector3(0, 0, t2-t1));
			this.reference.translation.mulAfter(T);
		}		
		System.out.println(this.reference.translation);
	
	}

	@Override
	protected String meshPath () {
		return "data/meshes/Translate.obj";
	}

}
