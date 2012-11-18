#/user/demo/dbpedia/abstract.tiny.train
#/user/demo/dbpedia/abstract.tiny.test

nb:
	- ./bin/hadoop dfs -rmr outputNB outputNBRequests outputNBAnswers outputNBResults outputNBAccuracy cachedGlobalCounts cachedLabelCounts outputNBFinalAccuracy
	jar -xvf MapReduceNB.jar
	./bin/hadoop jar MapReduceNB.jar input outputNB cachedGlobalCounts cachedLabelCounts
	
	jar -xvf MapReduceNBTest.jar
	./bin/hadoop jar MapReduceNBTest.jar test outputNB outputNBRequests outputNBAnswers outputNBResults outputNBAccuracy cachedGlobalCounts cachedLabelCounts outputNBFinalAccuracy
	./bin/hadoop dfs -cat outputNBAccuracy/part-r*
	./bin/hadoop dfs -cat outputNBFinalAccuracy/part-r*
