//package mlld.stochasticgradientdescent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogisticRegression
{
	public LogisticRegression()
	{

	}
	public LogisticRegression(SGD sgd, int dictionarySize, String[] labelList)
	{
		this.sgd = sgd;
		this.dictionarySize = dictionarySize;
		this.labelList = labelList;
	}

	public Model[] getSGDModel()
	{
		return sgd.getModel();
	}


	//@SuppressWarnings("unchecked")
	public void train(BufferedReader inReader, int iteration, int trainingInstances, String modelDir) throws IOException
	{
		int newTotalInstances=0;

		//int totalInstances = trainingInstances*(iteration-1)*labelList.size();
		int totalInstances = trainingInstances*(iteration-1);

		Model[] currentModel;

		System.out.println("Iteration " + iteration + ":");
		System.out.println("totalInstances = " + totalInstances + "\t" + "trainingInstances = " + trainingInstances);

		newTotalInstances = sgd.update(inReader, labelList, totalInstances, iteration);
		System.out.println("newTotalInstances = " + newTotalInstances);
		//totalInstances = newTotalInstances;

		if(iteration>=20)
		{
			double lambda = sgd.getLambda();
			double regularizationConstant = sgd.getRegularizationConstant();

			currentModel = sgd.getModel();
			for (int i=0; i<currentModel.length; i++)
			{
				Model model = currentModel[i];
				double[] B = model.getFeatureWeights();
				int[] A = model.getUpdateWeights();

				for (int key=0; key<B.length; key++)
				{
					double value = B[key];
					value = value*Math.pow( (1-2*lambda*regularizationConstant), (newTotalInstances-A[key]) );
					B[key] = value;
				}
				model.setFeatureWeights(B);
				currentModel[i] = model;
			}

			try
			{
				if(currentModel.length>0)
				{
					String modelName = currentModel[0].getModelName();
					String modelFile = modelDir + "/" + modelName + "." + String.valueOf(dictionarySize) + "-" + String.valueOf(regularizationConstant) + "." + "final";
					FileOutputStream fileOut = new FileOutputStream(modelFile);
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					out.writeObject(currentModel);
					out.close();
					fileOut.close();
				}
			}

			catch(IOException i)
			{
				i.printStackTrace();
			}
		}
	}

	protected SGD sgd;
	protected int dictionarySize;
	protected  String[] labelList;

	public static void main(String args[]) throws IOException
	{
		// randomized train file

		//int iteration = Integer.parseInt(args[3]); // number of iterations
		double regularizationConstant = Double.parseDouble(args[3]);
		String[] labelList = {"nl", "el", "ru", "sl", "pl", "ca", "fr", "tr", "hu", "de", "hr", "es", "ga", "pt"};
		//String[] labelList = {"en", "de", "nd"};
		
		SGD sgd = new MemorySGD(Integer.parseInt(args[2]), regularizationConstant, labelList.length);


		// dictionary size
		LogisticRegression lr = new LogisticRegression(sgd, Integer.parseInt(args[2]), labelList);

		int iteration=0;
		File dir = new File(args[0]); // train directory
		for (File child : dir.listFiles())
		{
			if (".".equals(child.getName()) || "..".equals(child.getName())) 
			{
				continue;  // Ignore the self and parent aliases.
			}
			else
			{
				iteration++;
				BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(child.toString())));
				// Do something with child
				lr.train(inReader, iteration, Integer.parseInt(args[1]), args[4]); // number of examples
				inReader.close();
			}
		}
	}
}
