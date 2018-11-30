package ray2.light;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import egl.math.Vector2d;
import egl.math.Vector3d;
import egl.math.Colord;

public class Cubemap implements Environment {

	// Parameters
	String filename;
	double scaleFactor = 1.0;

	// Size of full cubemap texture
	int width, height;
	
	// Size of (square) single-face texture
	int blockSz;
	
	// Image data representing constant radiance over each pixel's area
	float[] imageData;
	
	// Table of cumulative probability with which each pixel will be selected
	// in random sampling.
	double[] cumProb;
	
	// Number of bits in representation of pixel count
	int mapBits; // 2^(mapBits-1) < width*height <= 2^mapBits

	public Cubemap() { }

	public void setFilename(String filename) {
		this.filename = filename;

		PNMHeaderInfo hdr = new PNMHeaderInfo();
		imageData = readPFM(new File(filename), hdr);

		width = hdr.width;
		height = hdr.height;
		blockSz = width / 3;

		// Set up cumulative probability table for sampling with binary search
		cumProb = new double[width*height+1];
		cumProb[0] = 0;

		for (int k = 1; k <= width*height; k++)
			cumProb[k] = cumProb[k-1] + calcPixelProb(k-1);

		for (int k = 1; k <= width*height; k++)
			cumProb[k] /= cumProb[width*height];

		for (mapBits = 0; (1 << mapBits) < width*height; mapBits++);
	}

	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}


	/*
	 * @see ray2.light.Environment#eval(egl.math.Vector3d, egl.math.Colord)
	 */
	@Override
	public void eval(Vector3d dir, Colord outRadiance) {
		Vector2d faceUV = new Vector2d();
		
		int iFace = dirToFace(dir, faceUV);
		int k = faceToIndex(iFace, faceUV);

		outRadiance.set(imageData[0 + 3*k], imageData[1 + 3*k], imageData[2 + 3*k]);
		outRadiance.mul(scaleFactor);
	}

	/* 
	 * @see ray2.light.Environment#sample(egl.math.Vector2d, egl.math.Vector3d, egl.math.Colord)
	 * In this implementation, sampling is achieved by performing a
	 * binary search on a cumulative probability table, which selects a pixel to
	 * sample, followed by selecting a random point within that pixel's square area.  Probability
	 * is defined by max(R,G,B) weighted by solid angle.
	 */
	@Override
	public double sample(Vector2d seed, Vector3d outDirection, Colord outRadiance) {

		// choose a pixel
		double searchProb = seed.x;
		int k = 0;

		for (int p = mapBits-1; p >= 0; p--)
			if (searchProb > cumProb[k + (1 << p)])
				k += (1 << p);

		double pixelProb = cumProb[k + 1] - cumProb[k];
		double seedX = (searchProb - cumProb[k]) / pixelProb;

		// choose u and v randomly in that pixel.  faceUV is the pixel center.
		Vector2d faceUV = new Vector2d();
		int iFace = indexToFace(k, faceUV);
		faceUV.x += (2 * seedX - 1) / blockSz;
		faceUV.y += (2 * seed.y - 1) / blockSz;

		// choose the direction based on face index and (u,v)
		faceToDir(iFace, faceUV, outDirection);
		
		double u = faceUV.x;
		double v = faceUV.y;
		
		outRadiance.set(imageData[0 + 3*k], imageData[1 + 3*k], imageData[2 + 3*k]);
		outRadiance.mul(scaleFactor);

		return pixelProb * (blockSz*blockSz/4.0) * Math.pow(1 + u*u + v*v, 1.5);
	}
	
	/*
	 * @see ray2.light.Environment#pdf(egl.math.Vector3d)
	 */
	@Override
	public double pdf(Vector3d dir) {
		Vector2d faceUV = new Vector2d();
		int iFace = dirToFace(dir, faceUV);
		int k = faceToIndex(iFace, faceUV);
		double pixelProb = cumProb[k+1] - cumProb[k];
		return pixelProb * (blockSz*blockSz/4.0) * Math.pow(1 + faceUV.x*faceUV.x + faceUV.y*faceUV.y, 1.5);
	}
	
	


	static protected int dirToFace(Vector3d dir, Vector2d outFaceUV) {
		// direction to cube face
		int iFace;
		double u, v;

		if (Math.abs(dir.x) > Math.abs(dir.y) && Math.abs(dir.x) > Math.abs(dir.z)) {
			iFace = (dir.x > 0) ? 0 : 1;
			u = dir.z / dir.x;
			v = dir.y / Math.abs(dir.x);
		} else if (Math.abs(dir.y) > Math.abs(dir.z)) {
			iFace = (dir.y > 0) ? 2 : 3;
			u = dir.x / Math.abs(dir.y);
			v = dir.z / dir.y;
		} else {
			iFace = (dir.z > 0) ? 4 : 5;
			u = dir.x / Math.abs(dir.z);
			v = -dir.y / dir.z;
		}

		outFaceUV.set(u, v);
		return iFace;
	}
	
	static protected void faceToDir(int iFace, Vector2d faceUV, Vector3d outDir) {
		double u = faceUV.x;
		double v = faceUV.y;

		switch (iFace) {
		case 0:
			outDir.set(1, v, u);
			break;
		case 1:
			outDir.set(-1, v, -u);
			break;
		case 2:
			outDir.set(u, 1, v);
			break;
		case 3:
			outDir.set(u, -1, -v);
			break;
		case 4:
			outDir.set(u, -v, 1);
			break;
		case 5:
			outDir.set(u, v, -1);
			break;
		}

		outDir.normalize();
	}

	protected int faceToIndex(int iFace, Vector2d faceUV) {
		// Table of where to find each face in the 3x4 grid of the map
		final int[][] faceLoc = { {2, 2}, {0, 2}, {1, 3}, {1, 1}, {1, 0}, {1, 2} }; 

		// (iu, iv) are the pixel coordinates within the face
		int iu = (int) (blockSz * (faceUV.x + 1) / 2);
		int iv = (int) (blockSz * (faceUV.y + 1) / 2);

		// (ix, iy) are the pixel coords in the whole map
		int ix = iu + blockSz * faceLoc[iFace][0];
		int iy = iv + blockSz * faceLoc[iFace][1];

		return ix + width * iy;
	}
	
	protected int indexToFace(int index, Vector2d outFaceUV) {
		
		// Table of which face is at each position in the 3x4 grid of the map
		final int[][] locFace = { {-1, 4, -1}, { -1, 3, -1}, {1, 5, 0}, {-1, 2, -1} };

		// (ix, iy) are the pixel coords in the whole map
		int ix = index % width;
		int iy = index / width;
		int iFace = locFace[iy / blockSz][ix / blockSz];

		// (iu, iv) are the pixel coords within a face
		int iu = ix % blockSz;
		int iv = iy % blockSz;

		outFaceUV.set(2 * (iu + 0.5) / (double) blockSz - 1, 2 * (iv + 0.5) / (double) blockSz - 1);

		return iFace;
	}

	protected double calcPixelProb(int k) {
		final double INV_4PI = 1 / (4*Math.PI);
		Vector2d faceUV = new Vector2d();
		if (indexToFace(k, faceUV) == -1) return 0;

		float r = imageData[0 + 3*k];
		float g = imageData[1 + 3*k];
		float b = imageData[2 + 3*k];

		double u = faceUV.x;
		double v = faceUV.y;

		return INV_4PI * Math.max(Math.max(r, g), b) / (float) Math.pow(1 + u*u + v*v, 1.5) / (blockSz * blockSz / 4.0);
	}

	public static class PNMHeaderInfo { 
		int width, height, bands;
		float maxval; 
	}

	public float[] readPFM(File pfmFile, PNMHeaderInfo hdr) {
		
		try {
			FileInputStream inf = new FileInputStream(pfmFile);
			DataInputStream inSt = new DataInputStream(inf);
			FileChannel inCh = inf.getChannel();

			int imageSize = readPPMHeader(inSt, hdr);

			if (imageSize == -1) return null;

			ByteBuffer imageBuffer = ByteBuffer.allocate(imageSize * 4);
			imageBuffer.order(ByteOrder.LITTLE_ENDIAN);
			imageBuffer.clear();
			inCh.read(imageBuffer);

			float[] imageData = new float[imageSize];
			imageBuffer.flip();
			imageBuffer.asFloatBuffer().get(imageData);

			return imageData;
		} catch (FileNotFoundException e) {
			System.err.println("readPFM: file not found: " + pfmFile.getName());
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static int readPPMHeader(DataInputStream in, PNMHeaderInfo info) throws IOException {
		
		// Read PNM header of the form 'P[F]\n<width> <height>\n<maxval>\n'
		if (in.readByte() != 'P') {
			System.err.println("readPFM: not a PNM file");
			return -1;
		}

		byte magic = in.readByte();
		int bands;

		if (magic == 'F') bands = 3;
		else {
			System.err.println("readPFM: Unsupported PNM variant 'P" + magic + "'");
			return -1;
		}

		int width = Integer.parseInt(readWord(in));
		int height = Integer.parseInt(readWord(in));
		int imageSize = width * height * bands;
		float maxval = Float.parseFloat(readWord(in));

		if (info != null) {
			info.width = width;
			info.height = height;
			info.bands = bands;
			info.maxval = maxval;
		}

		return imageSize;
	}

	static String readWord(DataInputStream in) throws IOException {
		char c;
		String s = "";

		while (Character.isWhitespace(c = (char) in.readByte()))
			;
		s += c;
		while (!Character.isWhitespace(c = (char) in.readByte()))
			s += c;

		return s;
	}
}
