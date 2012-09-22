package dtaSelectProcessor;

import java.util.ArrayList;
import java.util.Scanner;

public class DtaSelectObject {

	private ArrayList<String> header = new ArrayList<String>();
	private ArrayList<Protein> proteinArray = new ArrayList<Protein>();
	private ArrayList<String> footer = new ArrayList<String>();
	
	/**
	 * Initializes the DtaSelectObject
	 */
	public DtaSelectObject(Scanner sc){
		proteinArray = new ArrayList<Protein>();
		boolean inHeader = true;
		Protein currentProt = new Protein("");
		while (sc.hasNext()){
			String line = sc.nextLine();
			
			if(inHeader){
				if(line.startsWith("L")){ inHeader = false;} else {
					header.add(line);
					continue;
				}
			}
			if(line.startsWith("L")){
				if(currentProt.getWholeString().length() > 0){
					proteinArray.add(currentProt);
				}
				currentProt = new Protein(line);				
			}
			if(line.startsWith("D")){
				currentProt.addRead(line);
			}
			if(line.startsWith("C")){
				footer.add(line);
			}
		}
	}
	
	/**
	 * @return an arrayList of strings which is the output of the dta select file
	 */
	public ArrayList<String> dtaObjectToStringArray(){
		@SuppressWarnings("unchecked")
		ArrayList<String> out = (ArrayList<String>) header.clone();
		
		for (Protein prot : proteinArray){
			if( prot.isExisting() ){
				for(String line : prot.getOutput()){
					out.add(line);
				}
			}			
		}
		for(String line : footer){
			out.add(line);
		}
		
		return out;
		
	}
	
	/**
	 * @return an arrayList of strings which is the output of the dta select file, only
	 * including the peptide reads that are above the xcor threshold.
	 */
	public ArrayList<String> dtaObjectToStringArrayExistingOnly(){
		@SuppressWarnings("unchecked")
		ArrayList<String> out = (ArrayList<String>) header.clone();
		
		for (Protein prot : proteinArray){
			if( prot.isExisting() ){
				for(String line : prot.getOutputExistingOnly()){
					out.add(line);
				}
			}			
		}
		for(String line : footer){
			out.add(line);
		}
		
		return out;
		
	}
	
	/**
	 * Applies the threshold in xCorThresh to all PeptideReads inside each Protein.
	 * @param xCorThresh
	 * @param deltaCNThresh TODO
	 */
	public void applyThreshold(double[] xCorThresh, double deltaCNThresh){
		for (Protein prot : proteinArray){
			prot.applyThreshold(xCorThresh, deltaCNThresh);
		}
	}
	/**
	 * Updates all proteins' existing status.
	 */
	public void updateAllProteins(){
		for (Protein prot : proteinArray){
			prot.updateExistStatus();
		}
	}
	
	/**
	 * @return an int array 2 entries, with the first int representing the number of decoy reads present
	 * and the 2nd int representing the number of real reads present
	 */
	public int[] report(){
		int totalReals = 0;
		int totalDecoys = 0;
		for(Protein prot : proteinArray){
			if(!prot.isExisting()){
				continue;
			}
			if(prot.isDecoy()){
				totalDecoys+=1;  // prot.getNumberUniquePeptides();
			}else{
				totalReals+=1; // prot.getNumberUniquePeptides();
			}
		}
		int[] out = {totalDecoys,totalReals};
		return out;
	}
	
}
