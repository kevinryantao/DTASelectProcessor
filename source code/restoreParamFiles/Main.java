/**
 * 
 */
package restoreParamFiles;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * @author Kevin
 *
 */
public class Main {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		System.setOut(new PrintStream("ErrorRateParameter.txt"));
		System.out.println("0.02");
		System.setOut(new PrintStream("ManualEntryMode.txt"));
		System.out.println("false 1.5 1.67 3.0 0.5");
	}

}
