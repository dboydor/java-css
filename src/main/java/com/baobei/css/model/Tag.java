/*
 * This models a single CSS tag
 * 
 * ie. 
 *   h2 > a:active
 * 
 * David Boyd
 */
package com.baobei.css.model;

import java.util.Vector;

public class Tag {
    public static final int RELATION_DESCENDANT = 0; // A B
    public static final int RELATION_CLASS = 1; // .
    public static final int RELATION_ID = 2; // #
    public static final int RELATION_CHILD = 3; // A > B
    public static final int RELATION_SIBLING = 4; // A ~ B
    public static final int RELATION_SIBLING_ADJACENT = 5; // A + B

    public static final int ATTRIB_NONE = 0; // no match
    public static final int ATTRIB_ANY = 1; // any match
    public static final int ATTRIB_EQUALS = 2; // =
    public static final int ATTRIB_EQUALS_DASH = 3; // |=
    public static final int ATTRIB_LIST_WORD = 4; // ~=
    public static final int ATTRIB_BEGINS = 5; // ^=
    public static final int ATTRIB_ENDS = 6; // $=
    public static final int ATTRIB_CONTAINS = 7; // *=

    public String name; // Name can be null, ie. #id_something
    public int relation;
    public String attribKey;
    public String attribValue;
    public int attribMatch;
    public Vector<String> pseudos; // Array of strings, or null if no pseudos defined for this tag
    public Vector<String> pseudoArgs; // Array of strings, or null if no pseudos defined for this tag

    public void addPseudo(String pseudo) {
        if (pseudos == null) {
            pseudos = new Vector<String>(1);
            pseudoArgs = new Vector<String>(1);
        }

        pseudos.addElement(pseudo);
        pseudoArgs.addElement(null);
    }

    public void addPseudoArg(String arg) {
        int last = pseudoArgs.size() - 1;

        String value = pseudoArgs.elementAt(last);

        if (value == null)
            value = arg;
        else
            value += arg;

        pseudoArgs.setElementAt(value, last);
    }

    // 
    // a = count the number of ID selectors in the selector
    // b = count the number of class selectors, attributes selectors, and pseudo-classes in the selector
    // c = count the number of type selectors and pseudo-elements in the selector
    // 
    // Result is (a * 10000) + (b * 100) + c
    int getWeight() {
        int a = 0, b = 0, c = 0;

        if (attribKey != null)
            b++;

        if (pseudos != null)
            b += pseudos.size();

        return (b * 100) + 1;
    }

    void print(boolean lastTag) {
        if (name != null)
            System.out.print(name);

        if (attribKey != null) {
            System.out.print('[');
            System.out.print(attribKey);

            if (attribMatch > Tag.ATTRIB_ANY) {
                switch (attribMatch) {
                    case Tag.ATTRIB_EQUALS:
                        System.out.print("=\"");
                        break;

                    case Tag.ATTRIB_EQUALS_DASH:
                        System.out.print("|=\"");
                        break;

                    case Tag.ATTRIB_LIST_WORD:
                        System.out.print("~=\"");
                        break;

                    case Tag.ATTRIB_BEGINS:
                        System.out.print("^=\"");
                        break;

                    case Tag.ATTRIB_ENDS:
                        System.out.print("$=\"");
                        break;

                    case Tag.ATTRIB_CONTAINS:
                        System.out.print("*=\"");
                        break;
                }

                System.out.print(attribValue + "\"");
            }

            System.out.print(']');
        }

        if (pseudos != null) {
            int count = pseudos.size();
            for (int x = 0; x < count; x++) {
                System.out.print(":" + pseudos.elementAt(x));

                String arg = pseudoArgs.elementAt(x);
                if (arg != null)
                    System.out.print("(" + arg + ")");
            }
        }

        switch (relation) {
            case Tag.RELATION_DESCENDANT:
                if (!lastTag)
                    System.out.print(" ");
                break;

            case Tag.RELATION_CLASS:
                System.out.print(".");
                break;

            case Tag.RELATION_ID:
                System.out.print("#");
                break;

            case Tag.RELATION_CHILD:
                System.out.print(" > ");
                break;

            case Tag.RELATION_SIBLING:
                System.out.print(" ~ ");
                break;

            case Tag.RELATION_SIBLING_ADJACENT:
                System.out.print(" + ");
                break;
        }
    }
}