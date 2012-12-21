import java.util.List;

//basically the delta structure:
public class CtrlStruct {
	int index = 0;
	List<Object> contents = null;
	CtrlStruct(int id, List<Object> cnts){
		index = id;
		contents = cnts;
	}
	CtrlStruct(int id){
		index = id;
	}
	void setCtrlStructIdx(int id){
		index = id;
	}
	void setCtrlStructContent(List<Object> cnts){
		contents = cnts;
	}
	List<Object> getcontents(){
		return contents;
	}
}
