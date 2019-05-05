import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class VMWriter 
{
	private PrintWriter writer;
	
	VMWriter(File out) throws FileNotFoundException
	{
		writer = new PrintWriter(out);
	}
	
	public void writePush(String segment, int index)
	{
		writer.write("push " + segment + " " + index +"\n");
	}
	
	public void writePop(String segment, int index)
	{
		writer.write("pop " + segment + " " + index + "\n");
	}
	
	public void writeArithmetic(String command)
	{
		writer.write(command + "\n");
	}
	
	public void writeLabel(String label)
	{
		writer.write("label " + label + "\n");
	}
	
	public void writeGoto(String label)
	{
		writer.write("goto " + label + '\n');
	}
	
	public void writeIf(String label)
	{
		writer.write("if-goto " + label + "\n");
	}
	
	public void writeCall(String name, int nArgs)
	{
		writer.write("call " + name + " " + nArgs + "\n");
	}
	
	public void writeFunction(String name, int nLocals)
	{
		writer.write("function " + name + " " + nLocals + "\n");
	}
	
	public void writeReturn()
	{
		writer.write("return\n");
	}
	
	public void writeClose()
	{
		writer.close();
	}
}
