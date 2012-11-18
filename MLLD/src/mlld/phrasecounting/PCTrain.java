package mlld.phrasecounting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class PCTrain
{
	public void PCTrain()
	{
		
	}
	
	public static void main(String[] args) throws IOException
	{
		PCTrain train = new PCTrain();
		
		// unigrams file
		BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));

		// bigrams file
		//BufferedReader inReader1 = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));

		StreamAndSortPhrase pc = new StreamAndSortPhrase();
		
		// unigrams+bigrams output file
		FileWriter fstream = new FileWriter(args[1], true);

		// Global counts file
		BufferedWriter out = new BufferedWriter(fstream);

		pc.CollectNgramCounts(inReader, out);
		out.close();
		//pc.CollectNgramCounts(inReader1);
	}
}
