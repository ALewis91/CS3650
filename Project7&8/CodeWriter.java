import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CodeWriter 
{
	private PrintWriter pw;
	private String fileName;
	private int jumpID;
	private int returnLabelNumber;

	// Constructor takes output file as arg
	public CodeWriter(File outFile) throws FileNotFoundException
	{
		pw = new PrintWriter(outFile); 		// Create printwriter for out file
		fileName = "";						// init current file name
		jumpID = 0;							// init jumpId 
		returnLabelNumber = 0;				// init return label
	}
	
	// Sets current file name based for static vars
	public void setFileName(String filename)
	{
		fileName = filename;
	}
	
	// Writes arithmetic and logic commands
	public void writeArithmetic(String command)
	{
		String cmd = "";
		if (command.compareTo("add") == 0)
			cmd += prepArithmetic() + "M=M+D\n"; // writes addition command
		else if (command.compareTo("sub") == 0)
			cmd += prepArithmetic() + "M=M-D\n"; // writes subtraction command
		else if (command.compareTo("neg") == 0)		// writes negation command
		{
			cmd += "D=0\n";
			cmd += "@SP\n";
			cmd += "A=M-1\n";
			cmd += "M=D-M\n";
		}
		else if (command.compareTo("not") == 0)		// writes logical not command
		{
			cmd += "@SP\n";
			cmd += "A=M-1\n";
			cmd += "M=!M\n";
		}
		else if (command.compareTo("eq") == 0)		// writes equal logic command
		{
			cmd += prepLogic("JNE");
			jumpID++;								// increment jumpID
		}
		else if (command.compareTo("gt") == 0)		// writes greater than logic command
		{
			cmd += prepLogic("JLE");
			jumpID++;								// increment jumpID
		}
		else if (command.compareTo("lt") == 0)		// writes less than logic command
		{
			cmd += prepLogic("JGE");
			jumpID++;								// increment jumpID
		}
		else if (command.compareTo("and") == 0)
			cmd += prepArithmetic() + "M=M&D\n";
		else if (command.compareTo("or") == 0)
			cmd += prepArithmetic() + "M=M|D\n";
		else
			System.out.println("Invalid Command");
		
		pw.write(cmd);								// write the command to the output file
	}
	
	// Writes push and pop commands in hack from VM instructions
	void writePushPop(String command, String segment, int index)
	{
		String cmd = "";
		if (command.compareTo("C_PUSH") == 0)		
		{
			if (segment.compareTo("constant") == 0)	
			{
				cmd = "@" + index + "\n" +
						"D=A\n" +
						"@SP\n" +
						"A=M\n" +
						"M=D\n" +
						"@SP\n" +
						"M=M+1\n";
			}
			else if (segment.compareTo("this") == 0)
				cmd += pushTemplate1("THIS", index);
			else if (segment.compareTo("that") == 0)
				cmd += pushTemplate1("THAT", index);
			else if (segment.compareTo("local") == 0)
				cmd += pushTemplate1("LCL", index);
			else if (segment.compareTo("argument") == 0)
				cmd += pushTemplate1("ARG", index);
			else if (segment.compareTo("temp") == 0)
			{
				cmd += "@R" + (5 + index) + "\n" +		
						"D=M\n" +					// Store value in R(5+index) in D
						"@SP\n" +	
						"A=M\n" +					// Go to top of stack
						"M=D\n" +					// Store temp value on stack
						"@SP\n" +
						"M=M+1\n";					// Increment stack pointer
			}
			else if (segment.compareTo("pointer") == 0 && index == 0)
				cmd += pushTemplate2("THIS");
			else if (segment.compareTo("pointer") == 0 && index == 1)
				cmd += pushTemplate2("THAT");
			else if (segment.compareTo("static") == 0)
				cmd += pushTemplate2(fileName + (index));
		}
		else if (command.compareTo("C_POP") == 0)
		{
			if (segment.compareTo("this") == 0)
				cmd += popTemplate1("THIS", index);
			else if (segment.compareTo("that") == 0)
				cmd += popTemplate1("THAT", index);
			else if (segment.compareTo("local") == 0)
				cmd += popTemplate1("LCL", index);
			else if (segment.compareTo("argument") == 0)
				cmd += popTemplate1("ARG", index);
			else if (segment.compareTo("temp") == 0)
			{
				cmd += "@SP\n" +			// Pop top of stack into D
						"AM=M-1\n" +
						"D=M\n" +
						"@" + (5 + index) + "\n" +	
						"M=D\n";			// Store in R(5+index)
			}
			else if (segment.compareTo("pointer") == 0 && index == 0)
				cmd += popTemplate2("THIS");
			else if (segment.compareTo("pointer") == 0 && index == 1)
				cmd += popTemplate2("THAT");
			else if (segment.compareTo("static") == 0)
				cmd += popTemplate2(fileName + (index));
		}
		
		pw.write(cmd);
	}
	
	// Closes the printwriter
	public void close()
	{
		pw.close();
	}
	
	// Loads first operand into M and second operand into D
	private String prepArithmetic()
	{
		String cmd = "@SP\n" +
		"AM=M-1\n" +
		"D=M\n" +
		"A=A-1\n";
		return cmd;
	}
	
	// Branch based on jump condition 
	private String prepLogic(String jmp)
	{
		String cmd = prepArithmetic() +			// loads first operand into M and second operand into D
					"D=M-D\n" +
					"@FALSE" + jumpID + "\n" +
					"D;" + jmp + "\n" +
					"@SP\n" +
					"A=M-1\n" +
					"M=-1\n" +
					"@CONTINUE" + jumpID + "\n" +
					"0;JMP\n" +
					"(FALSE" + jumpID + ")\n" +
					"@SP\n" +
					"A=M-1\n" +
					"M=0\n" +
					"(CONTINUE" + jumpID + ")\n";
		
		return cmd;
	}
	
	// hack translation for vm push commands given index and segment - this, that, temp, arg, local
	private String pushTemplate1(String segment, int index)
	{
		String cmd = "@" + segment + "\n" +
					"D=M\n" +
					"@" + index + "\n" +
					"A=D+A\n" +
					"D=M\n" +				// Store value @ segment (base) + index (disp) in D
					"@SP\n" +
					"A=M\n" +				// Go to top of stack
					"M=D\n" +				// Store value at top of stack
					"@SP\n" +
					"M=M+1\n";				// Increment stack pointer
		return cmd;				
	}
	
	// hack translation for vm push commands given segment - static and pointer
	private String pushTemplate2(String segment)
	{
		String cmd = "@" + segment + "\n" +		// go to segment
					"D=M\n" +								// store value in segment in D
					"@SP\n" +								
					"A=M\n" +	
					"M=D\n" +								// Store value in seg @ top of stack
					"@SP\n" +	
					"M=M+1\n";								// increment stack pointer
		return cmd;
	}

	// hack translation for vm pop commands given index and segment - this, that, temp, arg, local
	private String popTemplate1(String segment, int index)
	{
		String cmd = "@" + segment + "\n" +
					"D=M\n" +
					"@" + index + "\n" +
					"D=D+A\n" +					// Store value in segment (base) + index (disp) in D
					"@R15\n" +
					"M=D\n" +					// Store this addr in gpr 15
					"@SP\n" +
					"AM=M-1\n" +				// Go to top stack value
					"D=M\n" +
					"@R15\n" +
					"A=M\n" +
					"M=D\n";					// Store top of stack @ addr in gpr 15
		return cmd;
	}
	
	// hack translation which pops top of stack directly into base addr of segment 
	private String popTemplate2(String segment)
	{
		String cmd = "@" + segment + "\n" +
					"D=A\n" +
					"@R15\n" +
					"M=D\n" +				// Store addr of segment in R15
					"@SP\n" +
					"AM=M-1\n" +
					"D=M\n" +				// Pop top of stack into D
					"@R15\n" +
					"A=M\n" +				
					"M=D\n";				// Store D in to segment
		return cmd;
	}
	
	// Project 8
	// initializes stack pointer to 256 and calls Sys.init with 0 local variables
	public void writeInit()
	{
		String cmd = "@256\n" +
		"D=A\n" +
		"@SP\n" +
		"M=D\n";
		pw.write(cmd);
		writeCall("Sys.init", 0);
	}
	
	// Writes a new label
	public void writeLabel(String label)
	{
		pw.write("(" + label + ")\n");
	}
	
	// Jumps to provided label unconditionally
	public void writeGoto(String label)
	{
		String cmd = "";
		cmd += "@" + label + "\n";
		cmd += "0;JMP\n";
		pw.write(cmd);
	}
	
	// 
	public void writeIf(String label)
	{
		String cmd = prepArithmetic();
		cmd += "@" +  label + "\n";
		cmd += "D;JNE\n";
		pw.write(cmd);
	}
	
	public void writeCall(String fxName, int numArgs)
	{
		String label = "RETURN" + returnLabelNumber;	// Generate unique return label
		returnLabelNumber++;							
		String cmd = "@" + label + "\n" +				// load addr of return into A
		"D=A\n" +
		"@SP\n" +
		"A=M\n" +
		"M=D\n" +
		"@SP\n" +
		"M=M+1\n" +										// Push return addr onto top of stack and inc SP
		pushTemplate2("LCL") +							// Push local variables addr onto stack
		pushTemplate2("ARG") +							// Push argument addr onto stack
		pushTemplate2("THIS") +							// Push This value onto stack
		pushTemplate2("THAT") +							// Push That value onto stack
		"@SP\n" +
		"D=M\n" +
		"@5\n" +
		"D=D-A\n" +
		"@" + numArgs + "\n" +
		"D=D-A\n" +
		"@ARG\n" +
		"M=D\n" +										// Reposition ARG to SP-nArgs-5
		"@SP\n" +
		"D=M\n" +
		"@LCL\n" +
		"M=D\n" +										// Set LCL to SP
		"@" + fxName + "\n" +
		"0;JMP\n" +										// Jump to function
		"(" + label + ")\n";							// Write return label directly after fx call
		pw.write(cmd);
	}
	
	// Writes a return from a function call - R13 is frame pointer, R14 is return addr
	public void writeReturn()
	{
		String[] segs = {"THAT", "THIS", "ARG", "LCL"};
		String cmd = "@LCL\n" +
					"D=M\n" +
					"@R13\n" +
					"M=D\n" +		// Set frame pointer to LCL
					"@5\n"+
					"A=D-A\n"+
					"D=M\n" +
					"@R14\n" +
					"M=D\n" +		// Set return addr to value @ LCL - 5
					"@ARG\n" +
					"D=M\n" +
					"@R15\n" +
					"M=D\n" +		// Store ARG in R15
					"@SP\n" +
					"AM=M-1\n" +
					"D=M\n" +		// Pop result into D
					"@R15\n" +
					"A=M\n" +
					"M=D\n" +		// Store D @ R15
					"@ARG\n" +
					"D=M+1\n" +		 
					"@SP\n" +
					"M=D\n";		// Set SP to ARG + 1
	
		// Restore segment values from stack
		int i = 1;
		for (String s : segs)
		{
			cmd += "@" + i + "\n" +
					"D=A\n" +
					"@R13\n" +
					"A=M-D\n" +
					"D=M\n" +
					"@" + s + "\n" +
					"M=D\n";
			i++;
					
		}
		cmd += "@R14\n" +
				"A=M\n" +
				"0;JMP\n";		// Jump to return address
		pw.write(cmd);
	}
	
	// Creates a new label for the function and allocates stack space for local vars
	public void writeFunction(String fxName, int numLocals)
	{
		pw.write("(" + fxName + ")\n");
		
		for (int x = 0; x < numLocals; x++)
			writePushPop("C_PUSH", "constant", 0);
	}
}
