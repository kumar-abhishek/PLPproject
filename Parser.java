import java.io.IOException;
import java.util.Arrays;

//RPAL grammar

public class Parser{
	Token nextToken;
	Lexer lexer = null;
	TreeBuilder treeBuilder;
	
	Parser(String fileName){
		try{
			lexer = new Lexer(fileName);
			nextToken = lexer.scan();  //call the lexer
			treeBuilder = new TreeBuilder(); 
		}
		catch(IOException e){
			//System.out.println("file not found");
			System.exit(1);
		}
	}
	
	void Read(Token token) { //throws IOException
		//System.out.println("reading : " + token.name);
		if( (token.type == TokenType.ID) || (token.type == TokenType.INTEGER) || (token.type == TokenType.STRING) ){
			//System.out.println("nhmmmm: nextToken: " + nextToken.type + "| " + nextToken.name);
			
			//TODO: needs cleanup!!!!!!!!!!!!!!!!!
			/*
			String tokenName = null;
			if(token.type == TokenType.ID){
				//System.out.println("in Parser ID: I am here!!!" );
				tokenName = "<ID:" + token.name + ">";
			}
			else if(token.type == TokenType.INTEGER){
				tokenName = "<INT:" + token.name + ">";
			}
			else if(token.type == TokenType.STRING){
				tokenName = "<STR:'" + token.name + "'>";
			}
			*/
			treeBuilder.BuildTree(token, 0);
		}
		try { 
			nextToken = lexer.scan();  //call the lexer
			//nextToken.printTokenAttributes();
			//System.out.println("Read() : 2: nhmmmm: nextToken: " + nextToken.type + "| " + nextToken.name);
			if(nextToken.type == TokenType.EOF){
				//System.out.println("DONE PARSING THE PROGRAM");
				return;
			}
		}
		catch(IOException e){
			System.out.println(" !!!!!! error !!!!! ");  //TODO: check for proper error handling here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		}
	}
	void ParseError(String tokenName, Token foundToken){
		System.out.println("Expected token '" + tokenName + "' but found '" + foundToken.name + "' on line num " + foundToken.location);
		System.exit(1);
	}
	
	/*
	 * # Expressions ############################################
		E	->'let' D 'in' E		=> 'let'
			->'fn' Vb+ '.' E		=> 'lambda'
			->Ew;
	 */	
	void E(){
		//System.out.println("E()");
		//System.out.println("yep i m here 10");
		if(nextToken.type == TokenType.LET){ 
			Token letToken = nextToken;
			Read(nextToken); //let
			D();
			if(nextToken.type != TokenType.IN){
				ParseError("in", nextToken);
			}
			//System.out.println("yep i m here 3");
			Read(nextToken); //in
			E();
			//System.out.println("yep i m here 2: nextToken" + nextToken.name + " |type: " + nextToken.type);
			treeBuilder.BuildTree(letToken, 2);
		}
		else if(nextToken.type == TokenType.FN){
			//System.out.println("yep i m here 11");
			Token lambdaToken = new Token(TokenType.LAMBDA, "lambda");
			Read(nextToken); //fn
			int n = 0;
			Vb();
			while((nextToken.type == TokenType.ID) || (nextToken.type == TokenType.L_PAREN)){
				Vb();
				++n;
			} 
			if(!((nextToken.name).equals("."))){
				ParseError(".", nextToken);
			}
			Read(nextToken); //. (dot)
			E();
			treeBuilder.BuildTree(lambdaToken, n+2);
		}
		else{
			//System.out.println("yep i m here 12, nextToken: "+ nextToken.name);
			Ew();
		}
		//System.out.println("yep i m here 1");
	}
	
	/*
	 * Ew	->T 'where' Dr			=> 'where'
			->T;
	 */
	void Ew(){
		//System.out.println("Ew()");
		T();
		if(nextToken.type == TokenType.WHERE){
			Token whereToken = nextToken;
			Read(nextToken);
			Dr();
			treeBuilder.BuildTree(whereToken, 2);
		}
	}
	
	/* 
	 *	# Tuple Expressions ######################################
		T 	-> Ta ( ',' Ta )+ => 'tau'
			-> Ta ;
	 */
	void T(){
		//System.out.println("T()");
		Ta();
		//System.out.println("\n\n\nhere in T()\n\n\n\n");
		if(nextToken.type == TokenType.COMMA){
			//Token tauToken = nextToken;
			Token tauToken = new Token(TokenType.TAU, "tau");
			int n=0;
			do{
				Read(nextToken);
				Ta();
				++n;
			} while(nextToken.type == TokenType.COMMA);
			//System.out.println("T(): value of n: " + n);
			treeBuilder.BuildTree(tauToken, n+1);
		}
	}	
	/*
	 * 		Ta 	-> Ta 'aug' Tc => 'aug'
				-> Tc ;
	 */
	void Ta(){
		//System.out.println("Ta()");
		Tc();
		while(nextToken.type == TokenType.AUG){
				Token augToken = nextToken;
				Read(nextToken);
				Tc();
				treeBuilder.BuildTree(augToken, 2);
		} 
	}
	/*
	 * Tc 	-> B '->' Tc '|' Tc => '->'
			-> B ;
	 */
	void Tc(){
		//System.out.println("Tc()");
		B();
		if(nextToken.type == TokenType.CONDITIONAL){
			Token condToken = nextToken;
			Read(nextToken);
			Tc();
			if(nextToken.type != TokenType.BAR){
				ParseError("|", nextToken);
			}
			Read(nextToken);
			Tc();
			treeBuilder.BuildTree(condToken, 3); // ->
		}
	}
	
	/*
	 * # Boolean Expressions ####################################
		B 	-> B 'or' Bt => 'or'
			-> Bt ;	
	 */
	void B(){
		//System.out.println("B()");
		Bt();
		while(nextToken.type == TokenType.OR){
				Token orToken = nextToken;
				Read(nextToken);
				Bt();
				treeBuilder.BuildTree(orToken, 2);	 
		}
	}
	/*
	 * Bt	-> Bt '&' Bs => '&'
			-> Bs ;
	 */
	void Bt(){
		//System.out.println("Bt()");
		Bs();
		while(nextToken.type == TokenType.AND_BIN){
				Token andToken = nextToken;
				Read(nextToken);
				Bs();
				treeBuilder.BuildTree(andToken, 2);  
		}
	}
	
	/*
	 * Bs	-> 'not' Bp => 'not'
			-> Bp ;
	 */
	void Bs(){
		//System.out.println("Bs()");
		if(nextToken.type == TokenType.NOT){
			Token notToken = nextToken;
			Read(nextToken);
			Bp();
			treeBuilder.BuildTree(notToken, 1);
		}
		else{
			Bp();
		}
	}
	/*
	 * Bp 	-> A ('gr' | '>' ) A => 'gr'
			-> A ('ge' | '>=') A => 'ge'
			-> A ('ls' | '<' ) A => 'ls'
			-> A ('le' | '<=') A => 'le'
			-> A 'eq' A => 'eq'
			-> A 'ne' A => 'ne'
			-> A ;
	 */
	void BpHelper(Token token){
		Read(nextToken);
		A();
		treeBuilder.BuildTree(token, 2);
	}
	void Bp(){
		//System.out.println("Bp()");
		A();
		if(Arrays.asList("gr", ">", "ge", ">=", "ls", "<", "le", "<=", "eq", "ne").contains(nextToken.name)){
			BpHelper(nextToken);
			/*
			switch(nextToken.name){
			case "gr":
				BpHelper(nextToken);
				break;
			case ">":
				BpHelper("gr");
				break;
			case "ge":
				BpHelper("ge");
				break;
			case ">=":
				BpHelper("ge");
				break;
			case "ls":
				BpHelper("ls");
				break;
			case "<":
				BpHelper("ls");
				break;
			case "le":
				BpHelper("le");
				break;
			case "<=":
				BpHelper("le");
				break;
			case "eq":
				BpHelper("eq");
				break;
			case "ne":
				BpHelper("ne");
				break;
			}
		*/
		}
	}
	/*
	 * # Arithmetic Expressions #################################
		A 	-> A '+' At => '+'
			-> A '-' At => '-'
			->	 '+' At
			->	 '-'At =>'neg'
			-> At ;
	 */
	void A(){
		//System.out.println("A()");
		switch(nextToken.name){
			case "+":
				Read(nextToken);
				At();
				break;
			case "-":
				//Token negToken = nextToken;
				Token negToken = new Token(TokenType.NEG, "neg");
				Read(nextToken);
				At();
				treeBuilder.BuildTree(negToken, 1);
				break;
			default:
				At();
		}
		while(nextToken.name.equals("+") || nextToken.name.equals("-")){
			Token currentToken = nextToken; //save present token
			Read(nextToken);
			At();
			treeBuilder.BuildTree(currentToken, 2);
		}
	}
	/*
	 * 		At 	-> At '*' Af => '*'
				-> At '/' Af => '/'
				-> Af ;
	 */
	void At(){
		//System.out.println("At()");
		Af();
		while(nextToken.name.equals("*") || nextToken.name.equals("/")){
			Token currentToken = nextToken; //save present token
			Read(nextToken);
			Af();
			treeBuilder.BuildTree(currentToken, 2);
		}		
	}
	/*
	 * 		Af 	-> Ap '**' Af => '**'
				-> Ap ;
	 */
	void Af(){
		//System.out.println("Af()");
		Ap();
		while(nextToken.name.equals("**")){
			Token expToken = nextToken;
			Read(nextToken);
			Ap();
			treeBuilder.BuildTree(expToken, 2); // **
		}
	}
	/*
	 * 		Ap 	-> Ap '@' '<IDENTIFIER>' R => '@'
				-> R ;
	 */
	void Ap(){
		//System.out.println("Ap()");
		R();
		while(nextToken.name.equals("@")){
			Token atToken = nextToken;
			Read(nextToken);
			if(nextToken.type != TokenType.ID){
				ParseError("ID", nextToken);
			}
			Read(nextToken);
			R();
			treeBuilder.BuildTree(atToken, 3); // @
		}
	}
	
	/*
	 * # Rators And Rands #######################################
			R 	-> R Rn => 'gamma'
				-> Rn ;
	 */
	void R(){
		//System.out.println("R()");
		if(Rn() == true){
			while(Rn() == true){
				//treeBuilder.BuildTree("gamma", 2);
				Token gammaToken = new Token(TokenType.GAMMA, "gamma");
				treeBuilder.BuildTree(gammaToken, 2);
			}
		}
		/*
		//System.out.println("\n\n\n\n Ya I am in 1st line of R()\n\n\n");
		Rn();
		//System.out.println("\n\n\n\n Ya I am above while of R()\n\n\n");
		while(Arrays.asList(TokenType.ID, TokenType.INTEGER, TokenType.STRING, TokenType.TRUE, TokenType.FALSE, TokenType.NIL, TokenType.L_PAREN, TokenType.DUMMY).contains(nextToken.type)){
			//System.out.println("\n\n\n\n Ya I am within while of R(): " + nextToken.name + "\n\n\n");
			Rn();
			treeBuilder.BuildTree("gamma", 2);
		}
		//System.out.println("returning from R() \n\n\n");
		*/
	}
	/*
	 * 			Rn 	-> '<IDENTIFIER>'
				-> '<INTEGER>'
				-> '<STRING>'
				-> 'true' => 'true'
				-> 'false' => 'false'
				-> 'nil' => 'nil'
				-> '(' E ')'
				-> 'dummy' => 'dummy' ;
	 */
	boolean Rn(){
		//System.out.println("Rn()");
		//System.out.println("\n\n\n\n\nRn(): " +nextToken.name + "\n\n\n\n" );
		if(Arrays.asList(TokenType.ID, TokenType.INTEGER, TokenType.STRING, TokenType.TRUE, TokenType.FALSE, TokenType.NIL, TokenType.L_PAREN, TokenType.DUMMY).contains(nextToken.type)){
			switch(nextToken.type){
				//TODO: needs cleanup: combine the cases:
				case ID:
				case INTEGER:
				case STRING:
					Read(nextToken);
					return true;
				case TRUE:
				case FALSE:
				case NIL:
				case DUMMY:
					Token currentToken = nextToken;
					Read(nextToken);
					treeBuilder.BuildTree(currentToken, 0); //nil or true or false 
					return true;
				case L_PAREN:
					Read(nextToken);
					E();
					if(nextToken.type != TokenType.R_PAREN){
						ParseError(")", nextToken);
					}
					Read(nextToken);
					return true;
			}
		}
		//ParseError("Wrong argument for Rn()", nextToken);
		return false;
	}
	/*
	 * Definitions
	 * # Definitions ############################################
			D 	-> Da 'within' D => 'within'
				-> Da ;
	 */
	void D(){
		//System.out.println("D()");
		//System.out.println("yep i m here 5");
		Da();
		//System.out.println("yep i m here 4");
		if(nextToken.type == TokenType.WITHIN){
			Token withinToken = nextToken;
			Read(nextToken);
			D();
			treeBuilder.BuildTree(withinToken, 2); // within
		}
		//System.out.println("yep i m here 5");
	}
	/*
	 * 			Da  -> Dr ( 'and' Dr )+ => 'and'
					-> Dr ;
	 */
	void Da(){
		//System.out.println("Da()");
		Dr();
		//System.out.println("here # 1 ");
		if(nextToken.type == TokenType.AND){
			//System.out.println("here # 2 ");
			int n = 0;
			Token andToken = nextToken;
			while(nextToken.type == TokenType.AND){
				Read(nextToken);
				Dr();
				++n;
			}
			//System.out.println("here # 3 ");
			treeBuilder.BuildTree(andToken, n+1);  // and
		}
	}
	
	/*
	 * 			Dr  -> 'rec' Db => 'rec'
					-> Db ;
	 */
	void Dr(){
		//System.out.println("Dr()");
		if(nextToken.type == TokenType.REC){
			Token recToken = nextToken;
			Read(nextToken);
			Db();
			treeBuilder.BuildTree(recToken, 1);
		}
		else {
			Db();
		}
	}
	
	/*
	 * 			Db  -> Vl '=' E => '='
				-> '<IDENTIFIER>' Vb+ '=' E => 'fcn_form'
				-> '(' D ')' ; 
	 */
	void Db(){
		//System.out.println("Db()");
		//System.out.println("yep i m here 6");
		if(Vl() == true){
			//System.out.println("Db(): Vl was true: nextToken.type: " + nextToken.type);
			if(nextToken.type == TokenType.EQUALS){
				Token eqToken = nextToken;
				//System.out.println("yep i m here 9");
				Read(nextToken);
				E();
				//System.out.println("\n\n\n\n!!!!!!!!I am back\n\n\n\n\n\n");
				treeBuilder.BuildTree(eqToken, 2); // =
 				return;
			}
			else{
					int n = 0;
					while(nextToken.type == TokenType.ID || nextToken.type == TokenType.L_PAREN){
						//System.out.println("yep i m here 7");
						Vb();
						++n;
					}
					if(nextToken.type != TokenType.EQUALS){
						ParseError("=", nextToken);
					}
					Read(nextToken);
					E();
					Token functionFormToken = new Token(TokenType.FUNCTION_FORM, "function_form");
					treeBuilder.BuildTree(functionFormToken, n + 2);
			}
		}
		else if(nextToken.type == TokenType.L_PAREN){
			//System.out.println("yep i m here 8");
			Read(nextToken);
			D();
			if(nextToken.type != TokenType.R_PAREN){
				ParseError(")", nextToken);
			}
			Read(nextToken);
		}
		else {
			ParseError("( Left Parenthesis OR  ID ", nextToken);
		}
		//System.out.println("yep i m here 8");
	}
	
	/*
	 * # Variables ##############################################
		Vb  -> '<IDENTIFIER>'
			-> '(' Vl ')'
			-> '(' ')' => '()';
	 */
	void Vb(){
		//System.out.println("Vb()");
		if(nextToken.type == TokenType.ID){
			Read(nextToken);
		}
		else if(nextToken.type == TokenType.L_PAREN){
			//System.out.println("within Vb(): inside L_PAREN\n\n\n");
			Read(nextToken);
			if(nextToken.type == TokenType.ID){
				//System.out.println("here 1 \n\n\n");
				Vl();
				if(nextToken.type != TokenType.R_PAREN){
					ParseError(")", nextToken);
				}
				Read(nextToken);
			}
			else if(nextToken.type == TokenType.R_PAREN){
				Token rparenToken = nextToken;
				Read(nextToken);
				//treeBuilder.BuildTree("()", 2);
				treeBuilder.BuildTree(rparenToken, 2); //be careful about this while printing AST
			}
			else {
				ParseError("ID or )", nextToken);
			}
		}
		else {
			ParseError("Left Parenthesis OR ID", nextToken);
		}
	}
	/*
	 * 		Vl -> '<IDENTIFIER>' list ',' => ','?;
	 * 		
	 * 		returns true if able to create a node else false
	 */
	boolean Vl(){
		//System.out.println("Vl()");
		int n = 1;
		Token commaToken = new Token(); 
		if(nextToken.type == TokenType.ID){
			Read(nextToken);
			if(nextToken.type == TokenType.COMMA){
				commaToken = nextToken;
				while(nextToken.type == TokenType.COMMA){
					Read(nextToken);
					++n;
					if(nextToken.type != TokenType.ID){
						ParseError("ID", nextToken);
					}
					Read(nextToken);
				}
			}
		}
		/*
		else {
			//ParseError("ID", nextToken);
			return false;
		}
		*/
		//System.out.println("in Vl(): n is " + n );
		if(n == 1){
			return true;
		}
		if(n > 1){
			treeBuilder.BuildTree(commaToken, n); // ,
			return true;
		}
		return false;
	}
}
