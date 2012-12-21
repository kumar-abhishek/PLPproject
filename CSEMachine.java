import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

public class CSEMachine {
	private static final Logger logger = Logger.getLogger(CSEMachine.class.getName());
	Stack<Object> control = null;
	Stack<Object> stack = null;
	Stack<Environment> curEnvStack = null; //stack of environments: top contains current environment
	int curEnvIdx = 0; //current environment index
	int maxEnvIdx = 0; //max env index reached 
	//int curDeltaIdx = 0; //current control struct index
	Map<Integer, CtrlStruct> mapCtrlStructs = null;
	CtrlStructGenerator ctrlGenerator = null;
	Map<Integer, Environment> mapEnvironments = null;
	
	CSEMachine(CtrlStructGenerator cGenerator){
		control = new Stack<Object>();
		stack = new Stack<Object>();
		curEnvStack = new Stack<Environment>();
		ctrlGenerator = cGenerator;
		mapCtrlStructs = cGenerator.getCtrlStructs();
		mapEnvironments = new HashMap<Integer, Environment>();
	}
	
	void addToControl(Object obj){
		control.push(obj);
	}
	void addToStack(Object obj){
		stack.push(obj);
	}
	void addDeltaToControl(int idx){
		List<Object> delta = mapCtrlStructs.get(idx).getcontents();
		//add all items of delta into Control
		for(Object obj: delta){
			addToControl(obj);
		}
	}
	void Print(Object rand){
		logger.info("in Print");
		if(rand instanceof Token){
			Token token = (Token)rand;
			logger.info("Printing token: " + token + " | token.name: " + token.name );
			if(token.name instanceof String){
				//logger.info("hell ya");
			}
			String printItem = token.name;
			if(printItem.contains("\\n")){
				logger.info("found \\n in Print()");
				int start = printItem.indexOf("\\n");
				String s1 = printItem.substring(0, start);
				String s2 = printItem.substring(start+2, printItem.length());
				Print(new Token(TokenType.STRING, s1));
				logger.info("printing new line");
				System.out.println();
				Print(new Token(TokenType.STRING, s2));
			}
			else if( printItem.contains("\\t")){
				logger.info("found \\t in Print()");
				int start = printItem.indexOf("\\t");
				String s1 = printItem.substring(0, start);
				String s2 = printItem.substring(start+2, printItem.length());
				Print(new Token(TokenType.STRING, s1));
				System.out.print("\t");
				Print(new Token(TokenType.STRING, s2));
			}
			else {
				logger.info("else part of Print");
				System.out.print(printItem);
			}
		}
		else if(rand instanceof Tuple){
			((Tuple)rand).printTuple();
		}
		else if(rand instanceof LambdaExpression){
			((LambdaExpression)rand).printLambdaExpression();
		}
		else if(rand instanceof String){
			System.out.print((String)rand);
		}
		else if(rand instanceof Environment){
			System.out.print("Environ" + ((Environment)rand).getEnvIdx());
		}
		else if(rand instanceof Tau){
			System.out.print("Tau" + ((Tau)rand).getIndex());
		}
		else if(rand instanceof Beta){
			System.out.print("Beta");
		}
		else {
			System.out.print("type of rand is something diff: " + rand + "| " + rand.getClass().getCanonicalName());
		}
	}
	
	Object apply(Object binop, Object rand1 , Object rand2){
		TokenType binopType = ((Token)binop).type;
		TokenType type1 = null;
		TokenType type2 = null;
		String val1 = null;
		String val2 = null;
		
		if(binop instanceof Token){
			logger.info("binop: " + ((Token)binop).name  + " | type: " + ((Token)binop).type);
		}
		if((rand1 instanceof Token) && (rand2 instanceof Token)){
			logger.info("rand1: " + ((Token)rand1).type + " | rand2: " + ((Token)rand2).type);
		}
		else{
			logger.info("rand1: " + rand1  + "| rand2: "+ rand2);
		}
		if(rand1 instanceof Tuple){
			logger.info("rand1 is instance of Tuple");
			Tuple rand1Tuple = (Tuple)rand1;
			rand1Tuple.addElement(rand2);
			return rand1Tuple;
		}
		if(rand1 instanceof Token){			
			logger.info("rand1 is instance of Token");
			type1 = ((Token)rand1).type;
			val1 = ((Token)rand1).name;
			logger.info("type1: " + type1 + " | val1: " + val1);
		}
		else if(rand1 instanceof String){
			logger.info("rand1 is instance of String");
			val1 = (String)rand1;
		}
		else {
			System.out.println("I dont know type of rand1" + rand1);
		}
		
		if(rand2 instanceof Token){
			logger.info("rand2 is instance of Token");
			type2 = ((Token)rand2).type;
			val2 = ((Token)rand2).name;
			logger.info("type2: " + type2 + " | val2: " + val2);
		}
		else if(rand2 instanceof String){
			logger.info("rand2 is instance of String");
			val2 = (String)rand2;
		}
		else {
			System.out.println("I dont know type of rand2: " + rand2);
		}
		
		logger.info("type1: " + type1 + " | type2: " + type2);
		// OR,  AND : logical binops
		if((binopType == TokenType.OR) || (binopType == TokenType.AND_BIN)){
			
			if( ((type1 != TokenType.TRUE) && (type1 != TokenType.FALSE)) || ((type2 != TokenType.TRUE) && (type2 != TokenType.FALSE)) ){
				System.out.println("Wrong type: true/false expected for both operands: type1: " + type1 + " | type2: " + type2);
				System.exit(-1);
			}
			
			
			if(binopType == TokenType.OR){
				if(val1.equals("false") && val2.equals("false")){
					logger.info("in OR: returning false");
					return new Token(TokenType.FALSE, "false");
				}
				else {
					logger.info("in OR: returning true, val1: " + val1 + " | val2: "+ val2);
					return new Token(TokenType.TRUE, "true");
				}
			}
			else {
				if(val1.equals("true") && val2.equals("true")){
					logger.info("binop AND: returning True");
					return new Token(TokenType.TRUE, "true");
				}
				else{
					logger.info("binop AND: returning false");
					return new Token(TokenType.FALSE, "false");
				}
			}
		}
		//EQ: logical
		
		// arithmetic binops
		//TODO: don't know if AUG needs to be handled here !!!!!!!!!!
		if(type1 != TokenType.INTEGER || type2 != TokenType.INTEGER){
			logger.info("probably Wrong operand type for binary operator: " + type1 + " or " + type2);
		}
		logger.info("val1: " + val1 + " | val2: "+ val2);
		int value1 = -1;//initialize
		int value2 = -2;  //initialize
		if(val1.length() == 1 && Character.isLetter(val1.charAt(0)) ){
			value1 = (int)(val1.charAt(0));
		}
		else {
			logger.info("val1: " + val1);
			try{
				value1 = Integer.parseInt(val1);
			} catch(Exception e){
				logger.info("val1 is not an integer");
			}
			
		}
		if(val2 != null && val2.length() == 1 && Character.isLetter(val2.charAt(0)) ){
			value2 = (int)(val2.charAt(0));
		}
		else{
			logger.info("val2: " + val2);
			try{
				value2 = Integer.parseInt(val2);
			}catch(Exception e){
				logger.info("val2 is not an integer");
			}
			
		}
		
		logger.info("value1: " + value1 + " | value2: "+ value2);
		switch(binopType){
		
		case PLUS:
			return new Token(TokenType.INTEGER, Integer.toString(value1 + value2));
		case MINUS:
			return new Token(TokenType.INTEGER, Integer.toString(value1 - value2));
		case TIMES:
			return new Token(TokenType.INTEGER, Integer.toString(value1 * value2));
		case DIVIDE:
			return new Token(TokenType.INTEGER, Integer.toString(value1 / value2));
		case AND_BIN:
			System.out.println("I am a wrong place");
			//return new Token(TokenType.INTEGER, Integer.toString(value1 & value2));
		case EXPO: // see if you need to return double here !!!! TODO!!
			return new Token(TokenType.INTEGER, Integer.toString((int)(Math.pow(value1, value2))));
		//TODO: clean needed here !!!!!!!!!!!!!
		case GR:
			if(value1 > value2){
				return new Token(TokenType.TRUE, "true");
			}
			else{
				return new Token(TokenType.FALSE, "false");
			}
		case GE:
			if(value1 >= value2){
				return new Token(TokenType.TRUE, "true");
			}
			else{
				return new Token(TokenType.FALSE, "false");
			}
		case LS:
			if(value1 < value2){
				return new Token(TokenType.TRUE, "true");
			}
			else{
				return new Token(TokenType.FALSE, "false");
			}
		case LE:
			if(value1 <= value2){
				return new Token(TokenType.TRUE, "true");
			}
			else{
				return new Token(TokenType.FALSE, "false");
			}
		case EQUALS:
			if(val1.equals(val2)){
				logger.info("returning true");
				return new Token(TokenType.TRUE, "true");
			}
			else{
				logger.info("returning false");
				return new Token(TokenType.FALSE, "false");
			}			
			/*
			if(value1 == value2){
				logger.info("returning true");
				return new Token(TokenType.TRUE, "true");
			}
			else{
				return new Token(TokenType.FALSE, "false");
			}
			*/
		case NE:
			logger.info("in binary operator : NE");
			if(!(val1.equals(val2))){
				logger.info("in NE: returning true");
				return new Token(TokenType.TRUE, "true");
			}
			else{
				logger.info("in NE: returning false");
				return new Token(TokenType.FALSE, "false");
			}
		case AUG:
			logger.info("in AUG");
			//nil is an empty tuple and a special case
			//left operand of aug has to be tuple
			//right operand of aug can be anything including a tuple
			if(val1.equals("nil")) {
				if(rand2 instanceof Tuple){
					return rand2;
				}
				else{
					if(rand2 instanceof Token){
						//add rand2 to a new tuple and return the new tuple
						Tuple t = new Tuple();
						t.addElement(rand2);
						return t;
					}
					else {
						System.out.println("AUG: rand2 is not a Token!!");
						System.exit(-1);
					}
				}
			}
			else if(rand1 instanceof Tuple){
				if(rand2 instanceof Tuple){
					//add all elements of rand2 to rand1
					Tuple t1 = (Tuple)rand1;
					Tuple t2 = (Tuple)rand2;
					int t2Size = t2.size();
					for(int i =0; i < t2Size; i++){
						t1.addElement(t2.getElementAtIndexN(i));
					}
					return t1;
				}
				else{
					if(rand2 instanceof Token){
						//add rand2 to a new tuple and return the new tuple
						Tuple t1 = (Tuple)rand1;
						t1.addElement(rand2);
						return t1;
					}
					else {
						System.out.println("AUG: rand2 is not a Token!!");
						System.exit(-1);
					}
				}
			}
			else {
				//error cond
				System.out.println("AUG: rand1 is not a tuple or nil!!" );
				if(rand1 instanceof Token){
					System.out.println("rand1: " + ((Token)rand1).name);
				}
				System.exit(-1);
			}
			break;
		default:
			System.out.println("no matching binary operator found: " + binopType.name());
		}
		//unreachable code: 
		System.out.println("Unreachable code !! Something wrong happened!!" );
		return null;
		
	}
	Object apply(Object unop, Object rand){
		logger.info("in unary apply");
		//NEG and NOT
		TokenType operatorType = ((Token)unop).type;
		TokenType operandType =  ((Token)rand).type;
		
		String value = ((Token)rand).name;
		if(operatorType == TokenType.NEG){
			if(operandType != TokenType.INTEGER){
				System.out.println("Wrong operandType for unary operator: " + operandType);
				System.exit(-1);
			}
			value = "-" + value;
			return new Token(TokenType.INTEGER,value);
		}
		else if(operatorType == TokenType.NOT){
			if(operandType == TokenType.FALSE){
				return new Token(TokenType.TRUE, "true");
			}
			else if(operandType == TokenType.TRUE) {
				logger.info("yes i was true token");
				return new Token(TokenType.FALSE, "false");
			}
			else{
				System.out.println("Wrong operandType for operand: " + operandType);
				System.exit(-1);
			}
		}
		else {
			System.out.println("unary operator type not known: " + operatorType);
			return null;
		}
		return null;
	}
	
	void CSEMachineInitiator(){
		Environment e = new Environment(curEnvIdx);
		addToControl(e);
		addDeltaToControl(0);
		addToStack(e);
		curEnvStack.push(e);
		mapEnvironments.put(curEnvIdx, e);
		//curEnvIdx += 1;
		//curDeltaIdx += 1;
	}
	
	void stackPrinter(Stack<Object> stk){
		if(stk.isEmpty()){
			System.out.println("Empty");
			return;
		}
		Stack<Object> tempStk = new Stack<Object>();
		while(!stk.isEmpty()){
			Object stkTop = stk.peek();
			Print(stkTop);
			System.out.print("\t");
			tempStk.push(stk.peek());
			stk.pop();
		}
		while(!tempStk.isEmpty()){
			stk.push(tempStk.peek());
			tempStk.pop();
		}
	}
	
	void CSEMachineOperator(){
		CSEMachineInitiator();
		//now start operating CSE Machine till control is not empty
		int count = 0;
		while(!control.isEmpty()){
			Object controlTop = control.peek();
			Object stackTop = stack.peek();
			
			stackPrinter(control);
			System.out.print("\n");
			stackPrinter(stack);
			System.out.println("\n\n");
			
			logger.info("controlTop: " + controlTop.getClass().getName());
			logger.info("stackTop: " + stackTop.getClass().getName());
			
			if(controlTop instanceof List){
				List<Object> list =  (List<Object>) (controlTop);
				//supposedly a delta which is a list of objects: lets fill up it's value 
				
				//remove delta from control first
				control.pop();
				for(Object item: list){
					if(item instanceof Token){
						logger.info("pushing item: "  + ((Token)item).name + " into control") ;
					}
					else {
						logger.info("item is not a Token: pushing item: "  + item + " into control") ;
						if(item instanceof List){
							for(Object i: (List<Object>)item){
								if(i instanceof Token){
									logger.info("i : " + ((Token)i).name);
								}
								else {
									logger.info("i : " + i);
								}
							}
						}
					}
					//control.push((Token)item);
					control.push(item);
				}
				//update controlTop now
				controlTop = control.peek();
			}
			
			
			switch(controlTop.getClass().getName()){
			case "LambdaExpression":
				//rule 2
				logger.info("applying rule 2");
				((LambdaExpression) controlTop).setEnvironmentId(curEnvIdx);
				control.pop();
				stack.push(controlTop);
				break;
				
			case "Token":
				if(((Token)controlTop).type == TokenType.GAMMA){
					logger.info("controlTop is gamma");
					if(stackTop instanceof LambdaExpression){
						//rule 4 and 11
						logger.info("applying rule 4");
						control.pop(); //remove gamma
						stack.pop(); //remove lambda 
						/*
						if(!(stack.peek() instanceof Tuple)){
							System.out.println("expected tuple on stack : for n-ary function");
							System.exit(-1);
						}
						Tuple rand = (Tuple)stack.peek(); //get rand  from stack
						*/
						Object rand = stack.peek(); //get rand from stack
						stack.pop(); //remove rand from stack
						LambdaExpression lambdaStack = (LambdaExpression)stackTop;
						int k = lambdaStack.idx;
						int envIdLambda = lambdaStack.envId;
						List<Token> tokenStackLambdaList = null;
						Token tokenStackLambda  = null;
						if(lambdaStack.item instanceof Token){
							tokenStackLambda = (Token)(lambdaStack.item); //the variable of lambda of stack
						}
						else {
							//a list of Tokens
							logger.info("applying rule 11");
							if(lambdaStack.item instanceof List){
								tokenStackLambdaList = (List<Token>)(lambdaStack.item);
							}
							else {
								System.out.println("tokenStackLambdaList is not a list, some error");
							}
						}
						//curEnvIdx += 1;
						maxEnvIdx += 1;
						curEnvIdx = maxEnvIdx;
						Environment env = new Environment(curEnvIdx);
						//logger.info("putting in current env " + curEnvIdx + "| variable : " + tokenStackLambda.name + "| value: " + rand);
						if(tokenStackLambdaList == null){
							logger.info("tokenStackLambdaList was null");
							env.setEnvParams(mapEnvironments.get(envIdLambda), (Object)(tokenStackLambda.name), (Object)rand);
						}
						else {
							//rand must be a list then
							if(!(rand instanceof Tuple)){
								System.out.println("rand is not a list , error !!!!: rule 11, rand is: " + rand);
							}
							else {
								logger.info("rand is: " + rand);
							}
							//Iterator<Token> itTok = tokenStackLambdaList.iterator();
							//Iterator<Token> itVal = ((List<Token>)rand).iterator();
							int cnt = 0;
							for(Token item: tokenStackLambdaList){
								logger.info("for loop in setEnvParams");
								env.setEnvParams(mapEnvironments.get(envIdLambda), item.name, (((Tuple)rand).getElementAtIndexN(cnt)));
								//logger.info("putting " + item.name + " = " +  ((Token)(((Tuple)rand).getElementAtIndexN(cnt))).name);
								++cnt;
							}
						}
						logger.info("pushing envid: " + curEnvIdx + " to control");
						control.push(env);
						addDeltaToControl(k); //k is from stack
						stack.push(env);
						//maintain environment variables
						curEnvStack.push(env);
						mapEnvironments.put(curEnvIdx, env);
					}
					else if(stackTop instanceof Tuple){
						//rule 10: tuple selection:
						logger.info("applying rule 10");
						control.pop(); //remove gamma from control
						stack.pop(); // remove tuple from stack
						logger.info("stackTop is: " + ((Token)(stack.peek())).name);
						int index = -1;
						if(stack.peek() instanceof Integer){
							index = (int)stack.peek();
						}
						else if(stack.peek() instanceof Token) {
							index = Integer.parseInt(((Token)stack.peek()).name);
						}
						else {
							System.out.println("stackTop not identified: " + stack.peek());
						}
						logger.info("index: " + index);
						Object Vi = ((Tuple)stackTop).getElementAtIndexN(index-1);
						stack.pop(); //remove index from stack
						stack.push(Vi); //insert Vi into stack
					}
					else if((stackTop instanceof Token) && ((Token)stackTop).type == TokenType.YSTAR){
						//rule 12
						logger.info("applying rule 12");
						control.pop(); //remove gamma from control
						stack.pop();
						Object objY= stack.peek();
						LambdaExpression lambdaY = (LambdaExpression)objY;
						stack.pop(); //remove lambda from stack
						if(lambdaY.item instanceof Token){
							logger.info("creating eta with: " + lambdaY.envId + " | " + lambdaY.idx + "| " + (Token)lambdaY.item);
							Eta eta = new Eta(lambdaY.envId, lambdaY.idx, (Token)lambdaY.item); //Eta(int environmentId, int id, Token tok){
							stack.push(eta);
						}
						else {
							System.out.println("bug for rule 12\n\n\n");
						}
					}
					else if(stackTop instanceof Eta){
						//rule 13
						logger.info("applying rule 13");
						control.push(new Token(TokenType.GAMMA, "gamma"));
						Eta eta = (Eta)stackTop;
						//Token tempLambdaToken = new Token(eta.token.type, eta.token.name);
						LambdaExpression lambdaStack =  new LambdaExpression(eta.envId, eta.idx, eta.token);
						stack.push(lambdaStack);
					}
					else if(stackTop instanceof Token){
						logger.info("stackTop: " + ((Token)stackTop).name);
						//rule 3: Result = Apply[Rator, Rand]
						switch(((Token)stackTop).name){
						case "Print":
							control.pop();
							stack.pop();
							Object rand = stack.peek(); //get rand from stack
							stack.pop(); //remove rand
							Print(rand);
							//not put the result on stack 
							stack.push(new Token(TokenType.DUMMY, "dummy"));
							break;
						case "Conc":
							stack.pop(); //remove conc
							stackTop = stack.peek();
							String str1 = ((Token)stackTop).name;
							stack.pop(); //remove str1 
							logger.info("in conc: stackTop: " + stack.peek());
 							String str2 = ((Token)stack.peek()).name;
							stack.pop(); //remove str2
							logger.info("in conc: str1: " + str1 + " | str2: " + str2);
							String str = str1 + str2; 
							stack.push(new Token(TokenType.STRING, str)); // push result in stack
							stackTop = stack.peek();
							control.pop(); //remove gamma from control
							if(!(((Token)controlTop).type == (TokenType.GAMMA))){
								System.out.println("GAMMA expected, error !!! controlTop: " + ((Token)controlTop).name);
							}
							control.pop(); // remove gamma from control
							controlTop = control.peek(); 
							break;
						case "Stem":
							//get 1st character of string
							logger.info("in Stem");
							control.pop(); //remove gamma from control
							stack.pop(); // remove stem
							stackTop = stack.peek();
							stack.pop(); //remove token whose stem has to be found
							String stemStr = ((Token)stackTop).name;
							logger.info("Stem: " + stemStr);
							String value = "";
							if(!stemStr.equals("")){
								value = "" + (((Token)stackTop).name).charAt(0);
								logger.info("value of Stem: " + value);
							}
							else {
								System.out.println("Stem: stemStr was empty, exiting");
								System.exit(-1);
							}
							stack.push(new Token(TokenType.STRING, value));
							break;	
						case "Stern":
							//get everything but 1st char of string
							logger.info("in Stern");
							control.pop(); //remove gamma from control
							stack.pop(); //remove stern from stack
							stackTop = stack.peek(); //get the string whose stern has to be returned
							stack.pop(); //remove the string whose stern has to be found
							String tempVal = (((Token)stackTop).name);
							logger.info("Stern to be applied to : " + tempVal);
							if(!tempVal.equals("")){
								tempVal = tempVal.substring(1, tempVal.length());
							}
							else {
								System.out.println("Stern: tempVal was empty, exiting ");
								System.exit(-1);
							}
							stack.push(new Token(TokenType.STRING, tempVal));
							logger.info("Stern: inserting " + tempVal + " into stack");
							break;
						case "Order":
							logger.info("in Order");
							control.pop(); //remove gamma
							stack.pop(); //remove order token
							stackTop = stack.peek();
							stack.pop(); //remove tuple whose order has to be found : may be nil
							if(stackTop instanceof Tuple){
								Tuple t = (Tuple)stackTop;
								int sz = t.size();
								stack.push(new Token(TokenType.INTEGER, Integer.toString(sz)));
								stackTop = stack.peek();
							}
							else if(stackTop instanceof Token){
								if(((Token)stackTop).type == TokenType.NIL){
									stack.push(new Token(TokenType.INTEGER, "0"));
									stackTop = stack.peek();
								}
								else {
									System.out.println("Order: stackTop is not an instance of Tuple: " + ((Token)stackTop).name);
								}
							}
							break;
						case "Null":
							//return true if Tuple T is nil, else false : Null T
							logger.info("in Null");
							control.pop(); //remove gamma
							stack.pop(); //remove Null token
							stackTop = stack.peek(); //get Tuple t
							
							if(stackTop instanceof Token){
								if(((Token)stackTop).type == TokenType.NIL){
									stack.push(new Token(TokenType.TRUE, "true"));
									stackTop = stack.peek();
									break;
								}
								else {
									System.out.println("Order: stackTop is not an instance of Tuple: " + ((Token)stackTop).name);
								}
							}
							else if(!(stackTop instanceof Tuple)){
								System.out.println("in Null: operand should be a Tuple: ");
								System.exit(-1);
							}
							stack.pop(); //remove Tuple t
							Tuple tNull = (Tuple)stackTop;
							if(tNull.size() == 0 ){
								stack.push(new Token(TokenType.TRUE, "true"));
							}
							else {
								stack.push(new Token(TokenType.FALSE, "false"));
							}
							stackTop = stack.peek();
							break;
						case "ItoS":
							logger.info("in ItoS");
							control.pop(); //remove gamma
							stack.pop(); //remove ItoS token
							stackTop = stack.peek(); //remove int to be converted to String
							stack.pop(); //remove integer from stack
							String valItoS = ""; //default val
							if(stackTop instanceof Token){
								valItoS = ((Token)stackTop).name;
							}
							else {
								System.out.println("parameter for ItoS not a token , error!!");
								System.exit(-1);
							}
							stack.push(new Token(TokenType.STRING, valItoS)); //insert final val in stack
							stackTop = stack.peek();
							//System.out.println("ItoS not implemented yet, error !!!!!!!!!!!!!!!!!!");
							//System.exit(-1);
							break;
						case "Isinteger":
							logger.info("in Isinteger");
							control.pop(); //remove gamma
							stack.pop(); //remove string Isinteger
							stackTop = stack.peek(); 
							stack.pop(); //remove the token to be checked
							if(!(stackTop instanceof Token)){
								logger.info("stackTop: stackTop is not a token, may be an error " + stackTop );
							}
							if((stackTop instanceof Token) && ((Token)stackTop).type ==  TokenType.INTEGER){
								logger.info("in Isinteger: returning true" + ((Token)stackTop).name );
								stack.push(new Token(TokenType.TRUE, "true"));
							}
							else {
								logger.info("in Isinteger: returning false");
								stack.push(new Token(TokenType.FALSE, "false"));
							}
							break;
						case "Istruthvalue":
							logger.info("in Istruthvalue");
							control.pop(); //remove gamma
							stack.pop(); //remove string Istruthvalue
							stackTop = stack.peek();
							stack.pop(); //remove the token to be checked
							if(!(stackTop instanceof Token)){
								stack.push(new Token(TokenType.FALSE, "false"));
								break;
								/*
								logger.info(stackTop.getClass().getCanonicalName());
								logger.info("in Istruthvalue: stackTop not an instance of Token!!! exiting");
								System.exit(-1);
								if(stackTop instanceof Tuple){
									logger.info("in Istruthvalue: printing tuple contents");
									((Tuple)stackTop).printTuple();
								}
								*/
							}
							if((((Token)stackTop).type ==  TokenType.TRUE) || (((Token)stackTop).type ==  TokenType.FALSE) ){
								stack.push(new Token(TokenType.TRUE, "true"));
							}
							else {
								stack.push(new Token(TokenType.FALSE, "false"));
							}
							break;
						case "Isstring":
							logger.info("in Isstring");
							control.pop(); //remove gamma
							stack.pop(); //remove string Isstring
							stackTop = stack.peek(); 
							stack.pop(); //remove the token to be checked
							if((stackTop instanceof Token) && ((Token)stackTop).type ==  TokenType.STRING){
								stack.push(new Token(TokenType.TRUE, "true"));
							}
							else {
								stack.push(new Token(TokenType.FALSE, "false"));
							}
							break;
						case "Istuple":
							logger.info("in Istuple");
							control.pop(); //remove gamma
							stack.pop(); //remove string Istuple
							stackTop = stack.peek(); 
							stack.pop(); //remove the token to be checked
							if(stackTop instanceof Tuple){
								logger.info("Istuple : returning true: it was a non-nil tuple");
								//((Tuple)stackTop).printTuple(); //to be commented
								stack.push(new Token(TokenType.TRUE, "true"));
							}
							else if(stackTop instanceof Token){
								if(((Token)stackTop).type == TokenType.NIL){
									logger.info("Istuple : returning true: nil encountered");
									stack.push(new Token(TokenType.TRUE, "true"));
								}
								else {
									logger.info("In Istuple, stackTop is Token but type is not nil, error !! stackTop: "+ ((Token)stackTop).name);
									//System.exit(-1);
									logger.info("Istuple : returning false");
									stack.push(new Token(TokenType.FALSE, "false"));
								}
							}
							else {
								logger.info("Istuple : returning false");
								stack.push(new Token(TokenType.FALSE, "false"));
							}
							break;
						case "Isfunction":
							logger.info("in Isinteger");
							System.out.println("Isfunction not implemented yet");
							System.exit(-1);
							/*
							control.pop(); //remove gamma
							stack.pop(); //remove string Isinteger
							stackTop = stack.peek(); 
							stack.pop(); //remove the token to be checked
							if(((Token)stackTop).type ==  TokenType.INTEGER){
								stack.push(new Token(TokenType.TRUE, "true"));
							}
							else {
								stack.push(new Token(TokenType.FALSE, "false"));
							}
							*/
							break;
						case "Isdummy":
							logger.info("in Isdummy");
							control.pop(); //remove gamma
							stack.pop(); //remove string Isdummy
							stackTop = stack.peek(); 
							stack.pop(); //remove the token to be checked
							if(((Token)stackTop).type ==  TokenType.DUMMY){
								stack.push(new Token(TokenType.TRUE, "true"));
							}
							else {
								stack.push(new Token(TokenType.FALSE, "false"));
							}
							break;
							
							
						default:
							System.out.println("didn't match Print/stem/stern/order/isinteger/isstring/istuple/istruthvalue/isfunction/isdummy, something else" + stackTop);
							if(stackTop instanceof Token){
								System.out.println(((Token)stackTop).name);
							}
						}
					}
					else {
						logger.info("stack: " + stackTop + " | control: " + controlTop);
						if(stackTop instanceof Token){
							logger.info("stackTop: " + ((Token)stackTop).name );
						}
						if(controlTop instanceof Token){
							logger.info("controlTop: " + ((Token)controlTop).name );
						}
						System.out.println("probably a wrong case in Token!!!!\n\n\n\n");
					}
				}
				else {
					//rule  6, 7: rator rand
					logger.info("in rule 6,7, else part: controlTop: " + controlTop + "| " + ((Token)controlTop).name);
					switch(((Token)controlTop).type){
						case OR:
						case AND:
							//take care of above these 2 logical binops !!!  
						case GR:
						case GE:
						case LS:
						case LE:
						case EQUALS:
						case NE: 
						case PLUS:
						case MINUS:
						case TIMES:
						case DIVIDE:
						case AUG:
						case AND_BIN:
						case EXPO : {/* Arithmetic binops */
							Object stackTop1 = stack.peek();
							stack.pop();
							Object stackTop2 = stack.peek();
							stack.pop();
							control.pop();
							Object operator = controlTop;
							if(stackTop1 instanceof String){
								logger.info("stackTop1: " + stackTop1);
							}
							if(stackTop2 instanceof String){
								logger.info("stackTop2: " + stackTop2);
							}
							//Result = Apply[binop, Rand, Result]
							Object result = apply(operator, stackTop1, stackTop2);
							stack.push(result);
							break;
						}
						case NEG:
						case NOT:{ /* unary operators */
							logger.info("in operator NOT");
							control.pop();
							stack.pop();
							Object result = apply(controlTop, stackTop);
							logger.info("NOT operator : pushing into stack : result: " + ((Token)result).name);
							stack.push(result);
							break;
						}
						default:
							logger.info("no operator matched: rule 6 or 7 !!!!!!!!!: " + ((Token)controlTop).type);
							//rule 1 : lookup in current or parent environments, and put the value on stack
							logger.info("applying rule 1");
							control.pop();
							Environment curEnv = curEnvStack.peek();
							String type = ((Token)controlTop).name;
							logger.info("type is : " + type);
							
							//first lookup in the environment tree
							if(((Token)controlTop).type == TokenType.ID) {
								//do a lookup 
								logger.info("here:" +((Token)controlTop).type.name() + "looking up for :" + ((Token)controlTop).name);
								logger.info("current envid: " + curEnvIdx);
								Object stackVal = curEnv.getVal(((Token)controlTop).name);
								if(stackVal == null){
									curEnv = curEnv.parent;
									while(curEnv != null){
										logger.info("looking up in env: " + curEnv.getEnvIdx());
										stackVal = curEnv.getVal((Object)(((Token)controlTop).name));
										if(stackVal != null){
											break;
										}
										curEnv = curEnv.parent;
									}
									
								}
								if(stackVal != null){
									logger.info("putting " + stackVal + " into Stack");
									stack.push(stackVal);
									if(stackVal instanceof Token){
										logger.info("pushing into stack value: " + ((Token)stackVal).name);
									}
								}
								
								if(stackVal == null){
									//if not found in the env tree, check if it was a special function
									//it make be a special function name which was not redefined
									if(type.equals("Print") || type.equals("Conc") || type.equals("Stern") || type.equals("Stem") || type.equals("Order") || type.equals("Isinteger") || type.equals("Istruthvalue") || type.equals("Isstring") || type.equals("Isinteger")  || type.equals("Istuple") || type.equals("Isfunction") || type.equals("Isdummy") || type.equals("ItoS") || type.equals("Null")){
										//just stack the Print, Stern, Stem, ItoS, Order, conc,  may be: aug too 
										logger.info("pushing into stack :" + type);
										stack.push(controlTop);
									}
									else {
										System.out.println("not found in any envs, some error!!!!!!");
										System.exit(-1);
									}
								}
							}
							else {
								//just put into stack from control
								logger.info("putting controlTop: "+ controlTop + "| " + ((Token)controlTop).name + " on stack");
								stack.push(controlTop);
							}
					}
				}
				break;
				
			case "Beta":
				//rule 8: conditional
				logger.info("applying rule 8");
				if((((Token)stackTop).name).equals("true")){
					control.pop(); //remove beta
					control.pop(); //remove else
					stack.pop();
				}
				else if((((Token)stackTop).name).equals("false")){
					control.pop();
					controlTop = control.peek();
					control.pop(); //remove else
					control.pop(); // remove then 
					control.push(controlTop); // insert else back
					stack.pop();
				}
				break;
			
			case "Tau":
				//rule 9: tuple formation
				logger.info("applying rule 9");
				control.pop();
				int n = ((Tau)controlTop).num;
				Tuple tuple = new Tuple();
				while(n > 0){
					if(stackTop instanceof Token){
						logger.info("adding " + ((Token)stackTop).name + " to tuple");
					}
					tuple.addElement(stackTop);
					stack.pop();
					stackTop = stack.peek();
					--n;
				}
				stack.push(tuple);
				break;
				
			case "Environment":
				//rule 5: exit env
				logger.info("applying rule 5");
				control.pop();
				stack.pop();
				stack.pop();
				stack.push(stackTop);
				logger.info("exiting env: " + curEnvStack.peek().getEnvIdx());
				curEnvStack.pop();
				if(!curEnvStack.isEmpty()){
					curEnvIdx = curEnvStack.peek().getEnvIdx(); //added on 4th dec
				}
				break;
			}
			
			//to break out of the while loop 
			//break;
			++count;
			if(count > 1597){
				System.out.println("Probably went into infinite loop, breaking !!!!!!!");
				break;
			}
		}
	}
	
}
