package asset.pipeline.jsx.symbols;

import java.util.List;
import java.util.ArrayList;
import java.io.*;

public class JsxElement extends GenericSymbol {
	public JsxElement(String name) {
		super(name);
	}

	public JsxElement(String name,String value,Integer line, Integer column,Integer position) {
		super(name,value,line,column,position);
	}
}