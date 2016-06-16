package asset.pipeline.jsx.symbols;

import java.util.List;

public interface Symbol {

	Integer getLine();
	Integer getColumn();
	Integer getPosition();
	Integer getLength();
	void setLength(Integer length);

	String getName();
	void setName(String name);

	String getValue();
	void setValue(String value);

	List<Symbol> getAttributes();
	void appendAttribute(Symbol symbol);

	List<Symbol> getChildren();
	void appendChild(Symbol symbol);

	Symbol getParent();
	void setParent(Symbol parent);
}