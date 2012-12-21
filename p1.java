
/*
 * TO run the diff command use this : 
 *  perl difftest.pl -1 "rpal/rpal -ast -noout FILE" -2 "java p1 -ast FILE" -t rpal/tests/
 *  OR:
 *  perl difftest.pl -1 "rpal/rpal -ast -noout FILE" -2 "java p1 -ast -noout FILE" -t rpal/tests/
 */
public class p1 {
//
//	
////	public static void main(String[] args){
//		///*
////		try{
////			Lexer L = new Lexer(args[2]);
////			
////			Token t = L.scan();
////			
////			while( (t != null) && t.type != TokenType.EOF) {
////				System.out.println("in main");
////				t.printTokenAttributes();
////				System.out.println("before scan: in main");
////				t = L.scan();
////				System.out.println("after scan: in main");
////			}
////			if(t != null){
////				System.out.println(t.type);
////			}
////			else {
////				System.out.println(" t is null\n");
////			}
////		}
////		catch(Exception e){
////			System.out.println("Exception in main() e: " + e);
////		} 
////		
//// 
////
////			String switch1 =  args[1];
////			if(switch1.equals("-ast"))
////			{
////				Parser P = new Parser(args[2]);
////				//start parsing
////				P.E();
////				//System.out.println("\n\n\n\nPRINTING AST\n\n\n\n");
////				P.treeBuilder.PrintTree();
////			}
////		
////	}

}
