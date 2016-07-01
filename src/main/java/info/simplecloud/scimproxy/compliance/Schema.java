package info.simplecloud.scimproxy.compliance;

import java.util.ArrayList;


public class Schema {

    private ArrayList<String> required = new ArrayList<String>();

    public void setRequired(ArrayList<String> required) {
            this.required = required;
    }

    public ArrayList<String> getRequired() {
            return required;
    }
    
    public void addItem(String s) {
            this.required.add(s);
    }	
	
	/*
	private ArrayList<SchemaAttribute> required = new ArrayList<SchemaAttribute>();

	public ArrayList<SchemaAttribute> getRequired() {
		return required;
	}
	
	public void addItem(SchemaAttribute attribute) {
		this.required.add(attribute);
	}
	*/
	
}
