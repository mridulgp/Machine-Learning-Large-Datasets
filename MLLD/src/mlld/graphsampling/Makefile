TESTFILE := /home/hadoop/workspace/Machine-Learning-for-Large-Datasets/MLLD/data/graph-sampling/test.adj
#SEED := "Machine Learning"
SEED := "A"
ALPHA := 0.3
EPSILON := 1e-5
OUTPUT := /home/hadoop/workspace/Machine-Learning-for-Large-Datasets/MLLD/data/graph-sampling/output


pagerank: ApproximatePageRank.class
	mkdir -p $(OUTPUT)
	java ApproximatePageRank $(TESTFILE) $(SEED) $(ALPHA) $(EPSILON) > $(OUTPUT)/pagerank.txt

snowball: ApproximatePageRank.class Conductance.class
	mkdir -p $(OUTPUT)
	java ApproximatePageRank $(TESTFILE) $(SEED) $(ALPHA) $(EPSILON) > $(OUTPUT)/pagerank.txt
	java Conductance $(SEED) $(OUTPUT)/pagerank.txt $(TESTFILE) $(OUTPUT)/conductance.csv
	rm -f $(OUTPUT)/pagerank.txt

ApproximatePageRank.class: ApproximatePageRank.java
	javac ApproximatePageRank.java

Conductance.class: Conductance.java
	javac Conductance.java
