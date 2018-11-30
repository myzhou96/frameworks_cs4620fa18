package ray2.tests;

import org.junit.Test;

import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;
import ray2.material.BSDFSamplingRecord;
import ray2.material.MicrofacetBSDF;

public class MicrofacetTests {
	
	double angle1 = 60.0/180.0 * Math.PI;
    double angle2 = -30.0/180.0 * Math.PI;
    Vector3d dir1 = new Vector3d(Math.sin(angle1), 0, Math.cos(angle1));
	Vector3d dir_reflect = new Vector3d(Math.sin(angle2), 0, Math.cos(angle2));
	Vector3d dir_refract = new Vector3d(Math.sin(angle2), 0, -Math.cos(angle2));
	Vector3d normal = new Vector3d(0,0,1);
	Colord outValue = new Colord(0,0,0);
	
	public Colord eval(Vector3d dir1, Vector3d dir2, Vector3d normal, int dist) {
		// Setting up.
        MicrofacetBSDF bsdf = new MicrofacetBSDF(new Colord(1.0,1.0,1.0), new Colord(1.0,1.0,1.0), 0.5, 1.5, dist);

        outValue.set(new Colord(0,0,0));
        bsdf.eval(dir1, dir2, normal, outValue);

        return outValue;
	}
	
	public double pdf(Vector3d dir1, Vector3d dir2, Vector3d normal, int dist) {
		// Setting up.
        MicrofacetBSDF bsdf = new MicrofacetBSDF(new Colord(1.0,1.0,1.0), new Colord(1.0,1.0,1.0), 0.5, 1.5, dist);
        double prob = bsdf.pdf(dir1, dir2, normal);
        return prob;
	}
	
	public double sample(Vector3d dir1, Vector3d normal, Vector2d seed, Vector3d outDir2, Colord outValue, int dist) {
		// Setting up.
        MicrofacetBSDF bsdf = new MicrofacetBSDF(new Colord(1.0,1.0,1.0), new Colord(1.0,1.0,1.0), 0.5, 1.5, dist);
        outValue.set(new Colord(0,0,0));
        BSDFSamplingRecord sr = new BSDFSamplingRecord(dir1, normal,false);
        double prob = bsdf.sample(sr, seed, outValue);
        return prob;
	}
	
	@Test
	public void testBeckmannEval() {
		System.out.println("\nTesting Microfacet Becakmann Eval.");
        System.out.println("==============================================");
        
        outValue = eval(dir1, dir_reflect, normal,0);
        Colord expectedIntensity = new Colord();
        expectedIntensity.set(0.3498099620956212, 0.3498099620956212, 0.3498099620956212);
        TestUtils.assertVector3Equal(outValue, expectedIntensity);
	} 
	
	@Test
	public void testGGXeval() {
		System.out.println("\nTesting Microfacet GGX Eval.");
        System.out.println("==============================================");
        
        outValue = eval(dir1, dir_reflect, normal, 1);
        
        Colord expectedIntensity = new Colord();
        expectedIntensity.set(0.3399154913418668, 0.3399154913418668, 0.3399154913418668);
        TestUtils.assertVector3Equal(outValue, expectedIntensity);
        
        // refracted direction
        outValue = eval(dir1, dir_refract, normal, 1);
        expectedIntensity.set(0.0, 0.0, 0.0);
        TestUtils.assertVector3Equal(outValue, expectedIntensity);
		
	}
	
	@Test
	public void testBeckmannpdf() {
		System.out.println("\nTesting Microfacet Beckmann pdf.");
        System.out.println("==============================================");
        
        double prob = pdf(dir1, dir_reflect, normal, 0);
        double expectedpdf = 0.3748090142144676;
        TestUtils.assertDoublesEqual(prob, expectedpdf);
        
        prob = pdf(dir1, dir_refract, normal, 0);
        expectedpdf = 0.0;
        TestUtils.assertDoublesEqual(prob, expectedpdf);
	}
	
	@Test
	public void testGGXpdf() {
		System.out.println("\nTesting Microfacet GGX pdf.");
        System.out.println("==============================================");
        
        double prob = pdf(dir1, dir_reflect, normal, 1);
        double expectedpdf = 0.3014743973187967;
        TestUtils.assertDoublesEqual(prob, expectedpdf);
        
        prob = pdf(dir1, dir_refract, normal, 1);
        expectedpdf = 0.0;
        TestUtils.assertDoublesEqual(prob, expectedpdf);
     	
     	
	}
	
	@Test
	public void testBeckmann() {
		System.out.println("\nTesting Microfacet Beckmann pdf integral.");
        System.out.println("==============================================");
        
        MicrofacetBSDF bsdf = new MicrofacetBSDF(new Colord(1.0,1.0,1.0), new Colord(1.0,1.0,1.0), 0.5, 1.5, 0);
        
        double cosThetaResolution = 100;
        double phiResolution = 400;
        
        double prob = 0.0;
        
        Vector3d IncomingVec = new Vector3d(Math.sin(Math.PI/3), 0, Math.cos(Math.PI/3));
       
    		for (int k = 0; k < phiResolution; ++k) {
    			double phi = k * 2 * Math.PI / phiResolution;
    			for (int j = 0; j < cosThetaResolution; ++j) {
    				double cosTheta = j * 1.0 / cosThetaResolution;

    				double sinTheta = Math.sqrt(Math.max(1.0 - cosTheta * cosTheta, 0.0));
    				double sinPhi = Math.sin(phi);
    				double cosPhi = Math.cos(phi);
    				Vector3d wo = new Vector3d( sinTheta * cosPhi, sinTheta * sinPhi, cosTheta);
    				prob += bsdf.pdf(IncomingVec, wo, new Vector3d(0.0, 0.0, 1.0));
	    		}
        }
        prob = prob * 2 * Math.PI / (cosThetaResolution * phiResolution);
		
        TestUtils.assertDoublesEqual(prob, 0.7387220488926413); 
        
	}
	
	@Test
	public void testBeckmannSample() {
		System.out.println("\nTesting Microfacet Beckmann sample.");
        System.out.println("==============================================");
        
        int testCount = 100;
        for (int i = 0; i < testCount; ++i) {
	        	// make random incident and normal direction random
	        double cosTheta = Math.random();
	    	    double sinTheta = Math.sqrt(Math.max(0, 1 - Math.pow(cosTheta, 2)));
	    	    double phi = 2 * Math.PI * Math.random();
	    	    double cosPhi = Math.cos(phi);
	    	    double sinPhi = Math.sin(phi);
	    	    
	    		Vector3d dira = new Vector3d(cosPhi * sinTheta, sinPhi * sinTheta, cosTheta);
	    		
	    		double cosTheta2 = Math.random();
	    	    double sinTheta2 = Math.sqrt(Math.max(0, 1 - Math.pow(cosTheta2, 2)));
	    	    double phi2 = 2 * Math.PI * Math.random();
	    	    double cosPhi2 = Math.cos(phi2);
	    	    double sinPhi2 = Math.sin(phi2);
	    	    
	    		Vector3d n = new Vector3d(cosPhi2 * sinTheta2, sinPhi2 * sinTheta2, cosTheta2);
 
	    		Vector2d seed = new Vector2d(0.1, 0.2);
            
            MicrofacetBSDF bsdf = new MicrofacetBSDF(new Colord(1.0,1.0,1.0), new Colord(1.0,1.0,1.0), 0.5, 1.5, 0);
            BSDFSamplingRecord bsr = new BSDFSamplingRecord(dira, n);
            Colord outValue = new Colord();
            double prob = bsdf.sample(bsr, seed, outValue);
            double prob2 = bsdf.pdf(dira, bsr.dir2, n);
            
            TestUtils.assertDoublesEqual(prob, prob2);
        }
        
	}
	
}
