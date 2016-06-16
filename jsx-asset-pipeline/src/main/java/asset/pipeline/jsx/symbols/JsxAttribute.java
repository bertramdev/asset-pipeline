package asset.pipeline.jsx.symbols;

import java.util.List;
import java.util.ArrayList;

public class JsxAttribute extends GenericSymbol {
	public JsxAttribute(String name) {
		super(name);
	}

	private String attributeType="value";

	public void setAttributeType(String type) {
		this.attributeType = type;
	}

	public String getAttributeType() {
		return this.attributeType;
	}

	public JsxAttribute(String name,String value,Integer line, Integer column,Integer position) {
		super(name,value,line,column,position);
	}
}