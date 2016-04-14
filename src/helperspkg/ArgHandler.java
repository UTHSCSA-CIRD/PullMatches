
package helperspkg;
import java.io.*;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Iterator;
import mainpkg.main;
import elementHandlerpkg.*;
import java.util.Collections;
/**
 * This class is basically your regular old static methods class used to keep the main class looking nice and 
 * streamlined. 
 * @author Laura Manuel
 */
public abstract class ArgHandler {
    /**
     * This method handles the Inclusion list that may be passed on the argument list. It makes sure that only patients in
     * the Inclusion file are available in the main.ptAvail list. NOTE that inclusion in the inclusion file does not
     * mean that the patient will be included. The exclusion list, pt list, and compare pt list all play a roll in this. 
     * 
     * Dependency: FHandler.readIntFromAscii
     * 
     * Fails the program if the inclusion file has no patients, doesn't open,etc.
     * 
     * @param inclusionFile The string name of the argument file. 
     */
    public static void handleInclusion(String inclusionFile){
        //this method accepts a string file and then will make sure that ptAvail contains only values in this file
        //note that if ptAvail does not already contain a value in inclusionFile it WILL NOT add that value; 
        File pts = FHandler.verifyOpenFile(inclusionFile, 'R');
        if(pts ==null){
            System.err.println("Patient inclusion file invalid!!!");
            System.exit(-1);
        }
        ArrayList<Integer> s = FHandler.readIntFromAscii(pts);
        if(s == null)System.exit(-1); //there was an error with that file, error report already generated
        
        int tmp; 
        //Finally optimized this. It's basically building a whole new ptAvail list
        //containing only those patients that are in both the include list and the current ptAvail list. 
        TreeSet<Integer> include = new TreeSet<>();
        for(Integer i:s){
            tmp = getIndArg0(i);
            if(tmp == -1){
                System.err.println("Warning: ID in the inclusion list " + i + " is not a valid patient.");
                continue;
            }
            if(main.ptAvail.contains(tmp)) {
                include.add(tmp);
            }else{
                System.out.println("Debug: ptAvail does not contain index: \t" + tmp + "\tof pt\t" + i);
            }
        }
        main.ptAvail = include;
    }
    /**
     * Handles the exclusion file. This makes sure that no patient ID in this file is found in the main.ptAvail list.
     * 
     * Dependency: FHandler.readIntFromAscii
     * 
     * Fails on: invalid file.
     * 
     * @param exclusionFile The string name of the exclusion file.
     */
    public static void handleExclusion(String exclusionFile){
        //this method accepts a string file and then will make sure that ptAvail does not contain any value in this file
        File pts = FHandler.verifyOpenFile(exclusionFile, 'R');
        if(pts ==null){
            System.err.println("Patient exception file invalid!!!");
            System.exit(-1);
        }
        ArrayList<Integer> s = FHandler.readIntFromAscii(pts);
        if(s == null)System.exit(-1); //there was an error with that file, error report already generated
        int tmp;
        for(Integer i:s){//foreach pt number remove the coresponding index from ptAvail if there. 
            tmp = getIndArg0(i);
            if(tmp == -1){
                System.err.println("Exception Handler error! ID "+ i +" is not in available pt list!!");
                continue;
            }
            main.ptAvail.remove(tmp);//remove the indexes
        }
    }
    /**
     * This method creates the searchContainers for the case patients that need matches. 
     * Creates the main.ptAvail and sclist. Adjusts the size of needs if there are not enough patients to accommodate
     * the requested number of matches. Note that this number is checked and possibly adjusted again after the inclusion and exclusion.
     * 
     * Fails on: inaccessible file, no patients in file, ptList not yet created. 
     * 
     * @param arg1 The string name for the file containing the list of case patients.
     */
    //possible upgrade- remove the check for size big enough for needs because this should also be checked again after inclusion exclusion
    public static void createSearchContainers(String arg1){
        //this method creates the search containers for the patients listed in arg file 1
        //Note that this should not be called until arg2 is handled as it requires the
        //ptList array to be initialized. 
        if(main.ptList == null){
            System.err.println("ERROR! Patient list was not yet created.");
            System.exit(-1);
        }
        File pts = FHandler.verifyOpenFile(arg1, 'R');
        if(pts ==null){
            System.err.println("Patient compare file invalid!!!");
            System.exit(-1);
        }
        ArrayList<Integer> s = FHandler.readIntFromAscii(pts);
        if(s == null)System.exit(-1); //there was an error with that file, error report already generated
        int tmp;
        main.ptAvail = new TreeSet<>();
        main.scList = new ArrayList<>();
        //check to make sure that we have enough patients for the requested number of matches. 
        
        if((main.ptList.size() - s.size())< (main.needs* s.size()) ){
            if(main.ptList.size()-s.size()<s.size()){
                System.err.println("We cannot collect even one level of matches for the number of patients you provided."); 
                System.exit(-1);
            }else{
                System.err.print("Warning! Needs would require: " + (s.size()*main.needs) + " patients we have: " + main.ptList.size()
                       + " reducing needs to "); 
                main.needs = (main.ptList.size() - s.size())/s.size(); // floor is naturally taken in integer arithmatic. 
                System.err.print(main.needs + "\n");
            } 
        }
        SearchContainer sc;
        //create the search containers.
        for(int i = 0; i<main.ptList.size(); i++){
            main.ptAvail.add(i);
        }
        for(Integer i:s){
            tmp = getIndArg0(i);
            if(tmp == -1){
                System.err.println("ERROR! ID "+ i +" from compare list is not in patient list!!");
                continue;
            }
            //per profiling- rehashing the cached needs consumes a large amount of processing time, but the elements consume 
            //less than 1% of the total memory at 2x. Reprofiling may be needed for large needs, however processing time with large
            //patient sets is more critical as we can utilize TACC for higher memory.
            //Note: once rehashing of the search containers is reduced to a minimum this part can be parallized. 
            sc = new SearchContainer(i, tmp, (main.needs*100));
            main.scList.add(sc);
            main.ptAvail.remove(tmp);
        } 
    }
    /**
     * Gets the index for a patientID. (Since we don't expect the user to know the index order of the patients,
     * they can just enter the patient numbers.)
     * @param pt The patient number we are looking for the index to.
     * @return The index of this patient in the main.ptAvail list. 
     */
    private static int getIndArg0(int pt){
        int high = main.ptList.size();
        int low = 0;
        int m = ((high-low)/2) + low;
        int cur;
        while(low <= high){
            cur = main.ptList.get(m).getPTNum();
            if(cur == pt){
                return m;
            }
            if(cur > pt){
                high = m-1;
            }else{
                low = m+1;
            }
            m = ((high-low)/2) + low;
        }
        return -1;
    }
    /**
    * Creates the buffered writer for the output file. 
    * @param arg4 The string name of the file we will be writing to. 
    * @return The BufferedWriter to write to.
    */
    public static BufferedWriter openOutFile(String arg4){
        //sets up the outpu file.
        //we don't need to check if the file exists since we want to create it if necessary and overwrite if necessary
        //and we're not in C ;-p 
        BufferedWriter bw;
        try{
           bw = new BufferedWriter(new FileWriter(arg4, false),(4*1024)); // second argument F means will overwrite if exists. 
           return bw;
        }catch(IOException e){
            System.err.println("IO error with creating buffered reader for output file! /n" + e.getMessage());
            System.exit(-1);
        }
        
        System.err.println("Uncaught exception opening outfile "+ arg4);
        System.exit(-1);
        return null;//Netbeans insists on a return statement despite the system.exit. *shrugs*
    }
    /**
     * Opens an existing levels.dat folder IF it exists and loads it. Otherwise we get a new LevelHolder.
     * @return A LevelHolder to be used with the import.
     */
    public static LevelHolder openLevels(){
        File levelsFile = FHandler.verifyOpenFile("levels.dat", 'B');
        LevelHolder levels = new LevelHolder();
        if(levelsFile != null){
            //load in that file
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(levelsFile))) {
                    levels = (LevelHolder)in.readObject();
            }catch(FileNotFoundException e){
                System.err.println("File not found exception when opening levels file." + e.getMessage());
                System.exit(-1);
            }catch(IOException e){
                System.err.println("IO exception when reading levels file." + e.getMessage());
                System.exit(-1);
            }catch(ClassNotFoundException e){
                System.err.println("Class not found exception when reading levels file." + e.getMessage());
                System.exit(-1);
            }
        }
        return levels;
    }
    /**
     * This depreciated the "loadNewInput" function because throttling it worked so much better! Memory was halved and it 
     * even ran a bit faster. 
     * 
     * Fails on: the inputFile cannot be opened or read.
     * 
     * @param inputFile The string name of the file of patients to load.
     * @param saveSummary Whether or not to save a summary of this file. The saving of the summary takes a while. 
     */
    public static void loadNewInputThrottled(String inputFile, boolean saveSummary){
        //this method is much slower as it has to process each line, but it keeps the memory consumption down. 
        LevelHolder levels = openLevels();
        File in = FHandler.verifyOpenFile(inputFile, 'R');
        if(in == null){
            System.err.println("Could not open load input file " + inputFile);
            System.exit(-1);
        }
        String a;
        ArrayList<Summary> summaries = new ArrayList<>();
        String [] aParse;
        int ptNum, ptNumC;//ptNumC maintains the current train of records for patient ptNum. 
        int qt;
        int r;
        ptNum = ptNumC = -1;
        ArrayList<SummaryLevel> ptLevels = new ArrayList<>();
        main.ptList = new ArrayList<>();
        try (BufferedReader fr = new BufferedReader(new FileReader(inputFile));){
            //read in the catalogue file and open all of its parts
            while((a=fr.readLine())!= null){
                //until EOF
                aParse = parseLevelLine(a);
                if(aParse == null) continue;
                ptNum = Integer.parseInt(aParse[0]);
                if(ptNum != ptNumC){//if we have a new patient, add the old patient to the patient list and clear the lists. 
                    if(ptNumC != -1){//if not loading patient.
                        Collections.sort(ptLevels);
                        main.ptList.add(new Summary(ptNumC, ptLevels));
                        ptLevels = new ArrayList<>();
                    }
                    ptNumC = ptNum;
                }
                qt = Integer.parseInt(aParse[1]);
                r = levels.treeContains(aParse[2]);
                if(r == -1){
                    r = levels.addLevel(aParse[2]);
                }
                ptLevels.add(new SummaryLevel(r, qt));
            }
        } catch (FileNotFoundException ex) {
            System.err.println("Error, file in readFilesFromAscii not found");
            System.exit(-1);//fatal error
        }catch(IOException e){
            System.err.println("IO Exception: " + e.getLocalizedMessage());
        }
        Collections.sort(ptLevels);
        main.ptList.add(new Summary(ptNum, ptLevels));
        //save the LevelHolder
        try (ObjectOutputStream outLevel = new ObjectOutputStream(new FileOutputStream(new File("levels.dat")))) {
            outLevel.writeObject(levels);
        }catch(FileNotFoundException e){
            System.err.println("File not found exception when opening levels file." + e.getMessage());
            System.exit(-1);
        }catch(IOException e){
            System.err.println("IO exception when writing to levels file." + e.getMessage());
            System.exit(-1);
        }
        if(saveSummary){
            //save the Summaries
            try (ObjectOutputStream outSummary = new ObjectOutputStream(new FileOutputStream(new File("summary.dat")))) {
                outSummary.writeObject(main.ptList);
            }catch(FileNotFoundException e){
                System.err.println("File not found exception when opening summary file." + e.getMessage());
                System.exit(-1);
            }catch(IOException e){
                System.err.println("IO exception when writing to summary file." + e.getMessage());
                System.exit(-1);
            }
        }
    }
    /*Depreciated- Replaced with loadNewInputThrottled. Profiler showed limited to no impact in the cost of 
    * processing as we load as opposed to loading then processing. This is likely to have to do with IO read times. 
    public static void loadNewInput(String inputFile, boolean saveSummary){
        //this takes a string as an argument and will try to open this new input file. 
        LevelHolder levels = openLevels();
        File in = FHandler.verifyOpenFile(inputFile, 'R');
        if(in == null){
            System.err.println("Could not open load input file " + inputFile);
            System.exit(-1);
        }
        ArrayList<String> input = FHandler.readLinesFromAscii(in);
        if(input == null){
            System.err.println("Input file " + inputFile + " did not produce any input.");
            System.exit(-1);
        }
        ArrayList<Summary> summaries = new ArrayList<>();
        String [] a;
        int ptNum, ptNumC;//ptNumC maintains the current train of records for patient ptNum. 
        int qt;
        int r;
        ptNum = ptNumC = -1;
        ArrayList<SummaryLevel> ptLevels = new ArrayList<>();
        main.ptList = new ArrayList<>();
        for(String i : input){
            a = parseLevelLine(i);
            if(a == null) continue;
            ptNum = Integer.parseInt(a[0]);
            if(ptNum != ptNumC){//if we have a new patient, add the old patient to the patient list and clear the lists. 
                if(ptNumC != -1){//if not loading patient.
                    Collections.sort(ptLevels);
                    main.ptList.add(new Summary(ptNumC, ptLevels));
                    ptLevels = new ArrayList<>();
                }
                ptNumC = ptNum;
            }
            qt = Integer.parseInt(a[1]);
            r = levels.treeContains(a[2]);
            if(r == -1){
                r = levels.addLevel(a[2]);
            }
            ptLevels.add(new SummaryLevel(r, qt));
        }//end for String i in input
        //handle that last patient.
        Collections.sort(ptLevels);
        main.ptList.add(new Summary(ptNum, ptLevels));
        //save the LevelHolder
        try (ObjectOutputStream outLevel = new ObjectOutputStream(new FileOutputStream(new File("levels.dat")))) {
            outLevel.writeObject(levels);
        }catch(FileNotFoundException e){
            System.err.println("File not found exception when opening levels file." + e.getMessage());
            System.exit(-1);
        }catch(IOException e){
            System.err.println("IO exception when writing to levels file." + e.getMessage());
            System.exit(-1);
        }
        if(saveSummary){
            //save the Summaries
            try (ObjectOutputStream outSummary = new ObjectOutputStream(new FileOutputStream(new File("summary.dat")))) {
                outSummary.writeObject(main.ptList);
            }catch(FileNotFoundException e){
                System.err.println("File not found exception when opening summary file." + e.getMessage());
                System.exit(-1);
            }catch(IOException e){
                System.err.println("IO exception when writing to summary file." + e.getMessage());
                System.exit(-1);
            }
        }
    }*/
    /**
     * Loads an existing summary file. 
     */
    public static void loadSummaries(){
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File("summary.dat")))) {
            
            main.ptList = (ArrayList<Summary>)in.readObject();
        }catch(FileNotFoundException e){
            System.err.println("File not found exception when opening summary file." + e.getMessage());
            System.exit(-1);
        }catch(IOException e){
            System.err.println("IO exception when reading summary file." + e.getMessage());
            System.exit(-1);
        }catch(ClassNotFoundException e){
            System.err.println("Class not found exception when reading summary file." + e.getMessage());
            System.exit(-1);
        }
        if(main.ptList == null){
            System.err.println("ERROR! Could not read summary.dat!!");
            System.exit(-1);
        }
    }
    /**
     * Parses a line from the load file. Currently parses by comma, but if you want/need to change it
     * it's an easy fix, just change the a=levelLine.split("WhateverParseCharacter(s)YouWant");
     * It also removes all quotation marks(") and white space in the strings. 
     * @param levelLine The string to be parsed.
     * @return An array of parsed strings. 
     */
    private static String[] parseLevelLine(String levelLine){
        if(levelLine == null || levelLine.length() == 0){
            System.err.println("Empty or null line given to parser.");
            return null;
        }
        ArrayList<String> parsed = new ArrayList<>();
        String[] a;
        a = levelLine.split(",");
        if(a.length  !=3){
            //we are expecting 3 columns.
            System.err.println(levelLine + " does not meet parsing criteria, improper number of columns(commas).");
            return null;
        }
        //removes the quotation marks and trims the white space 
        a[0] = a[0].replaceAll("\"", "");
        a[0] = a[0].trim();
        a[1] = a[1].replaceAll("\"", "");
        a[1] = a[1].trim();
        a[2] = a[2].replaceAll("\"", "");
        a[2] = a[2].trim();
        return a;
    }
    /**
     * This allows you to say" Include only THESE levels. Note: This is likely to be refined in 
     * the future...  Really should have a way of applying regular expressions to the levels to generate 
     * this inclusion/exclusion list for a particular level file. OR a way to code the levels with regular expressions.
     * @param e The levelHolder of levels we'll pull from to match the levels in f.
     * @param f A string containing the name of a file where the levels we want to include are stored. 
     */
    public static void levelInclusion(LevelHolder e, String f){
        File in = FHandler.verifyOpenFile(f, 'R');
        if(in == null){
            System.err.println("Could not open load input file " + f);
            System.exit(-1);
        }
        ArrayList<String> input = FHandler.readLinesFromAscii(in);
        if(input == null){
            System.err.println("Input file " + f + " did not produce any input.");
            System.exit(-1);
        }
        
        main.availLevels = new TreeSet<>();
        main.restrictLevels = true;
        int L;
        for(String s :input){
            L = e.treeContains(s);
            if(L <0){
                System.err.println("Warning: Could not find: " + s + " in levels.dat.");
                continue;
            }
            main.availLevels.add(L);
        }
    }
    /**
     * This allows you to exclude levels. NOTE: Like levelInclusion the Exclusion is likely to change as we use
     * regular expressions or at least provide the option to use regular expressions to help with inclusion and 
     * exclusion. 
     * @param e The level Holder form which we will be excluding the levels found in f.
     * @param f A string containing the name of the file where the exclusion levels are stored. 
     */
    public static void levelExclusion(LevelHolder e, String f){
        File in = FHandler.verifyOpenFile(f, 'R');
        if(in == null){
            System.err.println("Could not open load input file " + f);
            System.exit(-1);
        }
        ArrayList<String> input = FHandler.readLinesFromAscii(in);
        if(input == null){
            System.err.println("Input file " + f + " did not produce any input.");
            System.exit(-1);
        }
        //not a very efficient way of handling exceptions, but it should work... I might optimize later.
        main.availLevels = new TreeSet<>();
        main.restrictLevels = true;
        TreeSet<Integer> tmp = new TreeSet<>();
        int L;
        for(String s:input){ // get the IDs for the numbers
            L = e.treeContains(s);
            if(L <0){
                System.err.println("Warning: Could not find: " + s + " in levels.dat.");
                continue;
            }
            tmp.add(L);
        }
        //Transfer all levels not in the list.
        Iterator<Level> iter = e.getIterator();
        while(iter.hasNext()){
            L = iter.next().level;
            if(!tmp.contains(L)){
                main.availLevels.add(L);
            }
        }
    }
}
