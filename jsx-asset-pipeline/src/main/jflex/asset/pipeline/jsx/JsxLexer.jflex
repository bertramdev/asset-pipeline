package asset.pipeline.jsx;
import asset.pipeline.jsx.symbols.*;
import java.util.ArrayList;
import java.io.*;

/**
 * This class is a JSX lexer.
 * @author David Estes
 */
%%

%class JsxLexer
%unicode
%line
%column
%char
%type Symbol

%yylexthrow{
JsxParserException
%yylexthrow}

%{
  StringBuffer string = new StringBuffer();
  int curleyBraceCounter = 0;
  ArrayList<Symbol> elementStack = new ArrayList<Symbol>();
  JsxAttribute attribute;

  private Symbol symbol(String name) {
    Symbol sym = new GenericSymbol(name,null, yyline,yycolumn,yychar);
    if(elementStack.size() == 0) {
      return sym;
    } else {
      Symbol element = elementStack.get(elementStack.size()-1);
      element.appendChild(sym);
      return null;
    }
  }

  private Symbol symbol(String name, String value) {
    Symbol sym = new GenericSymbol(name,value, yyline,yycolumn,yychar);
    if(elementStack.size() == 0) {
      return sym;
    } else {
      Symbol element = elementStack.get(elementStack.size()-1);
      element.appendChild(sym);
      return null;
    }
  }

  private Symbol jsxElement(String value) {
    Symbol sym = new JsxElement("JSXElement",value, yyline,yycolumn-1,yychar-1);
    
    if(elementStack.size() == 0) {
      elementStack.add(sym);
      return sym;
    } else {
      Symbol element = elementStack.get(elementStack.size()-1);
      elementStack.add(sym);
      
      element.appendChild(sym);
      return null;
    }
  }

  private void addAttributeSymbol(String name, String value) {
    JsxAttribute attr = new JsxAttribute(name,value, yyline,yycolumn,yychar);
    Symbol sym = elementStack.get(elementStack.size()-1);
    sym.appendAttribute(attr);
  }

  private void addAttributeSymbol(JsxAttribute attr) {
    Symbol sym = elementStack.get(elementStack.size()-1);
    sym.appendAttribute(attr);
  }

  private void closeJsxElement() {
    Symbol sym = elementStack.get(elementStack.size()-1);
    sym.setLength(yychar - sym.getPosition() + yylength());
    elementStack.remove(elementStack.size()-1);
  }

%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]
WhiteSpaceOpt = [ \t\f]+?

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment}


TraditionalComment   = "/*" [^*]+ ~"*/" | "/*" "*"+ "/"
// Comment can be the last line of the file, without line terminator.
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}?
DocumentationComment = "/**" {CommentContent} "*"+ "/"
CommentContent       = ( [^*] | \*+ [^/*] )* 

AnyChar = [^]
Identifier = [:jletter:] [:jletterdigit:]*
JSXElement = {JSXSelfClosingElement} | {JSXOpeningElement} {JSXChildren}* {JSXClosingElement}
JSXOpeningElement = "<" {JSXElementName} [^>]* ">"
JSXClosingElement = "</" {JSXElementName} \s* ">"
JSXSelfClosingElement = "<" {JSXElementName} [^>]* "/>"
JSXElementName = {Identifier} | {JSXNamespacedName} | {JSXMemberExpression}
JSXNamespacedName = {Identifier} ":" {Identifier}
JSXMemberExpression = ({Identifier} "." {Identifier})+

/*Attributes*/
JSXAttributes = {JSXSpreadAttribute} | {JSXAttribute} | \s | [^>]
JSXSpreadAttribute = "{..." {AssignmentExpression}* ~"}"  
JSXAttribute = {JSXAttributeName} {WhiteSpaceOpt} "=" {WhiteSpaceOpt} {JSXAttributeValue}
JSXAttributeName = [:jletter:] [a-zA-Z0-9\-\_]* | {JSXNamespacedName}
JSXAttributeValue = "\"" {JSXDoubleStringCharacters} "\"" | "\'" {JSXSingleStringCharacters} "\'" | "{" {AssignmentExpression}* "}"
JSXDoubleStringCharacters = {JSXDoubleStringCharacter}*
JSXSingleStringCharacters = {JSXSingleStringCharacter}*
JSXDoubleStringCharacter = [^\"]
JSXSingleStringCharacter = [^\']

/* Children */
JSXChildren = {JSXChild}+
JSXChild = {JSXText} | {JSXElement} | {ChildExpression}
ChildExpression = "{" {AssignmentExpression}* "}"
JSXText = {JSXTextCharacter}+
JSXTextCharacter = [^\{\}\<\>]
AssignmentExpression = [^]

%state STRINGDOUBLE
%state STRINGSINGLE
%state JSXOPENINGELEMENT
%state JSXSELFCLOSINGELEMENT
%state JSXELEMENT
%state JSXATTRIBUTES
%state JSXATTRIBUTE
%state JSXATTRIBUTEVALUE
%state JSXCHILDREN
%state ASSIGNMENTEXPRESSION
%state ASSIGNMENTEXPRESSIONVALUE
%state ATTRIBUTEEXPRESSION
%%

/* keywords */
<YYINITIAL> {
  /* identifiers */ 
  {JSXSelfClosingElement}        {yybegin(JSXSELFCLOSINGELEMENT);yypushback(yylength()-1);}
  {JSXOpeningElement}            {yybegin(JSXOPENINGELEMENT);yypushback(yylength()-1);}
  /* literals */
  \"                             { string.setLength(0); yybegin(STRINGDOUBLE); }
  \'                             { string.setLength(0); yybegin(STRINGSINGLE); }

  /* comments */
  {Comment}                      { /* ignore */ }
 
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
  {AnyChar}                      { /* ignore */ }
}

<STRINGDOUBLE, STRINGSINGLE> {
  \\t                            { string.append('\t'); }
  \\n                            { string.append('\n'); }
  \\r                            { string.append('\r'); }
  \\                             { string.append('\\'); }
}
<STRINGDOUBLE> {
  \"                             { if(attribute != null) { yybegin(JSXATTRIBUTES); attribute.setValue(string.toString()) ; addAttributeSymbol(attribute); } else {yybegin(YYINITIAL);} }  
  \\\"                           { string.append('\"'); }
  [^\n\r\"\\]+                   { string.append( yytext() ); }
}

<STRINGSINGLE> {
  [^\n\r\'\\]+                   { string.append( yytext() ); }
  \'                             { if(attribute != null) { yybegin(JSXATTRIBUTES); attribute.setValue(string.toString()) ; addAttributeSymbol(attribute); } else {yybegin(YYINITIAL);} }
  \\'                            { string.append('\''); }
}



<JSXOPENINGELEMENT, JSXSELFCLOSINGELEMENT> {
  {JSXElementName}               {yybegin(JSXATTRIBUTES);Symbol sym = jsxElement(yytext()); if(sym != null) {return sym;}}
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}

<JSXOPENINGELEMENT> {
  >                             {yybegin(JSXCHILDREN);}
}
<JSXSELFCLOSINGELEMENT> {
  \/>                            {closeJsxElement(); if(elementStack.size() == 0) { yybegin(YYINITIAL); } else { yybegin(JSXCHILDREN); } }
}

<JSXATTRIBUTES> {
  \/>                            {yybegin(JSXSELFCLOSINGELEMENT); yypushback(yylength()); attribute = null; }
  >                             {yybegin(JSXOPENINGELEMENT); yypushback(yylength()) ; attribute = null;}
  {JSXSpreadAttribute}           {string.setLength(0);attribute = new JsxAttribute("JSXAttribute",null,yyline,yycolumn,yychar);attribute.setAttributeType("spreadAttribute"); yypushback(yylength()-4); yybegin(ASSIGNMENTEXPRESSION);}
  {JSXAttribute}                  {yybegin(JSXATTRIBUTE);yypushback(yylength()); attribute = new JsxAttribute("JSXAttribute",null,yyline,yycolumn,yychar);}
  {JSXAttributeName}             {attribute = new JsxAttribute("JSXAttribute",null,yyline,yycolumn,yychar);attribute.setName(yytext());addAttributeSymbol(attribute);}
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}

<JSXATTRIBUTE> {
  {JSXAttributeName}             {attribute.setName(yytext());}
  =                              {yybegin(JSXATTRIBUTEVALUE);}
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}

<JSXATTRIBUTEVALUE> {
  \"                             {yybegin(STRINGDOUBLE); string.setLength(0);}
  \'                             {yybegin(STRINGSINGLE); string.setLength(0);}
  {ChildExpression}              {yybegin(ASSIGNMENTEXPRESSION);string.setLength(0);yypushback(yylength() - 1);attribute.setAttributeType("assignmentExpression");}
  {WhiteSpace}                   { /* ignore */ }
}

<JSXCHILDREN> {
  {JSXClosingElement}             { closeJsxElement(); if(elementStack.size() == 0) { yybegin(YYINITIAL); } else { yybegin(JSXCHILDREN);} }
  {JSXSelfClosingElement}        {yybegin(JSXSELFCLOSINGELEMENT);yypushback(yylength()-1);}
  {JSXOpeningElement}            {yybegin(JSXOPENINGELEMENT);yypushback(yylength()-1);}
  
  
  {JSXText}                       { symbol("JSXText",yytext()); }
  {ChildExpression}               { yybegin(ASSIGNMENTEXPRESSION);yypushback(yylength() - 1);string.setLength(0); }
  {WhiteSpace}                    { /* ignore */ }
}

<ASSIGNMENTEXPRESSION> {
  \}                            {if(curleyBraceCounter > 0) { string.append(yytext()); curleyBraceCounter--; } else if(attribute != null) {attribute.setValue(string.toString());addAttributeSymbol(attribute); yybegin(JSXATTRIBUTES);string.setLength(0);} else {yybegin(JSXCHILDREN); symbol("JSXAssignmentExpression",string.toString());string.setLength(0);}}
  \{                            { string.append(yytext()); curleyBraceCounter++;}
  [^\{\}]                   { string.append( yytext() ); }
}


/* error fallback */
    [^]                              { throw new JsxParserException("Illegal character <"+
                                                        yytext()+"> found on line: " + (yyline+1) + " col: " + (yycolumn+1) ); }
