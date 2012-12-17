HOME := /home/hadoop/workspace/Machine-Learning-for-Large-Datasets/MLLD
HADOOP := /home/hadoop/hadoop-0.20.203.0
LIB := $(HOME)/lib
INPUTUNIGRAM := inputPhraseUnigram
INPUTBIGRAM := inputPhraseBigrams

phrases:
	echo $(HADOOP)
	- $(HADOOP)/bin/hadoop dfs -rmr outputPhraseRequests outputPhraseMessages outputPhraseMerge tempPhraseBigramCounts globalPhraseCounts finalPhraseOutput
	jar -xvf $(LIB)/MapReducePhraseCount.jar
	$(HADOOP)/bin/hadoop jar $(LIB)/MapReducePhraseCount.jar $(INPUTUNIGRAM) $(INPUTBIGRAM) outputPhraseRequests outputPhraseMessages outputPhraseMerge tempPhraseBigramCounts globalPhraseCounts
	jar -xvf $(LIB)/MapReducePCTest.jar
	$(HADOOP)/bin/hadoop jar $(LIB)/MapReducePCTest.jar outputPhraseMerge finalPhraseOutput globalPhraseCounts
	$(HADOOP)/bin/hadoop dfs -cat finalPhraseOutput/part-r* | sort -k2,2gr | head -n 20

