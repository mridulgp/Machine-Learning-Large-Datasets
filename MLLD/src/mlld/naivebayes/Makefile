#/user/demo/dbpedia/abstract.tiny.train
#/user/demo/dbpedia/abstract.tiny.test

HOME := /home/hadoop/workspace/Machine-Learning-for-Large-Datasets/MLLD
HADOOP := /home/hadoop/hadoop-0.20.203.0
LIB := $(HOME)/lib
TRAIN := input
TEST := test

nb:
	- $(HADOOP)/bin/hadoop dfs -rmr outputNB outputNBRequests outputNBAnswers outputNBResults outputNBAccuracy cachedGlobalCounts cachedLabelCounts outputNBFinalAccuracy
	jar -xvf $(LIB)/MapReduceNB.jar
	$(HADOOP)/bin/hadoop jar $(LIB)/MapReduceNB.jar $(TRAIN) outputNB cachedGlobalCounts cachedLabelCounts
	
	jar -xvf $(LIB)/MapReduceNBTest.jar
	$(HADOOP)/bin/hadoop jar $(LIB)/MapReduceNBTest.jar $(TEST) outputNB outputNBRequests outputNBAnswers outputNBResults outputNBAccuracy cachedGlobalCounts cachedLabelCounts outputNBFinalAccuracy
	$(HADOOP)/bin/hadoop dfs -cat outputNBAccuracy/part-r*
	$(HADOOP)/bin/hadoop dfs -cat outputNBFinalAccuracy/part-r*
