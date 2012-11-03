package mlld.phrasecounting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class PCTest extends PhraseCount
{
	public PCTest(Map<String, Long> globalCounterHash)
	{
		this.globalCounterHash = globalCounterHash;
	}
	
	public void storeGlobalCounts(BufferedReader inReader) throws IOException
	{
		while(inReader.ready())
		{
			String line = inReader.readLine();
			String[] countInfo = line.split("=");
			long count = Long.parseLong(countInfo[1]);
			
			globalCounterHash.put(countInfo[0], count);
		}
	}
	
	public void parseNgramInfo(BufferedReader inReader) throws IOException
	{
		long B_x=0, C_x=0, B_y=0, C_y=0, B_xy=0, C_xy=0;
		int flag=0;
		String tempCount="";
		getStopWords();

		long FNgramCount_unigram = globalCounterHash.get("Cx_count");
		long FNgramCount_ngram = globalCounterHash.get("Cxy_count");
		long BNgramCount_unigram = globalCounterHash.get("Bx_count");
		long BNgramCount_ngram = globalCounterHash.get("Bxy_count");
		long FVocabCount_unigram = globalCounterHash.get("Cx_vocab");
		long FVocabCount_ngram = globalCounterHash.get("Cxy_vocab");
		long BVocabCount_unigram = globalCounterHash.get("Bx_vocab");
		long BVocabCount_ngram = globalCounterHash.get("Bxy_vocab");

		while(inReader.ready())
		{
			flag=0;
			String line = inReader.readLine();
			String[] ngramInfo = line.split("\\t");
			String ngram = ngramInfo[0];
			String[] ngrams = ngram.split("\\s+");
			
			String[] countsInfo = ngramInfo[1].split(",");
			for(int i=0; i<countsInfo.length; i++)
			{
				if(countsInfo[i].startsWith("Bx="))
				{
					tempCount = countsInfo[i].replaceAll("Bx=", "");
					B_x = Long.parseLong(tempCount);
				}
				else if(countsInfo[i].startsWith("Cx="))
				{
					tempCount = countsInfo[i].replaceAll("Cx=", "");
					C_x = Long.parseLong(tempCount);
				}
				else if(countsInfo[i].startsWith("Bxy="))
				{
					tempCount = countsInfo[i].replaceAll("Bxy=", "");
					B_xy = Long.parseLong(tempCount);
				}
				else if(countsInfo[i].startsWith("Cxy="))
				{
					tempCount = countsInfo[i].replaceAll("Cxy=", "");
					C_xy = Long.parseLong(tempCount);
				}
				else if(countsInfo[i].startsWith("By="))
				{
					tempCount = countsInfo[i].replaceAll("By=", "");
					B_y = Long.parseLong(tempCount);
				}
				else if(countsInfo[i].startsWith("Cy="))
				{
					tempCount = countsInfo[i].replaceAll("Cy=", "");
					C_y = Long.parseLong(tempCount);
				}
			}
		
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
				double phraseness = computePhraseness(C_x, C_y, C_xy, FNgramCount_unigram, FNgramCount_ngram, FVocabCount_unigram, FVocabCount_ngram);
				double informativeness = computeInformativeness(B_xy, C_xy, BNgramCount_ngram, FNgramCount_ngram, BVocabCount_ngram, FVocabCount_ngram);
				double phraseScore = computePhraseScore(phraseness, informativeness);

				BigDecimal bd_phraseness = new BigDecimal(phraseness);
				BigDecimal bd_informativeness = new BigDecimal(informativeness);
				BigDecimal bd_phraseScore = new BigDecimal(phraseScore);

				String modifiedNgram = ngram.replaceAll("\\s+", "_");

				//System.out.println(ngram + "\t" + bd_phraseScore.toString() + "\t" + bd_phraseness.toString() + "\t" + bd_informativeness.toString());
				System.out.println(modifiedNgram + "\t" + phraseScore + "\t" + phraseness + "\t" + informativeness);
			}
		}

	}
	
	public double computePhraseness(long C_x, long C_y, long C_xy, long FNgramCount_unigram, long FNgramCount_ngram, long FVocabCount_unigram, long FVocabCount_ngram)
	{
		double p_w=0.0;
		double q_w=0.0;
		double alpha=1.0;
		double vocabPrior_ngram = ((double)1)/FVocabCount_ngram;
		double vocabPrior_unigram = ((double)1)/FVocabCount_unigram;
		
		p_w = ( (double)(C_xy + alpha*vocabPrior_ngram) )/( (double)FNgramCount_ngram + alpha);
		q_w = ( (double)(C_x + alpha*vocabPrior_unigram)*(C_y + alpha*vocabPrior_unigram) )/( ( (double)FNgramCount_unigram + alpha)*( (double)FNgramCount_unigram + alpha) );
				
		return computeKLDivergence(p_w, q_w);
	}
	
	public double computeInformativeness(long B_xy, long C_xy, long BNgramCount_ngram, long FNgramCount_ngram, long BVocabCount_ngram, long FVocabCount_ngram)
	{
		double p_w=0.0;
		double q_w=0.0;
		double alpha=1.0;
		double FVocabPrior_ngram = ((double)1)/FVocabCount_ngram;
		double BVocabPrior_ngram = ((double)1)/BVocabCount_ngram;

		p_w = ( (double)(C_xy + alpha*FVocabPrior_ngram) )/(FNgramCount_ngram + alpha);
		q_w = ( (double)(B_xy + alpha*BVocabPrior_ngram) )/(BNgramCount_ngram + alpha);
		
		return computeKLDivergence(p_w, q_w);
	}
	
	/**
	 * Compute KL divergence between two probabilities.
	 * The probabilities need to be smoothed before computing the value.
	 * @param p_w
	 * @param q_w
	 * @return
	 */
	public double computeKLDivergence(double p_w, double q_w)
	{
		double KLDivergence = p_w*Math.log(p_w/q_w);
		return KLDivergence;
	}
	
	public double computePhraseScore(double phraseness, double informativeness)
	{
		double alpha = (double)1/100000;
		double phraseScore = 0.0;
		phraseScore = (double)phraseness/(phraseness + alpha) + informativeness;
		return phraseScore;
	}
	
	protected Map<String, Long> globalCounterHash;
	
	public static void main(String[] args) throws IOException
	{
		Map<String, Long> globalCounterHash = new HashMap<String, Long>();
		PCTest pcTest = new PCTest(globalCounterHash);
		
		// Model file
		BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		
		// Global counts file
		BufferedReader inReader1 = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
				
		pcTest.storeGlobalCounts(inReader1);
		pcTest.parseNgramInfo(inReader);
	}
}
