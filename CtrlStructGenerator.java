import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;


/*
 * a map of CSs : delta0, delta1,...
 * each delta is a string
 */
public class CtrlStructGenerator {
	private class Triple<F, S, T>{
		public F first;
		public S second;
		public T third;
		public Triple(F first, S second, T third){
			this.first = first;
			this.second = second;
			this.third = third;
		}
	}
	
	Queue<Triple<Integer, Node, List<Object>> > queue = new LinkedList<Triple<Integer, Node, List<Object> > >();
	
	private static final Logger logger = Logger.getLogger(CtrlStructGenerator.class.getName());
	Map<Integer, CtrlStruct> mapCtrlStructs = null;
	int curIdxDelta = 0;
	Queue<Node> tempQueue = null;
	
	CtrlStructGenerator(){
		mapCtrlStructs = new HashMap<Integer, CtrlStruct>();
		//tempQueue = new LinkedList<Node>();
	}
	
	Map<Integer, CtrlStruct>getCtrlStructs(){
		return mapCtrlStructs;
	}
	
	void preOrderTraversal(Node root, List<Object> delta){
		//root null handling 
		logger.info("token type: " + root.token.type + "| name: " + root.token.name +  "\n\n\n");
		 switch (root.token.type) {
			case ID: 
			case INTEGER:
			case STRING:
			case TRUE:
			case FALSE: 
			case NIL:
			case YSTAR:
			case DUMMY : {
				delta.add(root.token);
				if(root.sibling != null){
					preOrderTraversal(root.sibling, delta);
				}
				return;
			}
			case LAMBDA : {
				curIdxDelta += 1;
				//currently all environment Indices of each lambda are set to 0, they will be 
				//set to proper values when the lambda gets moved to stack.
				LambdaExpression lambda = null;
				if(root.first.token.type == TokenType.COMMA){
					//rule 11
					List<Token> tauList = new ArrayList<Token>();
					Node first = root.first.first;
					while(first != null){
						tauList.add(first.token);
						first = first.sibling;
					}
					lambda = new LambdaExpression(0, curIdxDelta, tauList);
				}
				else {
					lambda = new LambdaExpression(0, curIdxDelta, root.first.token);
				}
				logger.info("adding lambda with id: " + curIdxDelta);
				delta.add(lambda);
				List<Object> deltaLambda = new ArrayList<Object>();
				queue.offer(new Triple<Integer, Node, List<Object>>(curIdxDelta, root.first.sibling, deltaLambda));
				/*
				if(root.first.sibling != null){
					List<Object> siblingDelta = new ArrayList<Object>();
					int savedIdxDelta = curIdxDelta;
					preOrderTraversal(root.first.sibling, siblingDelta);
					CtrlStruct ctrlDelta = new CtrlStruct(savedIdxDelta, siblingDelta);
					mapCtrlStructs.put(savedIdxDelta, ctrlDelta);
				}
				*/
				
				if(root.sibling != null){
					preOrderTraversal(root.sibling, delta);
				}
				return;
			}
			
			case NEG:
			case NOT : {/* Uniary operators */
				delta.add(root.token);
				preOrderTraversal(root.first, delta);
				if(root.sibling != null){
					preOrderTraversal(root.sibling, delta);
				}
				return;
			}
			case OR:
			case AND: {/* Logical binops */
				delta.add(root.token);
				preOrderTraversal(root.first, delta);
				if(root.sibling != null){
					preOrderTraversal(root.sibling, delta);
				}
				return;
			}
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
				delta.add(root.token);
				preOrderTraversal(root.first, delta);
				if(root.sibling != null){
					preOrderTraversal(root.sibling, delta);
				}
				return;
			}
			
			case CONDITIONAL : { /* The conditional */
				logger.info("adding ->");
				List<Object> delta2 = new ArrayList<Object>();
				//int savedcurIdxDelta = curIdxDelta;
				int savedcurIdxDelta2 = curIdxDelta + 1;
				int savedcurIdxDelta3 = curIdxDelta + 2;
				curIdxDelta += 2;
				
				Node node2 = root.first.sibling;
				Node node3 = root.first.sibling.sibling;
				
				node2.sibling = null;  //to avoid re-traversal
				/*
				preOrderTraversal(node2, delta2);
				CtrlStruct ctrlDelta2 = new CtrlStruct(savedcurIdxDelta2, delta2);
				mapCtrlStructs.put(savedcurIdxDelta2, ctrlDelta2);
				*/
				
				queue.offer(new Triple<Integer, Node, List<Object>>(savedcurIdxDelta2, node2, delta2));
				
				//node3.sibling = null;
				
				List<Object> delta3 = new ArrayList<Object>();
				/*
				preOrderTraversal(node3, delta3);
				CtrlStruct ctrlDelta3 = new CtrlStruct(savedcurIdxDelta3, delta3);
				mapCtrlStructs.put(savedcurIdxDelta3, ctrlDelta3);
				*/
				queue.offer(new Triple<Integer, Node, List<Object>>(savedcurIdxDelta3, node3, delta3));
				delta.add(delta2);
				delta.add(delta3);
				logger.info("adding beta \n\n\n");
				Beta beta = new Beta();
				delta.add(beta); // TODO: may create a problem : be careful!!!!!!!!!!!!!!!!
				
				//this is imp so that you don't traverse the sibling of the 1st child again
				//as you already did it above.
				root.first.sibling = null;
				logger.info("now callng preOrder for root.first: " + root.first.token.name );
				preOrderTraversal(root.first, delta);				
				
				if(root.sibling != null){
					preOrderTraversal(root.sibling, delta);
				}
				return;
			}
			case GAMMA : { /* The multifunctional gamma */
				logger.info("adding gamma");
				logger.info("root is: "  + root.token.name);
				delta.add(root.token);
				preOrderTraversal(root.first, delta);
				if(root.sibling != null){
					preOrderTraversal(root.sibling, delta);
				}
				return;
			}
			case TAU :  { /* Create a tau item */
				//Tau tau = new tau()
				//delta.add(root.token);
				Node node = root.first;
				Node nextNode = node.sibling;
				List<Object> deltasTau = new ArrayList<Object>();
				int counter = 0;
				while(node != null){
					node.sibling = null;
					preOrderTraversal(node, deltasTau);
					node = nextNode;
					if(node != null){
						nextNode = node.sibling;	
					}	
					++counter;
				}
				Tau tau = new Tau(counter);
				delta.add(tau);
				delta.addAll(deltasTau);
				if(root.sibling != null){
					preOrderTraversal(root.sibling, delta);
				}
				return;
			}
			default: 
				System.out.println("Don't know what do with: " + root.token.name  + "!!!!!!!!!!!!!\n\n\n\n");
				return;
			}
	}
	
	/*
	 * do a preorder traversal 
	 * and keep generating ctrl structs
	 */
	void generateCtrlStructs(Node root){
		List<Object> delta = new ArrayList<Object>();
		
		preOrderTraversal(root, delta);
		
		CtrlStruct ctrlDelta = new CtrlStruct(curIdxDelta, delta);
		mapCtrlStructs.put(0, ctrlDelta);
		while(!queue.isEmpty()){
			Triple<Integer, Node, List<Object>> p = queue.peek();
			int idx = p.first;
			Node node = p.second;
			List<Object> deltaQueue = p.third;
			preOrderTraversal(node, deltaQueue);
			//delta = new ArrayList<Object>();
			ctrlDelta = new CtrlStruct(idx, deltaQueue);
			mapCtrlStructs.put(idx, ctrlDelta);
			queue.remove();
		}
	}
	
	void printCtrlStructs(){
		logger.info("Starting printCtrlStructs:\n\n\n");
		for(Map.Entry<Integer, CtrlStruct> entry : mapCtrlStructs.entrySet()){
			logger.info("key: " + entry.getKey());
			for(Object obj : entry.getValue().getcontents()){
				if(obj instanceof Token){
					logger.info("value: " + ((Token)obj).name + " | " + ((Token)obj).type);
				}
				else if(obj instanceof LambdaExpression){
					logger.info("value: commented for now" );
					((LambdaExpression)obj).printLambdaExpression();
				}
				else if (obj instanceof ArrayList){
					logger.info("i am  a list");
					for(Object item: (ArrayList<Object>)obj){
						if(item instanceof Token){
							logger.info("Token item: " + ((Token)item).name + " | " + ((Token)item).type);
						}
						else{
							logger.info("item: " + item);
						}
					}
				}
				else {
					logger.info("i was not Token or LambdaExpression, value: " + obj);
				}
			}
			logger.info("next obj\n\n");
		}
	}
}
