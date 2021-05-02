# Connect-Five

#### Project Concept: Creating a Multi-Client Server Architecture to Spport a Game of Connect Five

#### Setup Process:

Run 'Server.java' from this directory 'Connect-Five/Server/src/me/jordanodonnell/connectfive/server'

Run 'Client.java' from this directory 'Connect-Five/Client/src/me/jordanodonnell/connectfive/client'

Now... this a multi-client server architecture therefore we need to use threads to distinguish between the different 'Client' objects once created. To run two instances of the 'Client.java' program simply click on 'Allow Parallel Run' in the 'Run/Debug Configurations Box' as shown below

![image](https://user-images.githubusercontent.com/36043248/116822875-3ac03680-ab79-11eb-9b91-8a1d87c05666.png)

Now run 'Client.java' again and you will have two instances of 'Client' class running and one instance of 'Sever' class running

N.B. The reason why we cannot simply run these programs via the terminal is due to the fact we are utilising resources (.png files) from our 'resources' pacakge... These items are not in scope when we run the 'Client' class via the terminal therefore we have to run them indiviudally within the Intellij IDE.

