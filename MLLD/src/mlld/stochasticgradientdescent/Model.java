//package mlld.stochasticgradientdescent;

import java.io.Serializable;
import java.util.Map;

public class Model implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Model()
	{
		
	}
	public Model(double[] featureWeights, int[] updateWeights)
	{
		this.featureWeights = featureWeights;
		this.updateWeights = updateWeights;
	}
	
	public double[] getFeatureWeights()
	{
		return featureWeights;
	}
	
	public int[] getUpdateWeights()
	{
		return updateWeights;
	}
	
	public String getModelName()
	{
		return modelName;
	}
	
	public double getLambda()
	{
		return lambda;
	}
	
	public double getRegularizationConstant()
	{
		return regularizationConstant;
	}
	
	public void setFeatureWeights(double[] featureWeights)
	{
		this.featureWeights = featureWeights;
	}
	
	public void setUpdateWeights(int[] updateWeights)
	{
		this.updateWeights = updateWeights;
	}
	
	public void setModelName(String modelName)
	{
		this.modelName = modelName;
	}
	
	public void setLambda(double lambda)
	{
		this.lambda = lambda;
	}
	
	public void setRegularizationConstant(double regularizationConstant)
	{
		this.regularizationConstant = regularizationConstant;
	}
	
	protected String modelName;
	protected double[] featureWeights;
	protected int[] updateWeights;
	protected double lambda;
	protected double regularizationConstant;
}
