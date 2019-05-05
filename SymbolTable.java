import java.util.HashMap;

public class SymbolTable 
{
	private HashMap<String, Symbol> methodScope;
	private HashMap<String, Symbol> classScope;
	private int staticCount;
	private int fieldCount;
	private int argumentCount;
	private int varCount;
	
	SymbolTable()
	{
		methodScope = new HashMap<>();
		classScope = new HashMap<>();
		staticCount = fieldCount = argumentCount = varCount = 0;
	}
	
	public void startSubroutine()
	{
		methodScope.clear();
		argumentCount = 0;
		varCount = 0;
	}
	
	public void define(String name, String type, String kind)
	{
		if (kind.equals("static"))
		{
			classScope.put(name, new Symbol(type, kind, staticCount++));
		}
		else if (kind.contentEquals("this"))
		{
			classScope.put(name, new Symbol(type, kind, fieldCount++));
		}
		else if (kind.contentEquals("argument"))
		{
			methodScope.put(name, new Symbol(type, kind, argumentCount++));
		}
		else if (kind.contentEquals("local"))
		{
			methodScope.put(name, new Symbol(type, kind, varCount++));
		}
		else 
			System.out.println("FUCKED UP: fuck    " + type);
	}
	
	public int varCount(String type)
	{
		if (type.equals("static"))
			return staticCount;
		else if (type.equals("field"))
			return fieldCount;
		else if (type.equals("argument"))
			return argumentCount;
		else if (type.equals("var"))
			return varCount;
		else
			return -1;
	}
	
	public String kindOf(String name)
	{
		if (methodScope.containsKey(name))
			return methodScope.get(name).getKind();
		else if (classScope.containsKey(name))
			return classScope.get(name).getKind();
		else
			return "NONE";
	}
	
	public String typeOf(String name)
	{
		if (methodScope.containsKey(name))
			return methodScope.get(name).getType();
		else if (classScope.containsKey(name))
			return classScope.get(name).getType();
		else
			return "";
	}
	
	public int indexOf(String name)
	{
		if (methodScope.containsKey(name))
			return methodScope.get(name).getIndex();
		else if (classScope.containsKey(name))
			return classScope.get(name).getIndex();
		else
			return -1;
	}
}
