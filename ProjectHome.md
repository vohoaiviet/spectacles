# Description #
The original aim of this project was to just recreate the system described in Nister and Stewenius's 2004 paper "Scalable Recognition using a Vocabulary Tree". I believe that the basic implementation of this has been completed, and I am currently working on some benchmarks, tests, and hopefully some improvements on it (right now it's only so so).

# Requirements #
They're pretty straightforward:
  * Java 1.5+
  * MySQL (any recent version)
  * Ant (any recent version)

# Getting Started #
## Setting up the storage ##
The first thing you need to do is set up the required tables for storing the image's descriptors.

You'll want to execute the SQL script in the root directory called "tables.sql" like:

```
mysql -uUSER -pPASS < /spectacles/tables.sql
```

This should set up the required tables and indexes for both SURF and SIFT descriptor storage.

## Update configuration file ##
In conf/conf.properties you need to update the jdbc username and password

## Creating an index ##
For a basic index, run the command line:

ant builder -Dargs="image\_input\_folder temp\_folder output\_folder sift kmeansforest"

Where:
**image\_input\_folder** - The folder which contains the images you'll want to search.
**temp\_folder** - Some temporary files are stored here, it's easiest to just set it to be the same as the output folder.
**output\_folder** - The folder which will hold the index.

**Now go get a cup of coffee or ten**. I highly recommend starting with a small set of images to test this first. I've build an index with 130k+ images and it takes a ton of memory and a long time to run. However, you'll be left with a very compact and fast index when it's all said and done.

## Running Queries ##
Here is an example snippet of how to query the index.
```
	QueryHandler handler = new QueryHandler("OUTPUT FOLDER FROM PREVIOUS STEP");
	try {
		SizedPriorityQueue<Score> scores = handler.findBest(new File("PATH TO IMAGE YOU WANT TO QUERY"));
		while ( scores.size() > 0 ){
			Score topScore = scores.pop();
			System.out.println(topScore.getScore());
			System.out.println(topScore.getTarget());
		}
	} catch (IOException e) {
		e.printStackTrace();
	}
```

# Example #
You can see an operating example of an index of 130,000 book covers here:

http://www.stromberglabs.com/code/visual-search

# License #
All of my other work has been three clause BSD, but because I don't have a free and clear implementation of the SIFT descriptor (I'm using the one from the Lire project, which is GPL) I have to license this as LGPL. One day I'll write a clean room version of the SIFT descriptor and relicense this as 3 clause BSD.