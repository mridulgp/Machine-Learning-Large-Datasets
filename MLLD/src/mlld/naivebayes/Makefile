#/user/demo/dbpedia/abstract.tiny.train
#/user/demo/dbpedia/abstract.tiny.test

HADOOP := /home/hadoop/hadoop-0.20.203.0
LIB := /home/hadoop/workspace/Machine-Learning-for-Large-Datasets/MLLD/lib

nb:
	- $(HADOOP)/bin/hadoop dfs -rmr outputNB outputNBRequests outputNBAnswers outputNBResults outputNBAccuracy cachedGlobalCounts cachedLabelCounts outputNBFinalAccuracy
	jar -xvf $(LIB)/MapReduceNB.jar
	$(HADOOP)/bin/hadoop jar $(LIB)/MapReduceNB.jar input outputNB cachedGlobalCounts cachedLabelCounts
	
	jar -xvf $(LIB)/MapReduceNBTest.jar
	$(HADOOP)/bin/hadoop jar $(LIB)/MapReduceNBTest.jar test outputNB outputNBRequests outputNBAnswers outputNBResults outputNBAccuracy cachedGlobalCounts cachedLabelCounts outputNBFinalAccuracy
	$(HADOOP)/bin/hadoop dfs -cat outputNBAccuracy/part-r*
	$(HADOOP)/bin/hadoop dfs -cat outputNBFinalAccuracy/part-r*
