import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class Environment {
	public static final Logger logger = Logger.getLogger(Environment.class.getName());
	//index of the env
	int idx;
	//map of values in the env
	Map<Object, Object> mapVars = null;
	//link to parent env
	Environment parent = null;
	
	Environment(int index){
		idx = index;
		mapVars = new HashMap<Object, Object>();
	}
	
	void setEnvParams(Environment parentEnv, Object key, Object value){
		mapVars.put(key, value);
		if(key instanceof Token && value instanceof Token){
			logger.info("setEnvParams: " + key + "| " + ((Token)key).name + "|  value: " + ((Token)value).name);
		}
		else {
			//logger.info("setEnvParams: key: " + key + "|  value: " + ((Token)value).name);
		}
		parent = parentEnv;
		logger.info("setting parent of environment: " + idx + " as env : " + parentEnv.idx);
	}
	
	int getEnvIdx(){
		return idx;
	}
	
	Object getVal(Object key){
		//logger.info("getVal: " + (Token)key + "| " + ((Token)key).name);
		if(mapVars.containsKey(key)){
			Object value = mapVars.get(key);
			logger.info("found in cur env id " + idx);
			if(value instanceof Token){
				logger.info("value: " + ((Token)value).name);
			}
			return value;
		}
		else{
			logger.info("not found in cur env");
			return null;
		}
	}
}
