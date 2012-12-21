import java.util.logging.*;

public class p2 {
	private static final Logger logger = Logger.getLogger(p2.class.getName());
	///*
	public static void main(String[] args){
		Logger log = LogManager.getLogManager().getLogger("");
		for (Handler h : log.getHandlers()) {
		    h.setLevel(Level.SEVERE);
		}
		
		//logger.info(args[0]);
		Parser P = new Parser(args[0]);
		//start parsing
		P.E();
		//System.out.println("\n\n\n\nPRINTING AST\n\n\n\n");
		//P.treeBuilder.PrintTree();
		Node root = P.treeBuilder.getTree();
		logger.info("Printing AST: ");
		//P.treeBuilder.Preorder(root, 0);
		
		logger.info("Standardizing tree now");
		TreeStandardizer treeStdd = new TreeStandardizer(P);
		root = treeStdd.StandardizeTree(root);
		logger.info("Printing Standardized tree");
		//P.treeBuilder.Preorder(root, 0);
		
		logger.info("generating control structures now:");
		CtrlStructGenerator cGenerator = new CtrlStructGenerator();
		cGenerator.generateCtrlStructs(root);
		//cGenerator.printCtrlStructs();
		
		logger.info("operating CSE Machine");
		CSEMachine cseMachine =  new CSEMachine(cGenerator);
		cseMachine.CSEMachineOperator();
		System.out.println();
	}
	//*/
}
