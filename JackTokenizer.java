import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class JackTokenizer 
{

	private ArrayList<String> tokens;
	private ArrayList<String> keyWords;
	private String symbols;
	private String jack;
	private String nTokenType;
	private String nKeyword;
	private char nSymbol;
	private String nIdentifier;
	private String nStrVal;
	private int nIntVal;
	private int position;
	//Types of terminal elements
	
	
	JackTokenizer(File inFile) throws FileNotFoundException
	{
		String currentLine;
		keyWords = new ArrayList<>();
		keyWords.add("class");
		keyWords.add("constructor");
		keyWords.add("function");
		keyWords.add("method");
		keyWords.add("field");
		keyWords.add("static");
		keyWords.add("boolean");
		keyWords.add("void");
		keyWords.add("true");
		keyWords.add("false");
		keyWords.add("null");
		keyWords.add("this");
		keyWords.add("let");
		keyWords.add("do");
		keyWords.add("if");
		keyWords.add("else");
		keyWords.add("while");
		keyWords.add("var");
		keyWords.add("int");
		keyWords.add("char");
		keyWords.add("return");

		tokens = new ArrayList<>();
		position = -1;
		symbols = "{}()[].,;+-*/&|<>=-~";
		Scanner fileScan = new Scanner(inFile);
		currentLine = "";
		jack = "";
		boolean inBlockComment = false;
		while (fileScan.hasNextLine())
		{
			currentLine = removeLineComments(fileScan.nextLine()).trim();
			for (int x = 0; x < currentLine.length(); x++)
			{
				if (inBlockComment && x < currentLine.length() && x > 0 &&
						currentLine.charAt(x-1) == '*' && currentLine.charAt(x) == '/')
					inBlockComment = false;
				else if (!inBlockComment && x < currentLine.length()-1 && currentLine.charAt(x) == '/'
						&& currentLine.charAt(x+1) == '*')
					inBlockComment = true;
				else if (!inBlockComment)
					jack += currentLine.charAt(x);
			}
		}
		fileScan.close();
		while (jack.length() > 0)
		{
			while (jack.charAt(0) == ' ')
				jack = jack.substring(1);
			
			for (int i = 0; i < keyWords.size(); i++)
			{
				if (jack.startsWith(keyWords.get(i)))
				{
					tokens.add(keyWords.get(i));
					jack = jack.substring(keyWords.get(i).length());
					i = keyWords.size();
				}
			}
			
			if (symbols.contains(jack.substring(0, 1)))
			{
				char symbol = jack.charAt(0);
				tokens.add(symbol + "");
				jack = jack.substring(1);
			}
			else if (jack.substring(0, 1).equals("\""))
			{
				jack = jack.substring(1);
				String strConst = "\"";
				while (jack.charAt(0) != '\"')
				{
					strConst += jack.charAt(0);
					jack = jack.substring(1);
				}
				strConst += '\"';
				tokens.add(strConst);
				jack = jack.substring(1);
			}
			else if (Character.isDigit(jack.charAt(0)))
			{
				String intConst = "";
				while(Character.isDigit(jack.charAt(0)))
				{
					intConst += jack.charAt(0);
					jack = jack.substring(1);
				}
				tokens.add(intConst);
			}
			else if (Character.isLetter(jack.charAt(0)) ||
					jack.substring(0, 1).equals("_"))
			{
				String identifier = "";
				while (Character.isLetter(jack.charAt(0)) ||
						jack.substring(0, 1).equals("_"))
				{
					identifier += jack.charAt(0);
					jack = jack.substring(1);
				}
				tokens.add(identifier);
			}
		}
		
	}
	
	public boolean hasMoreTokens()
	{
		return position < tokens.size()-1;
	}
	
	public void advance()
	{
		String currentToken = tokens.get(++position);
		if (keyWords.contains(currentToken))
		{
			nTokenType = "keyword";
			nKeyword = currentToken;
		}
		else if (symbols.contains(currentToken))
		{
			nTokenType = "symbol";
			nSymbol = currentToken.charAt(0);
		}
		else if (Character.isDigit(currentToken.charAt(0)))
		{
			nTokenType = "integerConstant";
			nIntVal = Integer.parseInt(currentToken);
		}
		else if (currentToken.substring(0, 1).equals("\""))
		{
			nTokenType = "stringConstant";
			nStrVal = currentToken.substring(1, currentToken.length()-1);
		}
		else if (Character.isLetter(currentToken.charAt(0)) ||
				currentToken.charAt(0) == '_')
		{
			nTokenType = "identifier";
			nIdentifier = currentToken;
		}
		else
			return;
	}
	
	public void back()
	{
		position--;
	}
	public String tokenType()
	{
		return nTokenType;
	}
	
	public String keyWord()
	{
		return nKeyword;
	}
	
	public char symbol()
	{
		return nSymbol;
	}
	
	public String identifier()
	{
		return nIdentifier;
	}
	
	public int intVal()
	{
		return nIntVal;
	}
	
	public String stringVal()
	{
		return nStrVal;
	}
	private String removeLineComments(String s)
	{
		int index = s.indexOf("//");
		if (index >= 0)
			return s.substring(0, index);
		else
			return s;
	}
	
		
}
