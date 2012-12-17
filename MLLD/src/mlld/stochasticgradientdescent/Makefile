HOME := /home/hadoop/workspace/Machine-Learning-for-Large-Datasets/MLLD
DICT := 10000
STEPSIZE := 0.1
TRAINFILE := $(HOME)/data/classification/abstract.tiny.train
TESTFILE := $(HOME)/data/classification/abstract.tiny.test
MODELDIR := $(HOME)/data/classification

train: Document.class LogisticRegression.class LRTest.class MemorySGD.class SGD.class Model.class
	#sh finalTrain.sh $(DICT) $(STEPSIZE) $(TESTFILE) $(TRAINFILE)
	sh finalTrain.sh $(DICT) $(STEPSIZE) $(TESTFILE) $(TRAINFILE) $(MODELDIR)

test: Document.class LogisticRegression.class LRTest.class MemorySGD.class SGD.class Model.class
	sh finalTest.sh $(DICT) $(STEPSIZE) $(TESTFILE) $(MODELDIR)

Document.class: Document.java
	javac Document.java

LRTest.class: LRTest.java
	javac LRTest.java

LogisticRegression.class: LogisticRegression.java
	javac LogisticRegression.java

MemorySGD.class: MemorySGD.java
	javac MemorySGD.java

SGD.class: SGD.java
	javac SGD.java

Model.class: Model.java
	javac Model.java
