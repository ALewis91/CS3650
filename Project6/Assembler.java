import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Assembler 
{

	public static void main(String[] args) throws FileNotFoundException 
	{
		String readFile = args[0];							// readFile from command line argument
		Parser fileParser = new Parser(readFile); 					// create a Parser for readFile
		String writeFile = readFile.substring(0, readFile.length()-3) + "hack";		// name writeFile as readFile with hack ext
		PrintWriter pw = new PrintWriter(new File(writeFile));				// create PrintWriter for writeFile
		String instruction;								// create var instruction to hold the current machine instr
		Code codeGetter = new Code();							// create a Code object codeGetter
		SymbolTable symbols = new SymbolTable();					// create a new symbol table called symbols
		initSymbols(symbols);								// initialize symbols with known symbols
		int nextRamAddr = 16;								// nextRamAddr var to keep track of next available addr
		int nextInstruction = 0;							// nextInstruction to keep track of instr # for labels
		
		while(fileParser.hasMoreCommands())						// loop while there are more commands in readFile
		{
			if(fileParser.commandType().compareTo("L_COMMAND") == 0)		// if a new label is encountered, add symbol 
			{									// and next instr # to symbol table
				symbols.addEntry(fileParser.symbol(), nextInstruction);
			}
			else
				nextInstruction++;						// else increment instr #
		}
		
		fileParser = new Parser(readFile);						// create new fileParser to reopen readFile
		
		while(fileParser.hasMoreCommands())						// loop while there are more commands in readFile
		{
			instruction = "";							// init current instruction to empty string
			if(fileParser.commandType().compareTo("C_COMMAND") == 0)		// if C-type instr
			{
				instruction += "111";						// append "111" to the instruction
				if(!fileParser.comp().contains("M"))				// append "0" to instruction if the computation contains "M"
					instruction += "0";
				else
					instruction += "1";					// else append "1" to instruction
				instruction += codeGetter.comp(fileParser.comp());		// append code for computation
				instruction += codeGetter.dest(fileParser.dest());		// append code for destination
				instruction += codeGetter.jump(fileParser.jump());		// append code for jump

			}
			else if(fileParser.commandType().compareTo("A_COMMAND") == 0)		// else if A-Type instr
			{
				instruction += "0";						// append "0" to front of instruction
				if (Character.isDigit(fileParser.symbol().charAt(0)))		// if the symbol begins with a digit
					instruction += fileParser.symbol();			// append the binary digit to instruction
				else if (symbols.contains(fileParser.symbol()))			// else if the symbol is not a digit and is in the symbol table
					instruction += fileParser.decToBin(symbols.getAddress(fileParser.symbol())); // append the binary equivalent of the decimal address from the table
				else								// else if the symbol is not a digit and is not in the symbol table
				{
					symbols.addEntry(fileParser.symbol(), nextRamAddr);	// add the symbol to the symbol table at the next available ram address
					instruction += fileParser.decToBin(nextRamAddr);	// append the binary equivalent of the decimal ram address
					nextRamAddr++;						// increment the next available ram address
				}
			}
			if (!(fileParser.commandType().compareTo("L_COMMAND") == 0))		// if the current instr is not a label
			{
				pw.write(instruction + '\n');					// write the instruction to the file and move to next line
			}
		}
		pw.close();									// close the PrintWriter

		
	}
	
	
	public static void initSymbols(SymbolTable T)						// initializes the symbol table with known symbols
	{											// and their addresses
		T.addEntry("SP", 0);
		T.addEntry("LCL", 1);
		T.addEntry("ARG", 2);
		T.addEntry("THIS", 3);
		T.addEntry("THAT", 4);
		T.addEntry("R0", 0);
		T.addEntry("R1", 1);
		T.addEntry("R2", 2);
		T.addEntry("R3", 3);
		T.addEntry("R4", 4);
		T.addEntry("R5", 5);
		T.addEntry("R6", 6);
		T.addEntry("R7", 7);
		T.addEntry("R8", 8);
		T.addEntry("R9", 9);
		T.addEntry("R10", 10);
		T.addEntry("R11", 11);
		T.addEntry("R12", 12);
		T.addEntry("R13", 13);
		T.addEntry("R14", 14);
		T.addEntry("R15", 15);
		T.addEntry("SCREEN", 16384);
		T.addEntry("KBD", 24576);
	}
	

}
