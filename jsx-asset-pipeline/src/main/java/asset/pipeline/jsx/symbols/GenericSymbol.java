package asset.pipeline.jsx.symbols;

import java.util.List;
import java.util.ArrayList;

public class GenericSymbol implements Symbol {
	private Integer line;
	private Integer column;
	private Integer position;
	private Integer length;

	private String name;
	private String value;

	private List<Symbol> children = new ArrayList<Symbol>();
	private List<Symbol> attributes = new ArrayList<Symbol>();
	private Symbol parent;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}


	public Integer getLine() {
		return line;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getColumn() {
		return column;
	}

	public Integer getPosition() {
		return position;
	}

	public List<Symbol> getChildren() {
		return children;
	}

	public List<Symbol> getAttributes() {
		return attributes;
	}

	public Symbol getParent() {
		return parent;
	}

	public void setParent(Symbol symbol) {
		this.parent = symbol;
	}

	public void appendChild(Symbol symbol) {
		children.add(symbol);
	}

	public void appendAttribute(Symbol symbol) {
		attributes.add(symbol);
	}

	public GenericSymbol(String name) {
		this.name = name;
	}

	public GenericSymbol(String name,String value,Integer line, Integer column,Integer position) {
		this.name = name;
		this.value = value;
		this.line = line;
		this.column = column;
		this.position = position;
	}
}
