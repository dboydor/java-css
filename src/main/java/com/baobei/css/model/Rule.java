/*
 * This models a single CSS rule
 * 
 * David Boyd
 */
package com.baobei.css.model;

import java.util.Vector;
import com.baobei.css.model.RuleValue;

public class Rule {
    public String name;
    public Vector<String> pseudos; // Used when attached to a XML Node to designate this rule is only applied for the pseudo state
    public Vector<String> pseudoArgs; // Used when attached to a XML Node to designate this rule is only applied for the pseudo state
    public int weight; // Used when attached to a XML Node.  See "specificity" in CSS3 selectors spec.  
    public Vector<RuleValue> values; // Array of RuleValues
    public RuleValue lastValue;

    // Used by equals() to return the Rule match result
    public static final int MATCH_NONE = 0;
    public static final int MATCH_NAME = 1; //  Anything greater than this means that weighted pseudos matched

    // Used by equals() to return the Rule match result
    public static final int EXPECTED = 0;
    public static final int ACTUAL = 1; //  Anything greater than this means that weighted pseudos matched
    public static final int WEIGHTED = 2; //  Anything greater than this means that weighted pseudos matched

    public RuleValue nextValue() {
        if (values == null)
            values = new Vector<RuleValue>(4);

        // Add the first tag to the first path
        lastValue = new RuleValue();
        values.addElement(lastValue);

        return lastValue;
    }

    // Shallow copy
    public Rule clone() {
        Rule result = new Rule();

        result.name = this.name;
        result.values = this.values;

        return result;
    }

    // Compare to see if two rules are equal
    // 
    //  psueodsWeight  - contains the relative weight of each pseudo match.  Lower weights are given to "platform" and "lang" pseudos
    //  match[EXPECTED] - Contains the expected match level for this rule.  This is needed if exact matching is neeeded by the caller.
    //  match[ACTUAL] - Contains the actual matching level.  This is needed if exact matching is neeeded by the caller.
    //  match[WEIGHTED] - Contains the actual weighted matching level
    public boolean equals(String rule, Vector<String> pseudos, Vector<String> pseudoArgs, Vector<Integer> pseudosWeight, int [] match) {
        int result = (name.equals(rule) ? MATCH_NAME : MATCH_NONE);

        if (match != null)
            match[EXPECTED] = match[ACTUAL] = match[WEIGHTED] = result;

        if (result == MATCH_NAME) {
            if (this.pseudos == null && (pseudos == null || pseudos.size() == 0)) {
                return true;

            } else if (this.pseudos != null && pseudos != null && pseudos.size() != 0) {
                int count = this.pseudos.size(), matching = 0, matchingWeight = 0;
                String pseudo, pseudoMatch, arg, argMatch;

                for (int x = 0; x < count; x++) {
                    pseudo = this.pseudos.elementAt(x);
                    arg = this.pseudoArgs.elementAt(x);

                    for (int y = 0; y < pseudos.size(); y++) {
                        pseudoMatch = pseudos.elementAt(y);

                        if (pseudoArgs != null && pseudo.equals(pseudoMatch)) {
                            argMatch = pseudoArgs.elementAt(y);

                            if (arg == null && argMatch == null) {
                                matching++;
                                if (pseudosWeight != null)
                                    matchingWeight += pseudosWeight.elementAt(y);

                            } else if (arg != null && argMatch != null) {
                                // For :lang and :platform pseudo, match with a case-insensitive starting-with
                                if ((pseudo.charAt(0) == 'l' && pseudo.charAt(1) == 'a') ||
                                    (pseudo.charAt(0) == 'p' && pseudo.charAt(1) == 'l')) {
                                    if (arg.equalsIgnoreCase(argMatch.substring(0, arg.length()))) {
                                        matching++;
                                        if (pseudosWeight != null)
                                            matchingWeight += pseudosWeight.elementAt(y);
                                    }
                                } else {
                                    if (arg.equals(argMatch)) {
                                        matching++;
                                        if (pseudosWeight != null)
                                            matchingWeight += pseudosWeight.elementAt(y);
                                    }
                                }
                            }
                        }
                    }
                }

                if (match != null) {
                    match[EXPECTED] = result + count;
                    match[ACTUAL] = result + matching;
                    match[WEIGHTED] = result + matchingWeight;
                }

                if (matching == count && count == pseudos.size())
                    return true;
            }
        }

        return false;
    }

    public String get() throws Exception {
        return this.values.elementAt(0).get();
    }

    public int getInt() throws Exception {
        return this.values.elementAt(0).getInt();
    }

    public int getColor() throws Exception {
        return this.values.elementAt(0).getColor();
    }

    public void print() {
        System.out.print("    " + name + ": ");

        int count = values.size();
        for (int x = 0; x < count; x++) {
            RuleValue value = this.values.elementAt(x);

            value.print();

            if (x != count - 1)
                System.out.print(" ");
        }

        System.out.println(";");
    }
}