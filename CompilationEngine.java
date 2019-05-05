import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;


public class CompilationEngine 
{
	private PrintWriter writer;
	private VMWriter vmWriter;
	private ArrayList<String> tokens;
	private int position;
	private String indent;
	private String tab = "  ";
	private SymbolTable symbolTable;
	private String className;
	private int labelNumber;
	private int nArgs;
	CompilationEngine (File in, PrintWriter output) throws FileNotFoundException
	{
		writer = output;
		indent = "";
		tokens = new ArrayList<>();
		position = 0;
		nArgs = 0;
		position = 0;
		labelNumber = 0;
		symbolTable = new SymbolTable();
		vmWriter = new VMWriter(new File(in.getName().substring(0, in.getName().length() - 5) + ".vm"));
		Scanner input = new Scanner(in);
		
		while (input.hasNextLine())
		{
			String ele = input.nextLine().trim();
			if (ele.length() > 0 && !ele.equals("<tokens>") && !ele.equals("</tokens>"))
				tokens.add(ele);
		}
		
		input.close();
		
		if (tokens.get(position).contains("<keyword> class </keyword>"))
			compileClass();
		else
			System.out.println("no class" + tokens.get(position));
		vmWriter.writeClose();
	}
	
	public void compileClass()
	{
		indent += tab;
		writer.write("<class>\n");					 		// write class declaration
		if (!tokens.get(position).contains("<keyword> class </keyword>"))
			System.out.println("Error: expecting class declaration");
		writer.write(indent + tokens.get(position++) + "\n");		// write <keyword> class </keyword>
		if (!tokens.get(position).contains("<identifier>"))
			System.out.println("Error: class name must be token type identifier " + tokens.get(position+1));
		className = getToken(tokens.get(position));
		writer.write(indent + tokens.get(position++) +"\n");		// write class name
		if (tokens.get(position).contains("<symbol> { </symbol>")) 
		{
			writer.write(indent + tokens.get(position++) + "\n");	// write '{'
			compileClassVarDec();							// write class variable declarations
			compileSubroutine();							// write class subroutines
			if (tokens.get(position).contains("<symbol> } </symbol>")) // if end of class
			{
				writer.write(indent + tokens.get(position) + "\n");	// write '}'
				writer.write("</class>\n");						// close class
			}
		}
		else
		{
			System.out.println("Error: expecting '{' symbol after class declaration");
		}
	}
	
	public void compileClassVarDec()
	{
		if (tokens.get(position).contains("<keyword> static </keyword>") || tokens.get(position).contains("<keyword> field </keyword>"))
		{
			while (tokens.get(position).contains("<keyword> static </keyword>") || tokens.get(position).contains("<keyword> field </keyword>"))
			{
				writer.write(indent + "<classVarDec>\n");								// write class variable declaration
				indent += tab;
				
				String varKind = segID(getToken(tokens.get(position)));
				writer.write(indent + tokens.get(position++) + "\n");
				String varType = getToken(tokens.get(position));
				writer.write(indent + tokens.get(position++) + "\n");
				String varName = getToken(tokens.get(position));
				symbolTable.define(varName, varType, varKind);
				writer.write(indent + tokens.get(position++) + "\n");					// write variable name
				while (tokens.get(position).contains("<symbol> , </symbol>"))	// while there are more class variables of this type
				{
					writer.write(indent + tokens.get(position++) + "\n");	
					varName = getToken(tokens.get(position));
					symbolTable.define(varName, varType, varKind);
					writer.write(indent + tokens.get(position++) + "\n");				// write variable name
				}

				if (tokens.get(position).contains("<symbol> ; </symbol>"))	// write close classVarDec
				{
					writer.write(indent + tokens.get(position++) + "\n");
					indent = indent.substring(2);
					writer.write(indent + "</classVarDec>\n");
				}
				else
				{
					//throw exception
				}
			}				
		}
		else
			return;
		return;
	}
	
	public void compileSubroutine()
	{
		while (!tokens.get(position).contains("<symbol> } </symbol>"))
		{
		writer.write(indent + "<subroutineDec>\n");
		indent += tab;
		while (tokens.get(position).contains("<keyword> function </keyword>") || 
				tokens.get(position).contains("<keyword> method </keyword>") ||
				tokens.get(position).contains("<keyword> constructor </keyword>"))
		{
			symbolTable.startSubroutine();
			String subroutineType = getToken(tokens.get(position));
			if (subroutineType.equals("method"))
				symbolTable.define("this", className, "argument");
			writer.write(indent + tokens.get(position++).toString() + "\n");	// write function || method || constructor
			writer.write(indent + tokens.get(position++) + "\n");  	// write identifier - return type
			String subroutineName = getToken(tokens.get(position));
			writer.write(indent + tokens.get(position++) + "\n");	// write identifier - subroutine name
			compileParameterList();							// write subroutine parameters
			writer.write(indent + "<subroutineBody>\n");
			indent += tab;
			writer.write(indent + tokens.get(position++) + "\n");	// write '{'
			while (tokens.get(position).contains("<keyword> var </keyword>"))	// write all subroutine variables
			{
				writer.write(indent + "<varDec>\n");
				String varKind = "local";
				indent += tab;
				writer.write(indent + tokens.get(position++) + "\n");			// write var declaration
				String varType = getToken(tokens.get(position));
				writer.write(indent + tokens.get(position++) + "\n");			// write data type
				String varName = getToken(tokens.get(position));
				writer.write(indent + tokens.get(position++) + "\n");			// write var name
				symbolTable.define(varName, varType, varKind);
				while (tokens.get(position).contains("<symbol> , </symbol>"))	// declare more vars of same type and scope
				{
					writer.write(indent + tokens.get(position++) + "\n");		// write ','
					varName = getToken(tokens.get(position));
					writer.write(indent + tokens.get(position++) + "\n");		// write var name
					symbolTable.define(varName, varType, varKind);
				}
				writer.write(indent + tokens.get(position++) + "\n");			// write ';'
				indent = indent.substring(2);
				writer.write(indent + "</varDec>\n");
			}
			
			String function = className + '.' + subroutineName;
			vmWriter.writeFunction(function, symbolTable.varCount("var"));
			if (subroutineType.equals("method"))
			{
				vmWriter.writePush("argument", 0);
				vmWriter.writePop("pointer", 0);
			}
			else if (subroutineType.equals("constructor"))
			{
				vmWriter.writePush("constant", symbolTable.varCount("field"));
				vmWriter.writeCall("Memory.alloc", 1);
				vmWriter.writePop("pointer", 0);
			}
			compileStatements();
			indent = indent.substring(2);
		}
		indent += tab;
		writer.write(indent + tokens.get(position++) + "\n"); 	// write '}'
		indent = indent.substring(2);
		writer.write(indent + "</subroutineBody>\n");
		indent = indent.substring(2);
		writer.write(indent + "</subroutineDec>\n");
	}
		return;
	}
	
	public void compileParameterList()
	{
		if (tokens.get(position).contains("<symbol> ( </symbol>"))
		{
			writer.write(indent + tokens.get(position++) + "\n");	// write '('
			writer.write(indent + "<parameterList>\n");
			indent += tab;
			String varKind = "argument";
			while (tokens.get(position).contains("<keyword>") || 
					tokens.get(position).contains("<identifier>"))
			{
				String varType = getToken(tokens.get(position));
				writer.write(indent + tokens.get(position++) + "\n");	// write param data type
				String varName = getToken(tokens.get(position));
				writer.write(indent + tokens.get(position++) + "\n");	// write param name
				if (tokens.get(position).contains("<symbol> , </symbol>")) // if multiple params
					writer.write(indent + tokens.get(position++) + "\n");	// write ','
				symbolTable.define(varName, varType, varKind);
			}
			if (tokens.get(position).contains("<symbol> ) </symbol>")) // no more params
			{
				indent = indent.substring(2);
				writer.write(indent + "</parameterList>\n");
				writer.write(indent + tokens.get(position++) + "\n"); // write ')'
			}
			else
			{
				// throw exception
			}
		}
		else
			//throw exception
		return;
	}
	
	public void compileStatements()
	{
		writer.write(indent + "<statements>\n"); 
		indent += tab;
		while (tokens.get(position).contains("<keyword> let </keyword>") || 
				tokens.get(position).contains("<keyword> if </keyword>") || 
				tokens.get(position).contains("<keyword> while </keyword>") ||
				tokens.get(position).contains("<keyword> do </keyword>") ||
				tokens.get(position).contains("<keyword> return </keyword>"))
		{
			if(tokens.get(position).contains("<keyword> let </keyword>"))
				compileLet();
			else if (tokens.get(position).contains("<keyword> if </keyword>"))
				compileIf();
			else if (tokens.get(position).contains("<keyword> while </keyword>"))
				compileWhile();
			else if (tokens.get(position).contains("<keyword> do </keyword>"))
				compileDo();
			else if(tokens.get(position).contains("<keyword> return </keyword>"))
				compileReturn();
		}
		indent = indent.substring(2);
		writer.write(indent + "</statements>\n"); 
		return;
	}
	
	public void compileLet()
	{
		writer.write(indent + "<letStatement>\n");
		indent += tab;
		writer.write(indent + tokens.get(position++) + "\n");	// write 'let' keyword
		String varName = getToken(tokens.get(position));
		writer.write(indent + tokens.get(position++) + "\n");	// write var name
		boolean arrayAssignment = false;
		if (tokens.get(position).contains("<symbol> [ </symbol>"))	// if an array element
		{
			arrayAssignment = true;
			vmWriter.writePush(symbolTable.kindOf(varName), symbolTable.indexOf(varName));	// push arr
			writer.write(indent + tokens.get(position++) + "\n");	// write '[' symbol
			compileExpression();
			writer.write(indent + tokens.get(position++) + "\n");	// write ']' symbol
			vmWriter.writeArithmetic("add");
			
			while (tokens.get(position).contains("<symbol> ] </symbol>"))
			{
				vmWriter.writeArithmetic("add");
				position++;
			}
		}
		
		writer.write(indent + tokens.get(position++) + "\n");	// write '=' symbol
		compileExpression();
		
		indent = indent.substring(2);
		writer.write(indent + "</letStatement>\n");
		
		if (arrayAssignment)
		{
			vmWriter.writePop("temp", 0);
			vmWriter.writePop("pointer", 1);
			vmWriter.writePush("temp", 0);
			vmWriter.writePop("that", 0);
		}
		else
			vmWriter.writePop(symbolTable.kindOf(varName), symbolTable.indexOf(varName));

		if (tokens.get(position).contains("<symbol> ; </symbol>"))
			writer.write(indent + tokens.get(position++) + "\n");	// write ';' symbol
		else
		{
			System.out.println("Error: expecting ';' after variable assignment");
		}
		
		
	}
	
	public void compileIf()
	{
		String elseLabel = "ELSE" + labelNumber++;
		String continueLabel = "CONTINUE" + labelNumber++;		writer.write(indent + "<ifStatement>\n");
		indent += tab;
		writer.write(indent + tokens.get(position++) + "\n");	
		if (tokens.get(position).contains("<symbol> ( </symbol>"))
		{
			writer.write(indent + tokens.get(position++) + "\n");	// write '('
			compileExpression();
			vmWriter.writeArithmetic("not");
			vmWriter.writeIf(elseLabel);
			if (tokens.get(position).contains("<symbol> ) </symbol>"))
			{
				writer.write(indent + tokens.get(position++) + "\n");	// write ')' closing conditional statement
				if (tokens.get(position).contains("<symbol> { </symbol>"))
				{
					writer.write(indent + tokens.get(position++) + "\n");	// write '{' start block statement
					compileStatements();
					if (tokens.get(position).contains("<symbol> } </symbol>")) 
					{
						writer.write(indent + tokens.get(position++) + "\n");	// write '}' close block statement
						vmWriter.writeGoto(continueLabel);
						vmWriter.writeLabel(elseLabel);					
					}
					else
						System.out.println("Error: expecting '}' after if statements");
					if (tokens.get(position).contains("<keyword> else </keyword>"))
					{
						writer.write(indent + tokens.get(position++) + "\n");	// write else declaration
						if (tokens.get(position).contains("<symbol> { </symbol>"))
						{
							writer.write(indent + tokens.get(position++) + "\n");	// write '{' start block
							compileStatements();
							if (tokens.get(position).contains("<symbol> } </symbol>"))
								writer.write(indent + tokens.get(position++) + "\n");	// write '}' end block
							else
								System.out.println("Error: expecting '}' after else statements");
						}
					}
				}
				vmWriter.writeLabel(continueLabel);
			}
			
		}
		else
			System.out.println("Error: expecting '(' after if declaration");
		indent = indent.substring(2);
		writer.write(indent + "</ifStatement>\n");
	}
	
	public void compileWhile()
	{
		String whileLabel = "WHILE" + labelNumber++;
		String continueLabel = "CONTINUE" + labelNumber++;
		vmWriter.writeLabel(whileLabel);		
		writer.write(indent + "<whileStatement>\n");
		indent += tab;
		writer.write(indent + tokens.get(position++) + "\n");	// write keyword while
		if (tokens.get(position).contains("<symbol> ( </symbol>"))
			writer.write(indent + tokens.get(position++) + "\n");	// write ')'
		else
			System.out.println("Error: expecting '(' before while condition");
		compileExpression();
		if (tokens.get(position).contains("<symbol> ) </symbol>"))
			writer.write(indent + tokens.get(position++) + "\n");	// write ')'
		else
			System.out.println("Error: expecting ')' before while statements");
		vmWriter.writeArithmetic("not");
		vmWriter.writeIf(continueLabel);
		if (tokens.get(position).contains("<symbol> { </symbol>"))
			writer.write(indent + tokens.get(position++) + "\n");	// write '{'
		else
			System.out.println("Error: expecting '{' before while statements");
		compileStatements();
		if (tokens.get(position).contains("<symbol> } </symbol>"))
			writer.write(indent + tokens.get(position++) + "\n");	// write '}'
		vmWriter.writeGoto(whileLabel);
		vmWriter.writeLabel(continueLabel);
		indent = indent.substring(2);
		writer.write(indent + "</whileStatement>\n");

	}
	
	public void compileDo()
	{
		writer.write(indent + "<doStatement>\n");			 	// write do statement declaration
		indent += tab;
		writer.write(indent + tokens.get(position++) + "\n"); 		// write "do"
		compileSubroutineCall();
		writer.write(indent + tokens.get(position++) + "\n"); 		// write ';'
		indent = indent.substring(2);
		writer.write(indent + "</doStatement>\n");	
		vmWriter.writePop("temp", 0);
	}
	
	public void compileReturn()
	{
		writer.write(indent + "<returnStatement>\n");
		indent += tab;
		writer.write(indent + tokens.get(position++) + "\n");	// write 'return' keyword
		if (!tokens.get(position).contains("<symbol> ; </symbol>"))
			compileExpression();
		else
			vmWriter.writePush("constant", 0);
		if (tokens.get(position).contains("<symbol> ; </symbol>"))
			writer.write(indent + tokens.get(position++) + "\n");	// write ';'
		else
			System.out.println("Error: expecting ';' after return statement");
		indent = indent.substring(2);
		writer.write(indent + "</returnStatement>\n");
		vmWriter.writeReturn();
	}
	
	public void compileExpressionList()
	{
		nArgs = 0;
		writer.write(indent + "<expressionList>\n");
		indent += tab;
		if (!tokens.get(position).contains("<symbol> ) </symbol>")) // empty list
		{
			nArgs++;
			compileExpression();
			while (tokens.get(position).contains("<symbol> , </symbol>"))
			{
				writer.write(indent + tokens.get(position++) + "\n");	// write ','
				compileExpression();
				nArgs++;
			}
		}
		indent = indent.substring(2);
		writer.write(indent + "</expressionList>\n");
	}
	
	public void compileExpression()
	{
		writer.write(indent + "<expression>\n");
		indent += tab;
		compileTerm();
		if (isOp(tokens.get(position)))
		{
			String op = getToken(tokens.get(position));
			writer.write(indent + tokens.get(position++) + "\n");	// write operator
			compileTerm();
			if (op.equals("&lt;"))
				vmWriter.writeArithmetic("lt");
			else if (op.equals("&gt;"))
				vmWriter.writeArithmetic("gt");
			else if (op.equals("&amp;"))
				vmWriter.writeArithmetic("and");
			else if (op.equals("+"))
				vmWriter.writeArithmetic("add");
			else if (op.equals("-"))
				vmWriter.writeArithmetic("sub");
			else if (op.equals("*"))
				vmWriter.writeCall("Math.multiply", 2);
			else if (op.equals("/"))
				vmWriter.writeCall("Math.divide", 2);
			else if (op.equals("="))
				vmWriter.writeArithmetic("eq");
			else if (op.equals("|"))
				vmWriter.writeArithmetic("or");
			
		}
		indent = indent.substring(2);
		writer.write(indent + "</expression>\n");
	}
	
	public void compileTerm()
	{
		writer.write(indent + "<term>\n");
		indent += tab;
		String varName = getToken(tokens.get(position));
		if (tokens.get(position).contains("<identifier>"))
		{
			if (tokens.get(position+1).contains("<symbol> [ </symbol>"))	// if array access
			{
				vmWriter.writePush(symbolTable.kindOf(varName), symbolTable.indexOf(varName));
				writer.write(indent + tokens.get(position++) + "\n");	// write var name
				writer.write(indent + tokens.get(position++) + "\n");	// write '['
				compileExpression();
				writer.write(indent + tokens.get(position++) + "\n");	// write ']'
				vmWriter.writeArithmetic("add");	// calc based + displacement and pop address into that
				vmWriter.writePop("pointer", 1);	
				vmWriter.writePush("that", 0); 		// push pointer to array[k] onto stack
			}
			else if (tokens.get(position+1).contains("<symbol> . </symbol>") ||
						tokens.get(position+1).contains("<symbol> ( </symbol>"))	// if subroutine call
				compileSubroutineCall();
			else
			{
				writer.write(indent + tokens.get(position++) + "\n");	// write var name
				vmWriter.writePush(symbolTable.kindOf(varName), symbolTable.indexOf(varName));
			}
		}
		else
		{
			if(isUnaryOp(tokens.get(position)))
			{	
				writer.write(indent + tokens.get(position++) + "\n");	// write unary operator
				compileTerm();							// recursive call
				if (varName.equals("-"))
					vmWriter.writeArithmetic("neg");
				else if (varName.equals("~"))
					vmWriter.writeArithmetic("not");
			}
			else if (tokens.get(position).contains("<symbol> ( </symbol>"))
			{
				writer.write(indent + tokens.get(position++) + "\n");	// write '('
				compileExpression();
				writer.write(indent + tokens.get(position++) + "\n");	// write ')'			
			}
			else if (isKeywordConstant(tokens.get(position)) || 
					tokens.get(position).contains("<stringConstant>") ||
					tokens.get(position).contains("<integerConstant>"))
			{
				writer.write(indent + tokens.get(position++) + "\n");	// write ')'	
				if (tokens.get(position-1).contains("<stringConstant>"))
				{
					
					vmWriter.writePush("constant", varName.length());		// allocate heap mem for new string
					vmWriter.writeCall("String.new", 1);
					
					for (int x = 0; x < varName.length(); x++)
					{
						vmWriter.writePush("constant", (int)varName.charAt(x));
						vmWriter.writeCall("String.appendChar", 2);
					}
				}
				else if (tokens.get(position-1).contains("<integerConstant>"))
				{
					vmWriter.writePush("constant", Integer.parseInt(varName));
				}
				else if (tokens.get(position-1).contains("<keyword> true </keyword>"))
				{
					vmWriter.writePush("constant", 0);
					vmWriter.writeArithmetic("not");
				}
				else if (tokens.get(position-1).contains("<keyword> false </keyword>"))
				{
					vmWriter.writePush("constant", 0);
				}
				else if (tokens.get(position-1).contains("<keyword> null </keyword>"))
				{
					vmWriter.writePush("constant", 0);
				}
				else if (tokens.get(position-1).contains("<keyword> this </keyword>"))
				{
					vmWriter.writePush("pointer", 0);
				}

			}
		}
		indent = indent.substring(2);
		writer.write(indent + "</term>\n");			 		// write close term declaration
	}
	////////////////// Work on this one still
	public void compileSubroutineCall()
	{
		String name = getToken(tokens.get(position));
		writer.write(indent + tokens.get(position++) + "\n");		// write function name or var name
		if (tokens.get(position).contains("<symbol> . </symbol>")) //method call
		{
			String objectName = name;
			writer.write(indent + tokens.get(position++) + "\n");			// write '.'
			name = getToken(tokens.get(position));
			String type = symbolTable.typeOf(objectName);
			
			if (type.equals(""))
				name = objectName + '.' + name;
			else
			{
				vmWriter.writePush(symbolTable.kindOf(objectName), symbolTable.indexOf(objectName));
				name = symbolTable.typeOf(objectName) + '.' + name;
			}
			writer.write(indent + tokens.get(position++) + "\n");			// write method name
			writer.write(indent + tokens.get(position++) + "\n");			// write '('
			compileExpressionList();
			writer.write(indent + tokens.get(position++) + "\n");			// write ')'
			if (!type.equals(""))
				nArgs++;
			vmWriter.writeCall(name, nArgs);
		}
		else if (tokens.get(position).contains("<symbol> ( </symbol>"))	// function call
		{
			vmWriter.writePush("pointer", 0);
			writer.write(indent + tokens.get(position++) + "\n");			// write '('
			compileExpressionList();						// write arguments
			nArgs++;
			writer.write(indent + tokens.get(position++) + "\n"); // write ')'
			vmWriter.writeCall(className + "." + name, nArgs);
		}
	}
	
	private boolean isUnaryOp(String token)
	{
		if (token.contains("<symbol> - </symbol>") || 
				token.contains("<symbol> ~ </symbol>"))
			return true;
		else 
			return false;
	}
	
	private boolean isOp(String token)
	{
		if (token.contains("<symbol> + </symbol>") || 
				token.contains("<symbol> - </symbol>") || 
				token.contains("<symbol> * </symbol>") || 
				token.contains("<symbol> / </symbol>") || 
				token.contains("<symbol> &lt; </symbol>") || 
				token.contains("<symbol> &gt; </symbol>") || 
				token.contains("<symbol> &amp; </symbol>") || 
				token.contains("<symbol> | </symbol>") || 
				token.contains("<symbol> = </symbol>"))
			return true;
		else return false;
	}
	
	private boolean isKeywordConstant(String token)
	{
		if (token.contains("<keyword> true </keyword>") ||
				token.contains("<keyword> false </keyword>") ||
				token.contains("<keyword> null </keyword>") ||
				token.contains("<keyword> this </keyword>"))
			return true;
		else
			return false;
	}
	
	private String getToken(String line)
	{
		line = line.substring(line.indexOf('>') + 2);
		line = line.substring(0, line.indexOf('<') - 1);
		return line;
	}
	
	private String segID(String kind)
	{
		if (kind.equals("field"))
			return "this";
		else if (kind.equals("static"))
			return "static";
		else if (kind.equals("var"))
			return "local";		
		else if (kind.equals("argument"))
			return "argument";
		else
			return "none";

	}
}


