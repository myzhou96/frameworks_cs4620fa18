package ray2.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.RombergIntegrator;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;
import javafx.util.Pair;
import ray2.Ray;

public class TestUtils {
    public static final double EPSILON_D = 1e-6;

    public static void assertRaysEqual(String message, Ray ray0, Ray ray1) {
        System.out.println(message);
        assertRaysEqual(ray0, ray1);
    }

    public static void assertRaysEqual(Ray ray0, Ray ray1) {
        Vector3d dir0 = new Vector3d(ray0.direction);
        Vector3d dir1 = new Vector3d(ray1.direction);
        assertVector3dEqual(dir0.normalize(), dir1.normalize());
        assertVector3dEqual(ray0.origin, ray1.origin);
    }

    public static void assertVector3dEqual(Vector3d v0, Vector3d v1) {
        assertTrue(v0.equalsApprox(v1, 1e-6));
    }

    public static void assertVector3Equal(Colord outValue, Colord expectedIntensity) {
    	assertTrue(outValue.equalsApprox(expectedIntensity));
    }

    public static void assertDoublesEqual(double d0, double d1) {
        assertTrue(Math.abs(d0 - d1)< EPSILON_D);
    }

	static UnivariateFunction bind(BiFunction<Double, Double, Double> fn, double x) {
		return (y) -> fn.apply(x, y);
	}
	
	// ----- Some simple utilities for testing random sampling code -----
	
	/**
	 * Use Pearson's Chi-squared test to say whether this histogram agrees with a given discrete distribution.
	 * @param obs The observed counts in each bin
	 * @param exp The expected counts in each bin
	 */
	static void assertDistributionsMatch(int obs[], double exp[]) {
		final double P = 0.01; // significance threshold for reporting a failure
		double chiSquared = 0;
		for (int i = 0; i < obs.length; i++)
			chiSquared += Math.pow(obs[i] - exp[i], 2) / exp[i];
		// Pearson's chi-squared test: null hypothesis implies this value is distributed
		// according to a chi^2 distribution with n-1 degrees of freedom.  The cumulative 
		// probability exceeds 1-P only with probability P.
		ChiSquaredDistribution chi2 = new ChiSquaredDistribution(null, obs.length - 1);
		double pval = 1 - chi2.cumulativeProbability(chiSquared); 
		if (pval < P) {
			System.err.println("obs. count\texpected");
			for (int i = 0; i < obs.length; i++)
				System.err.println(obs[i] + "\t" + exp[i]);
			fail("assertDistributionsMatch: results differ significantly from expected values (p = " + pval + ")");
		}
	}

	/**
	 * Use Pearson's Chi-squared test to say whether this histogram differs from a uniform distribution.
	 * @param hist The histogram that is supposed to be uniform
	 */
	static void assertUniform(int hist[], int n) {
		final double P = 0.01; // significance threshold for reporting a failure
		double expectedCount = n / (double) hist.length;
		double chiSquared = 0;
		for (int i = 0; i < hist.length; i++)
			chiSquared += Math.pow(hist[i] - expectedCount, 2) / expectedCount;
		// Pearson's chi-squared test: null hypothesis implies this value is distributed
		// according to a chi^2 distribution with n-1 degrees of freedom.  The cumulative 
		// probability exceeds 1-P only with probability P.
		ChiSquaredDistribution chi2 = new ChiSquaredDistribution(null, hist.length - 1);
		assertTrue(chi2.cumulativeProbability(chiSquared) < 1 - P);
	}
	
	public static void testAgainstCDF(Function<Vector2d, Double> fn, Function<Double, Double> cdf, double min, double max) {
		final int NBINS = 10, NTRIALS = 10000;
		int hist[] = new int[NBINS];
		for (int i = 0; i < NTRIALS; i++) {
			double val = fn.apply(new Vector2d(rand(), rand()));
			hist[Math.max(0, Math.min(NBINS-1, (int) Math.floor(NBINS * (val - min) / (max - min))))]++;
		}
		assertEquals(0.0, cdf.apply(min), 1e-6);
		assertEquals(1.0, cdf.apply(max), 1e-6);
		double exp[] = new double[NBINS];
		for (int i = 0; i < NBINS; i++)
			exp[i] = (cdf.apply(min + (i + 1) / (double) NBINS * (max - min)) -
			          cdf.apply(min + i / (double) NBINS * (max - min))) * NTRIALS;
		assertDistributionsMatch(hist, exp);	
	}

	public static void testForUniformity(Function<Vector2d, Double> fn) {
		final int NBINS = 10, NTRIALS = 10000;
		int hist[] = new int[NBINS];
		for (int i = 0; i < NTRIALS; i++) {
			double val = fn.apply(new Vector2d(rand(), rand()));
			hist[Math.max(0, Math.min(NBINS-1, (int) Math.floor(NBINS * val)))]++;
		}
		assertUniform(hist, NTRIALS);		
	}

	// ---- more elaborate utility for testing random sampling code ----
	// adapted from Nori chi2test.cpp	

	// chi2 test on two double arrays
	public static Pair<Boolean, Double> chi2(int nCells, double obs[], double exp[], int sampleCount, 
			double minExpFrequency, double significanceLevel, int numTests) {
		int pooledFrequencies = 0;
		int pooledExpFrequencies = 0;
		int pooledCells = 0; 
		int chsq = 0;
		int dof = 0;
		double pval = 0;
	
		// sort exp index according to ascending exp value
		int[] cell = IntStream.range(0, exp.length)
	            .boxed().sorted((i, j) -> Double.compare(exp[i], exp[j])).mapToInt(ele -> ele).toArray();
		
	    for (int ind: cell) {
		    	if (exp[ind] < 0) {
		    		System.out.println("error: negative freq");
		        return new Pair<Boolean, Double>(false, 0.0);
		    	} else if (exp[ind] == 0){
		    		if (obs[ind] > 1e-5) {
			    		System.out.println("error: nonzero count with zero expected");
		    			return new Pair<Boolean, Double>(false, 0.0);
		    		}
		    	} else if (exp[ind] < minExpFrequency) {
		    		// Pool cells with low expected frequencies 
	            pooledFrequencies += obs[ind];
	            pooledExpFrequencies += exp[ind];
	            pooledCells += 1;
		    	} else if (pooledExpFrequencies > 0 && pooledExpFrequencies < minExpFrequency) {
		    		// Keep on pooling cells until a sufficiently high expected frequency is achieved. 
	            pooledFrequencies += obs[ind];
	            pooledExpFrequencies += exp[ind];
	            pooledCells += 1;
		    	} else {
		    		double diff = obs[ind] - exp[ind];
	            chsq += (diff*diff) / exp[ind];
	            dof += 1;
		    	}
	    }
	    
	    if (pooledExpFrequencies > 0 || pooledFrequencies > 0) {
	    		System.out.printf("Pooled %s to ensure sufficiently high expected cell frequencies (> %s )  ", pooledCells, minExpFrequency);
	    		double diff = pooledFrequencies - pooledExpFrequencies;
	        chsq += (diff*diff) / pooledExpFrequencies;
	        dof += 1;
	    }
	    
	    //  additional DF reduction due to model parameters
	    dof -= 1;
	    
	    if (dof <= 0) {
	    		System.out.printf("The number of degrees of freedom ( %s ) is too low!", dof);
	        return new Pair<Boolean, Double>(false,0.0);
	    }
	
	    // Probability of obtaining a test statistic at least as extreme as the one observed under the assumption
	    // that the distributions match 
	    ChiSquaredDistribution chi2 = new ChiSquaredDistribution(null, dof);
	    pval = 1 - chi2.cumulativeProbability(chsq);
	    
	    System.out.printf("Chi^2 statistic = %s   (d.o.f. =  %s )   pval = %.3f\n", chsq, dof, pval);			    
	
	    // Apply the Sidak correction term, since we'll be conducting multiple independent
	    // hypothesis tests. This accounts for the fact that the probability of a failure
	    // increases quickly when several hypothesis tests are run in sequence. */
	    double alpha = 1.0 - Math.pow((1.0 - significanceLevel), (1.0 / numTests));
	    Boolean result = false;
	    if (pval < alpha || Double.isNaN(pval) || Double.isInfinite(pval)) {
	    		System.out.println("results differ significanlty from correct values.");
			System.out.println("observed\texpected");
			for (int k = 0; k < obs.length; k++)
				System.out.println(obs[k] + "\t" + exp[k]);
	    		result = false;
	    }
	    else result = true;
	
		return new Pair<Boolean, Double>(result, pval);
	}

	// Nested Romberg2D integration over a 2D rectangle
	public static double Romberg2D(BiFunction<Double, Double, Double> fn, double x0, double y0, double x1, double y1) {
		RombergIntegrator integrator = new RombergIntegrator(1e-5, 1e-5, 1, 32);
		RombergIntegrator integrator2 = new RombergIntegrator(1e-5, 1e-5, 1, 32);
		
		UnivariateFunction Romberg1D = (y)->integrator2.integrate(10000000, bind(fn,y), x0, x1);
		double value = integrator.integrate(10000000, Romberg1D, y0, y1);
	
		return value;
	}

	

	static double rand() {
		return Math.random();
	}
}
