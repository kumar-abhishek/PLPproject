class Node {
	Node first;
	Node sibling;
	Node previous;
	//String tokenName;
	Token token;
	//Node tokenType;
	Node(Token tok){
		first = null;
		sibling = null;
		previous = null;
		token = tok;
	}
}
