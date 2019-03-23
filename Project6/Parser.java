import java.io.*;
import java.util.Scanner;

public class Parser 				// Opens file from argument and parses asm instr into fields
{
	
	private Scanner codeScan;		// Reads each line of assembly text
	private String currentLine;		// Holds the current instruction
	private int linePosition;		// Current position in the current instruction

	
	public Parser(String filename) throws FileNotFoundException	// Constructor
	{
		File assemblyCode = new File(filename);			// Open assembly file
		codeScan = new Scanner(assemblyCode);			// Create scanner to read file
	}
	
	boolean hasMoreCommands()					// returns true if has more commands
	{			
		while (codeScan.hasNext())				// loop until EoF or a valid command
		{
			currentLine = codeScan.nextLine();		// set current instruction to next line
			linePosition = 0;				// scan until a non whitespace of '/' char
			while (linePosition < currentLine.length() &&
				   currentLine.charAt(linePosition) == ' ')
				linePosition++;
			int lineStart = linePosition;			// mark beginning of asm instruction
			while (linePosition < currentLine.length() &&	// scan until end of line or comment
					!(currentLine.charAt(linePosition) == '/'
				&& currentLine.charAt(linePosition+1) == '/'))
				linePosition++;
			int lineEnd = linePosition;			// mark end of asm instruction
			currentLine = currentLine.substring(lineStart, lineEnd); // create substr for the asm instruction
			if (currentLine.length() > 0)			// if the line has a valid instr return true
				return true;
		}
		return false;						// else return false
	}
	
	
	public String commandType()					// returns the command type of the current asm instr
	{
		if (currentLine.charAt(0) == '@')			// if the asm instr begins with '@' return A-type
			return "A_COMMAND";
		else if (currentLine.charAt(0) == '(')			// else if the asm instr begins with '(' return L-type
			return "L_COMMAND";
		else							// else return C-type
			return "C_COMMAND";
	}
	
	String symbol()							// returns the symbol following @ for A-type or L-type instr
	{
		if (commandType() == "A_COMMAND")			// if A-type instr return the binary constant or symbol
		{
			linePosition = 1;				// begin scanning just past the '@' symbol
			if (Character.isDigit(currentLine.charAt(linePosition))) // if it begins with a digit, return the binary converted number
			{
				while (linePosition < currentLine.length() &&
						Character.isDigit(currentLine.charAt(linePosition)))
					linePosition++;
				return decToBin(Integer.parseInt(currentLine.substring(1, linePosition)));
			}
			else						// else return the symbol
				return currentLine.substring(1, currentLine.length());
		}
		else if (commandType() == "L_COMMAND")			// if L-type instr return the symbol within the parentheses
		{
			linePosition = 1;
			while (linePosition < currentLine.length() &&
				   currentLine.charAt(linePosition) != ')')
				linePosition++;
			return currentLine.substring(1, linePosition);		
		}
		else 
			return "";					// else return empty string
	}
	
	public String dest()						// returns the destination of the computation in the instr
	{
		String dst = "";					// initialize dst to empty string
		linePosition = 0;
		if (commandType() == "C_COMMAND")			// if C-type instr
		{
			if (currentLine.contains("="))			// if there exists a destination, or '=' in the instr
			{
				while(linePosition < currentLine.length() &&	// add non-whitespace char before '=' to dest
						currentLine.charAt(linePosition) != '=')
				{
					if(!(currentLine.charAt(linePosition) == ' '))
						dst += currentLine.charAt(linePosition);	
					linePosition++;
				}
			}
			else						// else if no '=', return 'null'
				dst = "null";
		}
		return dst;						// return empty string if not C-type instr
	}
	
	public String comp()						// returns computation part of the instr
	{
		String cmp = "";					// init cmp to empty string
		linePosition = 0;					// set linePosition to 0
		if (commandType() == "C_COMMAND")			// if C-type instr
		{
			if (currentLine.contains("="))			// with a destination
			{
				while(currentLine.charAt(linePosition) != '=')	// scan until you reach past the dest
					linePosition++;
				linePosition++;					
				while(linePosition < currentLine.length() &&	// add all non-whitespace char before ';' and end of str to cmp
						currentLine.charAt(linePosition) != ';')
				{
					if(currentLine.charAt(linePosition) != ' ')
						cmp += currentLine.charAt(linePosition);
					linePosition++;
				}
			}
			else						// else if there is no destination
			{
				while(linePosition < currentLine.length() &&	// add all non-white space char before ';' and end of str to cmp
						currentLine.charAt(linePosition) != ';')
				{
					if(currentLine.charAt(linePosition) != ' ')
						cmp += currentLine.charAt(linePosition);
					linePosition++;
				}
			}
		}
		return cmp;						// return cmp
	}
	
	public String jump()						// returns the jump condition part of the instr
	{
		String jmp = "";					// init jmp to empty string
		if (commandType() == "C_COMMAND")			// if C-type instr
		{
			linePosition = 0;				// set linePosition to 0
			if (!currentLine.contains(";"))			// if instr does not contain ';', return null bc no jump
				return "null";
			while(currentLine.charAt(linePosition) != ';')	// else, scan until past the ';' char
				linePosition++;
			linePosition++;
			while(linePosition < currentLine.length())	// add all non-whitespace chars before end of string to jmp
			{
				if(!(currentLine.charAt(linePosition) == ' '))
					jmp += currentLine.charAt(linePosition);
				linePosition++;
			}
		}
		return jmp;						// return jmp
	}
	
	public String decToBin(int decimal)				// returns binary conversion of decimal number
	{
		int remainder;
		String binary = "";
		if (decimal == 0)					// computer binary equivalent of decimal number
			binary = "0";
		while (decimal > 0)
		{
			remainder = decimal%2;
			binary = remainder + binary;
			decimal = decimal/2;
		}
		while (binary.length() < 15)				// left pads binary digit with 0's until length is 15 digits
			binary = "0" + binary;
		return binary;						// return binary digit string
	}
	
}


