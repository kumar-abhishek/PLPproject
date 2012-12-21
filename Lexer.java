import java.io.*;
import java.util.logging.Logger;

class Result{
	public boolean success;
	public Token token;
	Result(boolean suc, Token t){
		success = suc;
		token = t;
	}
}

class Lexer{
	private static final Logger logger = Logger.getLogger(Lexer.class.getName());
	int curPosInLine;
	BufferedReader reader = null;
	String currentLine;
	int curLineNum;
	
	//ArrayList<String> curLineWordList;
	int curIdx; //current index in the current Line
	Lexer(String fileName) throws IOException {
		try{
			reader = new BufferedReader(new FileReader(fileName));
		}
		catch(IOException e){
			//logger.info("file " + fileName + " not found");
			System.exit(1);
		}
		currentLine = reader.readLine();
		if(currentLine == null){
			//logger.info("Empty source program, Exiting!!!");
			System.exit(1);
		}
		curLineNum = 1;
		curIdx = 0;
		//curLineWordList = new ArrayList<String>(Arrays.asList(currentLine.split("\\s")));
	}
	
	boolean isValidEscapeChar(char char1, char char2){
		if(char1 == 92) { //check for backslash \
			switch(char2){
			case 92:  	//check for \\
				return true;
			case 39:  	//check for \'
				return true;
			case 't':	//check for \t
				return true;
			case 'n':	//check for \n
				return true;
			default:
				return false;
			}
		}
		return false;
	}

	boolean isOperator(char testChar){
		String operators = "'\"+-*<>&.@/:=~|$!#%^_[]{}`?";
		//may want to check for more characters in testString, and not just the 1st character !!!!!!!!!!!!!!!!!!!
		if(operators.indexOf(testChar) != -1){
			return true;
		}
		else{
			return false;
		}
	}

	boolean isSpace(char testChar){
		return Character.isWhitespace(testChar);
	}

	boolean isWhiteSpace(String testString){
		for(int i = 0;i< testString.length(); i++){
			if(!Character.isWhitespace(testString.charAt(i))){
				return false;
			}
		}
		return true;
	}

	TokenType getIdentifierType(String testString){
		if(testString.length() == 2){
			if(testString.equals("in")){
				return TokenType.IN;
			}
			else if(testString.equals("gr")){
				return TokenType.GR;
			}
			else if(testString.equals("ge")){
				return TokenType.GE;
			}
			else if(testString.equals("ls")){
				return TokenType.LS;
			}
			else if(testString.equals("le")){
				return TokenType.LE;
			}
			else if(testString.equals("eq")){
				return TokenType.EQUALS;
			}
			else if(testString.equals("ne")){
				return TokenType.NE;
			}
			else if(testString.equals("or")){
				return TokenType.OR;
			}
			else if(testString.equals("fn")){
				return TokenType.FN;
			}
		}
		else if(testString.length() == 3){
			if(testString.equals("let")){
				return TokenType.LET;
			}
			else if(testString.equals("not")){
				return TokenType.NOT;
			}
			else if(testString.equals("neg")){
				return TokenType.NEG;
			}
			else if(testString.equals("nil")){
				return TokenType.NIL;
			}
			else if(testString.equals("and")){
				return TokenType.AND;
			}
			else if(testString.equals("rec")){
				return TokenType.REC;
			}
			else if(testString.equals("aug")){
				return TokenType.AUG;
			}
		}
		else if(testString.length() == 4){
			if(testString.equals("true")){
				return TokenType.TRUE;
			}
		}
		else if(testString.length() == 5){
			if(testString.equals("false")){
				return TokenType.FALSE;
			}
			else if(testString.equals("where")){
				return TokenType.WHERE;
			}
			else if(testString.equals("dummy")){
				return TokenType.DUMMY;
			}
		}
		else if(testString.length() == 6){
			if(testString.equals("within")){
				return TokenType.WITHIN;
			}
		}
		//none of above matched
		return TokenType.ID;
		
	}

	boolean isAlphabet(char ch){
		if( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ){
			return true;
		}
		return false;
	}
	
	Result makeIdentifier(String testString){
		Result r;
		int i = curIdx;
		int prevIdx = i;
		if(isAlphabet(testString.charAt(i++))){
			while(i < testString.length() && (Character.isDigit(testString.charAt(i)) || isAlphabet(testString.charAt(i)) || (testString.charAt(i) == '_'))){
				++i;
			}
			String testToken = testString.substring(prevIdx, i); 
			curIdx = i;
			r = new Result(true, new Token(getIdentifierType(testToken), testToken, testToken.length(), curLineNum));
		}
		else{
			r = new Result(false, null);
		}
		return r;
	}
		
	Result makeString(String testString){
		logger.info("str");
		Result r = null;
		int i = curIdx;
		int prevIdx = i;
		logger.info("" + testString.charAt(i));
		if((testString.charAt(i)) == 39){ //check for a apostrophe
			logger.info("isApostrophe");
			++i;
			//while(Character.isLetterOrDigit(testString.charAt(i)) || isSpace(testString.charAt(i))
			//		|| isOperator(testString.charAt(i)) || /* isPunc(testString.charAt(i)) || */ isValidEscapeChar(testString.charAt(i), testString.charAt(i+1) )){
			while(testString.charAt(i) != 39){
				logger.info("printin i : " + i);
				if((testString.length()>1) && isValidEscapeChar(testString.charAt(i), testString.charAt(i+1))){
					i += 2;
				}
				else {
					++i;
				}
			}
			if(testString.charAt(i) == 39) {
				String testToken = testString.substring(prevIdx+1, i);
				curIdx = i+1;
				r= new Result(true, new Token(TokenType.STRING, testToken, testToken.length(), curLineNum));
			}
		}
		else{
			r = new Result(false, null);
		}
		return r;
	}
	
	boolean makeComment(String testString){
		if( ( (testString.length()-(curIdx+1)) >= 2 ) && (testString.charAt(curIdx) == '/') && (testString.charAt(curIdx+1) == '/')){
			logger.info("ya i am a comment");
			return true;
		}
		return false;
	}
	
	Result makeInteger(String testLine){
		Result r = null;
		int i = curIdx;
		int prevIdx = i;
		if(Character.isDigit(testLine.charAt(i)) ){
			while( (i < testLine.length()) && Character.isDigit(testLine.charAt(i)) ){
				++i;
			}
			String testToken = testLine.substring(prevIdx, i);
			curIdx = i;
			r = new Result(true, new Token(TokenType.INTEGER, testToken, testToken.length(), curLineNum ));
		}
		else{
			r = new Result(false, null);
		}
		return r;
	}

	Result makeDelimiter(String testString){
		String puncList = "();,";
		Result r = null;
		char ch = testString.charAt(curIdx);
		if(puncList.indexOf(ch) != -1){
			TokenType t = null; 
			switch(ch){
			case '(':
				t = TokenType.L_PAREN;
				break;
			case ')':
				t = TokenType.R_PAREN;
				break;
			case ';':
				t = TokenType.SEMI;
				break;
			case ',':
				t = TokenType.COMMA;
				break;	
			}
			curIdx += 1;
			r = new Result(true, new Token(t, String.valueOf(ch), 1, curLineNum ));
		}
		else{
			r = new Result(false, null);
		}
		return r;
	}

	TokenType getTypeReservedOperators(char operator){
		switch(operator){
			case '.' : 
				return TokenType.PERIOD;
			case '&' :
				return TokenType.AND_BIN; 
			case '*' :
				return TokenType.TIMES; 
			case '+' : 
				return TokenType.PLUS; 
			case '-' : 
				return TokenType.MINUS; 
			case '/' : 
				return TokenType.DIVIDE; 
			case '@' : 
				return TokenType.AT; 
			case '|' : 
				return TokenType.BAR; 
			case '=' : 
				return TokenType.EQUALS;
			default:
				return TokenType.OP; //generic other operator
		}
	}
	
	Result makeOperator(String testString){
		//logger.info("Lexer: i m here #1");
		Result r = null;
		int i = curIdx;
		if(isOperator(testString.charAt(i))){
			//logger.info("Lexer: i m here #2");
			//check for 2 character operators: >=, <=, ->, **
			if(testString.charAt(i) == '>' &&  testString.charAt(i+1) == '='){
				curIdx += 2;
				r = new Result(true, new Token(TokenType.GE, "ge", 2, curLineNum));
				return r;
			}
			else if(testString.charAt(i) == '<' && testString.charAt(i+1) == '='){
				curIdx += 2;
				r = new Result(true, new Token(TokenType.LE, "le", 2, curLineNum));
				return r;
			}
			else if(testString.charAt(i) == '-' && testString.charAt(i+1) == '>'){
				curIdx += 2;
				r = new Result(true, new Token(TokenType.CONDITIONAL, "->", 2, curLineNum));
				return r;
			}	
			else if(testString.charAt(i) == '*' && testString.charAt(i+1) == '*'){
				curIdx += 2;
				r = new Result(true, new Token(TokenType.EXPO, "**", 2, curLineNum));
				return r;
			}
			else {
				//if any of these  '\"+-*<>&.@/:=~|$!#%^_[]{}`?
				//logger.info("ya i m here");
				++curIdx;
				TokenType type = getTypeReservedOperators(testString.charAt(i));
				r = new Result(true, new Token(type, String.valueOf(testString.charAt(i)), 1, curLineNum));
			}
		
		}
		else{
			//logger.info("ya i m here2");
			r = new Result(false, null);
		}
		return r;
	}
	
	/*
	 * Returns 1 if able to make a delimiter from the set: ();,
	 */
	
	/*
	 * gets next token in the currentLine if possible
	 * otherwise moves currentLine to next line in file
	 */
	Token scan() throws IOException{
		Result r = null;
		
		while((curIdx < currentLine.length()) &&  Character.isWhitespace(currentLine.charAt(curIdx))){
			++curIdx;
		}
		//logger.info("here");
		if(curIdx >= currentLine.length()){
			currentLine = reader.readLine();
			//logger.info("after reading : currentLine:" + currentLine+ "|ends");

			//logger.info("here1");

			if(currentLine == null){
				//logger.info("hmmmmmmmm");
			}
			
			String newline = System.getProperty("line.separator");
			if( !(currentLine == null) &&  (currentLine.equals(newline) || currentLine.equals("") || currentLine.equals("\r") || currentLine.equals("\n"))){
				logger.info("here3");
				curLineNum += 1;
				currentLine = reader.readLine();
			}
				
			
			//logger.info("final");
			if(currentLine == null){
				//logger.info("done with program");
				//done with lexing the program.
				Token token = new Token(TokenType.EOF, "EOF", 3, curLineNum);
				return token;
			}
			curLineNum += 1;
			curIdx = 0;
			//curLineWordList = new ArrayList<String>(Arrays.asList(currentLine.split("\\s")));
		}
		Token token = null;
		//logger.info("here: curIdx: " + curIdx + "|  line:" +(currentLine) + "|done");
		while((curIdx < currentLine.length()) && Character.isWhitespace(currentLine.charAt(curIdx))){
			logger.info("ok within while| curIdx: "  + curIdx);
			++curIdx;
		}
		if(curIdx >= currentLine.length()){
			logger.info("heree: done eeeee");
			logger.info(currentLine );
			curLineNum += 1;
			currentLine = reader.readLine();
			logger.info(currentLine );
			curIdx = 0;
			if(currentLine == null){
				//logger.info("done with program");
				//done with lexing the program.
				token = new Token(TokenType.EOF, "EOF", 3, curLineNum);
				return token;
			}
			while((curIdx < currentLine.length()) &&  Character.isWhitespace(currentLine.charAt(curIdx))){
				++curIdx;
			}
			while((currentLine != null) && currentLine.equals("")){
				logger.info("is empty");
				curLineNum += 1;
				curIdx = 0;
				currentLine = reader.readLine();
			}
			if(currentLine == null){
				logger.info("done with program");
				//done with lexing the program.
				token = new Token(TokenType.EOF, "EOF", 3, curLineNum);
				return token;
			}
		}
		while((curIdx < currentLine.length()) &&  Character.isWhitespace(currentLine.charAt(curIdx))){
			++curIdx;
		}
		
		logger.info("ok | curIdx: "  + curIdx + "| currentLine: "+ currentLine);
		
		if(makeComment(currentLine)){
			logger.info("\n\n\n\n\n\n\nyes it was  a comment\n\n\n");
			currentLine = reader.readLine();
			//may need some fix here regarding currentLine null checking !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			curIdx = 0; // TODO : make this change
			curLineNum += 1;
			token = scan();
			//currentLine = reader.readLine();
		}
		else if( (r = makeIdentifier(currentLine)).success){
			logger.info("ok3");
			token = r.token;
			logger.info("ok4");
		}
		else if ((r=makeString(currentLine)).success){
			logger.info("ok1");
			token = r.token;
			logger.info("ok2");
		}
		else if((r=makeOperator(currentLine)).success){
			token = r.token;
			//logger.info("ok5");
		}
		else if((r = makeInteger(currentLine)).success){
			token = r.token;
		}
		else if((r = makeDelimiter(currentLine)).success){
			token = r.token;
		}
		else {
			logger.info("\n\n\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!ERROR CASE IN Lexer!!!!!!!!!!!!!!!!!!!!!!!\n\n\n\n\n\n\n\n");
		}
		logger.info("Lexer: returning tokentype: "  + token.type + " | name: " + token.name) ;
		return token;
	}
	
}