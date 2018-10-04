package manip;

import egl.math.*;
import gl.RenderObject;

public class RotationManipulator extends Manipulator {

	protected String meshPath = "Rotate.obj";

	public RotationManipulator(ManipulatorAxis axis) {
		super();
		this.axis = axis;
	}

	public RotationManipulator(RenderObject reference, ManipulatorAxis axis) {
		super(reference);
		this.axis = axis;
	}

	//assume X, Y, Z on stack in that order
	@Override
	protected Matrix4 getReferencedTransform() {
		Matrix4 m = new Matrix4();
		switch (this.axis) {
		case X:
			m.set(reference.rotationX).mulAfter(reference.translation);
			break;
		case Y:
			m.set(reference.rotationY)
				.mulAfter(reference.rotationX)
				.mulAfter(reference.translation);
			break;
		case Z:
			m.set(reference.rotationZ)
			.mulAfter(reference.rotationY)
			.mulAfter(reference.rotationX)
			.mulAfter(reference.translation);
			break;
		}
		return m;
	}

	@Override
	public void applyTransformation(Vector2 lastMousePos, Vector2 curMousePos, Matrix4 viewProjection) {
		// TODO#A3: Modify this.reference.rotationX, this.reference.rotationY, or this.reference.rotationZ
		//   given the mouse input.
		// Use this.axis to determine the axis of the transformation.
		// Note that the mouse positions are given in coordinates that are normalized to the range [-1, 1]
		//   for both X and Y. That is, the origin is the center of the screen, (-1,-1) is the bottom left
		//   corner of the screen, and (1, 1) is the top right corner of the screen.
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
			manipAxis = this.getReferencedTransform().clone().mulDir(new Vector3(1, 0, 0));
		}
		else if(this.axis == ManipulatorAxis.Y){
			manipAxis = this.getReferencedTransform().clone().mulDir(new Vector3(0, 1, 0));
		}
		else{
			manipAxis = this.getReferencedTransform().clone().mulDir(new Vector3(0, 0, 1));
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
		
		Vector3 angleVectorC = intersectionC.clone().sub(manipOrigin).normalize();
		Vector3 angleVectorL = intersectionL.clone().sub(manipOrigin).normalize();
//		float dotAngle = angleVectorC.clone().dot(angleVectorL.clone());
//		float angle = (float) Math.acos(dotAngle);
		double angle = angleVectorC.angle(angleVectorL);
		if(Double.isNaN(angle) || angle == 0.0) return;
		//use cross product to get normal to the plane from intersections
		Vector3 crossAngle = intersectionC.clone().cross(intersectionL);
		if(crossAngle.dot(manipNormalL) < 0){
			angle *= -1;
		}
		System.out.println("angle: " + angle);
		Matrix4 T = new Matrix4();
		System.out.println(this.axis);
		if(this.axis == ManipulatorAxis.X){
			T = this.reference.rotationX.createRotationX((float)angle);
			this.reference.translation.mulAfter(T);
		}
		else if(this.axis == ManipulatorAxis.Y){
			T = this.reference.rotationX.createRotationY((float)angle);
			this.reference.translation.mulAfter(T);
		}
		else if(this.axis == ManipulatorAxis.Z){
			T = this.reference.rotationX.createRotationZ((float)angle);
			this.reference.translation.mulAfter(T);
		}

	}

	@Override
	protected String meshPath () {
		return "data/meshes/Rotate.obj";
	}
}
