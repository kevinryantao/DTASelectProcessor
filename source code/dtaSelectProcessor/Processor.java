package dtaSelectProcessor;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Processor {

	private DtaSelectObject dtaSelect;
	private double errorRate = 0.02;

	// These are hard-coded limits to how low or high the thresholds can go. I came up with these by taking
	// 90% of the minimums given to me, and 110% of the maximums given to me, by Dr. Steffen.
	private static final double MIN_1 = 1.20;
	private static final double MAX_1 = 1.90;
	private static final double MIN_2 = 1.40;
	private static final double MAX_2 = 2.75;
	private static final double MIN_3 = 2.50;
	private static final double MAX_3 = 3.85;
	private static final double[][] BOUNDS = { { MIN_1, MAX_1 }, { MIN_2, MAX_2 }, { MIN_3, MAX_3 } };
	private static final double[] ORIGIN = { MAX_1, MAX_2, MAX_3 };
	private double deltaCN = 0.05;

	private HashMap<doubleArrayMapKey, int[]> memoizedEvaluations;
	private double[] lastEvaluation = new double[3];
	
	/**
	 * Creates a "processor" and links the processor to the DTASelect object.
	 * 
	 * @param dtaSelect
	 */
	public Processor(DtaSelectObject dtaSelect) {
		this.dtaSelect = dtaSelect;
		memoizedEvaluations = new HashMap<doubleArrayMapKey, int[]>();
	}

	public void runDumb(double[] xCorThresh, double deltaCNThresh) {
		System.err.println("Starting...");
		int[] totalReads = evaluate(xCorThresh, deltaCNThresh);
		System.err.println("No. of Decoys\tNo. of Reals");
		System.err.println(totalReads[0] + "\t\t\t\t" + totalReads[1]);
	}

	/**
	 * Processes the data according the error rate and the selected mode
	 * 
	 * @param errorRate
	 *            A decimal representing the maximum ratio of decoy reads to real reads
	 * @param deltaCNThresh
	 *            a String representing the desired mode. If the String doesn't match correctly, the default is to
	 *            maximize charge state -.2 reads.
	 */
	public void runSmart(double errorRate, double deltaCNThresh) {
		System.err.println("Starting...");

		this.errorRate = errorRate;
		this.deltaCN = deltaCNThresh;
		double[] eVector = { ORIGIN[0] - BOUNDS[0][0], ORIGIN[1] - BOUNDS[1][0], ORIGIN[2] - BOUNDS[2][0] };
		xCorDataPoint answer;

		double[] eigenvectorStart = phaseOne();
		eVector = eigenvectorStart;

		System.err.println("...");
		System.err.println("...");
		System.err.println("Phase two: Commencing search for local maxima");
		System.err.println("Number of Real Reads");
		System.err.println("-.1\t-.2\t-.3\tCurrent");
		
		xCorDataPoint currentAnswer = new xCorDataPoint(eVector);
		while (true) {
			double[] vector1 = eVector.clone();
			vector1[0] += .05;
			double[] vector2 = eVector.clone();
			vector2[1] += .05;
			double[] vector3 = eVector.clone();
			vector3[2] += .05;

			xCorDataPoint newAnswer1 = new xCorDataPoint(vector1);
			xCorDataPoint newAnswer2 = new xCorDataPoint(vector2);
			xCorDataPoint newAnswer3 = new xCorDataPoint(vector3);

			int[] reals = { newAnswer1.getFinalReads()[1], newAnswer2.getFinalReads()[1],
					newAnswer3.getFinalReads()[1], currentAnswer.getFinalReads()[1] };
			for (int i : reals) {
				System.err.print(i + "\t");
			}
			Arrays.sort(reals);
			System.err.print("Eigenvector: " + eVector[0] + " " + eVector[1] + " " + eVector[2]);

			double[] currentThresh = currentAnswer.getThreshold();
			System.err.println("\tThresholds: \t" + currentThresh[0] + "\t" + currentThresh[1] + "\t"
					+ currentThresh[2]);

			if (currentAnswer.getFinalReads()[1] == reals[3]) {
				break;
			}
			if (newAnswer1.getFinalReads()[1] == reals[3]) {
				currentAnswer = newAnswer1;
				eVector = vector1;
			} else if (newAnswer2.getFinalReads()[1] == reals[3]) {
				currentAnswer = newAnswer2;
				eVector = vector2;
			} else if (newAnswer3.getFinalReads()[1] == reals[3]) {
				currentAnswer = newAnswer3;
				eVector = vector3;
			}
		}
		answer = new xCorDataPoint(eVector);

		int[] totalReads = answer.getFinalReads();
		double[] finalThresh = minimizeXCorThresh(answer.getThreshold(), totalReads[0]);
		
		totalReads = evaluate(finalThresh, deltaCN);

		System.err.println("Reads: " + totalReads[0] + " decoys and " + totalReads[1] + " reals.");
		System.err.println("Thresholds: " + finalThresh[0] + " " + finalThresh[1] + " " + finalThresh[2]);
	}

	/**
	 * Prints the dtaSelect object to System.out
	 */
	public void printOutToFile() {
		dtaSelect.applyThreshold(lastEvaluation, deltaCN);
		ArrayList<String> output = dtaSelect.dtaObjectToStringArray();
		for (String s : output) {
			System.out.println(s);
		}
	}
	
	/**
	 * Prints the dtaSelect object to the specified printstream
	 * @param ps
	 */
	public void printOutToFile(PrintStream ps){
		dtaSelect.applyThreshold(lastEvaluation, deltaCN);
		ArrayList<String> output = dtaSelect.dtaObjectToStringArray();
		for (String s : output) {
			ps.println(s);
		}
	}
	
	/**
	 * Prints the dtaSelect object to the specified printstream
	 * @param ps
	 */
	public void printOutToFileSatisfiesXCor(PrintStream ps){
		dtaSelect.applyThreshold(lastEvaluation, deltaCN);
		ArrayList<String> output = dtaSelect.dtaObjectToStringArrayExistingOnly();
		for (String s : output) {
			ps.println(s);
		}
	}

	/**
	 * @return a double array representing the top eigenvector, from a variegated swath of eigenvectors.
	 */
	private double[] phaseOne() {

		System.err.println("Phase One: Determining best starting eigenvector");

		ArrayList<double[]> eigenvectorArrayList = new ArrayList<double[]>();

		double[] eigenvector = { .4, .4, .4 };
		eigenvectorArrayList.add(eigenvector);
		double[] eigenvector2 = { .2, .5, .5 };
		eigenvectorArrayList.add(eigenvector2);
		double[] eigenvector3 = { .5, .2, .5 };
		eigenvectorArrayList.add(eigenvector3);
		double[] eigenvector4 = { .5, .5, .2 };
		eigenvectorArrayList.add(eigenvector4);
		double[] eigenvector5 = { .8, .3, .1 };
		eigenvectorArrayList.add(eigenvector5);
		double[] eigenvector6 = { .1, .3, .8 };
		eigenvectorArrayList.add(eigenvector6);
		double[] eigenvector7 = { .1, .8, .3 };
		eigenvectorArrayList.add(eigenvector7);
		double[] eigenvector8 = { .8, .1, .3 };
		eigenvectorArrayList.add(eigenvector8);
		double[] eigenvector9 = { .3, .8, .1 };
		eigenvectorArrayList.add(eigenvector9);
		double[] eigenvector10 = { .3, .1, .8 };
		eigenvectorArrayList.add(eigenvector10);

		double[] eigenvector11 = { Math.random() / 2, Math.random(), Math.random() };
		eigenvectorArrayList.add(eigenvector11);
		double[] eigenvector12 = { Math.random(), Math.random() / 2, Math.random() };
		eigenvectorArrayList.add(eigenvector12);
		double[] eigenvector13 = { Math.random(), Math.random(), Math.random() / 2 };
		eigenvectorArrayList.add(eigenvector13);
		double[] eigenvector14 = { Math.random() / 2, Math.random() / 2, Math.random() / 2 };
		eigenvectorArrayList.add(eigenvector14);

		int highestTotalReads = 0;
		double[] bestEigenVector = eigenvector;

		for (double[] eVector : eigenvectorArrayList) {
			xCorDataPoint currentAnswer = new xCorDataPoint(eVector);
			if (currentAnswer.getFinalReads()[1] >= highestTotalReads) {
				highestTotalReads = currentAnswer.getFinalReads()[1];
				bestEigenVector = eVector;
			}
			System.err.print("No. of real proteins: " + currentAnswer.getFinalReads()[1] + "\t");

			System.err.print("Eigenvector: " + eVector[0] + "\t" + eVector[1] + "\t" + eVector[2]);

			double[] currentThresh = currentAnswer.getThreshold();
			System.err.println("\tThresholds: \t" + currentThresh[0] + "\t" + currentThresh[1] + "\t"
					+ currentThresh[2]);
		}

		System.err.println("Best eigenvector in phase one: " + bestEigenVector[0] + "\t" + bestEigenVector[1] + "\t"
				+ bestEigenVector[2]);
		return bestEigenVector;
	}

	/**
	 * @param xCorThresh
	 * @param deltaCN TODO
	 * @return the int[] of total decoys and total real reads. The first int representing the number of decoy reads
	 *         present and the 2nd int representing the number of real reads present
	 */
	public int[] evaluate(double[] xCorThresh, double deltaCNThresh) {
		lastEvaluation = xCorThresh;
		doubleArrayMapKey xCor = new doubleArrayMapKey(xCorThresh);
		if(memoizedEvaluations.containsKey(xCor)){
			return memoizedEvaluations.get(xCor);
		}
		
		dtaSelect.applyThreshold(xCorThresh, deltaCNThresh);
		dtaSelect.updateAllProteins();
		int [] report = dtaSelect.report();
		memoizedEvaluations.put(xCor, report);
		return report;
	}
	
	/**
	 * @param xCorThresh
	 * @param deltaCN TODO
	 * @return the int[] of total decoys and total real reads. The first int representing the number of decoy reads
	 *         present and the 2nd int representing the number of real reads present
	 */
	public int[] evaluate(double xCorThresh1,double xCorThresh2,double xCorThresh3, double deltaCNThresh) {
		double[] xCorThresh = {xCorThresh1,xCorThresh2,xCorThresh3};
		return evaluate(xCorThresh, deltaCN);
	}
	
	private double[] minimizeXCorThresh(double[] xCorThresh, int maxDecoys){
		
		double[] threeFirst = xCorThresh.clone();
		minimizeThreshold(threeFirst,3, maxDecoys);
		int[] threeResults = minimizeThreshold(threeFirst,1, maxDecoys);
		
		double[] oneFirst = xCorThresh.clone();
		minimizeThreshold(oneFirst,1, maxDecoys);
		int[] oneResults = minimizeThreshold(oneFirst,3, maxDecoys);
		
		double[] twoFirst = xCorThresh.clone();
		if(threeResults[1] > oneResults[1]){			
			minimizeThreshold(twoFirst, 2, maxDecoys);
			int[] twoResults = minimizeThreshold(twoFirst,3, maxDecoys);
			
			if(twoResults[1] > threeResults[1]){
				return twoFirst;
			} else{
				return threeFirst;
			}
		} else {
			minimizeThreshold(twoFirst, 2, maxDecoys);
			int[] twoResults = minimizeThreshold(twoFirst,1, maxDecoys);
			
			if(twoResults[1] > oneResults[1]){
				return twoFirst;
			} else{
				return oneFirst;
			}
		}
	}
	
	private int[] minimizeThreshold(double[] xCorThresh, int chargeState, int maxDecoys){
		double chargeDiff = 1;
		while(true){
			xCorThresh[chargeState-1] -= chargeDiff;
			
			int[] result = evaluate(xCorThresh[0],xCorThresh[1],xCorThresh[2], deltaCN);
			
			if(result[0] > maxDecoys) {
				xCorThresh[chargeState-1] += chargeDiff;
				chargeDiff = chargeDiff / 2;
			}
			if(chargeDiff < 0.00048){
				return result;
			}
		}
	}

	/**
	 * This class determines the threshold at which the error rate is equal to the requested error rate, given an
	 * eigenvector along which to sample. It uses binary search along the one dimensional eigenvector to locate it. It
	 * stores threshold and counts of peptides.
	 * 
	 * @author Kevin
	 */
	private class xCorDataPoint {
		private double[] threshold = new double[3];
		private int[] finalReads = { -1, -1 };

		private xCorDataPoint(double[] eigenvector) {
			double maxCoef = findMaxCoefficient(eigenvector);
			double diffCoef = maxCoef / 4.0;
			double currentCoef = maxCoef / 2.0;
			int[] decoysOverReal = { -1, -1 };
			while (diffCoef > 0.0005) {
				double[] currentThresh = { ORIGIN[0] - currentCoef * eigenvector[0],
						ORIGIN[1] - currentCoef * eigenvector[1], ORIGIN[2] - currentCoef * eigenvector[2] };
				decoysOverReal = evaluate(currentThresh, deltaCN);
				if (Math.abs(errorRate - 1.0 * decoysOverReal[0] / decoysOverReal[1]) < 0.0001) {
					break;
				}
				if (1.0 * decoysOverReal[0] / decoysOverReal[1] < errorRate) {
					// lower threshold
					currentCoef += diffCoef;
					diffCoef = diffCoef / 2;
				} else {
					// raise threshold
					currentCoef -= diffCoef;
					diffCoef = diffCoef / 2;
				}
				// System.err.println("Reads: "+ decoysOverReal[0]+" decoys and "+ decoysOverReal[1]+" reals.");
				// System.err.println("Thresholds: "+ currentThresh[0] + " "+ currentThresh[1]+ " "+ currentThresh[2]);
			}
			threshold[0] = ORIGIN[0] - currentCoef * eigenvector[0];
			threshold[1] = ORIGIN[1] - currentCoef * eigenvector[1];
			threshold[2] = ORIGIN[2] - currentCoef * eigenvector[2];
			setFinalReads(decoysOverReal);

		}

		/**
		 * Finds the maximum coefficient that can be applied to the eigenvector before it goes out of bounds
		 * 
		 * @param eigenvector
		 * @return
		 */
		private double findMaxCoefficient(double[] eigenvector) {
			double x = 100000;
			double y = 100000;
			double z = 100000;
			if (eigenvector[0] > 0)
				x = (ORIGIN[0] - BOUNDS[0][0]) / eigenvector[0];
			if (eigenvector[1] > 0)
				y = (ORIGIN[1] - BOUNDS[1][0]) / eigenvector[1];
			if (eigenvector[2] > 0)
				z = (ORIGIN[2] - BOUNDS[2][0]) / eigenvector[2];
			return Math.min(x, Math.min(y, z));
		}

		/**
		 * @param finalReads
		 *            the finalReads to set
		 */
		private void setFinalReads(int[] finalReads) {
			this.finalReads = finalReads;
		}

		/**
		 * @return the finalReads
		 */
		public int[] getFinalReads() {
			return finalReads;
		}

		/**
		 * @return the threshold
		 */
		public double[] getThreshold() {
			return threshold;
		}

	}

	private class doubleArrayMapKey{
		double[] doubleArray = new double [3];
		
		private doubleArrayMapKey(double x, double y, double z){
			doubleArray[0] = x;
			doubleArray[1] = y;
			doubleArray[2] = z;
		}
		
		private doubleArrayMapKey(double[] input){
			doubleArray[0] = input[0];
			doubleArray[1] = input[1];
			doubleArray[2] = input[2];
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(doubleArray);
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			doubleArrayMapKey other = (doubleArrayMapKey) obj;
			if (!Arrays.equals(doubleArray, other.doubleArray))
				return false;
			return true;
		}
	}
}
