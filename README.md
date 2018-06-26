# OpenHarvester


OpenHarvesters harvest publications metadata from different sources (such as CrossRef, PubMed and DBLP) and identify publications for an author.

It works in two steps.

Step 1 (Search Databases): Search Publications (by author last name) in the database of interest. For example, if author's name is "Angela, Poole", the search string should be "Poole".

*** Depending on how common an author's last name is, in most cases the search step may take from 1 minute to 20 minutes (or more)

Step 2 (Claim Publications):  Claim publications from the downloaded data. For this, a user should record some important information in the author profile, including first name, middle initial, last name and preferably affiliation string. For example, if one is affiliated to Duke University, one may want to add "Duke" in the affiliation list. Adding a co-author in the author profile may also help in identifying correct publications.

# How to Run
Use following command to run the jar file.
```
java -jar PH.jar <<path to the jar file folder>>
```

For example, 
```
java -jar PH.jar /Users/mj125/Documents/OpenHarvester/
```
