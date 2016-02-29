# PullMatches
Java app matching algorithms

Invocation:

`java -jar PullPatientMatches.jar [1] [2] (-LS loadFile.txt -o matched.txt -i inclusionFile -m b)`

1. Sorted ascii file with no duplicates: pt to match one pt per line
2. int number of matches to pull per patient
3. (optional) "-L|-LS" "loadFile.txt" - ascii summary file from oracle sorted by ptNum, comma deliminated
  this is used when you need to load a new patient set. Note that if it exists the level table is reused.
  As a memory intensive, processing light option you can choose -L instead of -LS to not save the summary file.
  To create this file, you can do `SELECT PATIENT_NUM, COUNT(*) , CONCEPT_CD||MODIFIER_CD FROM OBSERVATION_FACT GROUP BY PATIENT_NUM ORDER BY PATIENT_NUM`
4. (optional) "-o" "output file" - can be omitted. Default file is matched.txt
5. (optional) "-i|e""inclusion/exclusionFile" - Sorted ascii file with no duplicates: Can be omitted ascii list of ptIDs that should be included or excluded from the comparison.
6. (optional) "-m" "e|b" e- euclidean distance b-binary distance - default binary
