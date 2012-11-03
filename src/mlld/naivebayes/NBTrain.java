package mlld.naivebayes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class NBTrain
{
	private static final Document Document = null;

	public NBTrain()
	{
		eventCounterHash = new HashMap<String,Integer>();
	}
	
	protected Map<String, Integer> eventCounterHash;
	
	public static void main (String[] args) throws IOException
	{
		Map<String, Integer> eventCounterHash = new HashMap<String, Integer>();
		NaiveBayes nb = new StreamAndSortNB(eventCounterHash);
		
		// training file
		BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		//BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
		
		// Y=c file
		FileWriter fstream = new FileWriter(args[1]);
		BufferedWriter out = new BufferedWriter(fstream);
		eventCounterHash =  nb.train(inReader, out);
		
		//File file = new File("C:\\Users\\Mridul Gupta\\Desktop\\Machine Learning with Large Datasets\\Assignments\\Datasets\\test.txt");
		((StreamAndSortNB)nb).printEventCounts(out);
		out.close();
	}
}