import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class JackCompiler {

	public static void main(String[] args) throws FileNotFoundException
	{
		File input = new File(args[0]);
		String outputFile;
		PrintWriter writer;
		JackTokenizer jt;
		CompilationEngine ce;
		if (input.isDirectory())
		{
			File[] files = input.listFiles();
			for (File f : files)
			{
				if (f.getName().endsWith(".jack"))
				{
					jt = new JackTokenizer(f);
					outputFile = f.getName().substring(0, f.getName().length()-5) + "T.xml";
					writer = new PrintWriter(new File(outputFile));
					tokenize(jt, writer);
					File out = new File(outputFile);
					outputFile = outputFile.substring(0, outputFile.length() - 5) + ".xml";
					writer = new PrintWriter(outputFile);
					ce = new CompilationEngine(out, writer);
					writer.close();
				}
			}
		}
		else if (input.getName().endsWith(".jack"))
		{
			jt = new JackTokenizer(input);	
			outputFile = input.getName().substring(0, input.getName().length()-5) + "T.xml";
			writer = new PrintWriter(new File(outputFile));
			tokenize(jt, writer);
			File out = new File(outputFile);
			outputFile = outputFile.substring(0, outputFile.length() - 5) + ".xml";
			writer = new PrintWriter(outputFile);
			ce = new CompilationEngine(out, writer);
			writer.close();
		}
	}
	
	public static void tokenize(JackTokenizer jt, PrintWriter writer)
	{
		writer.write("<tokens>\n");
		while (jt.hasMoreTokens())
		{
			jt.advance();
			writer.write("<" + jt.tokenType() + "> ");
			if (jt.tokenType().equals("keyword"))
				writer.write(jt.keyWord());
			else if (jt.tokenType().equals("symbol"))
			{
				if (jt.symbol() == '<')
					writer.write("&lt;");
				else if (jt.symbol() == '>')
					writer.write("&gt;");
				else if (jt.symbol() == '"')
					writer.write("&quot;");
				else if (jt.symbol() == '&')
					writer.write("&amp;");
				else
					writer.write(jt.symbol());
			}	
			else if (jt.tokenType().equals("integerConstant"))
			{
				writer.write(jt.intVal() + "");
			}
			else if (jt.tokenType().equals("stringConstant"))
				writer.write(jt.stringVal());
			else if (jt.tokenType().equals("identifier"))
				writer.write(jt.identifier());
			writer.write(" </" + jt.tokenType() + ">\n");
		}
		writer.write("</tokens>\n");
		writer.close();
		
	}
}
