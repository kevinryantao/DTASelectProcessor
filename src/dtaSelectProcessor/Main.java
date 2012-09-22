package dtaSelectProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

public class Main {

	
	private static final String ERROR_RATE_PARAMETER = "../ErrorRateParameter.txt";
	private static final String DTA_SELECT_OUT_TXT = "DTASelectOut.txt";
	private static final String DTA_SELECT_TXT = "../DTASelect.txt";
	private static final String DTA_SELECT_OUT_TXT_SATISFIES_XCOR = "DTASelectOutSatisfiesXCor.txt";
	private static final String INFO_ON_LAST_RUN_TXT = "infoOnLastRun.txt";
	private static final PrintStream consoleOut = System.out;
	private static final PrintStream consoleErr = System.err;

	/**
	 * @param args
	 *            The input is the threshold level desired, as a double.
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		System.setErr(new PrintStream(new File(INFO_ON_LAST_RUN_TXT)));
		
		double errorRate = 0.02;
		boolean manual = false;
		double[] thresholds = new double[3];
		double deltaCNThresh = 0.05;
		try {
			Scanner readErrorRate = new Scanner(Main.class.getResourceAsStream(ERROR_RATE_PARAMETER));
			if (readErrorRate.hasNext()) {
				errorRate = Double.parseDouble(readErrorRate.next());
			}
		} catch (Exception e) {
		}
		
		try{
			Scanner manualMode = new Scanner(Main.class.getResourceAsStream("../ManualEntryMode.txt"));
			if (manualMode.hasNextBoolean()) {
				manual = manualMode.nextBoolean();
				if (manual){
					thresholds[0] = manualMode.nextDouble();
					thresholds[1] = manualMode.nextDouble();
					thresholds[2] = manualMode.nextDouble();
					deltaCNThresh = manualMode.nextDouble();
				}
			}
		} catch (Exception e) {
			System.err.println("Parsing error with 'ManualEntryMode.txt'... if on 'true' mode, are there 4 numbers after it?  The first three represent the XCor thresholds and the 4th number represents the deltaCN threshold.");
			e.printStackTrace();
			System.err.println("\n\n\n");
		}
		
		try {
			System.setOut(new PrintStream(new File(DTA_SELECT_OUT_TXT)));
			Processor p = new Processor(new DtaSelectObject(new Scanner(
					Main.class.getResourceAsStream(DTA_SELECT_TXT))));
			
			if(!manual){
				System.err.println("Error Rate Parameter: " + errorRate +" DeltaCN Threshold: "+deltaCNThresh);
				p.runSmart(errorRate, deltaCNThresh);
				p.printOutToFile();
				p.printOutToFileSatisfiesXCor(new PrintStream(new File(DTA_SELECT_OUT_TXT_SATISFIES_XCOR)));
				System.out.close();
				System.err.close();
			} else{
				System.err.println("XCor Thresholds: " + thresholds[0]+" " + thresholds[1]+" " + thresholds[2]+" DeltaCN Threshold: "+deltaCNThresh);
				p.runDumb(thresholds, deltaCNThresh);
				p.printOutToFile();
				p.printOutToFileSatisfiesXCor(new PrintStream(new File(DTA_SELECT_OUT_TXT_SATISFIES_XCOR)));
				System.out.close();
				System.err.close();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
