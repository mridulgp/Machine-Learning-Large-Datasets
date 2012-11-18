//package mlld.stochasticgradientdescent;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class LRTest
{
	public LRTest()
	{

	}
	public LRTest(SGD sgd, int dictionarySize, String[] labelList)
	{
		this.sgd = sgd;
		this.dictionarySize = dictionarySize;
		this.labelList = labelList;
	}

	public Model[] getSGDModel()
	{
		return sgd.getModel();
	}

	public void test(BufferedReader inReader, String modelFile) throws IOException
	{
		/*String modelName = "sgd.lr";
		String modelFile = modelName + "." + String.valueOf(dictionarySize) + "." + "final";*/
		Model[] currentModel;
		try
		{
			FileInputStream fileIn = new FileInputStream(modelFile);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			currentModel = (Model[]) in.readObject();
			sgd.setModel(currentModel);
			in.close();
			fileIn.close();
			
			for (int i=0; i<sgd.getModel().length; i++)
			{
				Model model = sgd.getModel()[i];
				//System.out.println("Feature hash size = " + model.getFeatureWeights().size() + " Feature update hash size = " + model.getUpdateWeights().size());
			}
		}
		catch(IOException i)
		{
			i.printStackTrace();
			return;
		}
		catch(ClassNotFoundException c)
		{
			System.out.println("Model class not found!");
			c.printStackTrace();
			return;
		}
		
		sgd.classify(inReader, labelList);
	}

	protected SGD sgd;
	protected int dictionarySize;
	protected String[] labelList;

	public static void main(String args[]) throws IOException
	{
		// test file
		BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		SGD sgd = new MemorySGD(Integer.parseInt(args[2]));

		//String[] labelList = {"nl", "el", "ru", "sl", "pl", "ca", "fr", "tr", "hu", "de", "hr", "es", "ga", "pt"};
		String[] labelList = {"en", "de", "nd"};
		
		// dictionary size
		LRTest lrTest = new LRTest(sgd, Integer.parseInt(args[2]), labelList);
		lrTest.test(inReader, args[1]); // model file
	}
}
