import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class Tuple {
	private static final Logger logger = Logger.getLogger(Tuple.class.getName());
	List<Object> contents = null;
	Tuple(){
		contents = new ArrayList<Object>();
	}
	void addElement(Object obj){
		contents.add(obj);
	}
	Object getElementAtIndexN(int idx){
		logger.info("in getElementAtIndexN: idx: " + idx);
		return contents.get(idx);
	}
	int size(){
		return contents.size();
	}
	void printTuple(){
		logger.info("in printTuple");
		Tuple randTuple = (Tuple)this;
		System.out.print("(");
		int id = 0;
		//logger.info((Tuple)rand);
		for(;id < randTuple.size() - 1; id++){
			Object tupleItem = randTuple.getElementAtIndexN(id);
			if(tupleItem instanceof Token){
				logger.info("in printTuple: " + (Token)tupleItem);
				System.out.print(((Token)(tupleItem)).name + ", ");
			}
			else if(tupleItem instanceof Tuple){
				logger.info("in printTuple: tupleItem is a Tuple again ");
				((Tuple)tupleItem).printTuple();
			}
			else {
				System.out.println("I dont know how to print this tupleContent: " + tupleItem);
			}
			
		}
		Object tupleContent = randTuple.getElementAtIndexN(id);
		if(tupleContent instanceof Token){
			System.out.print(((Token)tupleContent).name + ")");
		}
		else if (tupleContent instanceof Tuple) {
			((Tuple)tupleContent).printTuple();
		}
		else { 
			System.out.println("some error in printTuple");
		}
	}
}
