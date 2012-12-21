import java.util.logging.*;

//don't standardize unary and binary operators, conditionals, tuples, and n-ary functions.

class TreeStandardizer{
	private static final Logger logger = Logger.getLogger(TreeStandardizer.class.getName());

	Parser P;
	TreeStandardizer(Parser p1){
		P = p1;
	}
	/*
	 * do a postorder traversal of the AST and standardize the tree.
	 */
	Node StandardizeTree(Node root){
		if(root == null){
			return null;
		}
		root.first = StandardizeTree(root.first);
		if(root.sibling != null){
			root.sibling = StandardizeTree(root.sibling);
		}
		Node prevSibling = root.previous;
		Node nextSibling = root.sibling;
		//let, tau, within, fcn_form, and, where, rec, @
		switch(root.token.type){
			case LET:
				logger.info("standardizing let");
				root = StandardizeLet(root);
				break;
			case WITHIN:
				logger.info("standardizing within");
				root = StandardizeWithin(root);
				break;
			case FUNCTION_FORM:
				logger.info("standardizing fcn_form");
				root = StandardizeFcnForm(root);
				break;
			case AND:
				logger.info("standardizing and");
				root = StandardizeAnd(root);
				break;
			case WHERE:
				logger.info("standardizing where");
				root = StandardizeWhere(root);
				break;
			case REC:
				logger.info("standardizing rec");
				root = StandardizeRec(root);
				break;
			case AT:
				logger.info("standardizing at");
				root = StandardizeAt(root);
				break;	
		}//switch ends
		
		//fix prev and next sibling now
		root.previous = prevSibling;
		root.sibling = nextSibling;
		return root;
	}
	
	
	/*
	 * LET
	 *
	let => 		gamma 
	/ \ 	   / 	\ 
	=	P   lambda   E
	/ \   	 / \ 
	X E  	 X  P
	
	*/
	Node StandardizeLet(Node root) {
		Node equals = root.first; 
		Node P = equals.sibling;
		Node X = equals.first;
		Node E = X.sibling;
		
		logger.info("P is : " + P.token.name);
		
		Node gamma = new Node(new Token(TokenType.GAMMA, "gamma")); // new root
		Node lambda = new Node(new Token(TokenType.LAMBDA, "lambda"));
		gamma.first = lambda;
		lambda.sibling = E;
		E.previous = lambda;
		lambda.first = X;
		X.sibling = P;
		P.previous = X;
		
		return gamma;
	}

	/*
	 * tau
	tau => 		++gamma 
	| 			/ \ 
	E++ 	gamma  E
			/ \  
	  	  aug .nil 
	  
	 */
	Node StandardizeTau(Node tau){
		Node E = tau.first;
		Node tempE = E;
		Node newRoot = null;
		Node aug = null;
		Node tempESibling = null;
		
		Node gamma = new Node(new Token(TokenType.GAMMA, "gamma")); // new root
		Node gammaL = new Node(new Token(TokenType.GAMMA, "gamma")); 
		
		gamma.first = gammaL;
		gammaL.sibling = E;
		tempESibling = E.sibling;
		E.sibling = null;
		aug = new Node(new Token(TokenType.AUG, "aug"));
		gammaL.first = aug;
		E = tempESibling;
		
		while(E != null){
			gamma = new Node(new Token(TokenType.GAMMA, "gamma")); // new root
			gammaL = new Node(new Token(TokenType.GAMMA, "gamma")); 
			aug.sibling = gamma;
			gamma.first = gammaL;
			gammaL.sibling = E;
			tempESibling = E.sibling;
			E.sibling = null;
			aug = new Node(new Token(TokenType.AUG, "aug"));
			gammaL.first = aug;
			E = tempESibling;
		}
		aug.sibling = new Node(new Token(TokenType.NIL, "nil"));
		tempE.sibling = null;
		return newRoot;		
	}
	
	/*	//within
	 * 
				within    	=>    	    	    =     
		      /      \              		  /   \    
		     =         =         		 	X2  gamma 
		    / \       /   \             		/    \  
		 X1  E1      X2   E2       			 lambda   E1 
					                    	   /  \       
		                    			   	  X1   E2 
	 */
	Node StandardizeWithin(Node within){
		Node eq1 = within.first;
		Node eq2 = eq1.sibling;
		Node X1 = eq1.first;
		Node E1 = X1.sibling;
		Node X2 = eq2.first;
		Node E2 = X2.sibling;
		
		Node newRoot = new Node(new Token(TokenType.EQUALS, "=")); 
		newRoot.first = X2;
		Node gamma = new Node(new Token(TokenType.GAMMA, "gamma"));
		Node lambda = new Node(new Token(TokenType.LAMBDA, "lambda"));
		
		X2.sibling = gamma;
		gamma.previous = X2;
		gamma.first = lambda;
		lambda.sibling = E1;
		E1.previous = lambda;
		lambda.first = X1;
		X1.sibling = E2;
		E2.previous = X1;
		
		E1.sibling = null;
		
		return newRoot;
	}
	
	/*
	 * fcn_form
	 	fcn_form   		       	    =
     	 /   |  \         		   / \
   	 	P   V+   E       =>   	  P   +lambda
	                         		 	 /  \
                         				V   .E 
	 */
	Node StandardizeFcnForm(Node fcnForm){
		Node P = fcnForm.first;
		Node V = P.sibling;
		Node Vs = V.sibling;
		//Node E = V.sibling;
		
		Node newRoot = new Node(new Token(TokenType.EQUALS, "="));
		newRoot.first = P;
		Node lambda = new Node(new Token(TokenType.LAMBDA, "lambda"));
		P.sibling = lambda;
		lambda.previous = P;
		
		while(Vs.sibling != null){
			lambda.first = V;
			lambda = new Node(new Token(TokenType.LAMBDA, "lambda"));
			V.sibling = lambda;
			lambda.previous = V;
			V = Vs;
			Vs = Vs.sibling;
		}
		lambda.first = V;
		V.sibling = Vs;
		Vs.previous = V;
		return newRoot;
	}
	
	/* and
	 * 
	 	  and     =>         =       
           |             	/ \      
          =++              ,    tau    
          / \              |      |     
         X   E             X++   E++   

	 */
	Node StandardizeAnd(Node and){
		Node eq = and.first;
		//Node tempEq = eq;
		
		Node newRoot = new Node(new Token(TokenType.EQUALS, "=")); 
		Node comma = new Node(new Token(TokenType.COMMA, ","));
		Node tau = new Node(new Token(TokenType.TAU, "tau"));
		
		newRoot.first = comma;
		comma.sibling  = tau;
		tau.previous = comma;
		
		Node X = eq.first;
		Node E = X.sibling;
		
		comma.first = X;
		tau.first = E;
		
		eq = eq.sibling;
		while(eq != null){
			X.sibling = eq.first;
			eq.first.previous = X;
			E.sibling = eq.first.sibling;
			eq = eq.sibling;
			X = X.sibling;
			E = E.sibling;
		}
		X.sibling = null;//last X's sibling has to be null
		E.sibling = null;
			
		return newRoot;
	}
	
	/*
	 * where
	 * 
	    gamma 		          where
         / \                  / \
	lambda   E       <=      P   =   
   	/  \                   		/ \  
   X    P                      X   E 

	 */
	Node StandardizeWhere(Node where){
		Node P = where.first;
		Node eq = P.sibling;
		Node X = eq.first;
		Node E = X.sibling;
		
		Node gamma = new Node(new Token(TokenType.GAMMA, "gamma")); // new root
		Node lambda = new Node(new Token(TokenType.LAMBDA, "lambda"));
		gamma.first = lambda;
		lambda.sibling = E;
		E.previous = lambda;
		lambda.first = X;
		X.sibling = P;
		P.previous = X;
		P.sibling = null;
		
		return gamma;
	}

	/* rec
	 
	   rec     =>           =
        |                  / \
        =                 X   gamma
       / \                   /   \
      X   E           		Ystar  lambda
                          		   /  \
                          		  X    E
	 */
	Node StandardizeRec(Node rec){
		logger.info("in StandardizeRec");
		Node eq = rec.first;
		Node X = eq.first;
		Node E = X.sibling;
		
		Node newRoot = new Node(new Token(TokenType.EQUALS, "=")); // =
		newRoot.first = X;
		
		Node copyX = P.treeBuilder.createCopy(X);
		Node gamma = new Node(new Token(TokenType.GAMMA, "gamma"));
		X.sibling = gamma;
		gamma.previous = X;
		// TODO: may need to remove <> later !!!!
		Node yStar = new Node(new Token(TokenType.YSTAR, "Y*"));
		gamma.first = yStar;
		Node lambda = new Node(new Token(TokenType.LAMBDA, "lambda"));
		yStar.sibling = lambda;
		lambda.previous = yStar;
		
		lambda.first = copyX;
		copyX.sibling = E;
		E.previous = copyX;
		return newRoot;
	}
	
	
	/* @
	     @      =>      gamma
      / | \            	/   \
   	 E1 N  E2  		  gamma  E2
                       /  \
              	      N    E1
	 */
	
	Node StandardizeAt(Node at){
		Node E1 = at.first;
		Node N = E1.sibling;
		Node E2 = N.sibling;
		
		Node newRoot = new Node(new Token(TokenType.GAMMA, "gamma"));
		Node gammaL = new Node(new Token(TokenType.GAMMA, "gamma"));
		
		newRoot.first = gammaL;
		gammaL.sibling = E2;
		E2.previous = gammaL;
		gammaL.first = N;
		N.sibling = E1;
		E1.previous = N;
		E1.sibling = null;
		
		return newRoot;
	}

}