//package mlld.stochasticgradientdescent;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class SGD
{
	public SGD(int dictionarySize, double regularizationConstant)
	{
		modelSGDList =  new Model[3];
		this.dictionarySize = dictionarySize;
		this.regularizationConstant = regularizationConstant;
		this.featureHash = new HashMap<Integer, Integer>();
		this.prob = 0.5;
		this.score = 0.0;
	}
	
	public SGD(int dictionarySize)
	{
		modelSGDList =  new Model[3];
		this.dictionarySize = dictionarySize;
		this.featureHash = new HashMap<Integer, Integer>();
		this.prob = 0.5;
		this.score = 0.0;

	}
	
	public double getLambda()
	{
		return lambda;
	}
	
	public double getRegularizationConstant()
	{
		return regularizationConstant;
	}
	
	public Model[] getModel()
	{
		return modelSGDList;
	}
	
	public void setModel(Model modelSGD, int index)
	{
		modelSGDList[index] = modelSGD;
	}
	
	public void setModel(Model[] modelSGDList)
	{
		this.modelSGDList = modelSGDList; 
	}
	
	public abstract int update(BufferedReader inReader, String[] labelList, int totalInstances, int iteration) throws IOException;
	public abstract void classify(BufferedReader inReader, String[] labelList) throws IOException;

	protected Model[] modelSGDList;
	protected Map<Integer, Integer> featureHash;
	protected int dictionarySize;
	protected int trainingInstances;
	protected double lambda;
	protected double regularizationConstant;
	protected static double eta=0.5;
	double prob;
	double score;
}
