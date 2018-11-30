package ray2.material;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.function.BiFunction;

import org.junit.Test;

import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;
import javafx.util.Pair;
import ray2.tests.TestUtils;

public class BSDFTests {
		
	// ----- Test arbitrary BSDF sampling function -----
	// adapted from Nori chi2test.cpp	
	
	@FunctionalInterface
    interface Function3 <A, B, C, R> { 
    //R is like Return, but doesn't have to be last in the list nor named R.
        public R apply (A a, B b, C c);
    }
	
	/** f1 is bsdf sample function, f2 is bsdf pdf fucntion 
	 * @return */
	static void testSample(Function3<BSDFSamplingRecord, Vector2d, Colord, Double> f1, 
			Function3<Vector3d, Vector3d, Vector3d, Double>f2) {
		// testCount is how many tests to perform, for each test,
		// random generate an incoming direction
		int testCount = 1;
		double minExpFrequency = 5;
		double significancelevel = 0.01;
		int cosThetaResolution = 10;
		int phiResolution = 2 * cosThetaResolution;
		int samplePerBin = 50;
		int sampleCount = cosThetaResolution * phiResolution * samplePerBin;
		
		int res = cosThetaResolution * phiResolution;
		double obsFrequencies[] = new double[res];
		double expFrequencies[] = new double[res];
		
		for (int i = 0; i < testCount; ++i) {
		    // generate random incoming direction
		    double cosTheta = rand();
		    double sinTheta = Math.sqrt(Math.max(0, 1 - Math.pow(cosTheta, 2)));
		    double phi = 2 * Math.PI * Math.random();
		    double cosPhi = Math.cos(phi);
		    double sinPhi = Math.sin(phi);
		    System.out.printf("Accumulating %s samples into a %s x %s contingency table...\n",
		    		sampleCount, cosThetaResolution, phiResolution);

		    	Vector3d IncomingVec = new Vector3d(cosPhi * sinTheta, sinPhi * sinTheta, cosTheta);
		    
		    	Vector3d normal = new Vector3d();
		    	boolean keepGenerate = true;
		    	while (keepGenerate) {
		    		double cosTheta2 = rand();
			    double sinTheta2 = Math.sqrt(Math.max(0, 1 - Math.pow(cosTheta2, 2)));
			    double phi2 = 2 * Math.PI * Math.random();
			    double cosPhi2 = Math.cos(phi2);
			    double sinPhi2 = Math.sin(phi2);
			    normal.set(new Vector3d(cosPhi2 * sinTheta2, sinPhi2 * sinTheta2, cosTheta2));
			    
			    if (IncomingVec.dot(normal)>0) keepGenerate = false;
		    	}

		    for (int j = 0; j < sampleCount; ++j) {
		    		Colord outValue = new Colord(0.0, 0.0, 0.0);
		    		Vector2d seed = new Vector2d(rand(), rand());
		    		BSDFSamplingRecord sr = new BSDFSamplingRecord(IncomingVec,normal, false);
		    		double prob = f1.apply(sr, seed, outValue);
		    		
		    		assertEquals(prob, (double) f2.apply(IncomingVec, sr.dir2, normal), 1e-6);
		    		
		    		if (outValue.equals(new Vector3d(0.0, 0.0, 0.0))) continue;
		    		
		    		int tmp = (int) Math.floor((sr.dir2.z*0.5 + 0.5) * cosThetaResolution);
		    		int cosThetaBin = Math.min(Math.max(0, tmp), cosThetaResolution-1);
		    		
		    		double scaledPhi = Math.atan2(sr.dir2.y, sr.dir2.x) / (2 * Math.PI);
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
		    				Vector3d wo = new Vector3d( sinTheta_1 * cosPhi_1, sinTheta_1 * sinPhi_1, cosTheta_1);

		    				return f2.apply(IncomingVec, wo, normal);
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
	
	
	// ----- Tests for the Lambertian BSDF -----
	
	static double uniformHemisphereZ(Vector2d seed) {
		Vector3d dir = new Vector3d();
		LambertianBSDF.uniformHemisphere(seed, dir);
		assertEquals(dir.len(), 1.0, 1e-6);
		return dir.z;
	}

	@Test
	public void testUniformHemisphere() {
		TestUtils.testForUniformity((seed) -> uniformHemisphereZ(seed));
	}
	
	static double cosineHemisphereZ(Vector2d seed) {
		Vector3d dir = new Vector3d();
		LambertianBSDF.cosineHemisphere(seed, dir);
		assertEquals(dir.len(), 1.0, 1e-6);
		return Math.acos(dir.z);
	}

	@Test
	public void testCosineHemisphere() {
		// pdf for theta is cos theta * sin theta dtheta
		// cdf is the integral of that, which is 1/2 - 1/2 cos 2*theta
		TestUtils.testAgainstCDF((seed) -> cosineHemisphereZ(seed), (theta) -> 0.5 - 0.5 * Math.cos(2*theta), 0, Math.PI / 2);
	}
	
	static double sampleNdotW(BSDF b, Vector2d seed) {
		Vector3d dir1 = new Vector3d();
		Vector3d normal = new Vector3d(rand(), rand(), rand()).normalize();
		Colord value = new Colord();
		
		BSDFSamplingRecord sr = new BSDFSamplingRecord(dir1, normal,false);       
		b.sample(sr, seed, value);
		assertEquals(1.0, sr.dir2.len(), 1e-6);
		return sr.dir2.dot(normal);		
	}

	@Test
	public void testLambertianSample() {
		LambertianBSDF bsdf = new LambertianBSDF();
		TestUtils.testAgainstCDF((seed) -> Math.acos(sampleNdotW(bsdf, seed)),
				(theta) -> 0.5 - 0.5 * Math.cos(2*theta), 0, Math.PI / 2);
		testSample((sr, seed, outValue) -> bsdf.sample(sr, seed, outValue),
				   (dir1, dir2, normal) -> bsdf.pdf(dir1, dir2, normal));
	}
	
	
	// ----- Tests for the Microfacet BSDF -----
	
	@Test
	public void testMicrofacetSampleB() {
	    System.out.println("\nTesting Microfacet Beckmann sample.");
        System.out.println("==============================================");
        
		MicrofacetBSDF bsdf = new MicrofacetBSDF(new Colord(0.0,0.0,0.0), new Colord(1.0,1.0,1.0), 0.5, 1.5, 0);
		
		testSample((sr, seed, outValue)->bsdf.sample(sr, seed, outValue), 
				(dir1, dir2, normal)->bsdf.pdf(dir1, dir2, normal));
	}
	
	@Test
	public void testMicrofacetSampleG() {
	    System.out.println("\nTesting Microfacet GGX sample.");
        System.out.println("==============================================");
        
		MicrofacetBSDF bsdf = new MicrofacetBSDF(new Colord(0.0,0.0,0.0), new Colord(1.0,1.0,1.0), 0.5, 1.5, 1);
		
		testSample((sr, seed, outValue)->bsdf.sample(sr, seed, outValue), 
				(dir1, dir2, normal)->bsdf.pdf(dir1, dir2, normal));
	}
	
	
	// ----- Test for Fresnel utility function -----
	
    @Test
    public void testFresnel() {
        Vector3d normal = new Vector3d(1, 1, 1);
        Vector3d outgoing = new Vector3d(1, 1, 1);
        double refractiveIndex = 2.0f;
        MicrofacetBSDF bsdf = new MicrofacetBSDF();
        
        double result;
        result = bsdf.fresnel(normal, outgoing, refractiveIndex);
        
        assertTrue("Fresnel:\n"
                + "normal: " + normal + "\n"
                + "outgoing: " + outgoing + "\n"
                + "refractiveIndex: " + refractiveIndex + "\n"
                + "Expected: 0.1549192\n"
                + "Got: " + result, doublesEqual(0.1549192, result));
        
        outgoing.set(-1, 1, 0);
        result = bsdf.fresnel(normal, outgoing, refractiveIndex);
        assertTrue("Fresnel:\n"
                + "normal: " + normal + "\n"
                + "outgoing: " + outgoing + "\n"
                + "refractiveIndex: " + refractiveIndex + "\n"
                + "Expected: 1.0\n"
                + "Got: " + result, doublesEqual(1.0, result));
        
        normal.set(1, 2, 0);
        result = bsdf.fresnel(normal, outgoing, refractiveIndex);
        assertTrue("Fresnel:\n"
                + "normal: " + normal + "\n"
                + "outgoing: " + outgoing + "\n"
                + "refractiveIndex: " + refractiveIndex + "\n"
                + "Expected: 0.111111111\n"
                + "Got: " + result, doublesEqual(0.111111111, result));
        
        refractiveIndex = 5.0;
        result = bsdf.fresnel(normal, outgoing, refractiveIndex);
        assertTrue("Fresnel:\n"
                + "normal: " + normal + "\n"
                + "outgoing: " + outgoing + "\n"
                + "refractiveIndex: " + refractiveIndex + "\n"
                + "Expected: 0.44444444\n"
                + "Got: " + result, doublesEqual(0.44444444, result));
    }
    
    private boolean doublesEqual(double d0, double d1) {
        double epsilon = 1e-4;
        return Math.abs(d0 - d1) < epsilon;
    }


	static double rand() {
		return Math.random();
	}
	
	
}
