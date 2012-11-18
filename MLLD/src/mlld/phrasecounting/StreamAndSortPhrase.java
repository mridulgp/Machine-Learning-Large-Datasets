package mlld.phrasecounting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StreamAndSortPhrase extends PhraseCount
{
	public StreamAndSortPhrase()
	{
		super();
	}

	public StreamAndSortPhrase(List<String> mergedCounts)
	{
		this.mergedCounts = mergedCounts;
	}

	public long getFVocabCount()
	{
		return FVocabCount;
	}

	public long getBVocabCount()
	{
		return BVocabCount;
	}

	public void CollectNgramCounts(BufferedReader inReader, BufferedWriter out) throws IOException
	{
		long ngramFCount = 0;
		long ngramBCount = 0;
		long decadeNumber = 0;
		long count=0;
		String B="";
		String C="";
		String[] ngrams=null;
		int flag=0;

		getStopWords();

		while(inReader.ready())
		{
			flag=0;
			String line = inReader.readLine();
			String[] ngramInfo = line.split("\\t");
			decadeNumber = Integer.parseInt(ngramInfo[1]);
			long ngramCount = Integer.parseInt(ngramInfo[2]);
			ngram = ngramInfo[0];

			ngrams = ngram.split("\\s+");

			if(ngrams.length==1)
			{
				B = "Bx";
				C = "Cx";
			}
			else if (ngrams.length==2)
			{
				B = "Bxy";
				C = "Cxy";
			}

			if(ngramInfo.length==3)
			{
					count++;
					if(decadeNumber>=1960 && decadeNumber<1990)
					{
						BNgramCount = BNgramCount + ngramCount;					
					}
					else if(decadeNumber>=1990)
					{
						FNgramCount = FNgramCount + ngramCount;
					}

					if(count==1)
					{
						prevNgram = ngram;
						if(decadeNumber>=1960 && decadeNumber<1990)
						{
							ngramBCount = ngramCount;
							BVocabCount++;
						}
						else if(decadeNumber>=1990)
						{
							ngramFCount = ngramCount;
							FVocabCount++;
						}
					}
					else
					{
						if(ngram.equals(prevNgram))
						{
							if(decadeNumber>=1960 && decadeNumber<1990)
							{
								ngramBCount = ngramBCount + ngramCount;
							}
							else if(decadeNumber>=1990)
							{
								ngramFCount = ngramFCount + ngramCount;
							}
						}
						else if(!ngram.equals(prevNgram))
						{
							String[] prevNgrams = prevNgram.split("\\s+");
							for(int k=0; k<prevNgrams.length; k++)
							{
								if(stopWordSet.contains(prevNgrams[k]))
								{
									flag=1;
									break;
								}
							}
							//if(flag==0)
								System.out.println(prevNgram + "\t" + B + "=" + ngramBCount + "," + C + "=" + ngramFCount);
							
							prevNgram = ngram;

							ngramBCount=0;
							ngramFCount=0;
							if(decadeNumber>=1960 && decadeNumber<1990)
							{
								ngramBCount = ngramCount;
								BVocabCount++;
							}
							else if(decadeNumber>=1990)
							{
								ngramFCount = ngramCount;
								FVocabCount++;
							}
						}
					}
				}
			}
		flag=0;
		for(int k=0; k<ngrams.length; k++)
		{
			if(stopWordSet.contains(ngrams[k]))
			{
				flag=1;
				break;
			}
		}
		//if(flag==0)
			System.out.println(ngram + "\t" + B + "=" + ngramBCount + "," + C + "=" + ngramFCount);

		if(ngrams.length==1)
		{
			//out.write("Bx_vocab" + "=" + BVocabCount + "\n");
			//out.write("Cx_vocab" + "=" + FVocabCount + "\n");
			out.write("Bx_count" + "=" + BNgramCount + "\n");
			out.write("Cx_count" + "=" + FNgramCount + "\n");
		}
		else if(ngrams.length==2)
		{
			//out.write("Bxy_vocab" + "=" + BVocabCount + "\n");
			//out.write("Cxy_vocab" + "=" + FVocabCount + "\n");
			out.write("Bxy_count" + "=" + BNgramCount + "\n");
			out.write("Cxy_count" + "=" + FNgramCount + "\n");
		}
	}

	public void AggregateMessageCounts(BufferedReader inReader) throws IOException
	{
		long count=0;
		int flag=0;
		String[] B_ngram = null;
		String[] C_ngram = null;
		String[] phrase = null;
		String messageNgram = "";
		getStopWords();

		while(inReader.ready())
		{
			flag=0;
			String line = inReader.readLine();
			String[] ngramInfo = line.split("\\t");

			if(ngramInfo.length==2)
			{
				count++;
				String[] corpusCount = ngramInfo[1].split(",");
				ngram = ngramInfo[0];
				if(count==1)
				{
					prevNgram = ngram;
				}
				if(corpusCount.length==2)
				{
					B_ngram = corpusCount[0].split("=");
					C_ngram = corpusCount[1].split("=");
					messageNgram = ngramInfo[0];
				}
				phrase = corpusCount[0].split("\\s+");
				if(ngram.equals(prevNgram) && corpusCount.length==1 && messageNgram.equals(ngram))
				{
					for(int k=0; k<phrase.length; k++)
					{
						if(stopWordSet.contains(phrase[k]))
						{
							flag=1;
							break;
						}
					}

					if(phrase[0].equals(prevNgram))
					{
						if(flag==0)
						{
							System.out.print(phrase[0] + " " + phrase[1] + "\t" + "Bx" + "=" + B_ngram[1] + ",");
							System.out.println("Cx" + "=" + C_ngram[1]);
						}
					}
					else if(phrase[1].equals(prevNgram))
					{
						if(flag==0)
						{
							System.out.print(phrase[0] + " " + phrase[1] + "\t" + "By" + "=" + B_ngram[1] + ",");
							System.out.println("Cy" + "=" + C_ngram[1]);
						}
					}
				}
				else if(!ngram.equals(prevNgram))
				{
					prevNgram = ngram;
					phrase = null;
				}
			}
		}
		/*if(phrase!=null)
		{
			if(phrase[0].equals(prevNgram))
			{
				System.out.print(phrase[0] + " " + phrase[1] + "\t" + "Bx" + "=" + B_ngram[1] + ",");
				System.out.println("Cx" + "=" + C_ngram[1]);
			}
			else if(phrase[1].equals(prevNgram))
			{
				System.out.print(phrase[0] + " " + phrase[1] + "\t" + "By" + "=" + B_ngram[1] + ",");
				System.out.println("Cy" + "=" + C_ngram[1]);
			}
		}*/
	}

	public void MergeCounts(BufferedReader inReader) throws IOException
	{
		long count=0;

		while(inReader.ready())
		{
			String line = inReader.readLine();
			String[] ngramInfo = line.split("\\t");

			if(ngramInfo.length==2)
			{
				count++;
				ngram = ngramInfo[0];
				if(count==1)
				{
					prevNgram = ngram;
					mergedCounts.add(ngramInfo[1]);
				}
				if(ngram.equals(prevNgram) && count>1)
				{
					mergedCounts.add(ngramInfo[1]);
				}
				else if(!ngram.equals(prevNgram) && count>1)
				{
					System.out.print(prevNgram + "\t");
					for (int i=0; i<mergedCounts.size(); i++)
					{
						if(i<mergedCounts.size()-1)
							System.out.print(mergedCounts.get(i) + ",");
						else if(i==mergedCounts.size()-1)
							System.out.println(mergedCounts.get(i));			
					}

					prevNgram = ngram;
					mergedCounts.clear();
					mergedCounts.add(ngramInfo[1]);
				}
			}
		}
		System.out.print(prevNgram + "\t");
		for (int i=0; i<mergedCounts.size(); i++)
		{
			if(i<mergedCounts.size()-1)
				System.out.print(mergedCounts.get(i) + ",");
			else if(i==mergedCounts.size()-1)
				System.out.println(mergedCounts.get(i));			
		}
		mergedCounts.clear();
	}

	protected String ngram="";
	protected String prevNgram="";
	protected List<String> mergedCounts;
}
