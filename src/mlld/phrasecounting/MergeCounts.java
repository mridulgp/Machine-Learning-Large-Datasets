package mlld.phrasecounting;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MergeCounts
{
	public MergeCounts()
	{

	}

	public static void main(String[] args) throws IOException
	{
		List<String> mergedCounts = new ArrayList<String>();
		StreamAndSortPhrase pc = new StreamAndSortPhrase(mergedCounts);

		// phrase counts file
		BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));

		pc.MergeCounts(inReader);
	}
}
