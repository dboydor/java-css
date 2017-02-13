/*
 * This class creates tokens from a CSS text input
 * 
 * David Boyd
 */
package com.baobei.css;

import java.io.Reader;
import java.io.IOException;

public class Tokenizer {
    static final int BAR = 0; // |
    static final int BRACE_LEFT = 1; // {
    static final int BRACE_RIGHT = 2; // }
    static final int BRACKET_LEFT = 3; // [
    static final int BRACKET_RIGHT = 4; // ]
    static final int CARET = 5; // ^
    static final int COLON = 6; // :
    static final int COMMA = 7; // ,
    static final int DOLLAR = 8; // $
    static final int EQUALS = 9; // =
    static final int GREATHER_THAN = 10; // >
    static final int IDENTIFIER = 12; // color-background
    static final int PAREN_LEFT = 13; // (
    static final int PAREN_RIGHT = 14; // }
    static final int PLUS = 15; // +
    static final int SEMICOLON = 16; // ;
    static final int STAR = 17; // *
    static final int STRING = 18; // "something"
    static final int TILDE = 19;
    static final int ERROR = 20; // Syntax error found in the tokenizer

    public int line = 1;
    private int offset;

    int getNext(String reader, StringBuffer data, char[] back) throws IOException {
        char character = 0;
        boolean found = false, inComment = false; // , inCommentLine = false;
        int token = -1;
        boolean lookBack = (back[0] != 0);

        if (lookBack)
            character = back[0];

        while (!found && (lookBack || offset < reader.length())) {
            if (!lookBack)
                character = reader.charAt(offset++);
            else
                lookBack = false;

            if (character != '\n' && character != '\r') {
                //System.out.println(character);

                if (!inComment) {
                    switch (character) {
                        case ' ':
                        case '\t':
                            if (token == -1)
                                continue;
                            else if (token == IDENTIFIER)
                                found = true;
                            break;

                        case ';':
                            if (token == -1)
                                token = SEMICOLON;

                            found = (token != STRING);
                            break;

                        case ':':
                            if (token == -1)
                                token = COLON;

                            found = (token != STRING);
                            break;

                        case '{':
                            if (token == -1)
                                token = BRACE_LEFT;

                            found = (token != STRING);
                            break;

                        case '}':
                            if (token == -1)
                                token = BRACE_RIGHT;

                            found = (token != STRING);
                            break;

                        case '>':
                            if (token == -1)
                                token = GREATHER_THAN;

                            found = (token != STRING);
                            break;

                        case '[':
                            if (token == -1)
                                token = BRACKET_LEFT;

                            found = (token != STRING);
                            break;

                        case ']':
                            if (token == -1)
                                token = BRACKET_RIGHT;

                            found = (token != STRING);
                            break;

                        case '(':
                            if (token == -1)
                                token = PAREN_LEFT;

                            found = (token != STRING);
                            break;

                        case ')':
                            if (token == -1)
                                token = PAREN_RIGHT;

                            found = (token != STRING);
                            break;

                        case ',':
                            if (token == -1)
                                token = COMMA;

                            found = (token != STRING);
                            break;

                        case '=':
                            if (token == -1)
                                token = EQUALS;

                            found = (token != STRING);
                            break;

                        case '|':
                            if (token == -1)
                                token = BAR;

                            found = (token != STRING);
                            break;

                        case '^':
                            if (token == -1)
                                token = CARET;

                            found = (token != STRING);
                            break;

                        case '$':
                            if (token == -1)
                                token = DOLLAR;

                            found = (token != STRING);
                            break;

                        case '~':
                            if (token == -1)
                                token = TILDE;

                            found = (token != STRING);
                            break;

                        case '+':
                            if (token == -1)
                                token = PLUS;

                            found = (token != STRING);
                            break;

                            // Start comment block /* ... */
                        case '*':
                            if (token != STRING) {
                                if (back[0] == '/') {
                                    token = -1;
                                    inComment = true;
                                } else {
                                    if (token == -1)
                                        token = STAR;

                                    found = true;
                                }
                            }
                            break;

                        case '"':
                        case '\'':
                            if (token == -1 || token == STRING) {
                                if (token == -1) {
                                    token = STRING;
                                    continue;
                                } else
                                    found = true;
                            } else {
                                token = ERROR;
                                found = true;

                                data.setLength(0);
                                data.append("Unexpected string character");
                            }
                            break;

                        default:
                            if (token == -1)
                                token = IDENTIFIER;
                            break;
                    }

                    if (!found)
                        data.append(character);
                }
                // End comment block /* ... */
                else if (character == '/' && back[0] == '*') {
                    inComment = false;
                    data.setLength(0);
                }
            } else {
                if (character == '\r') {
                    line++;
                    if (token == STRING) {
                        token = ERROR;
                        found = true;

                        data.setLength(0);
                        data.append("String cannot extend to new line");
                    }
                }

                if (token != -1)
                    found = true;
            }

            back[0] = character;
        }

        // No look back needed for non-identifiers
        if (token != IDENTIFIER)
            back[0] = 0;

        return token;
    }

    static String getToken(int token) {
        switch (token) {
            case BAR:
                return "|";

            case BRACE_LEFT:
                return "{";

            case BRACE_RIGHT:
                return "}";

            case BRACKET_LEFT:
                return "[";

            case BRACKET_RIGHT:
                return "]";

            case CARET:
                return "^";

            case COLON:
                return ":";

            case COMMA:
                return ",";

            case DOLLAR:
                return "$";

            case EQUALS:
                return "=";

            case GREATHER_THAN:
                return ">";

            case IDENTIFIER:
                return "IDENTIFIER";

            case PAREN_LEFT:
                return "(";

            case PAREN_RIGHT:
                return ")";

            case PLUS:
                return "+";

            case SEMICOLON:
                return ";";

            case STAR:
                return "*";

            case STRING:
                return "STRING";

            case TILDE:
                return "~";
                
            case ERROR:
                return "ERROR";
        }

        return "NOT DEFINED";
    }
}