
public class Symbol 
{
	private String type;
	private String kind;
	private int index;
	
	Symbol(String symType, String symKind, int symIndex)
	{
		type = symType;
		kind = symKind;
		index = symIndex;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getKind()
	{
		return kind;
	}
	
	public int getIndex()
	{
		return index;
	}
}
