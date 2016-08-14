package asset.pipeline.jsx;

/**
* When a JSX Lexical parser error is reached this exception is thrown.
* @author David Estes
*/
public class JsxParserException extends Exception {
	public JsxParserException(String message) {
        super(message);
    }
}