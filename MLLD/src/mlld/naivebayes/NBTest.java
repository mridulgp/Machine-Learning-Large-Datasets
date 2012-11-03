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

public class NBTest
{
	private static final Document Document = null;

	public NBTest()
	{
		eventCounterHash = new HashMap<String,Integer>();
	}

	protected Map<String, Integer> eventCounterHash;

	public static void main (String[] args) throws IOException
	{
		int correctLabelCount=0;
		int totalLabelCount=0;
		Map<String, Integer> eventCounterHash = new HashMap<String, Integer>();
		NaiveBayes nb = new StreamAndSortNB(eventCounterHash);

		//BufferedReader inReader1 = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
		
		// Y=c file
		BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));

		// Model+test file
		//BufferedReader inReader1 = new BufferedReader(new InputStreamReader(System.in));
		BufferedReader inReader1 = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));

		((StreamAndSortNB) nb).storeLabelCounts(inReader);
		int count=0;
		
		//while(inReader1.ready())
		//{
			count++;
			nb.test(inReader1, count);
		//}

		correctLabelCount = ((StreamAndSortNB) nb).getCorrectLabelCount();
		totalLabelCount = ((StreamAndSortNB) nb).getDocCount();
		
		double accuracy = (double)correctLabelCount/totalLabelCount*100;

		System.out.println("Percent correct: " + correctLabelCount + "/" + 
				totalLabelCount + "=" + accuracy + "%");
	}
}