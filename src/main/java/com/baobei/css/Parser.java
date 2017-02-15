/*
 * This class parses tokens received from a CSS text input
 * 
 * It can parse and validate a nearly complete CSS3 grammer
 *
 * http://www.w3.org/TR/css3-selectors/
 * 
 * David Boyd
 */
package com.baobei.css;

import java.io.Reader;
import java.io.IOException;
import com.baobei.css.model.*;

public class Parser {
    private static final int NAME_FIRST = 1;
    private static final int NAME = 2;
    private static final int NAME_ATTRIBUTE_BEGIN = 3;
    private static final int NAME_ATTRIBUTE_EQUALS = 4;
    private static final int NAME_ATTRIBUTE_VALUE = 5;
    private static final int NAME_ATTRIBUTE_END = 6;
    private static final int NAME_PSEUDO = 7; // :focus
    private static final int NAME_PSEUDO_ARGUMENT = 8; // :nth-of-type(1)
    private static final int NAME_PSEUDO_END = 9;
    private static final int NAME_END = 10;
    private static final int RULE_NAME = 11;
    private static final int RULE_SEPARATOR = 12;
    private static final int RULE_VALUE = 13;
    private static final int RULE_VALUE_NAME = 14;
    private static final int RULE_VALUE_FUNCTION_ARGUMENT = 15;
    private static final int RULE_VALUE_FUNCTION_COMMA = 16;
    private static final int RULE_VALUE_FUNCTION_ARGUMENT_2 = 17; // For function arguments that are functions as well, ie: -webkit-gradient(color-stop(35%,#eeeeee)) 
    private static final int RULE_VALUE_FUNCTION_COMMA_2 = 18;
    private static final int RULE_END = 19;

    // Types of rule values returned by getRuleValueType()
    private static final int VALUE_IDENTIFIER = 0; // Function name
    private static final int VALUE_UNITS = 1; // pt, em, % values
    private static final int VALUE_PREDEFINED = 2; // none, underline, bold, italic, etc.

    //private Node dom;

    public Parser( /*Node dom*/ ) {
        //this.dom = dom;
    }

    // This is called after each selector is parsed and ready to be matched against the DOM
    public void selectorParsed(Selector selector) {
        selector.calcWeight();
        selector.print();
        //dom.match(selector);
    }

    /*private int getRuleValueType(StringBuffer data)
    {
        char first = data.charAt(0);
        
        if ((first >= '0' && first <= '9') || first == '.')
        {
            return VALUE_UNITS;
        }
        else
        {
            String type = data.toString();
            
            if (   (first == 'b' && type.equals("bold"))
                || (first == 'i' && type.equals("italic"))
                || (first == 'u' && type.equals("underline")))
            {
                return VALUE_PREDEFINED;
            }
            else
            {
                return VALUE_IDENTIFIER;
            }
        }
    } */

    public void parse(String reader) throws IOException {
        Tokenizer tokens = new Tokenizer();

        int state = NAME_FIRST, token;
        StringBuffer data = new StringBuffer(64);
        char[] back = new char[1];
        Selector selector = new Selector();

        while ((token = tokens.getNext(reader, data, back)) != -1) {
            //System.out.println("-> line " + tokens.line + " " + Tokenizer.getToken(token) + (data.length() == 0 ? "" : " -> " + data.toString()));

            switch (state) {
                case NAME_FIRST:
                case NAME:
                    switch (token) {
                        case Tokenizer.IDENTIFIER:
                            if (state == NAME) {
                                selector.nextTag();
                            }

                            state = NAME;
                            selector.setTagName(data.toString());
                            break;

                        case Tokenizer.GREATHER_THAN: // Child relationship: app > tabs
                            state = NAME;
                            selector.lastTag.relation = Tag.RELATION_CHILD;
                            break;

                        case Tokenizer.TILDE: // Sibling relationship: app ~ tabs
                            state = NAME;
                            selector.lastTag.relation = Tag.RELATION_SIBLING;
                            break;

                        case Tokenizer.PLUS: // Sibling adjacent relationship: app + tabs
                            state = NAME;
                            selector.lastTag.relation = Tag.RELATION_SIBLING_ADJACENT;
                            break;

                        case Tokenizer.BRACKET_LEFT: // Start of attribute match: [type="xyz"]
                            state = NAME_ATTRIBUTE_BEGIN;
                            break;

                        case Tokenizer.COLON: // Start of pseduo attribute: :focus
                            state = NAME_PSEUDO;
                            break;

                        case Tokenizer.STAR: // Wild-card match: *
                            state = NAME;
                            break;

                        case Tokenizer.COMMA: // A series of names, separated by commas
                            if (state == NAME) {
                                state = NAME;
                                selector.nextPath();
                            } else {
                                error("NAME", token, data, tokens.line);
                                return;
                            }
                            break;

                        case Tokenizer.BRACE_LEFT: // Start of rule block
                            if (state == NAME) {
                                state = RULE_NAME;
                            } else {
                                error("NAME", token, data, tokens.line);
                                return;
                            }
                            break;

                        default:
                            error("NAME", token, data, tokens.line);
                            return;
                    }
                    break;

                case NAME_PSEUDO:
                    switch (token) {
                        case Tokenizer.IDENTIFIER:
                            state = NAME_PSEUDO_END;
                            selector.lastTag.addPseudo(data.toString());
                            break;

                        case Tokenizer.COLON: // If you have two colons: ie. map::after
                            state = NAME_PSEUDO;
                            break;

                        default:
                            error("NAME_PSEUDO", token, data, tokens.line);
                            return;
                    }
                    break;

                case NAME_PSEUDO_ARGUMENT:
                    switch (token) {
                        case Tokenizer.IDENTIFIER:
                            state = NAME_PSEUDO_ARGUMENT;
                            selector.lastTag.addPseudoArg(data.toString());
                            break;

                        case Tokenizer.PLUS: // ie. nth-child(2n+1)
                            state = NAME_PSEUDO_ARGUMENT;
                            selector.lastTag.addPseudoArg("+");
                            break;

                        case Tokenizer.PAREN_RIGHT:
                            state = NAME;
                            break;

                        default:
                            error("NAME_PSEUDO_ARGUMENT", token, data, tokens.line);
                            return;
                    }
                    break;

                case NAME_END:
                case NAME_PSEUDO_END:
                    switch (token) {
                        case Tokenizer.IDENTIFIER:
                            if (state == NAME_PSEUDO_END) {
                                state = NAME;

                                selector.nextTag();
                                selector.setTagName(data.toString());
                            } else {
                                error("NAME_END", token, data, tokens.line);
                                return;
                            }
                            break;

                        case Tokenizer.PAREN_LEFT:
                            if (state == NAME_PSEUDO_END) {
                                state = NAME_PSEUDO_ARGUMENT;
                            } else {
                                error("NAME_END", token, data, tokens.line);
                                return;
                            }
                            break;

                        case Tokenizer.GREATHER_THAN: // Direct child descendant: app > tabs
                            state = NAME;
                            selector.lastTag.relation = Tag.RELATION_CHILD;
                            break;

                        case Tokenizer.BRACE_LEFT: // Start of rule block
                            state = RULE_NAME;
                            break;

                        case Tokenizer.COLON:
                            if (state == NAME_PSEUDO_END)
                                state = NAME_PSEUDO;
                            else
                                state = NAME;
                            break;

                        case Tokenizer.COMMA:
                            state = NAME;
                            selector.nextPath();
                            break;

                        default:
                            error("NAME_END", token, data, tokens.line);
                            return;
                    }
                    break;

                case NAME_ATTRIBUTE_BEGIN:
                    switch (token) {
                        case Tokenizer.IDENTIFIER:
                            state = NAME_ATTRIBUTE_EQUALS;
                            selector.lastTag.attribKey = data.toString();
                            selector.lastTag.attribMatch = Tag.ATTRIB_ANY;
                            break;

                        default:
                            error("NAME_ATTRIBUTE_BEGIN", token, data, tokens.line);
                            return;
                    }
                    break;

                case NAME_ATTRIBUTE_EQUALS:
                    switch (token) {
                        case Tokenizer.EQUALS:
                            state = NAME_ATTRIBUTE_VALUE;
                            if (selector.lastTag.attribMatch == Tag.ATTRIB_ANY)
                                selector.lastTag.attribMatch = Tag.ATTRIB_EQUALS;
                            break;

                        case Tokenizer.BAR: // ie. [att|="val"]
                            state = NAME_ATTRIBUTE_EQUALS;
                            selector.lastTag.attribMatch = Tag.ATTRIB_EQUALS_DASH;
                            break;

                        case Tokenizer.CARET: // ie. [att^="val"] 
                            state = NAME_ATTRIBUTE_EQUALS;
                            selector.lastTag.attribMatch = Tag.ATTRIB_BEGINS;
                            break;

                        case Tokenizer.DOLLAR: // ie. [att$="val"]
                            state = NAME_ATTRIBUTE_EQUALS;
                            selector.lastTag.attribMatch = Tag.ATTRIB_ENDS;
                            break;

                        case Tokenizer.STAR: // ie. [att*="val"]
                            state = NAME_ATTRIBUTE_EQUALS;
                            selector.lastTag.attribMatch = Tag.ATTRIB_CONTAINS;
                            break;

                        case Tokenizer.TILDE: // ie. [att~="val"]
                            state = NAME_ATTRIBUTE_EQUALS;
                            selector.lastTag.attribMatch = Tag.ATTRIB_LIST_WORD;
                            break;

                        case Tokenizer.BRACKET_RIGHT: // ie. object[type]
                            state = NAME;
                            break;

                        default:
                            error("NAME_ATTRIBUTE_EQUALS", token, data, tokens.line);
                            return;
                    }
                    break;

                case NAME_ATTRIBUTE_VALUE:
                    switch (token) {
                        case Tokenizer.STRING:
                            state = NAME_ATTRIBUTE_END;
                            selector.lastTag.attribValue = data.toString();
                            break;

                        default:
                            error("NAME_ATTRIBUTE_VALUE", token, data, tokens.line);
                            return;
                    }
                    break;

                case NAME_ATTRIBUTE_END:
                    switch (token) {
                        case Tokenizer.BRACKET_RIGHT:
                            state = NAME;
                            break;

                        case Tokenizer.BRACE_LEFT: // Start of rule block
                            state = RULE_NAME;
                            break;

                        default:
                            error("NAME_ATTRIBUTE_END", token, data, tokens.line);
                            return;
                    }
                    break;

                // -----------------------------------------------------
                // Example: background-color: #0000ff
                // -----------------------------------------------------
                case RULE_NAME:
                    switch (token) {
                        case Tokenizer.IDENTIFIER:
                            state = RULE_SEPARATOR;

                            selector.nextRule();
                            selector.lastRule.name = data.toString();
                            break;

                        case Tokenizer.BRACE_RIGHT: // End of rule block, back to parsing another selector
                            state = this.selectorEnd(selector);
                            break;

                        case Tokenizer.SEMICOLON: // Ignore empty semi-colons: ie. width: 10px;;
                            state = RULE_NAME;
                            break;

                        default:
                            error("RULE_NAME", token, data, tokens.line);
                            return;
                    }
                    break;

                case RULE_SEPARATOR:
                    switch (token) {
                        case Tokenizer.COLON:
                            state = RULE_VALUE;
                            break;

                        default:
                            error("RULE_SEPARATOR", token, data, tokens.line);
                            return;
                    }
                    break;

                case RULE_VALUE:
                    switch (token) {
                        case Tokenizer.IDENTIFIER:
                            state = RULE_VALUE_NAME;

                            selector.lastRule.nextValue();
                            selector.lastRule.lastValue.setName(data.toString());
                            break;

                        case Tokenizer.STRING:
                            state = RULE_VALUE_NAME;

                            selector.lastRule.nextValue();
                            selector.lastRule.lastValue.setName(data.toString());
                            selector.lastRule.lastValue.type = RuleValue.TYPE_STRING;
                            break;

                        default:
                            error("RULE_VALUE", token, data, tokens.line);
                            return;
                    }
                    break;

                case RULE_END:
                    switch (token) {
                        case Tokenizer.SEMICOLON:
                            state = RULE_NAME;
                            break;

                        default:
                            error("RULE_END", token, data, tokens.line);
                            return;
                    }
                    break;

                // -----------------------------------------------------
                // Rule values examples:
                //
                //  helvetica, arial
                //  solid .25em #bbb
                //  5px auto -webkit-focus-ring-color
                //  linear-gradient(top,  #404040,  #000000)
                //  -webkit-gradient(linear, left top, left bottom, color-stop(35%, #eeeeee), color-stop(100%, #cccccc))
                //  background-image: linear-gradient(top, color(#8f9091, 52%) 6%, color(#8f9091, -36%) 95%, color(#8f9091, -20%) 100%);
                // -----------------------------------------------------
                case RULE_VALUE_NAME:
                    switch (token) {
                        case Tokenizer.IDENTIFIER:
                            state = RULE_VALUE_NAME;

                            selector.lastRule.nextValue();
                            selector.lastRule.lastValue.setName(data.toString());
                            selector.lastRule.lastValue.type = RuleValue.TYPE_IDENTIFIER;
                            break;

                        case Tokenizer.STRING:
                            state = RULE_VALUE_NAME;

                            selector.lastRule.nextValue();
                            selector.lastRule.lastValue.setName(data.toString());
                            selector.lastRule.lastValue.type = RuleValue.TYPE_STRING;
                            break;

                        case Tokenizer.PAREN_LEFT:
                            state = RULE_VALUE_FUNCTION_ARGUMENT;
                            selector.lastRule.lastValue.type = RuleValue.TYPE_FUNCTION;
                            break;

                        case Tokenizer.COMMA:
                            state = RULE_VALUE;
                            break;

                        case Tokenizer.SEMICOLON:
                            state = RULE_NAME;
                            break;

                        case Tokenizer.BRACE_RIGHT: // This tolerates missing ';' for last rule
                            state = this.selectorEnd(selector);
                            break;

                        default:
                            error("RULE_VALUE_NAME", token, data, tokens.line);
                            return;
                    }
                    break;

                case RULE_VALUE_FUNCTION_ARGUMENT:
                    switch (token) {
                        case Tokenizer.IDENTIFIER:
                            state = RULE_VALUE_FUNCTION_COMMA;

                            selector.lastRule.lastValue.nextFunctionArg();
                            selector.lastRule.lastValue.lastArg.setName(data.toString());
                            break;

                        case Tokenizer.STRING:
                            state = RULE_VALUE_FUNCTION_COMMA;

                            selector.lastRule.lastValue.nextFunctionArg();
                            selector.lastRule.lastValue.lastArg.setName(data.toString());
                            selector.lastRule.lastValue.lastArg.type = RuleValue.TYPE_STRING;
                            break;

                        case Tokenizer.PAREN_RIGHT:
                            state = RULE_END;
                            break;

                        default:
                            error("RULE_VALUE_FUNCTION_ARGUMENT", token, data, tokens.line);
                            return;
                    }
                    break;

                case RULE_VALUE_FUNCTION_COMMA:
                    switch (token) {
                        case Tokenizer.IDENTIFIER:
                            state = RULE_VALUE_FUNCTION_COMMA;
                            selector.lastRule.lastValue.lastArg.setName(data.toString());
                            break;

                        case Tokenizer.EQUALS:
                            state = RULE_VALUE_FUNCTION_COMMA;
                            break;

                        case Tokenizer.COMMA:
                            state = RULE_VALUE_FUNCTION_ARGUMENT;
                            break;

                            // For function arguments that are functions as well, ie: -webkit-gradient(color-stop(35%,#eeeeee))
                        case Tokenizer.PAREN_LEFT:
                            state = RULE_VALUE_FUNCTION_ARGUMENT_2;
                            selector.lastRule.lastValue.lastArg.type = RuleValue.TYPE_FUNCTION;
                            break;

                        case Tokenizer.PAREN_RIGHT:
                            state = RULE_VALUE_NAME;
                            break;

                        default:
                            error("RULE_VALUE_FUNCTION_COMMA", token, data, tokens.line);
                            return;
                    }
                    break;

                case RULE_VALUE_FUNCTION_ARGUMENT_2:
                    switch (token) {
                        case Tokenizer.IDENTIFIER:
                            state = RULE_VALUE_FUNCTION_COMMA_2;

                            selector.lastRule.lastValue.lastArg.nextFunctionArg();
                            selector.lastRule.lastValue.lastArg.lastArg.setName(data.toString());
                            break;

                        case Tokenizer.STRING:
                            state = RULE_VALUE_FUNCTION_COMMA_2;

                            selector.lastRule.lastValue.lastArg.nextFunctionArg();
                            selector.lastRule.lastValue.lastArg.lastArg.setName(data.toString());
                            selector.lastRule.lastValue.lastArg.lastArg.type = RuleValue.TYPE_STRING;
                            break;

                        case Tokenizer.PAREN_RIGHT:
                            state = RULE_VALUE_FUNCTION_COMMA;
                            break;

                        default:
                            error("RULE_VALUE_FUNCTION_ARGUMENT_2", token, data, tokens.line);
                            return;
                    }
                    break;

                case RULE_VALUE_FUNCTION_COMMA_2:
                    switch (token) {
                        case Tokenizer.IDENTIFIER:
                            state = RULE_VALUE_FUNCTION_COMMA_2;
                            selector.lastRule.lastValue.lastArg.setName(data.toString());
                            break;

                        case Tokenizer.COMMA:
                            state = RULE_VALUE_FUNCTION_ARGUMENT_2;
                            break;

                        case Tokenizer.PAREN_RIGHT:
                            state = RULE_VALUE_FUNCTION_COMMA;
                            break;

                        default:
                            error("RULE_VALUE_FUNCTION_COMMA", token, data, tokens.line);
                            return;
                    }
                    break;
            }

            data.setLength(0);
        }
    }

    private int selectorEnd(Selector selector) {
        if (!selector.isEmpty())
            selectorParsed(selector);

        selector.nextSelector();

        return NAME_FIRST;
    }

    private void error(String state, int token, StringBuffer data, int line) {
        if (token == Tokenizer.ERROR) {
            System.out.println("ERROR at line " + line + ": " + data.toString());
        } else {
            System.out.println("ERROR at line " + line + ": unexpected token " + Tokenizer.getToken(token) + " at state " + state);
        }
    }
}