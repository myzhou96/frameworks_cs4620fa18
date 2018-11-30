package ray2.accel;

import egl.math.Vector3d;
import ray2.surface.Box;
import ray2.surface.Cylinder;
import ray2.surface.Sphere;
import ray2.surface.Triangle;

public class BboxUtils{
	// triangle surface
	public static void triangleBBox(Triangle t) {

		t.minBound = new Vector3d(Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		t.maxBound = new Vector3d(Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		Vector3d tmp = new Vector3d();
	
		t.averagePosition = new Vector3d();
				
		for (int i = 0; i < 3; i++) {
			tmp = new Vector3d(t.owner.getMesh().getPosition(t.face,i));
			t.tMat.mulPos(tmp);
			for (int j = 0; j < 3; j++) {
				t.averagePosition.set(j, t.averagePosition.get(j) + tmp.get(j));
				if (tmp.get(j) < t.minBound.get(j))
					t.minBound.set(j, tmp.get(j));
				if (tmp.get(j) > t.maxBound.get(j))
					t.maxBound.set(j, tmp.get(j));
			}
		}
		t.averagePosition.mul(1.0 / 3);
	};
	
	// box surface
	public static void boxBBx(Box b) {

		b.minBound = new Vector3d(Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		b.maxBound = new Vector3d(Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

		Vector3d[] v = new Vector3d[8];
		int[] k = new int[3];
		int count = 0;
		for (k[0] = 0; k[0] < 2; k[0]++) {
			for (k[1] = 0; k[1] < 2; k[1]++) {
				for (k[2] = 0; k[2] < 2; k[2]++) {
					v[count] = new Vector3d();
					for (int j = 0; j < 3; j++) {
						if (k[j] == 0)
							v[count].set(j, b.minPt.get(j));
						else
							v[count].set(j, b.maxPt.get(j));
					}
					count++;
				}
			}
		}

		b.averagePosition = new Vector3d();
		for (int i = 0; i < 8; i++) {
			b.tMat.mulPos(v[i]);
			for (int j = 0; j < 3; j++) {
				b.averagePosition.set(j, b.averagePosition.get(j) + v[i].get(j));
				if (v[i].get(j) < b.minBound.get(j))
					b.minBound.set(j, v[i].get(j));
				if (v[i].get(j) > b.maxBound.get(j))
					b.maxBound.set(j, v[i].get(j));
			}
		}
		b.averagePosition.mul(1.0 / 8);
		
	};
	
	// cylinder surface
	public static void cylinderBBox(Cylinder c) {
		c.averagePosition = new Vector3d(c.center);
		c.tMat.mulPos(c.averagePosition);

		c.minBound = new Vector3d(Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		c.maxBound = new Vector3d(Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

		Vector3d[] v = new Vector3d[8];
		int count = 0;
		for (int i = -1; i < 2; i += 2) {
			for (int j = -1; j < 2; j += 2) {
				for (int k = -1; k < 2; k += 2) {
					v[count] = new Vector3d(c.center);
					v[count].x += c.radius * i;
					v[count].y += c.radius * j;
					v[count].z += k * 0.5 * c.height;
					count++;
				}
			}
		}

		for (int i = 0; i < 8; i++) {
			c.tMat.mulPos(v[i]);
			for (int j = 0; j < 3; j++) {
				if (v[i].get(j) < c.minBound.get(j))
					c.minBound.set(j, v[i].get(j));
				if (v[i].get(j) > c.maxBound.get(j))
					c.maxBound.set(j, v[i].get(j));
			}
		}
	};
	
	// sphere surface
	public static void sphereBBox(Sphere s) {
		s.averagePosition = new Vector3d(s.center);
		s.tMat.mulPos(s.averagePosition);

		s.minBound = new Vector3d(Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		s.maxBound = new Vector3d(Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

		Vector3d[] v = new Vector3d[8];
		int count = 0;
		for (int i = -1; i < 2; i += 2) {
			for (int j = -1; j < 2; j += 2) {
				for (int k = -1; k < 2; k += 2) {
					v[count] = new Vector3d(s.center);
					v[count].x += s.radius * i;
					v[count].y += s.radius * j;
					v[count].z += s.radius * k;
					count++;
				}
			}
		}

		for (int i = 0; i < 8; i++) {
			s.tMat.mulPos(v[i]);
			for (int j = 0; j < 3; j++) {
				if (v[i].get(j) < s.minBound.get(j))
					s.minBound.set(j, v[i].get(j));
				if (v[i].get(j) > s.maxBound.get(j))
					s.maxBound.set(j, v[i].get(j));
			}
		}
	};
}