package org.apache.commons.httpclient.methods;

public class PatchMethod extends PostMethod {
	
	public PatchMethod(String url) {
		super(url);
	}
	
	@Override
	public String getName() {
		return "PATCH";
	}
}
