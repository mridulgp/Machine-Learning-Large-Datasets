//package mlld.stochasticgradientdescent;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class MemorySGD extends SGD
{
	public MemorySGD(int dictionarySize, double regularizationConstant)
	{
		super(dictionarySize, regularizationConstant);
	}

	public MemorySGD(int dictionarySize)
	{
		super(dictionarySize);
	}

	private static Vector<String> tokenizeDoc(String currentDoc)
	{
		String[] words = currentDoc.split("\\s+");
		Vector<String> tokens = new Vector<String>();

		for (int i=0; i<words.length; i++)
		{
			words[i] = words[i].replaceAll("\\W", "");

			if (words[i].length() > 0)
			{
				tokens.add(words[i]);
			}
		}
		return tokens;
	}

	protected Document read(String line) throws IOException
	{
		Document doc = new Document();

		String[] docInfo = line.split("\\t");
		if(docInfo.length == 2)
		{
			Vector<String> tokens = tokenizeDoc(docInfo[1]);
			doc.setTokenList(tokens);
			String[] docLabels = docInfo[0].split("\\,");
			String docLabel = null;

			for (int i=0; i<docLabels.length; i++)
			{
				/*if(docLabels[i].endsWith("CAT"))
						docLabel = docLabels[i];*/
				if(docLabels[i] != null)
				{
					doc.setDocLabels(docLabels[i]);
				}
			}
		}
		else
		{
			System.err.println("Document is not in the required format!");
			System.exit(0);
		}

		return doc;
	}

	public int update(BufferedReader inReader, String[] labelList, int totalInstances, int iteration) throws IOException
	{
		lambda = eta/(Math.pow((double)iteration, 2));
		double lcl = 0.0;

		while(inReader.ready())
		{
			totalInstances++;

			double[] B = new double[dictionarySize+2];
			int[] A = new int[dictionarySize+2];

			String line = inReader.readLine();
			Document doc = read(line);

			Vector<String> tokenList = doc.getTokenList();
			Vector<String> docLabels = doc.getDocLabels();
			double binaryLabel = 0.0;

			//System.out.println(docLabels);

			int docLength = tokenList.size();
			featureHash.put(0,1); // bias feature

			for (int i=0; i<docLength; i++)
			{
				String token = tokenList.get(i);
				int featureID = token.hashCode()%dictionarySize;
				if(featureID<0)
					featureID = featureID + dictionarySize;

				if(featureID==0)
				{
					featureID = dictionarySize;
				}

				if(featureHash.get(featureID)==null)
				{
					featureHash.put(featureID, 1);
				}
				else
				{
					int value = featureHash.get(featureID);
					value++;
					featureHash.put(featureID, value);
				}
			}

			for (int i=0; i<labelList.length; i++)
			{
				//prob = sigmoid(score);
				score = 0.0;
				String label = labelList[i];
				//System.out.println(label);

				Model model = new Model(B,A);
				model.setModelName("sgd.lr");

				if(modelSGDList[i]!=null)
				{
					model = modelSGDList[i];
					B = model.getFeatureWeights();
					A = model.getUpdateWeights();
				}
				else if(modelSGDList[i]==null)
				{
					B = new double[dictionarySize+2];
					A = new int[dictionarySize+2];
				}

				Set set1 = featureHash.entrySet();
				Iterator it1 = set1.iterator();

				while (it1.hasNext())
				{
					Map.Entry entry1 = (Map.Entry) it1.next();
					int featureID1 = (Integer)entry1.getKey();
					int featureValue1 = (Integer)entry1.getValue();

					//System.out.print(featureID1 + "=" + featureValue1 + "\t");

					double weight1 = B[featureID1];
					score = score + ((double)featureValue1)*weight1;
				}
				
				prob = sigmoid(score);
				
				Set set = featureHash.entrySet();
				Iterator it = set.iterator();
				while (it.hasNext())
				{
					Map.Entry entry = (Map.Entry) it.next();
					//System.out.println(entry.getKey() + " : " + entry.getValue());
					//String token = entry.getKey().toString();

					if(docLabels.contains(label))
						binaryLabel = 1.0;
					else
						binaryLabel = 0.0;

					int featureID = (Integer)entry.getKey();
					/*if(featureID<0)
						featureID = featureID + dictionarySize;*/

					//System.out.println("Token = " + token + "\t" + "Token ID = " + featureID);

					double featureValue = 0.0;
					if(featureHash.containsKey(featureID))
					{
						featureValue = ((double)featureHash.get(featureID));

						B[featureID] = B[featureID]*Math.pow( (1-2*lambda*regularizationConstant), (double)(totalInstances-A[featureID]) );

						B[featureID] = B[featureID] + lambda*(binaryLabel - prob)*featureValue;
						A[featureID] = totalInstances;
					}
				}

				model.setFeatureWeights(B);
				model.setUpdateWeights(A);
				modelSGDList[i] = model;

				//System.out.println();
				//prob = sigmoid(score);
				if(docLabels.contains(labelList[i]))
					lcl = lcl + Math.log(prob);

				else if(!docLabels.contains(labelList[i]))
					lcl = lcl + Math.log(1.0-prob);
			}
			featureHash = new HashMap<Integer, Integer>();
		}

		System.out.println("Conditional Log Likelihood at iteration " + iteration + ":" + "\t" + lcl);
		return totalInstances;
	}

	public void classify(BufferedReader inReader, String[] labelList) throws IOException
	{
		if(modelSGDList.length>0)
		{
			int finalCorrectInstances=0;
			int totalInstances=0;

			while(inReader.ready())
			{
				List<Double> probList = new ArrayList<Double>();
				double[] B = new double[dictionarySize+2];
				featureHash.clear();
				
				String probClass = "[";

				String line = inReader.readLine();
				Document doc = read(line);

				Vector<String> tokenList = doc.getTokenList();
				Vector<String> docLabels = doc.getDocLabels();
				Vector<String> predictedLabels = new Vector<String>();

				int docLength = tokenList.size();
				featureHash.put(0,1);

				for (int i=0; i<docLength; i++)
				{
					String token = tokenList.get(i);

					int featureID = token.hashCode()%dictionarySize;
					if(featureID<0)
						featureID = featureID + dictionarySize;

					if(featureID==0)
						featureID = dictionarySize;

					if(featureHash.get(featureID)==null)
					{
						featureHash.put(featureID, 1);
					}
					else
					{
						int value = featureHash.get(featureID);
						value++;
						featureHash.put(featureID, value);
					}
				}

				int correctInstances=0;
				double lcl=0.0;

				for (int i=0; i<labelList.length; i++)
				{
					Model model;
					//model.setModelName("sgd.lr");
					prob = 0.0;
					score = 0.0;

					model = modelSGDList[i];

					B = model.getFeatureWeights();

					Set set = featureHash.entrySet();
					Iterator it = set.iterator();
					while (it.hasNext())
					{
						Map.Entry entry = (Map.Entry) it.next();
						//System.out.println(entry.getKey() + " : " + entry.getValue());

						int featureID = (Integer)entry.getKey();
						if(featureID<0)
							featureID = featureID + dictionarySize;

						double featureValue=0.0;
						if(featureHash.containsKey(featureID))
						{
							featureValue = ((double)featureHash.get(featureID));
							score = score + B[featureID]*featureValue;
						}
					}

					prob = sigmoid(score);
					lcl = lcl + Math.log(prob);
					totalInstances++;

					if(prob>=1-prob)
					{
						String predictedLabel = labelList[i];
						predictedLabels.add(predictedLabel);
						probList.add(prob);
						if(docLabels.contains(predictedLabel))
							correctInstances++;
					}
					else
					{
						if(!docLabels.contains(labelList[i]))
							correctInstances++;
					}
					//System.out.print(labelList[i] + "=" + prob + "\t");
					probClass = probClass + labelList[i] + "=" + prob + ",";
				}

				probClass = probClass.substring(0,probClass.length()-1);
				System.out.println(docLabels + "\t" + predictedLabels + "\t" + probClass + "]");

				/*if(predictedLabels.isEmpty())
					correctInstances = correctInstances - docLabels.size();
				else
				{
					for (String docLabel : docLabels)
					{
						if (!predictedLabels.contains(docLabel))
							correctInstances--;
					}
				}*/

				finalCorrectInstances = finalCorrectInstances + correctInstances;
				//System.out.println(docLabels + "\t" + predictedLabels + "\t" + probList + "\t" + lcl);
			}

			double accuracy = ((double)finalCorrectInstances)/((double)totalInstances)*100.0;
			System.out.println("Accuracy " + "(" + finalCorrectInstances + "/" + totalInstances + "): " + accuracy + "%");
		}
		else
		{
			System.err.println("No model found!");
			System.exit(0);
		}
	}

	protected double sigmoid(double score)
	{
		if (score > overflow)
			score = overflow;
		else if (score < -overflow)
			score = -overflow;
		double exp = Math.exp(score);
		return exp/(1 + exp);
	}

	protected static double overflow=20;
}
