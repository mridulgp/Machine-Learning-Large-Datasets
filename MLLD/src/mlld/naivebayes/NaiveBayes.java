package mlld.naivebayes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public abstract class NaiveBayes
{
	public NaiveBayes()
	{
		labelCount = 0;
		labelSet = new HashSet<String>();
	}
	public NaiveBayes(List<Document> docs)
	{
		this.docs = docs;
		labelCount = 0;
	}
	
	public int getLabelCount()
	{
		return labelCount;
	}
	
	public List<Document> getDocsList()
	{
		return docs;
	}
	
	public Token getToken()
	{
		return token;
	}
	
	public void setLabelCount(int labelCount)
	{
		this.labelCount = labelCount;
	}
	
	public void setDocsList(List<Document> docs)
	{
		this.docs = docs;
	}
	
	public void setToken(Token token)
	{
		this.token = token;
	}
	public abstract void read();
	protected abstract Document read(BufferedReader inReader) throws IOException;
	public abstract void train();
	public abstract void test();
	
	public abstract Map<String, Integer> train(BufferedReader inReader) throws IOException;
	public abstract Map<String, Integer> train(BufferedReader inReader, BufferedWriter out) throws IOException;
	
	public abstract boolean test(BufferedReader inReader, BufferedReader inReader1) throws IOException;
	public abstract void test(BufferedReader inReader, int count) throws IOException;
	
	public abstract void printEventCounts();
	public abstract void printEventCountsToFile(File file) throws IOException;
	
	protected List<Document> docs;
	protected Token token;
	protected int labelCount;
	protected Set<String> labelSet;
	
}