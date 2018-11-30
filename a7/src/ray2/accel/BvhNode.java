package ray2.accel;

import ray2.Ray;
import egl.math.Vector3d;

/**
 * A class representing a node in a bounding volume hierarchy.
 *
 * @author pramook
 */
public class BvhNode {

	/** The current bounding box for this tree node.
	 *  The bounding box is described by
	 *  (minPt.x, minPt.y, minPt.z) - (maxBound.x, maxBound.y, maxBound.z).
	 */
	public final Vector3d minBound, maxBound;

	/**
	 * The array of children.
	 * child[0] is the left child.
	 * child[1] is the right child.
	 */
	public final BvhNode child[];

	/**
	 * The index of the first surface under this node.
	 */
	public int surfaceIndexStart;

	/**
	 * The index of the surface next to the last surface under this node.
	 */
	public int surfaceIndexEnd;

	/**
	 * Default constructor
	 */
	public BvhNode()
	{
		minBound = new Vector3d();
		maxBound = new Vector3d();
		child = new BvhNode[2];
		child[0] = null;
		child[1] = null;
		surfaceIndexStart = -1;
		surfaceIndexEnd = -1;
	}

	/**
	 * Constructor where the user can specify the fields.
	 * @param minBound
	 * @param maxBound
	 * @param leftChild
	 * @param rightChild
	 * @param start
	 * @param end
	 */
	public BvhNode(Vector3d minBound, Vector3d maxBound, BvhNode leftChild, BvhNode rightChild, int start, int end)
	{
		this.minBound = new Vector3d();
		this.minBound.set(minBound);
		this.maxBound = new Vector3d();
		this.maxBound.set(maxBound);
		this.child = new BvhNode[2];
		this.child[0] = leftChild;
		this.child[1] = rightChild;
		this.surfaceIndexStart = start;
		this.surfaceIndexEnd = end;
	}

	/**
	 * @return true if this node is a leaf node
	 */
	public boolean isLeaf()
	{
		return child[0] == null && child[1] == null;
	}

	/**
	 * Check if the ray intersects the bounding box.
	 * @param ray
	 * @return true if ray intersects the bounding box
	 */
	public boolean intersects(Ray ray) {

		Vector3d o = ray.origin;
		Vector3d d = ray.direction;

		double ox = o.x;
		double oy = o.y;
		double oz = o.z;
		double dx = d.x;
		double dy = d.y;
		double dz = d.z;

		double tMin = ray.start, tMax = ray.end;

		double txMin, txMax;
		if (dx >= 0) {
			txMin = (minBound.x - ox) / dx;
			txMax = (maxBound.x - ox) / dx;
		}
		else {
			txMin = (maxBound.x - ox) / dx;
			txMax = (minBound.x - ox) / dx;
		}
		if (tMin > txMax || txMin > tMax)
			return false;
		if (txMin > tMin)
			tMin = txMin;
		if (txMax < tMax)
			tMax = txMax;

		double tyMin, tyMax;
		if (dy >= 0) {
			tyMin = (minBound.y - oy) / dy;
			tyMax = (maxBound.y - oy) / dy;
		}
		else {
			tyMin = (maxBound.y - oy) / dy;
			tyMax = (minBound.y - oy) / dy;
		}
		if (tMin > tyMax || tyMin > tMax)
			return false;
		if (tyMin > tMin)
			tMin = tyMin;
		if (tyMax < tMax)
			tMax = tyMax;

		double tzMin, tzMax;
		if (dz >= 0) {
			tzMin = (minBound.z - oz) / dz;
			tzMax = (maxBound.z - oz) / dz;
		}
		else {
			tzMin = (maxBound.z - oz) / dz;
			tzMax = (minBound.z - oz) / dz;
		}
		if (tMin > tzMax || tzMin > tMax)
			return false;

		return true;
	}
}
