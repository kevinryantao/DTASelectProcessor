package dtaSelectProcessor;

/**
 * 
 */

public class PeptideRead {

	private double Xcor;
	private int fraction;
	private String peptideSequence;
	private String wholeString;
	private int chargeState;
	private double deltaCN;

	private boolean active;

	/*
	 * private String DeltCN; private String ObsMH; private String CalcMH; private String SpR; private String SpScore;
	 * private String IonPercent; private String number;
	 */

	/**
	 * @param Stores
	 *            the info in a line from DTASelect into a PeptideRead object
	 */
	public PeptideRead(String line) {
		setWholeString(line);

		String[] columns = line.split("\t");
		setXcor(Double.parseDouble(columns[3]));
		setDeltaCN(Double.parseDouble(columns[4]));
		setPeptideSequence(columns[11]);
		// gets the fraction number from the last 2 numbers in the 3rd column
		setFraction(Integer.parseInt(columns[2].substring(columns[2].length() - 2)));
		// gets the charge state from the last number in the 2nd column
		setChargeState(Integer.parseInt(columns[1].substring(columns[1].length() - 1)));
		setActive(true);

		if (chargeState > 3 || chargeState < 1) {
			System.err.println("Charge State not between 1 and 3.  Charge state : " + chargeState);
			System.err.println("Full line : " + wholeString);
		}
		if (fraction > 36 || fraction < 1) {
			System.err.println("Fraction not between 1 and 36.  Fraction : " + fraction);
			System.err.println("Full line : " + wholeString);
		}
	}

	/**
	 * @return the deltaCN
	 */
	public double getDeltaCN() {
		return deltaCN;
	}

	/**
	 * @param deltaCN the deltaCN to set
	 */
	private void setDeltaCN(double deltaCN) {
		this.deltaCN = deltaCN;
	}

	/**
	 * Applies the threshold to the peptide, and updates the "active" state accordingly.
	 * 
	 * @param xCorThresh
	 *            this should be a double array with 3 elements
	 * @param deltaCNThresh TODO
	 */
	public void applyThreshold(double[] xCorThresh, double deltaCNThresh) {
		if (xCorThresh.length != 3) {
			System.err.println("XCor threshold problem : " + xCorThresh.toString());
		}
		if (Xcor >= xCorThresh[chargeState - 1] && deltaCN >= deltaCNThresh) {
			setActive(true);
		} else {
			setActive(false);
		}
	}

	/**
	 * @param xcor
	 *            the xcor to set
	 */
	private void setXcor(double xcor) {
		Xcor = xcor;
	}

	/**
	 * @return the xCor
	 */
	public double getXcor() {
		return Xcor;
	}

	/**
	 * @param fraction
	 *            the fraction to set
	 */
	private void setFraction(int fraction) {
		this.fraction = fraction;
	}

	/**
	 * @return the fraction
	 */
	public int getFraction() {
		return fraction;
	}

	/**
	 * @param peptideSequence
	 *            the peptideSequence to set
	 */
	private void setPeptideSequence(String peptideSequence) {
		this.peptideSequence = peptideSequence;
	}

	/**
	 * @return the peptideSequence
	 */
	public String getPeptideSequence() {
		return peptideSequence;
	}

	/**
	 * @param wholeString
	 *            the wholeString to set
	 */
	private void setWholeString(String wholeString) {
		this.wholeString = wholeString;
	}

	/**
	 * @return the wholeString
	 */
	public String getWholeString() {
		return wholeString;
	}

	/**
	 * @param chargeState
	 *            the chargeState to set
	 */
	private void setChargeState(int chargeState) {
		this.chargeState = chargeState;
	}

	/**
	 * @return the chargeState
	 */
	public int getChargeState() {
		return chargeState;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	private void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

}
