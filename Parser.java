import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Parser 
{
	private Scanner fileScan;
	private String currentLine;
	private String[] LOGARITH = {"add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"};
	private String cType;
	private String arg1;
	private int arg2;
	
	// Constructor takes input file and creates a scanner for it
	Parser(File inFile) throws FileNotFoundException 
	{
		fileScan = new Scanner(inFile);
		currentLine = "";
		cType = "";
		arg1 = "";
		arg2 = -1;
		
	}

	// Checks to see if there are more lines of code in input file
	public boolean hasMoreCommands()
	{
		return fileScan.hasNextLine();
	}
	
	// Grabs next line until a valid instruction or EOF and parses it into sections
	public void advance()
	{
		// Stores next line of the file sans comments and extra spaces on the ends
		currentLine = removeComments(fileScan.nextLine()).trim();
		
		// continues grabbing next line until contains more than whitespace and/or comments
		while (currentLine.compareTo("") == 0 && fileScan.hasNext())
			currentLine = removeComments(fileScan.nextLine()).trim();
		
		// Splits the instruction into fields separated by whitespace
		String[] fields = currentLine.split(" ");
		
		// if not an arithmetic instruction, stores fields[1] in arg1
		if (fields.length > 1)
			arg1 = fields[1];
		// if a push or pop instruction, stores the index in arg2
		if (fields.length > 2)
			arg2 = Integer.parseInt(fields[2]);
		
		// converts VM instructions for use from Translator
		if (fields[0].compareTo("push") == 0)
			cType = "C_PUSH";
		else if (fields[0].compareTo("pop") == 0)
			cType = "C_POP";
		else if (fields[0].compareTo("if-goto") == 0)
			cType = "C_IF";
		else if (fields[0].compareTo("goto") == 0)
			cType = "C_GOTO";
		else if (fields[0].compareTo("function") == 0)
			cType = "C_FUNCTION";
		else if (fields[0].compareTo("call") == 0)
			cType = "C_CALL";
		else if (fields[0].compareTo("return") == 0)
			cType = "C_RETURN";
		else if (fields[0].compareTo("label") == 0)
			cType = "C_LABEL";
		else
		{
			for (String s : LOGARITH)
			{
				if (fields[0].compareTo(s) == 0)
				{
					cType = "C_ARITHMETIC";
					arg1 = fields[0];
					break;
				}
			}
		}
		
	}
	
	// returns the type of vm command of the current instruction
	public String commandType()
	{
		return cType;
	}
	
	// returns the first argument of the current instruction
	public String arg1()
	{
		return arg1;
	}
	
	// returns the second argument of the current instruction 
	public int arg2()
	{
		return arg2;
	}
	
	// returns a string without comments
	private String removeComments(String s)
	{
		int pos = s.indexOf("//");
		if (pos >= 0)
			s = s.substring(0, pos);
		
		return s;
		
	}
	
}
