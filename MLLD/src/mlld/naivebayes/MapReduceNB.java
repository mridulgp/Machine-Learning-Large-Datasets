package mlld.naivebayes;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class MapReduceNB
{
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

	public static enum globalCounters { vocabCount, totalDocs, labelSize };

	public static class MapCount extends Mapper<LongWritable, Text, Text, Text>
	{
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();

			Document doc = read(line);
			//StringTokenizer tokenizer = new StringTokenizer(line);
			Vector<String> labelList = doc.getDocLabels();
			Vector<String> tokens = doc.getTokenList();
			String label = "";

			for(int i=0; i<labelList.size(); i++)
			{
				context.getCounter(globalCounters.totalDocs).increment(1);
				int labelSize=0;
				label = "C=" + labelList.get(i);

				/*word.set(anyLabel);
				context.write(word, one);

				word.set(label);
				context.write(word, one);*/

				for (int j=0; j<tokens.size(); j++)
				{
					String labelWord = label + "^" + "W=" + tokens.get(j);
					word.set("W=" + tokens.get(j));
					context.write(word, new Text(labelWord));
					labelSize++;
				}

				/*String perLabelWords = label + "^" + "W=*";
				word.set(perLabelWords);
				context.write(word, new IntWritable(labelSize));*/
			}
		}
	}

	public static class ReduceCount extends Reducer<Text, Text, Text, Text>
	{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			String newKey = "";
			HashMap<String, Long> eventCounter = new HashMap<String, Long>();

			for (Text val : values)
			{
				if(eventCounter.get(val.toString())==null)
				{
					eventCounter.put(val.toString(), (long)1);
				}
				else
				{
					long eventCount = eventCounter.get(val.toString());
					eventCount++;
					eventCounter.put(val.toString(), eventCount);
				}
				//newKey = newKey + "," + val.toString();
			}
			//newKey = newKey.replaceFirst(",", "");
			//String newKey = key.toString().replaceAll("*", "#");
			//key = new Text(newKey);
			Iterator it = eventCounter.entrySet().iterator();
			String hashPair = "";

			while (it.hasNext())
			{
				Map.Entry<String, Long> pairs = (Map.Entry)it.next();
				hashPair = hashPair + pairs.getKey().toString() + " " + pairs.getValue().toString() + ",";
				//it.remove(); // avoids a ConcurrentModificationException
			}

			context.write(key, new Text(hashPair.substring(0, hashPair.length()-1)));
			context.getCounter(globalCounters.vocabCount).increment(1);
			context.write(new Text("A_vocabCount"), new Text("1"));
		}
	}

	public static class MapGlobalCounts extends Mapper<LongWritable, Text, Text, LongWritable>
	{
		private Text word = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();
			String[] eventInfo = line.split("\t");
			if(!eventInfo[0].equals("A_vocabCount"))
			{
				String[] labelWordInfo = eventInfo[1].split("\\,");

				for (int i=0; i<labelWordInfo.length; i++)
				{
					String[] labelWords = labelWordInfo[i].split("\\s+");
					long anyLabelWordCount = Long.parseLong(labelWords[1]);
					String[] labelInfo = labelWords[0].split("\\^", 2);
					word.set(labelInfo[0] + "^" + "W=*");
					context.write(word, new LongWritable(anyLabelWordCount));
				}
			}
			else
			{
				context.write(new Text("A_vocabCount"), new LongWritable(1));
			}
		}
	}

	public static class ReduceGlobalCounts extends Reducer<Text, LongWritable, Text, LongWritable>
	{
		/*public void configure(Configuration conf)
		{
		    Job job = new Job(conf);
		    RunningJob parentJob = client.getJob(JobID.forName( conf.get("mapred.job.id") ));
		    mapperCounter = parentJob.getCounters().getCounter(MAP_COUNTER_NAME);
		}*/

		public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException
		{
			long sum=0;
			for (LongWritable val : values)
			{
				sum = sum + val.get();
			}

			context.write(key, new LongWritable(sum));
			context.getCounter(globalCounters.labelSize).increment(1);

			//long vocabCount1 = context.getCounter(globalCounters.vocabCount).getValue();
		}
	}

	public static class MapDocLabelCounts extends Mapper<LongWritable, Text, Text, LongWritable>
	{
		private Text word = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();
			
			String[] tempGlobalCounts = line.split("\t");

			if(!line.startsWith("A_vocabCount"))
			{
				Document doc = read(line);
				Vector<String> labelList = doc.getDocLabels();
				String label = "";

				for(int i=0; i<labelList.size(); i++)
				{
					context.getCounter(globalCounters.totalDocs).increment(1);
					label = "C=" + labelList.get(i);
					word.set(label);
					context.write(word, new LongWritable(1));
				}
			}
			
			else if (line.startsWith("A_vocabCount"))
			{
				long vocabCount = Long.parseLong(tempGlobalCounts[1]);
				context.write(new Text("A_vocabCount"), new LongWritable(vocabCount));
			}
		}
	}

	public static class ReduceDocLabelCounts extends Reducer<Text, LongWritable, Text, LongWritable>
	{
		public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException
		{
			long sum=0;
			for (LongWritable val : values)
			{
				sum = sum + val.get();
			}
			context.write(key, new LongWritable(sum));

			//long labelSize1 = context.getCounter(globalCounters.labelSize).getValue();
			//long totalDocs1 = context.getCounter(globalCounters.totalDocs).getValue();

		}
	}

	public static void main(String[] args) throws Exception
	{
		Configuration conf = new Configuration();

		Job job = new Job(conf, "wordcount");

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setMapperClass(MapCount.class);
		job.setReducerClass(ReduceCount.class);

		job.setJarByClass(MapReduceNB.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0])); // input file
		FileOutputFormat.setOutputPath(job, new Path(args[1])); // outputNBTrain

		job.waitForCompletion(true);


		Job job1 = new Job(conf, "wordglobalcounts");

		FileInputFormat.addInputPath(job1, new Path(args[1])); // outputNBTrain
		FileOutputFormat.setOutputPath(job1, new Path(args[2])); // cachedGlobalCounts


		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(LongWritable.class);

		job1.setMapperClass(MapGlobalCounts.class);
		job1.setReducerClass(ReduceGlobalCounts.class);

		job1.setJarByClass(MapReduceNB.class);

		job1.setInputFormatClass(TextInputFormat.class);
		job1.setOutputFormatClass(TextOutputFormat.class);

		job1.waitForCompletion(true);


		Job job2 = new Job(conf, "wordlabelcounts");

		FileInputFormat.addInputPath(job2, new Path(args[0])); // input file
		FileOutputFormat.setOutputPath(job2, new Path(args[3])); // cachedLabelCounts

		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(LongWritable.class);

		job2.setMapperClass(MapDocLabelCounts.class);
		job2.setReducerClass(ReduceDocLabelCounts.class);

		job2.setJarByClass(MapReduceNB.class);

		job2.setInputFormatClass(TextInputFormat.class);
		job2.setOutputFormatClass(TextOutputFormat.class);

		job2.waitForCompletion(true);
	}
}
