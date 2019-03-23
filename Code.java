public class Code							// converts asm fields into binary codes
{
	public String dest(String dst)			// returns binary code for asm instr destination field
	{
		if(dst.compareTo("null") == 0)
			return "000";
		else if(dst.compareTo("M") == 0)
			return "001";
		else if(dst.compareTo("D") == 0)
			return "010";
		else if(dst.compareTo("MD") == 0)
			return "011";
		else if(dst.compareTo("A") == 0)
			return "100";
		else if(dst.compareTo("AM") == 0)
			return "101";
		else if(dst.compareTo("AD") == 0)
			return "110";
		else
			return "111";
	}
	
	public String comp(String cmp)			// returns binary code for asm instr computation field			
	{
		if (cmp.compareTo("0") == 0)
			return "101010";
		else if (cmp.compareTo("1") == 0)
			return "111111";
		else if (cmp.compareTo("-1") == 0)
			return "111010";
		else if (cmp.compareTo("D") == 0)
			return "001100";
		else if (cmp.compareTo("A") == 0 || 
				 cmp.compareTo("M") == 0)
			return "110000";
		else if (cmp.compareTo("!D") == 0)
			return "001101";
		else if (cmp.compareTo("!A") == 0 ||
				 cmp.compareTo("!M") == 0)
			return "110001";
		else if (cmp.compareTo("-D") == 0)
			return "001111";
		else if (cmp.compareTo("-A") == 0 ||
		 		 cmp.compareTo("-M") == 0)
			return "001111";
		else if (cmp.compareTo("D+1") == 0)
			return "011111";
		else if (cmp.compareTo("A+1") == 0 ||
				 cmp.compareTo("M+1") == 0)
			return "110111";
		else if (cmp.compareTo("D-1") == 0)
			return "001110";
		else if (cmp.compareTo("A-1") == 0 ||
				 cmp.compareTo("M-1") == 0)
			return "110010";
		else if (cmp.compareTo("D+A") == 0 ||
				 cmp.compareTo("D+M") == 0)
			return "000010";
		else if (cmp.compareTo("D-A") == 0 ||
				 cmp.compareTo("D-M") == 0)
			return "010011";
		else if (cmp.compareTo("A-D") == 0 ||
				 cmp.compareTo("M-D") == 0)
			return "000111";
		else if (cmp.compareTo("D&A") == 0 ||
				 cmp.compareTo("D&M") == 0)
			return "000000";
		else
			return "010101";
	}
	
	public String jump(String jmp)			// returns binary code for asm instr jump field
	{
		if (jmp.compareTo("null") == 0)
			return "000";
		else if (jmp.compareTo("JGT") == 0)
			return "001";
		else if (jmp.compareTo("JEQ") == 0)
			return "010";
		else if (jmp.compareTo("JGE") == 0)
			return "011";
		else if (jmp.compareTo("JLT") == 0)
			return "100";
		else if (jmp.compareTo("JNE") == 0)
			return "101";
		else if (jmp.compareTo("JLE") == 0)
			return "110";
		else 
			return "111";
	}
	
}