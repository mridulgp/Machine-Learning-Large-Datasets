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

public class RequestsRecord
{
	private static final Document Document = null;

	public RequestsRecord()
	{
		eventCounterHash = new HashMap<String,Integer>();
		featureFlag = 0;
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

	public void getRequests(Document doc, int docID)
	{
		Vector<String> tokenList = doc.getTokenList();
		for(int i=0; i<tokenList.size(); i++)
		{
			String token = "W=" + tokenList.get(i);
			String docIDString = "~ctr" + "_to_" + "id" + String.valueOf(docID);
			System.out.println(token + "\t" + docIDString);	
		}
	}

	protected Map<String, Integer> eventCounterHash;
	protected int featureFlag=0;

	public static void main (String[] args) throws IOException
	{

		RequestsRecord rr = new RequestsRecord();

		// model file
		BufferedReader inReader1 = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));

		//BufferedReader inReader1 = new BufferedReader(new InputStreamReader(System.in));
		/*File modelFile = new File(args[1]);
		BufferedReader inReader1 = new BufferedReader(new InputStreamReader(new FileInputStream(modelFile)));*/

		//((MemoryBasedNB) nb).storeCounts(inReader1);

		int docID=0;
		while(inReader1.ready())
		{
			String line = inReader1.readLine();
			String[] wordFeature = line.split("\\t", 2);
			System.out.println(line.trim());
		}
		// Test file
		BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		while(inReader.ready())
		{
			docID++;
			Document doc = rr.read(inReader);
			rr.getRequests(doc, docID);
		}
	}
}
