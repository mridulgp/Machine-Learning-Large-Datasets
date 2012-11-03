package mlld.phrasecounting;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class RecordMessages
{
	public RecordMessages()
	{

	}
	public void printMessages(BufferedReader inReader) throws IOException
	{
		while(inReader.ready())
		{
			String line = inReader.readLine();
			String[] phraseInfo = line.split("\\t");
			
			String[] words = phraseInfo[0].split("\\s+");
			System.out.println(words[0] + "\t" + phraseInfo[0]);
			System.out.println(words[1] + "\t" + phraseInfo[0]);
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		RecordMessages rm = new RecordMessages();

		// phrase file
		BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		
		rm.printMessages(inReader);
	}
}