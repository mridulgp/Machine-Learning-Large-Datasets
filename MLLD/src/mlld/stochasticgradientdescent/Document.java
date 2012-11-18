//package mlld.stochasticgradientdescent;

import java.util.Vector;

public class Document {

	public Document()
	{
		tokens = new Vector<String>();
		docLabels = new Vector<String>();
	}
	
	public Document(Vector<String> tokens, Vector<String> docLabels)
	{
		this.tokens = tokens;
		this.docLabels = docLabels;
	}
	
	protected Vector<String> tokens;
	protected Vector<String> docLabels;
	
	public void setTokenList(Vector<String> tokens)
	{
		this.tokens = tokens; 
	}
	
	public void setDocLabels (String docLabel)
	{
		docLabels.add(docLabel);
	}
	
	public Vector<String> getTokenList()
	{
		return tokens;
	}
	
	public Vector<String> getDocLabels()
	{
		return docLabels;
	}
}
