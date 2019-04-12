import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Translator 
{
	public static void main(String[] args) throws FileNotFoundException 
	{
		File out;
		ArrayList<File> inProcess = new ArrayList<>();
		String fileName = "";
		
		// if the argument provided is a vm file
		if (args[0].endsWith(".vm"))
		{
			// add the file to the file array inProcess
			inProcess.add(new File(args[0]));
			
			// create an output file using input file name with ext .asm
			fileName = args[0].substring(0, args[0].length()-3) + ".asm";
			out = new File(fileName);
		}
		
		// if the argument provided is a folder
		else
		{
			// Put all files in the folder into a file array allFiles
			File[] allFiles = ((new File(args[0]))).listFiles();
			
			// create an output file using input file name with ext .asm
			fileName = args[0] + ".asm";
			out = new File(fileName);
			
			// add .vm files to inProcess array
			for (File f : allFiles)
				if (f.getName().endsWith(".vm"))
					inProcess.add(f);
		}
		
		// Create a codeWriter object for the new output file
		CodeWriter codeWriter = new CodeWriter(out);
		
		// if the program has Sys.init function, calls it
		codeWriter.writeInit();
		
		// Parses each file in inProcess
		for (File f : inProcess)
		{
			Parser parser = new Parser(f);
			codeWriter.setFileName(f.getName());
			while (parser.hasMoreCommands())
			{
				parser.advance();
				if (parser.commandType().compareTo("C_ARITHMETIC") == 0)
					codeWriter.writeArithmetic(parser.arg1());
				else if (parser.commandType().compareTo("C_PUSH") == 0 || 
						 parser.commandType().compareTo("C_POP") == 0)
					codeWriter.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
				else if (parser.commandType().compareTo("C_LABEL") == 0)
					codeWriter.writeLabel(parser.arg1());
				else if (parser.commandType().compareTo("C_GOTO") == 0)
					codeWriter.writeGoto(parser.arg1());
				else if (parser.commandType().compareTo("C_IF") == 0)
					codeWriter.writeIf(parser.arg1());
				else if (parser.commandType().compareTo("C_CALL") == 0)
					codeWriter.writeCall(parser.arg1(), parser.arg2());
				else if (parser.commandType().compareTo("C_RETURN") == 0)
					codeWriter.writeReturn();
				else if (parser.commandType().compareTo("C_FUNCTION") == 0)
					codeWriter.writeFunction(parser.arg1(), parser.arg2());
			}
		}
		
		// Closes the printwriter of the codewriter
		codeWriter.close();
	}
}
