DICT := 10000
STEPSIZE := 0.1
TESTFILE := /home/hadoop/workspace/Machine-Learning-for-Large-Datasets/MLLD/data/classification/abstract.tiny.test
TRAINFILE := /home/hadoop/workspace/Machine-Learning-for-Large-Datasets/MLLD/data/classification/abstract.tiny.train

demo: Document.class LRTest.class LogisticRegression.class MemorySGD.class SGD.class Model.class
	#sh finalTrain.sh $(DICT) $(STEPSIZE) $(TESTFILE) $(TRAINFILE)
	sh finalTrain.sh $(DICT) $(STEPSIZE) $(TESTFILE) $(TRAINFILE)

test: Document.class LRTest.class LogisticRegression.class MemorySGD.class SGD.class Model.class
	sh finalTrain.sh $(DICT) $(STEPSIZE) $(TESTFILE) $(TRAINFILE)

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
