package mlld.phrasecounting;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class MapReducePhraseCount
{
	public static enum globalCounters { vocabCount, totalDocs, labelSize };

	public static class MapCount extends Mapper<LongWritable, Text, Text, Text>
	{
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();

			String[] phraseInfo = line.split("\t");
			int year = Integer.parseInt(phraseInfo[3]);

			String corpusInfo = "";
			String newKey = "";

			if(year>=1970 && year<2000)
			{
				corpusInfo = "B";
			}
			else if (year<=1960)
			{
				corpusInfo = "C";
			}

			String xy = "";
			if(phraseInfo[2].split("\\s+").length==1)
			{
				xy = "x";
			}
			else if (phraseInfo[2].split("\\s+").length==2)
			{
				xy = "xy";
			}
			if(!corpusInfo.equals("") && !xy.equals(""))
			{
				newKey = phraseInfo[2];
				String newValue = corpusInfo + xy + "=" + phraseInfo[4];
				context.write(new Text(newKey), new Text(newValue));
			}
		}
	}

	public static class ReduceCount extends Reducer<Text, Text, Text, Text>
	{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			HashMap<String, Long> eventCounter = new HashMap<String, Long>();

			for (Text val : values)
			{
				String[] corpusValues = val.toString().split("=");

				if(eventCounter.get(key.toString() + "_" + corpusValues[0])==null)
				{
					long count = Long.parseLong(corpusValues[1]);
					eventCounter.put(key.toString() + "_" + corpusValues[0], count);
				}
				else
				{
					long eventCount = eventCounter.get(key.toString() + "_" + corpusValues[0]);
					eventCount = eventCount + Long.parseLong(corpusValues[1]);
					eventCounter.put(key.toString() + "_" + corpusValues[0], eventCount);
				}
			}
			Iterator it = eventCounter.entrySet().iterator();
			String hashPair = "";
			String newKey = key.toString();

			while (it.hasNext())
			{
				Map.Entry<String, Long> pairs = (Map.Entry)it.next();
				hashPair = hashPair + pairs.getKey().toString().split("_",2)[1] + "=" + pairs.getValue().toString() + ",";

				//it.remove(); // avoids a ConcurrentModificationException
			}

			context.write(new Text(newKey), new Text(hashPair.substring(0, hashPair.length()-1)));
		}
	}

	public static class MapMessages extends Mapper<LongWritable, Text, Text, Text>
	{
		private Text word = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();
			String[] eventInfo = line.split("\t");
			String newKey = "";

			if(!eventInfo[0].contains("_count") && !eventInfo[0].contains("_vocab"))
			{
				String[] ngram = eventInfo[0].split("\\s+");

				if(ngram.length==2)
				{
					context.write(new Text(eventInfo[0]), new Text(eventInfo[1]));
					context.write(new Text(ngram[0]), new Text("M=" + eventInfo[0]));
					context.write(new Text(ngram[1]), new Text("M=" + eventInfo[0]));
				}
				else if (ngram.length==1)
				{
					context.write(new Text(eventInfo[0]), new Text(eventInfo[1]));
				}
			}
			/*
			else
			{
				context.write(new Text("A_vocabCount"), new LongWritable(1));
			}*/
		}
	}

	public static class ReduceMessages extends Reducer<Text, Text, Text, Text>
	{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			String fields = "";
			String countField = "";
			for (Text val : values)
			{
				if(val.toString().startsWith("M="))
					fields = fields + val.toString() + ";";
				else if (!val.toString().startsWith("M="))
					countField = val.toString();
			}

			String newValue = "";
			if(!fields.equals(""))
			{
				fields = fields.substring(0, fields.length()-1);
				newValue = countField + ";" + fields;
			}
			else
				newValue = countField;

			context.write(key, new Text(newValue));
			context.getCounter(globalCounters.labelSize).increment(1);

			//long vocabCount1 = context.getCounter(globalCounters.vocabCount).getValue();
		}
	}

	public static class MapAnswers extends Mapper<LongWritable, Text, Text, Text>
	{
		private Text word = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();

			String[] eventInfo = line.split("\t");
			String[] ngram = eventInfo[0].split("\\s+");
			String corpusInfo = "";

			if(ngram.length==1)
			{
				String[] messageInfo = eventInfo[1].split("\\;");
				for (int i=0; i<messageInfo.length; i++)
				{
					if(messageInfo[i].startsWith("M="))
					{
						messageInfo[i] = messageInfo[i].replaceFirst("M=", "");
						String[] messageNgramInfo = messageInfo[i].split("\\s+");

						if(messageNgramInfo[0].equals(eventInfo[0]) && (!corpusInfo.equals("")) )
							context.write(new Text(messageInfo[i]), new Text(corpusInfo));

						else if(messageNgramInfo[1].equals(eventInfo[0]) && (!corpusInfo.equals("")) )
						{
							String newCorpusInfo = corpusInfo.replaceAll("x", "y");
							context.write(new Text(messageInfo[i]), new Text(newCorpusInfo));
						}
					}
					else
					{
						corpusInfo = messageInfo[i];
					}
				}
			}
			else if (ngram.length == 2)
			{
				context.write(new Text(eventInfo[0]), new Text(eventInfo[1]));
			}
		}
	}

	public static class ReduceAnswers extends Reducer<Text, Text, Text, Text>
	{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			String fields = "";

			for (Text val : values)
			{
				fields = fields + val.toString() + ",";
			}

			fields = fields.substring(0, fields.length()-1);
			String newValue = fields; 
			context.write(key, new Text(newValue));

			//context.getCounter(globalCounters.labelSize).increment(1);
			//long vocabCount1 = context.getCounter(globalCounters.vocabCount).getValue();
		}
	}

	public static class MapBigramCounts extends Mapper<LongWritable, Text, Text, Text>
	{
		private Text word = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();

			String[] eventInfo = line.split("\t");
			String[] ngram = eventInfo[0].split("\\s+");
			String corpusInfo = "";

			String[] messageInfo = eventInfo[1].split("\\,");
			for (int i=0; i<messageInfo.length; i++)
			{
				if(messageInfo[i].contains("xy="))
				{
					long Bxy=0, Cxy=0;

					String[] countInfo = messageInfo[i].split("=");
					if(countInfo[0].equals("Bxy"))
					{
						Bxy = Long.parseLong(countInfo[1]);
						context.write(new Text("Bxy_count"), new Text(String.valueOf(Bxy)) );
						context.write(new Text("Bxy_vocab"), new Text("1"));

					}
					else if (countInfo[0].equals("Cxy"))
					{
						Cxy = Long.parseLong(countInfo[1]);
						context.write(new Text("Cxy_count"), new Text(String.valueOf(Cxy)) );
						context.write(new Text("Cxy_vocab"), new Text("1"));
					}
				}

				else if(messageInfo[i].contains("x=") || messageInfo[i].contains("y="))
				{
					String[] countInfo = messageInfo[i].split("=");
					long Bx=0, Cx=0, By=0, Cy=0;

					if(countInfo[0].equals("Bx"))
					{
						Bx = Long.parseLong(countInfo[1]);
						context.write(new Text("W=" + ngram[0]), new Text("B=" + String.valueOf(Bx)) );
					}
					if(countInfo[0].equals("Cx"))
					{
						Cx = Long.parseLong(countInfo[1]);
						context.write(new Text("W=" + ngram[0]), new Text("C=" + String.valueOf(Cx)) );
					}
					if(countInfo[0].equals("By"))
					{
						By = Long.parseLong(countInfo[1]);
						context.write(new Text("W=" + ngram[1]), new Text("B=" + String.valueOf(By)) );
					}
					if(countInfo[0].equals("Cy"))
					{
						Cy = Long.parseLong(countInfo[1]);
						context.write(new Text("W=" + ngram[1]), new Text("C=" + String.valueOf(Cy)) );
					}
				}
			}
		}
	}

	public static class ReduceBigramCounts extends Reducer<Text, Text, Text, Text>
	{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			String fields1 = "";
			String fields2 = "";

			long bigramCount=0;

			if(!key.toString().startsWith("W="))
			{
				for (Text val : values)
				{
					bigramCount = bigramCount + Long.parseLong(val.toString());
				}
				context.write(key, new Text(String.valueOf(bigramCount)));
			}

			else if (key.toString().startsWith("W="))
			{
				for (Text val : values)
				{
					if(val.toString().contains("B="))
					{
						if(!fields1.contains("B="))
							fields1 = fields1 + val.toString() + ",";
					}

					else if(val.toString().contains("C="))
					{
						if (!fields2.contains("C="))
							fields2 = fields2 + val.toString() + ",";
					}
				}

				String fields = fields1 + fields2;;
				String newValue = fields.substring(0, fields.length()-1);
				context.write(key, new Text(newValue));
			}
		}
	}

	public static class MapUnigramCounts extends Mapper<LongWritable, Text, Text, Text>
	{
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();
			String[] eventInfo = line.split("\t");

			if(eventInfo[0].startsWith("W="))
			{
				String[] messageInfo = eventInfo[1].split("\\,");
				for (int i=0; i<messageInfo.length; i++)
				{
					long Bx=0, Cx=0;

					String[] countInfo = messageInfo[i].split("=");

					if(countInfo[0].equals("B"))
					{
						Bx = Long.parseLong(countInfo[1]);
						context.write(new Text("Bx_count"), new Text(String.valueOf(Bx)) );
						context.write(new Text("Bx_vocab"), new Text("1"));
					}

					if(countInfo[0].equals("C"))
					{
						Cx = Long.parseLong(countInfo[1]);
						context.write(new Text("Cx_count"), new Text(String.valueOf(Cx)) );
						context.write(new Text("Cx_vocab"), new Text("1"));
					}
				}
			}

			else if (!eventInfo[0].startsWith("W="))
			{
				context.write(new Text(eventInfo[0]), new Text(eventInfo[1]));
			}
		}
	}

	public static class ReduceUnigramCounts extends Reducer<Text, Text, Text, Text>
	{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			long unigramCount=0;

			for (Text val : values)
			{
				unigramCount = unigramCount + Long.parseLong(val.toString());
			}
			context.write(key, new Text(String.valueOf(unigramCount)));
		}
	}


	public static void main(String[] args) throws Exception
	{
		Configuration conf = new Configuration();

		Job job = new Job(conf, "phrasecount");

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setMapperClass(MapCount.class);
		job.setReducerClass(ReduceCount.class);

		job.setJarByClass(MapReducePhraseCount.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPaths(job, args[0] + "," + args[1]); // corpus files
		FileOutputFormat.setOutputPath(job, new Path(args[2])); // unigrams + bigrams file

		job.waitForCompletion(true);


		Job job1 = new Job(conf, "phrasemessages");

		FileInputFormat.addInputPath(job1, new Path(args[2])); // unigrams + bigrams file
		FileOutputFormat.setOutputPath(job1, new Path(args[3])); // messages file

		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(Text.class);

		job1.setMapperClass(MapMessages.class);
		job1.setReducerClass(ReduceMessages.class);

		job1.setJarByClass(MapReducePhraseCount.class);

		job1.setInputFormatClass(TextInputFormat.class);
		job1.setOutputFormatClass(TextOutputFormat.class);

		job1.waitForCompletion(true);


		Job job2 = new Job(conf, "phraseanswers");

		FileInputFormat.addInputPath(job2, new Path(args[3])); // messages file
		FileOutputFormat.setOutputPath(job2, new Path(args[4])); // merged counts file

		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(Text.class);

		job2.setMapperClass(MapAnswers.class);
		job2.setReducerClass(ReduceAnswers.class);

		job2.setJarByClass(MapReducePhraseCount.class);

		job2.setInputFormatClass(TextInputFormat.class);
		job2.setOutputFormatClass(TextOutputFormat.class);

		job2.waitForCompletion(true);


		Job job3 = new Job(conf, "phrasebigramcounts");

		FileInputFormat.addInputPath(job3, new Path(args[4])); // merged counts file
		FileOutputFormat.setOutputPath(job3, new Path(args[5])); // bigram counts file -- tempPhraseBigramCounts 

		job3.setOutputKeyClass(Text.class);
		job3.setOutputValueClass(Text.class);

		job3.setMapperClass(MapBigramCounts.class);
		job3.setReducerClass(ReduceBigramCounts.class);

		job3.setJarByClass(MapReducePhraseCount.class);

		job3.setInputFormatClass(TextInputFormat.class);
		job3.setOutputFormatClass(TextOutputFormat.class);

		job3.waitForCompletion(true);


		Job job4 = new Job(conf, "phraseunigramcounts");

		FileInputFormat.addInputPath(job4, new Path(args[5])); // bigram counts file -- tempPhraseBigramCounts
		FileOutputFormat.setOutputPath(job4, new Path(args[6])); // unigram + bigram global counts file -- globalPhraseCounts 

		job4.setOutputKeyClass(Text.class);
		job4.setOutputValueClass(Text.class);

		job4.setMapperClass(MapUnigramCounts.class);
		job4.setReducerClass(ReduceUnigramCounts.class);

		job4.setJarByClass(MapReducePhraseCount.class);

		job4.setInputFormatClass(TextInputFormat.class);
		job4.setOutputFormatClass(TextOutputFormat.class);

		job4.waitForCompletion(true);
	}
}
