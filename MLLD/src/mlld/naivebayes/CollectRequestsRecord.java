package mlld.naivebayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class CollectRequestsRecord
{
	private static final Document Document = null;

	public CollectRequestsRecord()
	{
		eventCounterHash = new HashMap<String,Integer>();
		featureFlag = 0;
		wordFeature = null;

	}

	public void collectRequests(String line, int count)
	{
		String[] prevWordFeature = null;
		prevWordFeature = wordFeature;
		wordFeature = line.split("\\t", 2);

		/*if(count==1)
		{
			prevWordFeature = wordFeature;
		}*/

		if(prevWordFeature!=null)
		{
			if(!prevWordFeature[1].startsWith("~ctr_to_"))
			{
				featureString = prevWordFeature[1];
			}
			if(wordFeature[0].equals(prevWordFeature[0]))
			{

				String[] docInfo = wordFeature[1].split("_", 3);
				if(docInfo.length==3)
				{
					String[] featureInfo = featureString.split("\\^", 2);
					if(featureInfo[1].startsWith(wordFeature[0] + "="))
						System.out.println(docInfo[2] + "\t" + "~ctr_" + wordFeature[0] + "\t" + featureString);
				}
			}
			/*else if(!wordFeature[0].equals(prevWordFeature[0]))
			{
				prevWordFeature = wordFeature;
			}*/
		}
	}

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

	protected Map<String, Integer> eventCounterHash;
	protected int featureFlag=0;
	String[] wordFeature;
	String featureString = "";

	public static void main (String[] args) throws IOException
	{

		CollectRequestsRecord crr = new CollectRequestsRecord();

		//BufferedReader inReader1 = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));

		//BufferedReader inReader1 = new BufferedReader(new InputStreamReader(System.in));

		BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		int count=0;
		while(inReader.ready())
		{
			count++;
			String line = inReader.readLine().trim();
			crr.collectRequests(line, count);
		}

		// Test file
		BufferedReader inReader1 = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));

		int docID=0;
		while(inReader1.ready())
		{
			docID++;
			Document doc = crr.read(inReader1);
			String docIDString = "id" + docID;

			Vector<String> tokenList = doc.getTokenList();
			Vector<String> labelList = doc.getDocLabels();
			System.out.print(docIDString + "\t");
			for (String s : labelList) 
			{  
				System.out.print(s + ",");
			}  
			System.out.print("\t");  
			for (int i=0; i<tokenList.size(); i++)
			{
				String token = tokenList.get(i);
				System.out.print(token + " ");
			}
			System.out.println();
		}
	}
}
