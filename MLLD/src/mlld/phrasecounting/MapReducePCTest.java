package mlld.phrasecounting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

import mlld.naivebayes.MapReduceNB.globalCounters;

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class MapReducePCTest
{
	public static enum globalCounters { setupCounter_1 };

	public static class MapTest extends Mapper<LongWritable, Text, Text, Text>
	{
		private Text word = new Text();
		private HashMap<String, Long> globalEventCounter = new HashMap<String, Long>();
		protected long labelSize;
		protected long vocabCount;
		protected long totalDocs=0;
		protected HashMap<String, Long> neededHash;
		protected HashSet<String> stopWordSet = new HashSet<String>();

		public void getStopWords()
		{
			for (int i=0; i<stop_words.length; i++)
			{
				stopWordSet.add(stop_words[i]);
			}
		}

		protected static String[] stop_words = { "a", "able", "about", "across", "after",
			"all", "almost", "also", "am", "among", "an", "and", "any", "are",
			"as", "at", "be", "because", "been", "but", "by", "can", "cannot",
			"could", "dear", "did", "do", "does", "either", "else", "ever",
			"every", "for", "from", "get", "got", "had", "has", "have", "he",
			"her", "hers", "him", "his", "how", "however", "i", "if", "in",
			"into", "is", "it", "its", "just", "least", "let", "like",
			"likely", "may", "me", "might", "most", "must", "my", "neither",
			"no", "nor", "not", "of", "off", "often", "on", "only", "or",
			"other", "our", "own", "rather", "said", "say", "says", "she",
			"should", "since", "so", "some", "than", "that", "the", "their",
			"them", "then", "there", "these", "they", "this", "tis", "to",
			"too", "twas", "us", "wants", "was", "we", "were", "what", "when",
			"where", "which", "while", "who", "whom", "why", "will", "with",
			"would", "yet", "you", "your" };

		private void loadNgramGlobalCounts(Path cachePath) throws IOException
		{
			// note use of regular java.io methods here - this is a local file now
			BufferedReader wordReader = new BufferedReader(new FileReader(cachePath.toString()));			
			try
			{
				String line;
				while ((line = wordReader.readLine()) != null)
				{
					if(!line.equals(""))
					{
						String[] globalEventCountInfo = line.split("\t");
						long count = Long.parseLong(globalEventCountInfo[1]);
						String hashKey = globalEventCountInfo[0];
						globalEventCounter.put(hashKey, count);
					}
				}
			} 
			finally
			{
				wordReader.close();
			}
		}

		protected void setup(Context context) throws IOException
		{
			context.getCounter(globalCounters.setupCounter_1).increment(1);
			try
			{
				Path [] cacheFiles = DistributedCache.getLocalCacheFiles(context.getConfiguration());
				if (null != cacheFiles && cacheFiles.length > 0)
				{
					for (Path cachePath : cacheFiles)
					{
						loadNgramGlobalCounts(cachePath);
					}
				}
			}
			catch (IOException e)
			{
				System.err.println("IOException reading from distributed cache");
				System.err.println(e.toString());
			}

		}

		protected String parseNgramInfo(String line) throws IOException
		{
			long B_x=0, C_x=0, B_y=0, C_y=0, B_xy=0, C_xy=0;
			int flag=0;
			String tempCount="";
			getStopWords();

			long FNgramCount_unigram = globalEventCounter.get("Cx_count");
			long FNgramCount_ngram = globalEventCounter.get("Cxy_count");
			long BNgramCount_unigram = globalEventCounter.get("Bx_count");
			long BNgramCount_ngram = globalEventCounter.get("Bxy_count");
			long FVocabCount_unigram = globalEventCounter.get("Cx_vocab");
			long FVocabCount_ngram = globalEventCounter.get("Cxy_vocab");
			long BVocabCount_unigram = globalEventCounter.get("Bx_vocab");
			long BVocabCount_ngram = globalEventCounter.get("Bxy_vocab");

			flag=0;
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
				String result = modifiedNgram + "\t" + phraseScore + "\t" + phraseness + "\t" + informativeness;
				return result;
			}
			
			return null;
		}

		protected double computePhraseness(long C_x, long C_y, long C_xy, long FNgramCount_unigram, long FNgramCount_ngram, long FVocabCount_unigram, long FVocabCount_ngram)
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

		protected double computeInformativeness(long B_xy, long C_xy, long BNgramCount_ngram, long FNgramCount_ngram, long BVocabCount_ngram, long FVocabCount_ngram)
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
		protected double computeKLDivergence(double p_w, double q_w)
		{
			double KLDivergence = p_w*Math.log(p_w/q_w);
			return KLDivergence;
		}

		protected double computePhraseScore(double phraseness, double informativeness)
		{
			double alpha = (double)1/100000;
			double phraseScore = 0.0;
			//phraseScore = (double)phraseness/(phraseness + alpha) + informativeness;
			phraseScore = phraseness + informativeness;

			return phraseScore;
		}

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();
			String result = parseNgramInfo(line);
			
			if(result!=null)
			{
				context.write(new Text(result), new Text(""));
			}
		}
	}

	public static class ReduceTest extends Reducer<Text, Text, Text, Text>
	{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			
		}
	}

	public static void main(String[] args) throws Exception
	{
		Configuration conf1 = new Configuration();

		try
		{
			Path path1 = new Path(args[2]); // globalPhraseCounts
			FileSystem fs = FileSystem.get(new Path(args[2]).toUri(), new Configuration());

			FileStatus[] status = fs.listStatus(new Path(args[2]));

			for (int i=0;i<status.length;i++)
			{
				if(status[i].getPath().toUri().toString().contains("part-r-") &&  !status[i].getPath().toUri().toString().contains("log") && !status[i].getPath().toUri().toString().contains("SUCCESS"))
				{
					System.out.println((status[i].getPath()).toUri());
					DistributedCache.addCacheFile( (status[i].getPath()).toUri(), conf1);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("File not found");
		}

		Job job = new Job(conf1, "classifydocs");

		FileInputFormat.addInputPath(job, new Path(args[0])); // merged counts file
		FileOutputFormat.setOutputPath(job, new Path(args[1])); // output directory

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setMapperClass(MapTest.class);
		//job.setReducerClass(ReduceTest.class);

		job.setJarByClass(MapReducePCTest.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		job.waitForCompletion(true);

	}
}
