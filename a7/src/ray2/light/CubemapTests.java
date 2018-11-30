package ray2.light;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;

import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;
import javafx.util.Pair;
import ray2.tests.TestUtils;

public class CubemapTests extends Cubemap {

	@Test
	public void testUniformCubemap() {
		Cubemap unifCM = new Cubemap();
		unifCM.setFilename("data/textures/cubemaps/all_white.pfm");
		
		// Uniform cubemap should always return radiance 1.0
		Vector3d dir = new Vector3d();
		Colord radiance = new Colord();
		for (int i = 0; i < 10; i++) {
			unifCM.eval(dir, radiance);
			assertEquals(radiance.r(), 1.0, 1e-6);
			assertEquals(radiance.g(), 1.0, 1e-6);
			assertEquals(radiance.b(), 1.0, 1e-6);
		}
		
		// Sampling the uniform cubemap should always produce the same probability
		// There is some slight error associated with variation in pdf across each pixel
		// so we need a tolerance.  The worst pixel ought to be the corner, where the 
		// difference between the last pixel center and the corner of the cube should 
		// set the relative error.  The test cubemap is 256^2 so this ratio is
		// ((1 + 2 * (255/256)^2) / 3)^(3/2) ~= 0.992
		final int NTRIALS = 10000;
		Vector2d seed = new Vector2d();
		for (int i = 0; i < NTRIALS; i++) {
			seed.set(Math.random(), Math.random());
			double pdf = unifCM.sample(seed, dir, radiance);
			double probRatio = pdf * (4*Math.PI);
			assertEquals("pdf too far from 1/4pi at " + dir, 1.0, probRatio, 8.5e-3);
		}
	}
	
	@Test
	public void testDirToUV() {
		final int NTRIALS = 10000;
		Cubemap unifCM = new Cubemap();
		unifCM.setFilename("data/textures/cubemaps/all_white.pfm");
		for (int i = 0; i < NTRIALS; i++) {
			Vector3d dir = new Vector3d(Math.random(), Math.random(), Math.random()).normalize();
			Vector2d faceUV = new Vector2d();
			int iFace = Cubemap.dirToFace(dir, faceUV);
			Vector3d dir2 = new Vector3d();
			Cubemap.faceToDir(iFace, faceUV, dir2);
			assertTrue(dir.equalsApprox(dir2, 1e-6));
			int k = unifCM.faceToIndex(iFace, faceUV);
			Vector2d faceUV2 = new Vector2d();
			unifCM.indexToFace(k, faceUV2);
			assertTrue("index/face nonreversibility: " + faceUV + " too far from " + faceUV2, faceUV.equalsApprox(faceUV2, 1/128.0));
		}
	}
	
	@Test
	public void testSampleUniform() {
		Cubemap cm = new Cubemap();
		cm.setFilename("data/textures/cubemaps/all_white.pfm");
		testSample((seed, outDir, outRad) -> cm.sample(seed, outDir, outRad),
				(dir) -> cm.pdf(dir));
	}

	@Test
	public void testSampleCircle() {
		Cubemap cm = new Cubemap();
		cm.setFilename("data/textures/cubemaps/circle90.-z.pfm");
		testSample((seed, outDir, outRad) -> cm.sample(seed, outDir, outRad),
				(dir) -> cm.pdf(dir));
	}
	
	@Test
	public void testSampleImage() {
		Cubemap cm = new Cubemap();
		cm.setFilename("data/textures/cubemaps/uffizi_cross.pfm");
		testSample((seed, outDir, outRad) -> cm.sample(seed, outDir, outRad),
				(dir) -> cm.pdf(dir));
	}

	
	static double rand() {
		return Math.random();
	}

	@FunctionalInterface
    interface Function3 <A, B, C, R> { 
        public R apply (A a, B b, C c);
    }

	static void testSample(Function3<Vector2d, Vector3d, Colord, Double> sample, 
			Function<Vector3d, Double> pdf) {
		// testCount is how many tests to perform, for each test,
		// random generate an incoming direction
		int testCount = 1;
		double minExpFrequency = 5;
		double significancelevel = 0.01;
		int cosThetaResolution = 20;
		int phiResolution = 2 * cosThetaResolution;
		int samplePerBin = 50;
		int sampleCount = cosThetaResolution * phiResolution * samplePerBin;
		
		int res = cosThetaResolution * phiResolution;
		double obsFrequencies[] = new double[res];
		double expFrequencies[] = new double[res];
		
		for (int i = 0; i < testCount; ++i) {

			System.out.printf("Accumulating %s samples into a %s x %s contingency table...\n",
		    		sampleCount, cosThetaResolution, phiResolution);
		    		    
		    for (int j = 0; j < sampleCount; ++j) {
		    		Colord outValue = new Colord(0.0, 0.0, 0.0);
		    		Vector2d seed = new Vector2d(rand(), rand());
		    		Vector3d dir = new Vector3d();
		    		double prob = sample.apply(seed, dir, outValue);
		    		assertEquals((double) prob, (double) pdf.apply(dir), 1e-6);
		    		
		    		if (outValue.equals(new Vector3d(0.0, 0.0, 0.0))) continue;
		    		
		    		int tmp = (int) Math.floor((dir.z*0.5 + 0.5) * cosThetaResolution);
		    		int cosThetaBin = Math.min(Math.max(0, tmp), cosThetaResolution-1);
		    		
		    		double scaledPhi = Math.atan2(dir.y, dir.x) / (2 * Math.PI);
		    		if (scaledPhi < 0) scaledPhi += 1;
		    		
		    		int phiBin = Math.min(Math.max(0, (int) Math.floor(scaledPhi * phiResolution)),
		    				phiResolution-1);
		    		
		    		obsFrequencies[cosThetaBin * phiResolution + phiBin] += 1;
		    }
		    
		    System.out.println("Computing expected probabilities...");    
		    		
		    /** Numerically integrate the probability density
            function over rectangles in spherical coordinates. */
		    int count = 0;
		    for (int j = 0; j < cosThetaResolution; ++j) {
		    		double cosThetaStart = -1.0 + j * 2.0 / cosThetaResolution;
		    		double cosThetaEnd = -1.0 + (j+1) * 2.0 / cosThetaResolution;
		    		
		    		for (int k = 0; k < phiResolution; ++k) {
		    			double phiStart = k * 2 * Math.PI / phiResolution;
		    			double phiEnd = (k+1) * 2 * Math.PI / phiResolution;
		    			
		    			// Integrand here
		    			BiFunction<Double, Double, Double> f = (Double phi_1, Double cosTheta_1)-> {
		    				Double sinTheta_1 = Math.sqrt(Math.max(1.0 - cosTheta_1 * cosTheta_1, 0.0));
		    				Double sinPhi_1 = Math.sin(phi_1);
		    				Double cosPhi_1 = Math.cos(phi_1);
		    				Vector3d dir = new Vector3d( sinTheta_1 * cosPhi_1, sinTheta_1 * sinPhi_1, cosTheta_1);

		    				return pdf.apply(dir);
		    			};
		    			
		    			// Use Java RombergIntegrator to do numerical integration
		    			double integral = TestUtils.Romberg2D(f, cosThetaStart, phiStart, cosThetaEnd, phiEnd);	
		    			expFrequencies[count] += integral * sampleCount;
		    			count += 1;
		    		}
		    }
		    
		    System.out.println("Performing statistical test...");    

		    Pair<Boolean, Double> result = TestUtils.chi2(res, obsFrequencies, expFrequencies, sampleCount, minExpFrequency, significancelevel, testCount);
		    assertEquals(result.getKey(), true);
		}
		
	}
}
