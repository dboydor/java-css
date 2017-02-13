/*
 * This models a single CSS rule value
 * 
 * David Boyd
 */
package com.baobei.css.model;

import java.util.Vector;

public class RuleValue {
    public static final int TYPE_IDENTIFIER = 0;
    public static final int TYPE_STRING = 1;
    public static final int TYPE_FUNCTION = 2;

    Vector<String> names = new Vector<String>(1); // Array of RuleValues.  Used for TYPE_FUNCTION.
    public int type;
    Vector<RuleValue> args; // Array of RuleValues.  Used for TYPE_FUNCTION.
    public RuleValue lastArg;

    public String getName() {
        String result = "";

        int count = this.names.size();
        for (int x = 0; x < count; x++) {
            result += this.names.elementAt(x);
            if (x != count - 1)
                result += " ";
        }

        return result;
    }

    public void setName(String name) {
        // Strip off the start of color values
        if (name.charAt(0) == '#')
            name = name.substring(1);

        this.names.addElement(name);
    }

    public String get() throws Exception {
        String name = getName();

        if (type == RuleValue.TYPE_FUNCTION) {
            switch (name.charAt(0)) {
                // url() - Ignore and pass first argument
                case 'u':
                    return this.args.elementAt(0).get();

                default:
                    throw new Exception("Undefined function: " + name);
            }
        } else {
            return name;
        }
    }

    public int getInt() throws Exception {
        String value = getName();

        // Check for "pt" units
        int found = value.indexOf('p');
        if (found != -1)
            value = value.substring(0, found);

        // Check for "%" at end
        found = value.indexOf('%');
        if (found != -1)
            value = value.substring(0, found);

        return Integer.parseInt(value);
    }

    public int getColor() throws Exception {
        String name = getName();

        if (type == RuleValue.TYPE_FUNCTION) {
            switch (name.charAt(0)) {
                // saturation(color, saturation_percent)
                case 's':
                    return Color.saturation(args);

                default:
                    throw new Exception("Undefined function: " + name);
            }
        } else {
            return Integer.parseInt(name, 16);
        }
    }

    public RuleValue nextFunctionArg() {
        if (this.args == null)
            this.args = new Vector<RuleValue>(4);

        // Add the first tag to the first path
        lastArg = new RuleValue();
        this.args.addElement(lastArg);

        return lastArg;
    }

    public void print() {
        if (type == TYPE_STRING)
            System.out.print("\"");

        int count = this.names.size();
        for (int x = 0; x < count; x++) {
            System.out.print(this.names.elementAt(x));
            if (x != count - 1)
                System.out.print(" ");
        }

        if (type == TYPE_STRING) {
            System.out.print("\"");

        } else if (type == TYPE_FUNCTION) {
            System.out.print("(");

            if (args != null) {
                count = args.size();
                for (int x = 0; x < count; x++) {
                    RuleValue value = (RuleValue) this.args.elementAt(x);
                    value.print();

                    if (x != count - 1)
                        System.out.print(", ");
                }
            }

            System.out.print(")");
        }
    }
}