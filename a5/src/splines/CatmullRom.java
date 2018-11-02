/**
 * @author Jimmy, Andrew 
 */

package splines;
import java.util.ArrayList;

import egl.math.Matrix4;
import egl.math.Vector2;

public class CatmullRom extends SplineCurve {

	public CatmullRom(ArrayList<Vector2> controlPoints, boolean isClosed,
			float epsilon) throws IllegalArgumentException {
		super(controlPoints, isClosed, epsilon);
	}

	@Override
	public CubicBezier toBezier(Vector2 p0, Vector2 p1, Vector2 p2, Vector2 p3, float eps) {
		//TODO A5
		Matrix4 convertToBez = new Matrix4(
				0f, 1f, 0f, 0f,
				-1/6.0f, 1f, 1/6.0f, 0f,
				0f, 1/6.0f, 1f, -1/6.0f,
				0f, 0f, 1f, 0f
				);
		Matrix4 catPts = new Matrix4(
				p0.x, p0.y, 0f, 0f,
				p1.x, p1.y, 0f, 0f,
				p2.x, p2.y, 0f, 0f,
				p3.x, p3.y, 0f, 0f
				);
		Matrix4 c = new Matrix4();
		convertToBez.clone().mulBefore(catPts, c);
		return new CubicBezier(new Vector2(c.m[0], c.m[4]), new Vector2(c.m[1], c.m[5]), 
				new Vector2(c.m[2], c.m[6]), new Vector2(c.m[3], c.m[7]), eps);
	}
}
