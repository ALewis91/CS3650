import java.util.HashMap;

public class SymbolTable 							// Hashmap for symbols and their addresses
{
	private HashMap<String, Integer> table;					// declare hashmap variable table
	
	public SymbolTable()							// constructor
	{
		table = new HashMap<String, Integer>();				// initializes table
	}
	
	public void addEntry(String symbol, Integer address)			// adds a new symbol and address to table
	{
		table.put(symbol, address);
	}
	
	public boolean contains(String symbol)					// returns true if the symbol is in the table
	{
		return table.get(symbol) != null;
	}
	
	public Integer getAddress(String symbol)				// returns the address of the symbol
	{
		return table.get(symbol);
	}
}
