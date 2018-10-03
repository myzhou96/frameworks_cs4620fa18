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
		Matrix4 invViewProjection = viewProjection.clone().invert();
		Vector4 curMouseWorld = invViewProjection.mul(new Vector4(curMousePos.x, curMousePos.y, 1, 1));
		Vector4 lastMouseWorld = invViewProjection.mul(new Vector4(lastMousePos.x, lastMousePos.y, -1, 1));
		// TODO: projection
		Vector4 diff = new Vector4(curMouseWorld).sub(lastMouseWorld);
		switch (this.axis) {
			case X:
				this.reference.translation.set(0, 3, this.reference.translation.get(0, 3) + diff.x);
				break;
			case Y:
				this.reference.translation.set(1, 3, this.reference.translation.get(1, 3) + diff.y);
				break;
			case Z:
				this.reference.translation.set(2, 3, this.reference.translation.get(2, 3) + diff.z);
				break;
		}
	}

	@Override
	protected String meshPath () {
		return "data/meshes/Translate.obj";
	}

}
