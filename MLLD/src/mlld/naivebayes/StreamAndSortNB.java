package mlld.naivebayes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.lang.Math;

public class StreamAndSortNB extends NaiveBayes
{
	public StreamAndSortNB()
	{
		super();
	}

	public StreamAndSortNB(Map<String,Integer> eventCounterHash)
	{
		super();
		this.eventCounterHash = eventCounterHash;
		docEventCounter = new HashMap<String, Integer>();
		docCount = 0;
		correctLabelCount = 0;
		docLabelList = new Vector<String>();
		docTokenList = new Vector<String>();
		doc = new Document();

	}
	public StreamAndSortNB (List<Document> docs)
	{
		super(docs);
	}

	public Map<String, Integer> getEventCounterHash()
	{
		return eventCounterHash;
	}

	public void setEventCounterHash(Map<String, Integer> eventCounterHash)
	{
		this.eventCounterHash = eventCounterHash;
	}

	@Override
	protected Document read(BufferedReader inReader) throws IOException
	{
		String line = inReader.readLine();
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

	/**
	 * Add function to compute feature vector for inlinks documents
	 * @param doc
	 */

	private static void inlinksDoc (String currentDoc)
	{

	}

	private void eventCounter (Document doc, BufferedWriter out) throws IOException
	{
		Vector<String> tokens = doc.getTokenList();
		Vector<String> labelList = doc.getDocLabels();
		String label="";
		int labelSize = 0, labelInstance = 0;
		String perLabelWords="";

		for(int i=0; i<labelList.size(); i++)
		{
			long totalMem = Runtime.getRuntime().totalMemory();
			long freeMem = Runtime.getRuntime().freeMemory();

			/*long usedMem = totalMem - freeMem;
			double fracMem = ((double)usedMem / (double)totalMem);*/

			label = "Y=" + labelList.get(i);

			if(!eventCounterHash.containsKey(label))
			{
				eventCounterHash.put(label,1);
			}
			else
			{
				labelInstance = eventCounterHash.get(label);
				labelInstance++;
				eventCounterHash.put(label, labelInstance);					
			}

			for (int j=0; j<tokens.size(); j++)
			{
				String labelWord = label + "^" + "W=" + tokens.get(j);
				if(!eventCounterHash.containsKey(labelWord))
				{
					eventCounterHash.put(labelWord,1);
				}
				else
				{
					int labelWordInstance = eventCounterHash.get(labelWord);
					labelWordInstance++;
					eventCounterHash.put(labelWord, labelWordInstance);
				}
			}

			perLabelWords = label + "^" + "W=*";
			if(!eventCounterHash.containsKey(perLabelWords))
			{
				eventCounterHash.put(perLabelWords, tokens.size());
			}
			else
			{
				labelSize = eventCounterHash.get(perLabelWords);
				labelSize = labelSize + tokens.size();
				eventCounterHash.put(perLabelWords, labelSize);
			}
		}
	}

	public void printEventCounts(BufferedWriter out) throws IOException
	{
		Iterator<String> iterator = eventCounterHash.keySet().iterator();  

		while (iterator.hasNext())
		{  
			String key = iterator.next();
			int value = eventCounterHash.get(key);

			String eventInfo[] = key.split("\\^", 2);
			if(eventInfo.length==2)
			{
				if(!eventInfo[1].equals("W=*"))
				{
					System.out.println(eventInfo[1] + " " + key + "\t" + value);					
				}
				else
				{
					out.write(key + "\t" + value + "\n");
				}
			}
			else if(eventInfo.length==1)
			{
				out.write(key + "\t" + value + "\n");				
			}
		}
	}

	public void printEventCountsToFile(File file) throws IOException
	{
		Iterator<String> iterator = eventCounterHash.keySet().iterator();
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);

		while (iterator.hasNext())
		{
			String key = iterator.next();
			int value = eventCounterHash.get(key);

			out.write(key + "\t" + value);
			out.write("\n");
			System.out.println(key + "\t" + value);
		}
		out.close();
	}

	@Override
	public void train() 
	{

	}

	@Override
	public void test() 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void read() 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String,Integer> train(BufferedReader inReader, BufferedWriter out) throws IOException 
	{
		while(inReader.ready())
		{
			Document doc = read(inReader);
			labelCount++;
			eventCounter(doc, out);

			if(eventCounterHash.size() > 10000)
			{
				printEventCounts(out);
				//System.out.println("BEFORE = " + Runtime.getRuntime().freeMemory());
				eventCounterHash.clear();
				eventCounterHash = new HashMap<String, Integer>();
				//System.out.println("AFTER = " + Runtime.getRuntime().freeMemory() + "\n");
				//System.err.println("EXCEEDED ALLOWABLE USAGE!!!");
				//System.exit(1);				
			}
		}
		out.write("Y=ANY" + "\t" + labelCount + "\n");
		return eventCounterHash;
	}

	public void computeEventCounts(BufferedReader inReader, BufferedWriter out1)
	{
		try
		{
			String prevEvent = "";
			int eventCount = 0;
			int count=0;
			String[] eventInfo = null;
			int value = 0;
			String wordFeature[]=null;
			String prevWordFeature[]=null;
			String word="";
			String prevWord="";

			while(inReader.ready())
			{
				count++;
				String line = inReader.readLine();
				eventInfo = line.split("\\t");
				value = Integer.parseInt(eventInfo[1]);
				int sameWordEventCount=0;

				prevWordFeature = wordFeature;
				wordFeature = eventInfo[0].split("\\s+");
				word = wordFeature[0];

				if(count==1)
				{
					eventCount = value;
					prevEvent = eventInfo[0];
					prevWord = word;
					prevWordFeature = wordFeature;
					eventCounterHash.put(wordFeature[1], value);
				}
				if(word.equals(prevWord) && count>1)
				{
					eventCount = eventCount + value;
					if(eventCounterHash.get(wordFeature[1])!=null)
					{
						sameWordEventCount = eventCounterHash.get(wordFeature[1]);
					}
					sameWordEventCount = sameWordEventCount + value;
					eventCounterHash.put(wordFeature[1], sameWordEventCount);
				}
				else if( (!word.equals(prevWord)) && count>1)
				{		
					System.out.print(prevWord + "\t");
					Iterator<String> iterator = eventCounterHash.keySet().iterator();
					while (iterator.hasNext())
					{
						String key = iterator.next();
						int wordFeatureCount = eventCounterHash.get(key);
						System.out.print(key + "=" + wordFeatureCount + " ");
					}

					System.out.println();

					eventCounterHash = new HashMap<String, Integer>();
					if(eventCounterHash.get(wordFeature[1])!=null)
					{
						sameWordEventCount = eventCounterHash.get(wordFeature[1]);
					}
					sameWordEventCount = sameWordEventCount + value;
					eventCounterHash.put(wordFeature[1], sameWordEventCount);

					prevWordFeature = wordFeature;
					prevWord = word;
					eventCount = value;
				}
			}
			/*int sameWordEventCount=0;
			if(eventCounterHash.containsKey(wordFeature[1]))
			{
				sameWordEventCount = eventCounterHash.get(wordFeature[1]);
			}
			sameWordEventCount = sameWordEventCount + value;
			eventCounterHash.put(wordFeature[1], sameWordEventCount);*/
			System.out.print(word + "\t");
			Iterator<String> iterator = eventCounterHash.keySet().iterator();
			while (iterator.hasNext())
			{
				String key = iterator.next();
				int wordFeatureCount = eventCounterHash.get(key);
				System.out.print(key + "=" + wordFeatureCount + " ");
			}				
			System.out.println();
			eventCounterHash = new HashMap<String, Integer>();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void storeLabelCounts(BufferedReader inReader) throws NumberFormatException, IOException
	{
		while(inReader.ready())
		{
			String line = inReader.readLine();
			String[] eventInfo = line.split("\\t");
			//System.out.println(line);

			if(eventInfo.length==1)
			{
				vocabularySize = Integer.parseInt(eventInfo[0]);
			}

			else
			{
				int counter = Integer.parseInt(eventInfo[1]);
				int value=0;
				String[] event = eventInfo[0].split("\\^", 2);
				String eventName = "";

				if(event.length==2)
				{
					if(!eventCounterHash.containsKey(eventInfo[0]))
					{
						value = counter;
						eventCounterHash.put(eventInfo[0], value);
					}
					else
					{
						value = eventCounterHash.get(eventInfo[0]);
						value = value + counter;
						eventCounterHash.put(eventInfo[0], value);
					}
				}
				else if(event.length==1)
				{
					eventName = event[0];
					if(eventName.startsWith("Y="))
					{
						if(eventCounterHash.get(eventInfo[0])==null)
						{
							value = counter;
							eventCounterHash.put(eventInfo[0], value);
						}
						else
						{
							value = eventCounterHash.get(eventInfo[0]);
							value = value + counter;
							eventCounterHash.put(eventInfo[0], value);
						}
						if(!eventName.equals("Y=ANY"))
						{
							labelSet.add(eventName);
						}
					}
				}
			}
		}
	}

	private void storeDocModel (BufferedReader inReader, int count1) throws IOException
	{
		int count=0;
		while(inReader.ready())
		{
			String line = inReader.readLine().trim();
			count++;

			String prevDocID = docID;
			String[] docEventInfo = null;
			String[] prevDocEventInfo = docEventInfo;
			docEventInfo = line.split("\\t", 3);
			docID = docEventInfo[0];

			/*if(prevDocEventInfo!=null)
			{
				prevDocID = docID;
			}*/
			if(count==1)
			{
				prevDocID = docID;
				String[] docLabelListTemp = docEventInfo[1].split("\\,");
				List<String> list = Arrays.asList(docLabelListTemp);
				list = list.subList(0, list.size());
				docLabelList = new Vector<String>(list);

				String[] docTokens = docEventInfo[2].split("\\s+");
				List<String> list1 = Arrays.asList(docTokens);
				docTokenList = new Vector<String>(list1);
				doc = new Document(docTokenList, docLabelList);
				docCount++;
				//System.out.println(docTokenList);
			}

			if(docID.equals(prevDocID) && count>1)
			{
				String[] wordInfo = docEventInfo[1].split("~ctr_", 2);
				String word = wordInfo[1];
				String[] wordFeatureInfo = docEventInfo[2].split("\\s+");
				for(int i=0; i<wordFeatureInfo.length; i++)
				{
					String[] featureInfo = wordFeatureInfo[i].split("=");
					String featureValue = featureInfo[featureInfo.length-1];

					int numValue = Integer.parseInt(featureValue);
					String wordFeature = wordFeatureInfo[i].substring(0, (wordFeatureInfo[i].length()-featureValue.length()-1));
					docEventCounter.put(wordFeature, numValue);
					//System.out.println(docID + "\t" + wordFeature + "\t" + docEventCounter.get(wordFeature));
				}
			}
			else if ((!docID.equals(prevDocID)) && count>1)
			{
				//System.out.println(docTokenList);
				//System.out.println("CLASSIFYING");
				boolean matchLabel = classifyDocument(doc);
				docCount++;

				if(matchLabel)
					correctLabelCount++;

				prevDocID = docID;
				String[] docLabelListTemp = docEventInfo[1].split("\\,");
				List<String> list = Arrays.asList(docLabelListTemp);
				list = list.subList(0, list.size());
				docLabelList = new Vector<String>(list);

				String[] docTokens = docEventInfo[2].split("\\s+");
				List<String> list1 = Arrays.asList(docTokens);
				docTokenList = new Vector<String>(list1);
				doc = new Document(docTokenList, docLabelList);

				docEventCounter.clear();
			}
		}
		boolean matchLabel = classifyDocument(doc);

		if(matchLabel)
			correctLabelCount++;

		docEventCounter.clear();
	}

	/**
	 * Returns if the best predicted label is the actual label.
	 */
	private boolean classifyDocument(Document doc)
	{
		Vector<String> tokenList = doc.getTokenList();

		//System.out.println(doc.getTokenList().size());
		//System.out.println(doc.getTokenList());

		/*Iterator<String> iterator1 = docEventCounter.keySet().iterator();  

		while (iterator1.hasNext())
		{  
			String key = iterator1.next();
			int value = docEventCounter.get(key);
			System.out.print(key + "=" + value + " ");
		}
		System.out.println();*/

		HashSet<String> vocabulary = new HashSet<String>();

		for(int i=0; i<tokenList.size(); i++)
		{
			vocabulary.add("W=" + tokenList.get(i));
		}

		//System.out.println(doc.getDocLabels() + "\t" + tokenList);
		int V = vocabularySize;
		double alpha = 1.0;
		double q_j = (double)1.0/V;

		int anyLabelCount=1;
		String anyLabel = "Y=ANY";

		if(eventCounterHash.get(anyLabel)!=null)
			anyLabelCount = eventCounterHash.get(anyLabel);

		float q_y = (float)1/(labelSet.size());

		int anyLabelTokenCount=0;

		Map<String, Double> logProbLabel = new HashMap<String, Double>();

		Iterator<String> iterator = labelSet.iterator();
		double maxLogProb = -10000000;
		String bestLabelKey = "";

		while (iterator.hasNext())
		{
			String label = iterator.next().toString();
			String anyLabelToken = label + "^W=*";
			anyLabelTokenCount = eventCounterHash.get(anyLabelToken);
			int jointCount=0;
			int labelValue=0;
			String labelKey = label;
			if(eventCounterHash.get(labelKey)!=null)
			{
				labelValue = eventCounterHash.get(labelKey);
			}
			else
			{
				labelValue = 0;
			}

			double logProbFirstTerm = 0.0;
			double logProbSecondTerm = Math.log((double)(labelValue + alpha)/(double)(anyLabelCount + labelSet.size()));

			for(int i=0; i<tokenList.size(); i++)
			{
				String token = tokenList.get(i);
				String key = label + "^" + "W=" + token;

				if(docEventCounter.get(key)!=null)
				{
					jointCount = docEventCounter.get(key);
				}
				else
				{
					jointCount = 0;
				}
				logProbFirstTerm = logProbFirstTerm + Math.log((double)(jointCount + alpha)/(double)(anyLabelTokenCount + V));
			}
			double logProb = logProbFirstTerm + logProbSecondTerm;
			System.out.println("label:"+label+":"+logProb);
			if(maxLogProb < logProb)
			{
				maxLogProb = logProb;
				bestLabelKey = labelKey;
			}
			logProbLabel.put(labelKey,logProb);
		}
		Vector<String>listOfLabels = doc.getDocLabels();
		bestLabelKey = bestLabelKey.replaceFirst("Y=", "");

		System.out.println(listOfLabels + "\t" + bestLabelKey + "\t" + maxLogProb);

		if(listOfLabels.contains(bestLabelKey))
			return true;
		else
			return false;
	}

	@Override
	public void test(BufferedReader inReader, int count) throws IOException
	{
		storeDocModel(inReader, count);
	}

	@Override
	public boolean test(BufferedReader inReader, BufferedReader inReader1) throws IOException
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, Integer> train(BufferedReader inReader) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void printEventCounts() 
	{
		// TODO Auto-generated method stub

	}

	public int getDocCount()
	{
		return docCount;
	}

	public int getCorrectLabelCount()
	{
		return correctLabelCount;
	}

	protected Map<String, Integer> eventCounterHash;
	protected Set<String> eventCounterSet;
	protected Map<String, Integer> docEventCounter;
	int docCount;
	int correctLabelCount;
	Vector<String> docLabelList;
	Vector<String> docTokenList;
	String docID="";
	Document doc;
	int vocabularySize=1;

}
