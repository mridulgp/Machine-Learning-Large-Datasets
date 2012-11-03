package mlld.naivebayes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;

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

public class MapReduceNBTest
{
	public static enum globalCounters { setupCounter_1 };

	protected static Vector<String> tokenizeDoc(String currentDoc)
	{
		String[] words = currentDoc.split("\\s+");
		Vector<String> tokens = new Vector<String>();

		for (int i=0; i<words.length; i++)
		{
			words[i] = words[i].replaceAll("\\W", "");

			if (words[i].length() > 0)
			{
				tokens.add(words[i]);
			}
		}
		return tokens;
	}

	protected static Document read(String line) throws IOException
	{
		Document doc = new Document();

		String[] docInfo = line.split("\\t");
		if(docInfo.length == 2)
		{
			Vector<String> tokens = tokenizeDoc(docInfo[1]);
			doc.setTokenList(tokens);
			String[] docLabels = docInfo[0].split("\\,");

			for (int i=0; i<docLabels.length; i++)
			{
				/*if(docLabels[i].endsWith("CAT"))
					docLabel = docLabels[i];*/
				if(docLabels[i] != null)
				{
					doc.setDocLabels(docLabels[i]);
				}
			}
		}
		/*else
		{
			System.err.println("Document is not in the required format!");
			//System.exit(0);
		}*/

		return doc;
	}

	public static class MapRequest extends Mapper<LongWritable, Text, Text, Text>
	{
		private Text word = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();

			if(!line.startsWith("A_vocabCount"))
			{
				if(line.split("\t")[0].split("=").length==1)
				{
					Document doc = read(line);
					Vector<String> labelList = doc.getDocLabels();
					Vector<String> tokens = doc.getTokenList();
					String newKey = "a_ctr_to_id=";
					newKey = newKey + key.toString();

					for (int j=0; j<tokens.size(); j++)
					{
						word.set("W=" + tokens.get(j));
						context.write(word, new Text(newKey));
					}
				}
				else if(line.split("\t")[0].split("=").length>1)
				{
					String[] wordEventInfo = line.split("\t");

					word.set(wordEventInfo[0]);
					context.write(word, new Text(wordEventInfo[1]));
				}
			}
		}
	}

	public static class ReduceRequest extends Reducer<Text, Text, Text, Text>
	{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			HashMap<String, List<String> > eventCounter = new HashMap<String, List<String> > ();
			List<String> valueList = new ArrayList<String>();
			String hashKey = key.toString();

			int flag=0;

			for (Text val : values)
			{
				String valInfo = val.toString();

				if (!valInfo.startsWith("a_ctr_to_id="))
				{
					flag=1;
				}

				if(eventCounter.get(hashKey)==null && hashKey!=null)
				{
					valueList.add(valInfo);
					eventCounter.put(hashKey, valueList);
				}

				else if (hashKey!=null && eventCounter.get(hashKey)!=null)
				{
					valueList = eventCounter.get(hashKey);
					valueList.add(valInfo);
					eventCounter.put(hashKey, valueList);
				}
			}

			valueList = eventCounter.get(hashKey);
			String newValue1 = "";
			String newValue2 = "";

			if (valueList.size()>1 && flag==1)
			{
				for (String eventInfo : valueList)
				{
					if (eventInfo.startsWith("a_ctr_to_id="))
						newValue1 = newValue1 + eventInfo + " ";
					else
						newValue2 = newValue2 + eventInfo + " ";
				}
				newValue1 = newValue1.trim();
				newValue2 = newValue2.trim();

				context.write(key, new Text(newValue1 + "\t" + newValue2));
				flag=0;
			}
		}
	}

	public static class MapAnswers extends Mapper<LongWritable, Text, Text, Text>
	{
		private Text word = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();

			if(!line.startsWith("A_vocabCount"))
			{
				if(line.split("\t")[0].split("=").length==1)
				{
					String[] docInfo = line.split("\t", 2);
					Document doc = read(line);
					Vector<String> labelList = doc.getDocLabels();
					Vector<String> tokens = doc.getTokenList();
					String newKey = "a_ctr_to_id=";
					newKey = newKey + key.toString();

					String labels = docInfo[0];
					String newValue = "DOC=" + labels + " ";

					for (int j=0; j<tokens.size(); j++)
					{
						newValue = newValue + tokens.get(j) + " ";
					}
					newValue = newValue.trim();
					word.set(newValue);
					context.write(new Text(newKey), word);
				}

				else if(line.split("\t")[0].split("=").length>1)
				{
					String[] wordEventInfo = line.split("\t");
					String[] docInfo = wordEventInfo[1].split("\\s+");

					if(wordEventInfo.length>=3)
					{
						for (int i=0; i<docInfo.length; i++)
						{
							if(docInfo[i].startsWith("a_ctr_to_id="))
							{
								String docID = docInfo[i];
								String newKey = wordEventInfo[2];
								context.write(new Text(docID), new Text(newKey));
							}
						}
					}
				}
			}
		}
	}

	public static class ReduceAnswers extends Reducer<Text, Text, Text, Text>
	{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			String newValue = "";
			for (Text val : values)
			{
				newValue = newValue + val.toString() + "\t";
			}
			newValue = newValue.trim();
			context.write(key, new Text(newValue));
		}
	}

	public static class MapClassify extends Mapper<LongWritable, Text, Text, Text>
	{
		private Text word = new Text();
		private HashMap<String, Long> globalEventCounter = new HashMap<String, Long>();
		protected long labelSize;
		protected long vocabCount;
		protected long totalDocs=0;
		protected HashMap<String, Long> neededHash;
		protected static HashSet<String> labelSet = new HashSet<String>();

		private void loadAllGlobalCounts(Path cachePath) throws IOException
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

						String[] labelList = globalEventCountInfo[0].split("\\^", 2);
						if(labelList.length==1 && (!labelList[0].equals("A_vocabCount")) )
						{
							labelSet.add(labelList[0]);
							totalDocs = totalDocs + count;
						}
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
						loadAllGlobalCounts(cachePath);
					}
				}
			}
			catch (IOException e)
			{
				System.err.println("IOException reading from distributed cache");
				System.err.println(e.toString());
			}

		}

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();

			/*labelSize = context.getCounter(globalCounters.labelSize).getValue();
			vocabCount = context.getCounter(globalCounters.vocabCount).getValue();
			totalDocs = context.getCounter(globalCounters.totalDocs).getValue();*/

			vocabCount = globalEventCounter.get("A_vocabCount");

			neededHash = new HashMap<String, Long>();

			String[] testDocInfo = line.split("\t");
			String doc = "";
			String docLabels = "";

			for (int i=0; i<testDocInfo.length; i++)
			{
				if(!testDocInfo[i].startsWith("a_ctr_to_id="))
				{
					if (testDocInfo[i].startsWith("DOC="))
					{
						doc = testDocInfo[i].replaceFirst("DOC=", "");
					}
					else if (testDocInfo[i].startsWith("C="))
					{
						String[] docCountInfo = testDocInfo[i].split("\\,");
						for (int j=0; j<docCountInfo.length; j++)
						{
							String[] tempDoc = docCountInfo[j].split("\\s+");
							long docCounts = Long.parseLong(tempDoc[1]);
							neededHash.put(tempDoc[0], docCounts);
						}
					}

					else if (!testDocInfo[i].startsWith("C=") && !testDocInfo[i].startsWith("DOC="))
					{
						docLabels = testDocInfo[i];
					}
				}
			}

			String docContent = doc.replaceFirst("\\s+", "\t");
			Document document = read(docContent);

			String result = classifyDocument(document);

			context.write(new Text("docID:" + key.toString() + "__" + result), new Text(""));
		}

		private String classifyDocument(Document doc)
		{
			Vector<String> tokenList = doc.getTokenList();

			long V = vocabCount;
			System.out.println("vocabCount = " + vocabCount);
			double alpha = 1.0;
			double q_j = (double)(1.0/(double) V);

			long anyLabelCount = totalDocs;

			System.out.println("totalDocs = " + totalDocs);

			labelSize = labelSet.size();

			System.out.println("labelSize = " + labelSize);

			double q_y = (double) (1.0/(double) labelSize);

			long anyLabelTokenCount=0;

			Map<String, Double> logProbLabel = new HashMap<String, Double>();

			Iterator<String> iterator = labelSet.iterator();
			double maxLogProb = -10000000.0;
			String bestLabelKey = "";

			while (iterator.hasNext())
			{
				String label = iterator.next().toString();
				String anyLabelToken = label + "^W=*";

				anyLabelTokenCount = globalEventCounter.get(anyLabelToken);
				long jointCount=0;
				long labelValue=0;

				System.out.println(anyLabelToken + " = " + anyLabelTokenCount);

				String labelKey = label;
				if(globalEventCounter.get(labelKey)!=null)
				{
					labelValue = globalEventCounter.get(labelKey);
				}
				else
				{
					labelValue = 0;
				}

				double logProbFirstTerm = 0.0;
				//double logProbSecondTerm = Math.log((labelValue + alpha*q_y)/(anyLabelCount + alpha));
				double logProbSecondTerm = Math.log((double)(labelValue + alpha)/(double)(anyLabelCount + labelSize));


				for(int i=0; i<tokenList.size(); i++)
				{
					String token = tokenList.get(i);
					String key = label + "^" + "W=" + token;

					if(neededHash.get(key)!=null)
					{
						jointCount = neededHash.get(key);
						System.out.println(key + " = " + jointCount);

					}
					else
					{
						jointCount = 0;
					}
					//logProbFirstTerm = logProbFirstTerm + Math.log((jointCount + alpha*q_j)/(anyLabelTokenCount + alpha));
					logProbFirstTerm = logProbFirstTerm + Math.log((double)(jointCount + alpha)/(double)(anyLabelTokenCount + vocabCount));

				}
				double logProb = logProbFirstTerm + logProbSecondTerm;
				//System.out.println("label:"+label+":"+logProb);
				if(maxLogProb < logProb)
				{
					maxLogProb = logProb;
					bestLabelKey = labelKey;
				}
				logProbLabel.put(labelKey,logProb);
			}

			Vector<String>listOfLabels = doc.getDocLabels();
			bestLabelKey = bestLabelKey.replaceFirst("C=", "");

			System.out.println(listOfLabels + "\t" + bestLabelKey + "\t" + maxLogProb);
			String result = listOfLabels.toString() + "__" + bestLabelKey + "__" + String.valueOf(maxLogProb);

			return result;

			/*if(listOfLabels.contains(bestLabelKey))
				return true;
			else
				return false;*/
		}
	}

	public static class ReduceClassify extends Reducer<Text, Text, Text, Text>
	{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			String[] resultInfo = key.toString().trim().split("__");

			System.out.println("trueLabels = " + resultInfo[1]);
			System.out.println("predictedLabel = " + resultInfo[2]);

			String[] trueLabels = resultInfo[1].replaceAll("\\[", "").replaceAll("\\]", "").split("\\,\\s+");
			String predictedLabel = resultInfo[2];

			int flag=0;

			for (int i=0; i<trueLabels.length; i++)
			{
				if (predictedLabel.equals(trueLabels[i]))
				{
					flag=1;
					break;
				}

			}

			if(flag==1)
			{
				flag=0;
				context.write(new Text("Accuracy"), new Text("1"));
				//context.write(new Text("~TotalDocs"), new Text("1"));
			}
			else if (flag==0)
			{
				context.write(new Text("Accuracy"), new Text("0"));
			}

			context.write(new Text(key.toString().replaceAll("\t", "__")), new Text(""));
		}
	}

	public static class MapAccuracy extends Mapper<LongWritable, Text, Text, Text>
	{
		private Text word = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();
			String[] resultInfo = line.split("\t");
			if (resultInfo.length==1)
			{
				context.write(new Text(resultInfo[0]), new Text(""));
			}
			else
			{
				context.write(new Text(resultInfo[0]), new Text(resultInfo[1]));
			}
		}
	}

	public static class ReduceAccuracy extends Reducer<Text, Text, Text, Text>
	{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			String newValue = "";
			long correct=0;
			long incorrect=0;

			for (Text val : values)
			{
				if(key.toString().contains("Accuracy") && val.toString().equals("1"))
				{
					correct = correct + 1;
				}
				else if(key.toString().contains("Accuracy") && val.toString().equals("0"))
				{
					incorrect = incorrect + 1;
				}
			}

			if (key.toString().contains("Accuracy"))
			{
				double acc = (double) (correct*1.0/(double)(correct+incorrect))*100;
				context.write(new Text("Accuracy:"), new Text("(" + correct + "/" + incorrect + ")" + "\t" + acc + "%"));
			}
			else if (!key.toString().contains("Accuracy"))
			{
				newValue = key.toString().replaceAll("__", "\t");
				context.write(new Text(newValue.split("\t", 2)[1]), new Text("") );
			}
		}
	}
	
	public static class MapFinalAccuracy extends Mapper<LongWritable, Text, Text, Text>
	{
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();
			if(line.contains("Accuracy") || line.contains("accuracy"))
				context.write(new Text(line), new Text(""));
		}
	}
	
	public static class ReduceFinalAccuracy extends Reducer<Text, Text, Text, Text>
	{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			for (Text val : values)
			{
				if (key.toString().contains("Accuracy") || key.toString().contains("accuracy"))
					context.write(new Text(key.toString()), new Text(""));	
			}
		}
	}

	public static void main(String[] args) throws Exception
	{
		Configuration conf = new Configuration();

		Job job = new Job(conf, "requestcounts");

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setMapperClass(MapRequest.class);
		job.setReducerClass(ReduceRequest.class);

		job.setJarByClass(MapReduceNBTest.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPaths(job, args[0] + "," + args[1]); // test + model file
		FileOutputFormat.setOutputPath(job, new Path(args[2])); // model-requests file

		job.waitForCompletion(true);

		Job job1 = new Job(conf, "recordcounts");

		FileInputFormat.addInputPaths(job1, args[0] + "," + args[2]); // test + model-requests file
		FileOutputFormat.setOutputPath(job1, new Path(args[3])); // model-answer file

		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(Text.class);

		job1.setMapperClass(MapAnswers.class);
		job1.setReducerClass(ReduceAnswers.class);

		job1.setJarByClass(MapReduceNBTest.class);

		job1.setInputFormatClass(TextInputFormat.class);
		job1.setOutputFormatClass(TextOutputFormat.class);

		job1.waitForCompletion(true);

		Path path1 = new Path(args[6]); // cachedGlobalCounts
		Path path2 = new Path(args[7]); // cachedLabelCounts

		/*DistributedCache.addCacheFile(path1.toUri(), conf);
		DistributedCache.addCacheFile(path2.toUri(), conf);*/

		Configuration conf1 = new Configuration();
		String filename = "UNKNOWNFILE";

		try
		{
			filename = new Path(args[6]).toUri().toString();

			FileSystem fs = FileSystem.get(new Path(args[6]).toUri(), new Configuration());
			FileStatus[] status = fs.listStatus(new Path(args[6]));
			
			for (int i=0; i<status.length; i++)
			{
				if(status[i].getPath().toUri().toString().contains("part-r-") &&  !status[i].getPath().toUri().toString().contains("log") && !status[i].getPath().toUri().toString().contains("SUCCESS"))
				{
					System.out.println((status[i].getPath()).toUri());
					//DistributedCache.addCacheFile( (status[i].getPath()).toUri(), conf1);
					DistributedCache.addCacheFile( (status[i].getPath()).toUri(), conf1);
				}
			}

			filename = new Path(args[7]).toUri().toString();
			
			fs = FileSystem.get(new Path(args[7]).toUri(), new Configuration());
			FileStatus[] status1 = fs.listStatus(new Path(args[7]));
			
			for (int i=0;i<status1.length;i++)
			{
				if(status1[i].getPath().toUri().toString().contains("part-r-") &&  !status1[i].getPath().toUri().toString().contains("log") && !status1[i].getPath().toUri().toString().contains("SUCCESS"))
				{
					System.out.println((status1[i].getPath()).toUri());
					DistributedCache.addCacheFile( (status1[i].getPath()).toUri(), conf1);
					//DistributedCache.addCacheFile( (status1[i].getPath()).toUri(), job2.getConfiguration());
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("File: " + filename + "not found");
		}

		Job job2 = new Job(conf1, "classifydocs");

		FileInputFormat.addInputPath(job2, new Path(args[3])); // model-answer file
		FileOutputFormat.setOutputPath(job2, new Path(args[4])); // output directory

		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(Text.class);

		job2.setMapperClass(MapClassify.class);
		job2.setReducerClass(ReduceClassify.class);

		job2.setJarByClass(MapReduceNBTest.class);

		job2.setInputFormatClass(TextInputFormat.class);
		job2.setOutputFormatClass(TextOutputFormat.class);

		job2.waitForCompletion(true);

		Job job3 = new Job(conf1, "accuracydocs");

		FileInputFormat.addInputPath(job3, new Path(args[4])); // output directory
		FileOutputFormat.setOutputPath(job3, new Path(args[5])); // output + accuracy file

		job3.setOutputKeyClass(Text.class);
		job3.setOutputValueClass(Text.class);

		job3.setMapperClass(MapAccuracy.class);
		job3.setReducerClass(ReduceAccuracy.class);

		job3.setJarByClass(MapReduceNBTest.class);

		job3.setInputFormatClass(TextInputFormat.class);
		job3.setOutputFormatClass(TextOutputFormat.class);

		job3.waitForCompletion(true);
		
		Job job4 = new Job(conf1, "accuracyfinal");

		FileInputFormat.addInputPath(job4, new Path(args[5])); // output + accuracy file
		FileOutputFormat.setOutputPath(job4, new Path(args[8])); // final accuracy file

		job4.setOutputKeyClass(Text.class);
		job4.setOutputValueClass(Text.class);

		job4.setMapperClass(MapFinalAccuracy.class);
		job4.setReducerClass(ReduceAccuracy.class);

		job4.setJarByClass(MapReduceNBTest.class);

		job4.setInputFormatClass(TextInputFormat.class);
		job4.setOutputFormatClass(TextOutputFormat.class);

		job4.waitForCompletion(true);

	}
}