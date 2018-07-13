# OpenHarvester


OpenHarvesters harvest publications metadata from different sources (CrossRef, PubMed and DBLP) and identify publications for an author.

``` 
Latest Version of the Jar file -   PH-07132018.jar
```

# How to Run

Download the github repo in a prefered location and unzip it. The project contains the runnable jar file.

Use following command to run the jar file.
```
java -jar <<latest jar filename>> <<path to the jar file folder>>
```

For example, 
```
java -jar PH-07132018.jar /Users/mj125/Documents/OpenHarvester/
```

The program works in two separate steps.

Step 1 (Search Databases): Search Publications (by author last name) in the database of interest. For example, if author's name is "Angela, Poole", the search string should be "Poole".

*** Depending on how common an author's last name is, in most cases the search step may take from 1 minute to 20 minutes (or more). In future, once persistent layer is added, minimal required information can be stored from a data dump and hence live queries may not be required.

Step 2 (Claim Publications):  Claim publications from the downloaded data. For this, a user should record some important information in the author profile, including first name, middle initial, last name and preferably affiliation string. For example, if one is affiliated to Duke University, one may want to add "Duke" in the affiliation list. Adding a co-author in the author profile may also help in identifying correct publications.
