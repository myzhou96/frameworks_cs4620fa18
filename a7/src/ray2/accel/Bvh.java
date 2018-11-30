
package ray2.accel;

import java.util.Arrays;
import java.util.Comparator;

import ray2.IntersectionRecord;
import ray2.Ray;
import ray2.surface.Surface;
import egl.math.Vector3d;

/**
 * Class for Axis-Aligned-Bounding-Box to speed up the intersection look up time.
 *
 * @author ss932, pramook
 */
public class Bvh implements AccelStruct {

	// Performance counters
	public static int hitCount = 0;
	public static int missCount = 0;

	/** A shared surfaces array that will be used across every node in the tree. */
	private Surface[] surfaces;

	/** A comparator class that can sort surfaces by x, y, or z coordinate.
	 *  See the subclass declaration below for details.
	 */
	static MyComparator cmp = new MyComparator();

	/** The root of the BVH tree. */
	BvhNode root;

	public Bvh() { }

	/**
	 * Set outRecord to the first intersection of ray with the scene. Return true
	 * if there was an intersection and false otherwise. If no intersection was
	 * found outRecord is unchanged.
	 *
	 * @param outRecord the output IntersectionRecord
	 * @param ray the ray to intersect
	 * @param anyIntersection if true, will immediately return when found an intersection
	 * @return true if and intersection is found.
	 */
	public boolean intersect(IntersectionRecord outRecord, Ray rayIn, boolean anyIntersection) {
		return intersectHelper(root, outRecord, rayIn, anyIntersection);
	}

	/**
	 * A helper method to the main intersect method. It finds the intersection with
	 * any of the surfaces under the given BVH node.
	 *
	 * @param node a BVH node that we would like to find an intersection with surfaces under it
	 * @param outRecord the output InsersectionMethod
	 * @param rayIn the ray to intersect
	 * @param anyIntersection if true, will immediately return when found an intersection
	 * @return true if an intersection is found with any surface under the given node
	 */
	private boolean intersectHelper(BvhNode node, IntersectionRecord outRecord, Ray rayIn, boolean anyIntersection)
	{
		// A7: fill in this function.
		// Hint: For a leaf node, use a normal linear search. Otherwise, search in the left and right children.
		// Another hint: save time by checking if the ray intersects the node first before checking the childrens.
		if (!node.intersects(rayIn)) {
			missCount++;
			return false;
		}
		hitCount++;

		boolean ret = false;
		IntersectionRecord tmp = new IntersectionRecord();
		Ray ray = new Ray(rayIn.origin, rayIn.direction);
		ray.start = rayIn.start;
		ray.end = rayIn.end;
		if(node.isLeaf()) {
			for(int i=node.surfaceIndexStart; i<node.surfaceIndexEnd; i++) {
				if(surfaces[i].intersect(tmp, ray) && tmp.t < ray.end ) {
					if(anyIntersection) return true;
					ret = true;
					ray.end = tmp.t;
					if(outRecord!=null)
						outRecord.set(tmp);
				}
			}
		} else {
			for(int i=0;i<2;i++) {
				boolean hit = intersectHelper(node.child[i], tmp, ray, anyIntersection);
				if (tmp.t < ray.end && hit) {
					if (anyIntersection) return true;
					ret = true;
					ray.end = tmp.t;
					if (outRecord != null)
						outRecord.set(tmp);
				}
			}
		}
		return ret;
	}


	@Override
	public void build(Surface[] surfaces) {
		this.surfaces = surfaces;
		root = createTree(0, surfaces.length);
		System.out.println("Bvh: " + surfaces.length + " surfaces");
		System.out.println("Bvh: " + nodeCount(root) + " nodes, " + leafCount(root) + " leaves");
		System.out.println("Bvh: max depth " + maxDepth(root));
		System.out.println("Bvh: average child volume ratio " + volRatio(root).mean);
	}

	private int maxDepth(BvhNode node) {
		if (node.isLeaf())
			return 0;
		return 1 + Math.max(maxDepth(node.child[0]), maxDepth(node.child[1]));
	}

	private int nodeCount(BvhNode node) {
		if (node.isLeaf())
			return 1;
		return 1 + nodeCount(node.child[0]) + nodeCount(node.child[1]);
	}

	private int leafCount(BvhNode node) {
		if (node.isLeaf())
			return 1;
		return leafCount(node.child[0]) + leafCount(node.child[1]);
	}

	/*
	 * Average over all internal nodes of the ratio between the sum
	 * of the children's volumes and the parent's volume.
	 */
	private class RatioResult {
		double mean; int count;
		RatioResult(double mean, int count) {
			this.mean = mean;
			this.count = count;
		}
	}
	private RatioResult volRatio(BvhNode node) {
		if (node.isLeaf())
			return new RatioResult(0.0, 0);
		RatioResult r0 = volRatio(node.child[0]);
		RatioResult r1 = volRatio(node.child[1]);
		int count = 1 + r0.count + r1.count;
		double ratio = (nodeVol(node.child[0]) + nodeVol(node.child[1])) / nodeVol(node);
		double mean = (ratio + r0.mean * r0.count + r1.mean * r1.count) / count;
		return new RatioResult(mean, count);
	}

	private double nodeVol(BvhNode node) {
		return ((node.maxBound.x - node.minBound.x) *
				(node.maxBound.y - node.minBound.y) *
				(node.maxBound.z - node.minBound.z));
	}

	/**
	 * Create a BVH [sub]tree.  This tree node will be responsible for storing
	 * and processing surfaces[start] to surfaces[end-1]. If the range is small enough,
	 * this will create a leaf BvhNode. Otherwise, the surfaces will be sorted according
	 * to the axis of the axis-aligned bounding box that is widest, and split into 2
	 * children.
	 *
	 * @param start The start index of surfaces
	 * @param end The end index of surfaces
	 */
	private BvhNode createTree(int start, int end) {
		// A7: fill in this function.
		int i, j;

		// ==== Step 1 ====
		// Find out the BIG bounding box enclosing all the surfaces in the range [start, end)
		// and store them in minB and maxB.
		// Hint: To find the bounding box for each surface, use getMinBound() and getMaxBound() */
		Vector3d minB = new Vector3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		Vector3d maxB = new Vector3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

		for (i=start;i<end;i++) {
			Vector3d lo = surfaces[i].getMinBound();
			Vector3d hi = surfaces[i].getMaxBound();
			for(j=0;j<3;j++) {
				if(lo.get(j) < minB.get(j))
					minB.set(j, lo.get(j));
				if(hi.get(j) > maxB.get(j))
					maxB.set(j, hi.get(j));
			}
		}


		// ==== Step 2 ====
		// Check for the base case.
		// If the range [start, end) is small enough (e.g. less than or equal to 10), just return a new leaf node.
		if(end - start <= 10)
			return new BvhNode(minB, maxB, null, null, start, end);


		// ==== Step 3 ====
		// Figure out the widest dimension (x or y or z).
		// If x is the widest, set widestDim = 0. If y, set widestDim = 1. If z, set widestDim = 2.
		int widestDim = 0;
		for(i=1;i<3;i++)
			if(maxB.get(i)-minB.get(i)>maxB.get(widestDim)-minB.get(widestDim))
				widestDim = i;


		// ==== Step 4 ====
		// Sort surfaces according to the widest dimension.
		cmp.setIndex(widestDim);
		Arrays.sort(surfaces, start, end, cmp);


		// ==== Step 5 ====
		// Recursively create left and right children.
		BvhNode leftChild, rightChild;
		int mid = (start + end) >> 1;
		leftChild  = createTree(start, mid);
		rightChild = createTree(mid, end);

		return new BvhNode(minB, maxB, leftChild, rightChild, start, end);
	}

}

/**
 * A subclass that compares the average position two surfaces by a given
 * axis. Use the setIndex(i) method to select which axis should be considered.
 * i=0 -> x-axis, i=1 -> y-axis, and i=2 -> z-axis.
 *
 */
class MyComparator implements Comparator<Surface> {
	int index;
	public MyComparator() {  }

	public void setIndex(int index) {
		this.index = index;
	}

	public int compare(Surface o1, Surface o2) {
		double v1 = o1.getAveragePosition().get(index);
		double v2 = o2.getAveragePosition().get(index);
		if(v1 < v2) return 1;
		if(v1 > v2) return -1;
		return 0;
	}

}
