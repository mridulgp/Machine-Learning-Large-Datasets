package mlld.naivebayes;

public class Token
{
	public Token(String word, String docLabel)
	{
		this.word = word;
		this.docLabel = docLabel;
	}
	
	protected String word;
	protected String docLabel;
	
	public String getWord()
	{
		return word;
	}
	
	public String getDocLabel()
	{
		return docLabel;
	}
}
