package mlld.phrasecounting;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PhraseScore extends PhraseCount
{
	public PhraseScore(Map<String, Long> globalCounterHash)
	{
		this.globalCounterHash = globalCounterHash;
	}

	public void parseNgramInfo(BufferedReader inReader) throws IOException
	{
		int flag=0;
		getStopWords();
		double phraseness = 0.0;
		double informativeness = 0.0;
		double phraseScore = 0.0;

		while(inReader.ready())
		{
			flag=0;
			String line = inReader.readLine();
			String[] ngramInfo = line.split("\\t");
			String ngram = ngramInfo[0];
			String[] ngrams = ngram.split("_");

			for(int k=0; k<ngrams.length; k++)
			{
				if(stopWordSet.contains(ngrams[k]))
				{
					flag=1;
					break;
				}
			}
			if(flag==0)
			{
				phraseScore = Double.parseDouble(ngramInfo[1]);
				phraseness = Double.parseDouble(ngramInfo[2]);
				informativeness = Double.parseDouble(ngramInfo[3]);
				phraseScore = computePhraseScore(phraseness, informativeness);

				BigDecimal bd_phraseness = new BigDecimal(phraseness);
				BigDecimal bd_informativeness = new BigDecimal(informativeness);
				BigDecimal bd_phraseScore = new BigDecimal(phraseScore);

				//System.out.println(ngram + "\t" + bd_phraseScore.toString() + "\t" + bd_phraseness.toString() + "\t" + bd_informativeness.toString());
				System.out.println(ngram + "\t" + phraseScore + "\t" + phraseness + "\t" + informativeness);
			}
		}
	}

	public double computePhraseScore(double phraseness, double informativeness)
	{
		double alpha = (double)1/10000;
		double phraseScore = 0.0;
		phraseScore = (double)phraseness/(phraseness + alpha) + informativeness;
		return phraseScore;
	}

	protected Map<String, Long> globalCounterHash;

	public static void main(String[] args) throws IOException
	{
		Map<String, Long> globalCounterHash = new HashMap<String, Long>();
		PhraseScore pcTest = new PhraseScore(globalCounterHash);

		// Score file
		BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));

		pcTest.parseNgramInfo(inReader);
	}
}
