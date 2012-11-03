package mlld.naivebayes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class NBCountAdder
{
	private static final Document Document = null;

	public NBCountAdder()
	{
		
	}
	
	protected Map<String, Integer> eventCounterHash;
	
	public static void main (String[] args) throws IOException
	{
		Map<String, Integer> eventCounterHash = new HashMap<String, Integer>();
		NaiveBayes nb = new StreamAndSortNB(eventCounterHash);
		
		// counts file
		BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		//BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
		FileWriter fstream = new FileWriter(args[1]);
		BufferedWriter out1 = new BufferedWriter(fstream);
		((StreamAndSortNB) nb).computeEventCounts(inReader, out1);
		
		//File file = new File("C:\\Users\\Mridul Gupta\\Desktop\\Machine Learning with Large Datasets\\Assignments\\Datasets\\test.txt");
		//nb.printEventCounts();
	}
}