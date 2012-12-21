 enum TokenType {
	ID, INTEGER, STRING, EOF, L_PAREN, R_PAREN, SEMI, COMMA,
	IN, GR, GE, LS, LE, NE, OR, FN, AT, 
	OP , //for any of these: '\"+-*<>&.@/:=~|$!#%^_[]{}`?
	LET, NOT, NEG, NIL, AND, AND_BIN, REC, AUG, BAR,  
	EXPO, TRUE, PLUS, 
	WHERE, FALSE, DUMMY, TIMES, MINUS,   
	WITHIN, PERIOD, DIVIDE, EQUALS,
	CONDITIONAL, 
	ERROR, //not possible : just to check for error
	FUNCTION_FORM, GAMMA, TAU, LAMBDA, YSTAR // new tokenTypes: only for tree construction
	//define types here
	
}
public class Token{
	public TokenType type;
	public String name; 
	public int length; //the length of token //needed??
	public int location; //the line number of the token in the file
	Token(TokenType typ, String n, int len, int loc){
		type = typ;
		name = n;
		length = len;
		location = loc;
	}
	Token(){
		
	}
	Token(TokenType typ, String tokenName){
		type = typ;
		name = tokenName;
		length = tokenName.length();
		location = -1; //not known
	}
	void printTokenAttributes(){
		System.out.println(this.type + "\t" + this.name + "\t" + this.length + "\t" + this.location );
	}
}