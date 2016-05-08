/* Generated By:JavaCC: Do not edit this line. CompactSyntaxConstants.java */
package com.thaiopensource.relaxng.parse.compact;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface CompactSyntaxConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int NEWLINE = 37;
  /** RegularExpression Id. */
  int NOT_NEWLINE = 38;
  /** RegularExpression Id. */
  int WS = 39;
  /** RegularExpression Id. */
  int DOCUMENTATION = 40;
  /** RegularExpression Id. */
  int DOCUMENTATION_CONTINUE = 41;
  /** RegularExpression Id. */
  int SINGLE_LINE_COMMENT = 42;
  /** RegularExpression Id. */
  int DOCUMENTATION_AFTER_SINGLE_LINE_COMMENT = 43;
  /** RegularExpression Id. */
  int SINGLE_LINE_COMMENT_CONTINUE = 44;
  /** RegularExpression Id. */
  int BASE_CHAR = 45;
  /** RegularExpression Id. */
  int IDEOGRAPHIC = 46;
  /** RegularExpression Id. */
  int LETTER = 47;
  /** RegularExpression Id. */
  int COMBINING_CHAR = 48;
  /** RegularExpression Id. */
  int DIGIT = 49;
  /** RegularExpression Id. */
  int EXTENDER = 50;
  /** RegularExpression Id. */
  int NMSTART = 51;
  /** RegularExpression Id. */
  int NMCHAR = 52;
  /** RegularExpression Id. */
  int NCNAME = 53;
  /** RegularExpression Id. */
  int IDENTIFIER = 54;
  /** RegularExpression Id. */
  int ESCAPED_IDENTIFIER = 55;
  /** RegularExpression Id. */
  int PREFIX_STAR = 56;
  /** RegularExpression Id. */
  int PREFIXED_NAME = 57;
  /** RegularExpression Id. */
  int LITERAL = 58;
  /** RegularExpression Id. */
  int FANNOTATE = 59;
  /** RegularExpression Id. */
  int ILLEGAL_CHAR = 60;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int AFTER_SINGLE_LINE_COMMENT = 1;
  /** Lexical state. */
  int AFTER_DOCUMENTATION = 2;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\"[\"",
    "\"=\"",
    "\"&=\"",
    "\"|=\"",
    "\"start\"",
    "\"div\"",
    "\"include\"",
    "\"~\"",
    "\"]\"",
    "\"grammar\"",
    "\"{\"",
    "\"}\"",
    "\"namespace\"",
    "\"default\"",
    "\"inherit\"",
    "\"datatypes\"",
    "\"empty\"",
    "\"text\"",
    "\"notAllowed\"",
    "\"|\"",
    "\"&\"",
    "\",\"",
    "\"+\"",
    "\"?\"",
    "\"*\"",
    "\"element\"",
    "\"attribute\"",
    "\"(\"",
    "\")\"",
    "\"-\"",
    "\"list\"",
    "\"mixed\"",
    "\"external\"",
    "\"parent\"",
    "\"string\"",
    "\"token\"",
    "<NEWLINE>",
    "<NOT_NEWLINE>",
    "<WS>",
    "<DOCUMENTATION>",
    "<DOCUMENTATION_CONTINUE>",
    "<SINGLE_LINE_COMMENT>",
    "<DOCUMENTATION_AFTER_SINGLE_LINE_COMMENT>",
    "<SINGLE_LINE_COMMENT_CONTINUE>",
    "<BASE_CHAR>",
    "<IDEOGRAPHIC>",
    "<LETTER>",
    "<COMBINING_CHAR>",
    "<DIGIT>",
    "<EXTENDER>",
    "<NMSTART>",
    "<NMCHAR>",
    "<NCNAME>",
    "<IDENTIFIER>",
    "<ESCAPED_IDENTIFIER>",
    "<PREFIX_STAR>",
    "<PREFIXED_NAME>",
    "<LITERAL>",
    "\">>\"",
    "<ILLEGAL_CHAR>",
  };

}
