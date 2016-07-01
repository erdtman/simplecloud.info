package info.simplecloud.scimproxy.compliance;

import java.util.ArrayList;


public class SchemaAttribute {
	
	private String name = "";
	private String type = "";
	private String description = "";
	private String schema = "";
	private boolean readOnly = false;
	private boolean required = false;
	private boolean caseExact = false;

	// root only attributes
	private boolean multiValued = false; 
	private String multiValuedAttributeChildName = "";
	private ArrayList<SchemaAttribute> subAttributes = new ArrayList<SchemaAttribute>();

	// subattribute only attribute
	private ArrayList<String> canonicalValues = new ArrayList<String>(); // subAttribute only!

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getSchema() {
		return schema;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isRequired() {
		return required;
	}

	public void setCaseExact(boolean caseExact) {
		this.caseExact = caseExact;
	}

	public boolean isCaseExact() {
		return caseExact;
	}

	public void setMultiValued(boolean multiValued) {
		this.multiValued = multiValued;
	}

	public boolean isMultiValued() {
		return multiValued;
	}

	public void setMultiValuedAttributeChildName(
			String multiValuedAttributeChildName) {
		this.multiValuedAttributeChildName = multiValuedAttributeChildName;
	}

	public String getMultiValuedAttributeChildName() {
		return multiValuedAttributeChildName;
	}

	public void setSubAttributes(ArrayList<SchemaAttribute> subAttributes) {
		this.subAttributes = subAttributes;
	}

	public ArrayList<SchemaAttribute> getSubAttributes() {
		return subAttributes;
	}

	public void setCanonicalValues(ArrayList<String> canonicalValues) {
		this.canonicalValues = canonicalValues;
	}

	public ArrayList<String> getCanonicalValues() {
		return canonicalValues;
	}

	
	
}
