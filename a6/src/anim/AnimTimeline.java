package anim;

import java.util.TreeSet;

import common.SceneObject;
import egl.math.Matrix3;
import egl.math.Matrix4;
import egl.math.Quat;
import egl.math.Vector3;
import egl.math.Vector4;

/**
 * A timeline for a particular object in the scene.  The timeline holds
 * a sequence of keyframes and a reference to the object that they
 * pertain to.  Via linear interpolation between keyframes, the timeline
 * can compute the object's transformation at any point in time.
 * 
 * @author Cristian
 */
public class AnimTimeline {
	
	/**
	 * A sorted set of keyframes.  Invariant: there is at least one keyframe.
	 */
	public final TreeSet<AnimKeyframe> frames = new TreeSet<>(AnimKeyframe.COMPARATOR);
	
	/**
	 * The object that this timeline animates
	 */
	public final SceneObject object;

	/**
	 * Create a new timeline for an object.  The new timeline initially has the object
	 * stationary, with the same transformation it currently has at all times.  This is
	 * achieve by creating a timeline with a single keyframe at time zero.
	 * @param o Object
	 */
	public AnimTimeline(SceneObject o) {
		object = o;
		
		// Create A Default Keyframe
		AnimKeyframe f = new AnimKeyframe(0);
		f.transformation.set(o.transformation);
		frames.add(f);
	}
	
	/**
	 * Add A keyframe to the timeline.
	 * @param frame Frame number
	 * @param t Transformation
	 */
	public void addKeyFrame(int frame, Matrix4 t) {
		// TODO#A6: Add an AnimKeyframe to frames and set its transformation
		System.out.println("ADDED #" + frame);
		System.out.println("size " + frames.size());
		AnimKeyframe f = new AnimKeyframe(frame);
		object.transformation.set(t);
		f.transformation.set(t);
		frames.add(f);	
	}
	
	/**
	 * Remove a keyframe from the timeline.  If the timeline is empty,
	 * maintain the invariant by adding a single keyframe with the given
	 * transformation.
	 * @param frame Frame number
	 * @param t Transformation
	 */
	public void removeKeyFrame(int frame, Matrix4 t) {
		// TODO#A6: Delete a frame, you might want to use Treeset.remove
		// If there is no frame after deletion, add back this frame.
		frames.remove(frame);
		if(frames.size() <= 0){
			AnimKeyframe f = new AnimKeyframe(frame);
			frames.add(f);
			object.transformation.set(t);
			f.transformation.set(t);
		}
	}

	
	/**
	 * Takes a rotation matrix and decomposes into Euler angles. 
	 * Returns a Vector3 containing the X, Y, and Z degrees in radians.
	 * Formulas from http://nghiaho.com/?page_id=846
	 */
	public static Vector3 eulerDecomp(Matrix3 mat) {
		double theta_x = Math.atan2(mat.get(2, 1), mat.get(2, 2));
		double theta_y = Math.atan2(-mat.get(2, 0), Math.sqrt(Math.pow(mat.get(2, 1), 2) + Math.pow(mat.get(2, 2), 2)));
		double theta_z = Math.atan2(mat.get(1, 0), mat.get(0, 0));
		
		return new Vector3((float)theta_x, (float)theta_y, (float)theta_z);
	}
	
	
	/**
	 * Update the transformation for the object connected to this timeline to the current frame
	 * @curFrame Current frame number
	 * @rotation Rotation interpolation mode: 
	 * 0 - Euler angles, 
	 * 1 - Linear interpolation of quaternions,
	 * 2 - Spherical linear interpolation of quaternions.
	 */
	public void updateTransformation(int curFrame, int rotation) {
		//TODO#A6: You need to get pair of surrounding frames,
		// calculate interpolation ratio,
		// calculate Translation, Scale and Rotation Interpolation,
		// and combine them.
		// Argument curFrame is current frame number
		// Argument rotation is rotation interpolation mode
		// 0 - Euler angles, 
		// 1 - Linear interpolation of quaternions,
		// 2 - Spherical linear interpolation of quaternions.
		
		System.out.println("UPDATING");
		System.out.println(curFrame);
		AnimKeyframe f = new AnimKeyframe(curFrame);
		AnimKeyframe prev = frames.floor(f);
		AnimKeyframe next = frames.ceiling(f);
		
		Matrix3 prevTran;
		Matrix3 nextTran;
		if (prev == null){
			prev = next;
		}
		else if(next == null){
			next = prev;
		}
		prevTran = new Matrix3(prev.transformation);
		nextTran = new Matrix3(next.transformation);
		System.out.println("prev #: " + prev.frame + " next #: " + next.frame);
		
		Matrix3 prevR = new Matrix3();
		Matrix3 prevS = new Matrix3();
		Vector3 prevT = prev.transformation.getTrans();
		Matrix3 nextR = new Matrix3();
		Matrix3 nextS = new Matrix3();
		Vector3 nextT = next.transformation.getTrans();
		System.out.println("prev trans: " + prevT);
		System.out.println("next trans: " + nextT);
		
		prevTran.polar_decomp(prevR, prevS);
		nextTran.polar_decomp(nextR, nextS);
		
		Vector3 prevEuler = eulerDecomp(prevR);
		Vector3 nextEuler = eulerDecomp(nextR);
		
		float t;
		if (prev.frame == next.frame){
			t = 1f;
		}
		else{
			 t = (curFrame - prev.frame)/(prev.frame + next.frame);
		}
		
		System.out.println("value of t: " + t);
		
		Matrix3 curS = prevS.createScale(t).add(nextS.createScale(1-t));
		Vector3 curT = prevT.mul(t).add(nextT.mul(1-t));
		Vector3 curEuler = prevEuler.mul(t).add(nextEuler.mul(1-t));
		
		//converting the resulting angles back to a rotation matrix.
		Matrix3 curR = Matrix3.createRotationX(curEuler.z)
				.mulAfter(Matrix3.createRotationX(curEuler.y))
				.mulAfter(Matrix3.createRotationX(curEuler.x));
		
		//Recompose the constituents to give a transformation for the current frame.
		Matrix4 temp = new Matrix4(curS.clone().mulBefore(curR));
		temp.m[12] = curT.x;
		temp.m[13] = curT.y;
		temp.m[14] = curT.z;
		f.transformation.set(temp);
		System.out.println("final matrix:" + temp);
		
	}
}
