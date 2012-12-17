HOME := /home/hadoop/workspace/Machine-Learning-for-Large-Datasets/MLLD/
INPUTFILE := $(HOME)/data/graph-sampling/test.adj
#SEED := "Machine Learning"
SEED := "A"
ALPHA := 0.3
EPSILON := 1e-5
OUTPUTDIR := $(HOME)/data/graph-sampling/output


pagerank: ApproximatePageRank.class
	mkdir -p $(OUTPUTDIR)
	java ApproximatePageRank $(INPUTFILE) $(SEED) $(ALPHA) $(EPSILON) > $(OUTPUTDIR)/pagerank.txt

snowball: ApproximatePageRank.class Conductance.class
	mkdir -p $(OUTPUTDIR)
	java ApproximatePageRank $(INPUTFILE) $(SEED) $(ALPHA) $(EPSILON) > $(OUTPUTDIR)/pagerank.txt
	java Conductance $(SEED) $(OUTPUTDIR)/pagerank.txt $(INPUTFILE) $(OUTPUTDIR)/conductance.csv
	rm -f $(OUTPUTDIR)/pagerank.txt

ApproximatePageRank.class: ApproximatePageRank.java
	javac ApproximatePageRank.java

Conductance.class: Conductance.java
	javac Conductance.java
