# duplicatedBugFinder


After wasting hours on duplicated bugs,  multiple times, 
I was thinking about creating a tool that will help find bugs with similar bug descriptions, 
I brought this up to my teammates for SOFI 2018 hackthon, then here it is.
for POC, simply used "Levenshtein Distance", aka "Edit Distance".

- Many thanks to [Matt Fang](https://github.com/mattqfang) and [Keith Rogers](https://github.com/keith-rogers) for coding and testing the JIRA REST API part to retrieve Jira bugs!    :+1:  :shipit:
- Many thanks to [Margaret Yang](https://github.com/margaretycf) and [myself](https://github.com/gli00001) for coding and testing the similarity search part!   :+1:  :shipit:


Wanna try out?

# usage
## check out this repository:
```
git clone https://github.com/gli00001/duplicatedBugFinder.git
```

## build the artifact at the project root folder:
```
cd  duplicatedBugFinder
mvn package
```

## find the generated jar, for example:  _./target/duplicatedBugFinder-1.0-SNAPSHOT.jar_
```
JAR_PATH="./target/duplicatedBugFinder-1.0-SNAPSHOT.jar"
```
```
BASE64ENCODED="eHl6OjEyMw=="
``` 
   #### you can get the Base64Encoded from [online tool](https://www.base64encode.org) after inputting "xyz:123", assuming 'xyz' is your username and '123' is the password

```
JIRA_HOSTNAME="www.dummySoFiJira.com" # I will be surprised if my coworker pings me for the real value, (⌐■_■) 
```

## execute:
```
java -jar -DjiraHost="${JIRA_HOSTNAME}" -DauthHeaderValue="Basic ${BASE64ENCODED}" ${JAR_PATH}
```

                                        
## go to browser, for example, to find if bug SOFI-54499 has duplicates or similar matches :
```
http://localhost:8080/issue/SOFI-54499    for default top 10 result
http://localhost:8080/issue/SOFI-54499/5  for requested top 5  result
```

