import java.util.Stack;

import java.util.EmptyStackException;

class TreeBuilder{
	private Stack<Node> stack;
	
	TreeBuilder(){
		stack = new Stack<Node>();
	}
	
	/*
	 * return the AST built by Parser
	 */
	Node getTree(){
		Node root = stack.pop();
		return root;
	}
	//deep copy
	Node createCopy(Node root){
		if(root == null){
			return null;
		}
		Node node = new Node(root.token);
		node.first = createCopy(root.first);
		//System.out.println(dots + root.tokenName + " ");
		
		if(root.sibling != null){
			node.sibling = createCopy(root.sibling);
		}
		return node;
	}
	
	void Preorder(Node root, int level){
		//if(level >200)
		//	return;
		if(root == null){
			return;
		}
		String dots = "";
		for(int i = 0; i< level; i++){
			dots += '.';
		}
		// add <> to various things + special handling for printing special tokens
		String printTokenName = "";
	 	if(root.token.type == TokenType.ID){
			//System.out.println("in Parser ID: I am here!!!" );
			printTokenName = "<ID:" + root.token.name + ">";
		}
		else if(root.token.type == TokenType.INTEGER){
			printTokenName = "<INT:" + root.token.name + ">";
		}
		else if(root.token.type == TokenType.STRING){
			printTokenName = "<STR:'" + root.token.name + "'>";
		}
		else if(root.token.type == TokenType.NIL){
			printTokenName = "<" + root.token.name + ">";
		}
		else if(root.token.type == TokenType.TRUE){
			printTokenName = "<" + root.token.name + ">";
		}
		else if(root.token.type == TokenType.FALSE){
			printTokenName = "<" + root.token.name + ">";
		}
		else if(root.token.type == TokenType.DUMMY){
			printTokenName = "<" + root.token.name + ">";
		}
		else if(root.token.type == TokenType.YSTAR){
			printTokenName = "<" + root.token.name + ">";
		}
		else {
			printTokenName = root.token.name;
		}
		//may have to add  () here for rparenthesis 
		
		System.out.println(dots + printTokenName + " ");
		Preorder(root.first, level + 1);
		if(root.sibling != null){
			Preorder(root.sibling, level);
		}
	}
	
	void PrintTree(){
		Node root = stack.pop();
		if(root == null){
			System.out.println("!!!!!ERROR in PrintTree()!!!");
		}
		Preorder(root, 0);
	}
	
	public void BuildTree(Token token, int n){
		//System.out.println("need to insert into stack: " + tokenName + "\n\n\n");
		Node node = new Node(token);
		if(n==0){
			stack.push(node);
			//System.out.println("inserted into stack: " + node.tokenName + " | n==0 \n\n\n");
		}
		else if(n ==1){
			Node stackNode = stack.pop();
			node.first = stackNode;
			stack.push(node);
			//System.out.println("inserted into stack: " + node.tokenName + " | n == 1\n\n\n");
		} 
		else if(n==2){
			try{
				Node stackNode1 = stack.pop();
				//System.out.println(stackNode1.tokenName + " sz: " + stack.size());
				
				Node stackNode2 = stack.pop();
				//System.out.println(stackNode2.tokenName + " sz: " + stack.size());
				node.first = stackNode2;
				stackNode2.sibling = stackNode1;
				stackNode1.previous = stackNode2;
				stack.push(node);
				//System.out.println("inserted into stack: " + node.tokenName + " | n == 2\n\n\n");
			}
			catch(EmptyStackException e){
				//System.out.println("\n\n\n\n\n\n\n\nEmpty STAACK exception\n\n\n\n\n\n\n\n\n");
				for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
				    System.out.println(ste);
				}
			}

		}
		//poly-ary tree case 
		else {
			BuildPolyaryTree(n, node);
		}
	}
	
	public void BuildPolyaryTree(int n, Node newNode){
			Node temp = stack.pop();
			//System.out.println("1st poppedd item : " + temp.tokenName);
			//Node leftNodeTemp = temp;
			--n;
			while(n > 0){	
				Node rightNode = stack.pop();
				//System.out.println("poppedd item : " + rightNode.tokenName + "| n = " + n);
				rightNode.sibling = temp;
				temp.previous = rightNode;
				temp = rightNode;
				--n;
			}
			newNode.first = temp;
			stack.push(newNode);
			//System.out.println("inserted into stack: " + newNode.tokenName + " | n > 2 case \n\n\n");
	}
}