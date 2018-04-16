package EnodeB.Components;

public enum EnodeBComponentTypes {
XLP("XLP"),DAN("DAN");
	
private String type;

EnodeBComponentTypes(String type){
	this.type=type;
}

@ Override
public String toString() {
	return this.type;
}

}