/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helperspkg;
import java.io.*;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Iterator;
import mainpkg.main;
import elementHandlerpkg.*;
import java.util.Collections;
/**
 *
 * @author manuells
 */
public abstract class ArgHandler {
    public static void getFileRefTree(String arg2){
        //takes arg 2- an ascii text file listing all of the files 
        File catalogue = FHandler.verifyOpenFile(arg2, 'R');
        if(catalogue == null){
            System.err.println("Catalogue file invalid!!!");
            System.exit(-1);
        }
        ArrayList<File> catalogueList = FHandler.readFilesFromAscii(catalogue, 'R');
        if(catalogueList == null){
            System.err.println("No lines read from catalogue list!!!");
            System.exit(-1);
        }
        if(catalogueList.size() < 2){
            System.err.println("Insufficient lines in the list. ");
            System.exit(-1);
        }
        
        
        //initialize FileRefs to hold the RandomAccessFiles and hold the indexes. 
        //Note:  index starts at 0 and is non-inclusive of the end index. 
        long tmp = 0;
        main.refs = new TreeSet<>();
        for(int i = 1; i< catalogueList.size();i++){
            FileRef ref = new FileRef();
            boolean a = ref.initFile(catalogueList.get(i), tmp);
            if(!a){
                System.err.println("FileRef initialization error!");
                System.exit(-1);
            }
            tmp = ref.getEndIndex();//update start index.
            main.refs.add(ref);
        }
    }
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
        //temporary tree that we will search through so we can remove from ptAvail. 
        TreeSet<Integer> include = new TreeSet<>();
        for(Integer i:s){
            tmp = getIndArg0(i);
            if(tmp == -1){
                System.err.println("Inclusion List error! ID " + i + " is not an available patient.");
                continue;
            }
            include.add(tmp);
        }
        s = new ArrayList<>();
        Iterator iter = main.ptAvail.iterator();//iterate through ptAvail
        while(iter.hasNext()){
            tmp = (Integer)iter.next();
            if(!include.contains(tmp))s.add(tmp);//cache the values we need to remove so as not to disturb the iterator
        }
        for(Integer i : s){
            main.ptAvail.remove(i);//remove now that we are nolonger using the iterator. 
        }
    }
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
    public static void createSearchContainers(String arg1){
        //this method creates the search containers for the patients listed in arg file 1
        //Note that this should not be called until arg2 is handled (getFileRefTree) as it requires the
        //ptList array to be initialized. 
        //also needs to have had args[2] the third argument read.
        if(main.ptList == null){
            System.err.println("ERROR! getFileRefTree must be called first.");
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
            //Parallizing within the "clear" option which is the most likely location to "rehash" is not recommended due to latency.
            //I.E. the potential to be waiting for some threads to finish. and the overhead of spawning or utilizing these threads if
            //we can reduce the rehashing. This is the optimal location as all search containers should take approximately the same
            //amount of time to finish.
            sc = new SearchContainer(i, tmp, (main.needs*100));
            main.scList.add(sc);
            main.ptAvail.remove(tmp);
        } 
    }
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
        //removes the quotation marks sand trims the white space 
        a[0] = a[0].replaceAll("\"", "");
        a[0] = a[0].trim();
        a[1] = a[1].replaceAll("\"", "");
        a[1] = a[1].trim();
        a[2] = a[2].replaceAll("\"", "");
        a[2] = a[2].trim();
        return a;
    }
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
