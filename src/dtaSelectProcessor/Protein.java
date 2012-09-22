package dtaSelectProcessor;

import java.util.ArrayList;

public class Protein {
	/*
	 * private String Length; private String MolWt; private String pI; private String ValidationStatus; private String
	 * Descript;
	 */

	private String wholeString;
	// Boolean describing whether it's real or a decoy
	private boolean decoy;
	private ArrayList<PeptideRead> peptideList = new ArrayList<PeptideRead>();
	// Boolean describing whether there is enough evidence for it to exist or not
	private boolean existing;

	/**
	 * @param pr
	 */
	public void addRead(String str) {
		peptideList.add(new PeptideRead(str));
	}

	/**
	 * @return an ArrayList of Strings, fit for output to txt file. If protein isn't "existing", then it will be blank.
	 *         All peptides are included.
	 */
	public ArrayList<String> getOutput() {
		ArrayList<String> out = new ArrayList<String>();
		if (!existing) {
			return out;
		}
		out.add(getWholeString());
		for (PeptideRead pepRead : peptideList) {
				out.add(pepRead.getWholeString());
		}
		return out;
	}
	
	/**
	 * @return an ArrayList of Strings, fit for output to txt file. If protein isn't "existing", then it will be blank.
	 *         All inactive peptides are also excluded.
	 */
	public ArrayList<String> getOutputExistingOnly() {
		ArrayList<String> out = new ArrayList<String>();
		if (!existing) {
			return out;
		}
		out.add(getWholeString());
		for (PeptideRead pepRead : peptideList) {
			if(pepRead.isActive()){
				out.add(pepRead.getWholeString());
			}				
		}
		return out;
	}

	/**
	 * Constructs a protein object
	 * 
	 * @param line
	 *            the whole input line from the DTASelect file.
	 */
	public Protein(String line) {
		setWholeString(line);
		String[] columns = line.split("\t");
		if (columns.length > 1) {
			if (columns[1].contains("REVERSE")) {
				setDecoy(true);
			} else {
				setDecoy(false);
			}
		}
	}

	/**
	 * Applies the threshold in xCorThresh to all peptideReads inside.
	 * 
	 * @param xCorThresh
	 * @param deltaCNThresh TODO
	 */
	public void applyThreshold(double[] xCorThresh, double deltaCNThresh) {
		for (PeptideRead pr : peptideList) {
			pr.applyThreshold(xCorThresh, deltaCNThresh);
		}
	}

	/**
	 * @return a count of the number of active, unique peptideSequences for the protein.
	 */
	/*public int getNumberUniquePeptides() {
		Set<String> pepSequenceSet = new HashSet<String>();
		for (PeptideRead pRead : peptideList) {
			if (pRead.isActive()) {
				pepSequenceSet.add(pRead.getPeptideSequence());
			}
		}
		return pepSequenceSet.size();
	}*/

	/**
	 * Performs a check to see if there are two or more active peptide reads from the same fraction are present. If yes,
	 * then the "existing" flag is set to true. If not, then the "existing" flag is set to false.
	 */
	public void updateExistStatus() {
		for (PeptideRead pr : peptideList) {
			if (pr.isActive()) {
				for(int i = 0; i < peptideList.size(); i++){
					PeptideRead pr2 = peptideList.get(i);
					if(pr2.isActive()){
						if(pr.getFraction() == pr2.getFraction()){
							if(!pr.getPeptideSequence().equals(pr2.getPeptideSequence()) || pr.getChargeState() != pr2.getChargeState()){
								existing = true;
								return;
							}
						}
					}
				}
			}
		}
		existing = false;
	}

	/**
	 * @return existing.
	 */
	public boolean isExisting() {
		return existing;
	}

	/**
	 * @param wholeString
	 *            the wholeString to set
	 */
	public void setWholeString(String wholeString) {
		this.wholeString = wholeString;
	}

	/**
	 * @return the wholeString
	 */
	public String getWholeString() {
		return wholeString;
	}

	/**
	 * @param decoy
	 *            the real to set
	 */
	private void setDecoy(boolean decoy) {
		this.decoy = decoy;
	}

	/**
	 * @return the real
	 */
	public boolean isDecoy() {
		return decoy;
	}

}
