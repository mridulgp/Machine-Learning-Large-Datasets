package mlld.phrasecounting;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CollectMessageCounts
{
	public CollectMessageCounts()
	{
		
	}
	
	public static void main (String[] args) throws IOException
	{
		StreamAndSortPhrase pc = new StreamAndSortPhrase();
		
		// unigrams+phrase combined file
		BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		
		pc.AggregateMessageCounts(inReader);
	}
}
