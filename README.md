# duplicatedBugFinder


After wasting hours on duplicated bugs,  multiple times, 
I was thinking about creating a tool that will help find bugs with similar bug descriptions, 
I brought this up in Company 2018 hackthon, then here it is.
for POC, simply used "Levenshtein Distance", aka "Edit Distance".

- Many thanks to [Matt Fang](https://github.com/mattqfang) and [Keith Rogers](https://github.com/keith-rogers) for coding and testing the JIRA REST API part to retrieve Jira bugs!    :+1:  :shipit:
- Many thanks to [Margaret Yang](https://github.com/margaretycf) and [myself](https://github.com/gli00001) for coding and testing the similarity search part!   :+1:  :shipit:

Hopefully this tool can help
- ![#c5f015](https://placehold.it/15/c5f015/000000?text=+) `developers reduce repeated work`
- ![#c5f015](https://placehold.it/15/c5f015/000000?text=+) `QAs find similar test plans`
- ![#c5f015](https://placehold.it/15/c5f015/000000?text=+) `PMs link related issues together`
 

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
   #### Base64Encoded from "xyz:123", assuming 'xyz' is your username and '123' is the password, local Postman client can generate that value for you

```
JIRA_HOSTNAME="www.dummyJira.com" # I will be surprised if you ask me for the real value, (⌐■_■) 
```

## execute:
```
java -jar -DjiraHost="${JIRA_HOSTNAME}" -DauthHeaderValue="Basic ${BASE64ENCODED}" ${JAR_PATH}
```

                                        
## go to browser, for example, to find if bug BUG-54499 has duplicates or similar matches :
```
http://localhost:8080/issue/BUG-54499    for default top 10 result
http://localhost:8080/issue/BUG-54499/5  for requested top 5  result
```

# contacts
- [Me](https://github.com/gli00001) 
- [Matt Fang](https://github.com/mattqfang) 
- [Margaret Yang](https://github.com/margaretycf) 
- [Keith Rogers](https://github.com/keith-rogers) 

