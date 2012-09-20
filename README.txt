DTASelectProcessor Version 1.4 Readme

Author - Kevin Tao, August 9, 2010.  Duke University Class of 2011
Algorithm based on instructions from Dr. Martin Steffen

How to use:

Make sure the file named "DTASelect.txt" is in the same directory as the DTASelectProcessor.jar file.  

Optional: If you want to choose the error rate of decoys that will be allowed, make sure the file named "ErrorRateParameter.txt" 
is also in the folder.  In that file, put a single decimal, representing the maximum tolerance ratio of decoy peptide reads to real peptide reads.

If there is no error rate set, the default value will be 0.02 or 2%.  The default deltaCN threshold is 0.05.

Optional: If you want to run the program with manual threshold values, make sure the file "ManualEntryMode.txt" is in the folder.  If the first
word in that file says "true" then the next three decimal numbers will be the threshold values, and the 4th decimal number will be the deltaCN threshold.

This program will output a new DTASelect-format file, named "DTASelectOut.txt"
It will also output a file called "infoOnLastRun.txt" so you can see the details of the run, and any errors that occur will be logged in this file.


Troubleshooting-

Are the parameter files are missing or you don't know the syntax for these parameters???  
-If you are missing the files ErrorRateParameter.txt or ManualEntryMode.txt, run the .jar file called RestoreParamFiles.jar to restore the parameter
files to original settings.