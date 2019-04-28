import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;


public class CompilationEngine 
{
	private PrintWriter writer;
	private ArrayList<String> tokens;
	private int position;
	private String indent;
	private String tab = "  ";
	CompilationEngine (Scanner input, PrintWriter output)
	{
		writer = output;
		indent = "";
		tokens = new ArrayList<>();
		position = 0;
		while (input.hasNextLine())
		{
			String ele = input.nextLine().trim();
			if (ele.length() > 0 && !ele.equals("<tokens>") && !ele.equals("</tokens>"))
				tokens.add(ele);
		}

		if (tokens.get(position).contains("<keyword> class </keyword>"))
			compileClass();
		else
			System.out.println("no class" + tokens.get(position));
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
				writer.write(indent + tokens.get(position++) + "\n");					// write variable scope - field | static
				writer.write(indent + tokens.get(position++) + "\n");					// write variable type - primitive or obj
				writer.write(indent + tokens.get(position++) + "\n");					// write variable name
				while (tokens.get(position).contains("<symbol> , </symbol>"))	// while there are more class variables of this type
				{
					writer.write(indent + tokens.get(position++) + "\n");				// write ','
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
			writer.write(indent + tokens.get(position++).toString() + "\n");	// write function || method || constructor
			writer.write(indent + tokens.get(position++) + "\n");  	// write identifier - return type
			writer.write(indent + tokens.get(position++) + "\n");	// write identifier - subroutine name
			compileParameterList();							// write subroutine parameters
			writer.write(indent + "<subroutineBody>\n");
			indent += tab;
			writer.write(indent + tokens.get(position++) + "\n");	// write '{'
			while (tokens.get(position).contains("<keyword> var </keyword>"))	// write all subroutine variables
			{
				writer.write(indent + "<varDec>\n");
				indent += tab;
				writer.write(indent + tokens.get(position++) + "\n");			// write var declaration
				writer.write(indent + tokens.get(position++) + "\n");			// write data type
				writer.write(indent + tokens.get(position++) + "\n");			// write var name
				while (tokens.get(position).contains("<symbol> , </symbol>"))	// declare more vars of same type and scope
				{
					writer.write(indent + tokens.get(position++) + "\n");		// write ','
					writer.write(indent + tokens.get(position++) + "\n");		// write var name
				}
				writer.write(indent + tokens.get(position++) + "\n");			// write ';'
				indent = indent.substring(2);
				writer.write(indent + "</varDec>\n");
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
			System.out.println("Parameter " + tokens.get(position));
			writer.write(indent + tokens.get(position++) + "\n");	// write '('
			writer.write(indent + "<parameterList>\n");
			indent += tab;
			while (tokens.get(position).contains("<keyword>") || 
					tokens.get(position).contains("<identifier>"))
			{
				writer.write(indent + tokens.get(position++) + "\n");	// write param data type
				writer.write(indent + tokens.get(position++) + "\n");	// write param name
				if (tokens.get(position).contains("<symbol> , </symbol>")) // if multiple params
					writer.write(indent + tokens.get(position++) + "\n");	// write ','
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
		writer.write(indent + tokens.get(position++) + "\n");	// write var name
		if (tokens.get(position).contains("<symbol> [ </symbol>"))	// if an array element
		{
			writer.write(indent + tokens.get(position++) + "\n");	// write '[' symbol
			compileExpression();
			writer.write(indent + tokens.get(position++) + "\n");	// write ']' symbol
		}
		
		if (tokens.get(position).contains("<symbol> = </symbol>"))
		{
			writer.write(indent + tokens.get(position++) + "\n");	// write '=' symbol
			compileExpression();
			if (tokens.get(position).contains("<symbol> ; </symbol>"))
				writer.write(indent + tokens.get(position++) + "\n");	// write ';' symbol
			else
				System.out.println("Error: expecting ';' after variable assignment");

		}
		else
			System.out.println("Error: expecting '=' symbol");
		indent = indent.substring(2);
		writer.write(indent + "</letStatement>\n");

	}
	
	public void compileIf()
	{
		writer.write(indent + "<ifStatement>\n");
		indent += tab;
		writer.write(indent + tokens.get(position++) + "\n");	
		if (tokens.get(position).contains("<symbol> ( </symbol>"))
		{
			writer.write(indent + tokens.get(position++) + "\n");	// write '('
			compileExpression();
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
			}
			
		}
		else
			System.out.println("Error: expecting '(' after if declaration");
		indent = indent.substring(2);
		writer.write(indent + "</ifStatement>\n");
	}
	
	public void compileWhile()
	{
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
		if (tokens.get(position).contains("<symbol> { </symbol>"))
			writer.write(indent + tokens.get(position++) + "\n");	// write '{'
		else
			System.out.println("Error: expecting '{' before while statements");
		compileStatements();
		if (tokens.get(position).contains("<symbol> } </symbol>"))
			writer.write(indent + tokens.get(position++) + "\n");	// write '}'
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
		writer.write(indent + "</doStatement>\n");			 		// write do statement declaration
	}
	
	public void compileReturn()
	{
		writer.write(indent + "<returnStatement>\n");
		indent += tab;
		writer.write(indent + tokens.get(position++) + "\n");	// write 'return' keyword
		if (!tokens.get(position).contains("<symbol> ; </symbol>"))
			compileExpression();
		if (tokens.get(position).contains("<symbol> ; </symbol>"))
			writer.write(indent + tokens.get(position++) + "\n");	// write ';'
		else
			System.out.println("Error: expecting ';' after return statement");
		indent = indent.substring(2);
		writer.write(indent + "</returnStatement>\n");

	}
	
	public void compileExpressionList()
	{
		writer.write(indent + "<expressionList>\n");
		indent += tab;
		if (!tokens.get(position).contains("<symbol> ) </symbol>")) // empty list
		{
			compileExpression();
			while (tokens.get(position).contains("<symbol> , </symbol>"))
			{
				writer.write(indent + tokens.get(position++) + "\n");	// write ','
				compileExpression();
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
			writer.write(indent + tokens.get(position++) + "\n");	// write operator
			compileTerm();
		}
		indent = indent.substring(2);
		writer.write(indent + "</expression>\n");
	}
	
	public void compileTerm()
	{
		writer.write(indent + "<term>\n");
		indent += tab;
		if (tokens.get(position).contains("<identifier>"))
		{
			if (tokens.get(position+1).contains("<symbol> [ </symbol>"))	// if array access
			{
				writer.write(indent + tokens.get(position++) + "\n");	// write var name
				writer.write(indent + tokens.get(position++) + "\n");	// write '['
				compileExpression();
				writer.write(indent + tokens.get(position++) + "\n");	// write ']'
			}
			else if (tokens.get(position+1).contains("<symbol> . </symbol>") ||
						tokens.get(position+1).contains("<symbol> ( </symbol>"))	// if subroutine call
				compileSubroutineCall();
			else
				writer.write(indent + tokens.get(position++) + "\n");	// write var name
		}
		else
		{
			if(isUnaryOp(tokens.get(position)))
			{
				writer.write(indent + tokens.get(position++) + "\n");	// write unary operator
				compileTerm();							// recursive call
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
				writer.write(indent + tokens.get(position++) + "\n");	// write ')'			
		}
		indent = indent.substring(2);
		writer.write(indent + "</term>\n");			 		// write close term declaration
	}
	
	public void compileSubroutineCall()
	{
		writer.write(indent + tokens.get(position++) + "\n");		// write function name or var name
		if (tokens.get(position).contains("<symbol> . </symbol>")) //method call
		{
			writer.write(indent + tokens.get(position++) + "\n");			// write '.'
			writer.write(indent + tokens.get(position++) + "\n");			// write method name
			writer.write(indent + tokens.get(position++) + "\n");			// write '('
			compileExpressionList();						// write arguments
			writer.write(indent + tokens.get(position++) + "\n");			// write ')'
		}
		else if (tokens.get(position).contains("<symbol> ( </symbol>"))	// function call
		{
			writer.write(indent + tokens.get(position++) + "\n");			// write '('
			compileExpressionList();						// write arguments
			writer.write(indent + tokens.get(position++) + "\n");			// write ')'
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
}


