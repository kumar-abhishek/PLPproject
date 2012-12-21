import java.util.ArrayList;
//import java.util.List;


public class LambdaExpression {
	int envId;
	int idx;
	//Token token;
	Object item;
	LambdaExpression(int environmentId, int id, Object tok){
		envId = environmentId;
		idx = id;
		item = tok;
	}
	void setEnvironmentId(int id){
		envId = id;
	}
	int getEnvironmentId(){
		return envId;
	}
	void printLambdaExpression(){
		if(item instanceof Token){
			//System.out.print("[lambda closure: " + ((Token)item).name + ": " + idx + "]");
			System.out.print("lambda~" + envId +  "~" + ((Token)item).name  + "~" + idx);
		}
		else if(item instanceof ArrayList){
			//System.out.println("Lambda's item is a list");
			String lamVars = "";
			for(Object it: (ArrayList<Object>)item){
				//System.out.println("it: " + ((Token)it).name);
				lamVars += ((Token)it).name + ',';
			}
			System.out.print("lambda~" + envId +  "~" + lamVars + "~" + idx);
		}
	}
}
