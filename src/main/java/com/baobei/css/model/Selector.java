/*
 * Data structure that models a single CSS selector
 * 
 * David Boyd
 
 Selector
    paths                               -> Vector (1 to many paths)
        path                            -> Vector (1 to many tags)
            tag                         -> Tag
                attribute               
                    key
                    value
                    match-type
                pseudo
                    arg

    rules                               -> Vector (1 to many Rules)
        rule                            -> Rule
            key
            value                       -> Vector (1 to many RuleValues)
                arg
                    function            -> Vector (1 to many arguments)
                        arg
                            function    -> Vector (1 to many arguments) 
                                arg
                                arg

 */
package com.baobei.css.model;

import java.util.Vector;

public class Selector {
    Vector<Vector<Tag>> paths = new Vector<Vector<Tag>>(8);
    Vector<Rule> rules = new Vector<Rule>(16);
    Vector lastPath;
    public Tag lastTag;
    public Rule lastRule;
    private Vector<Integer> weights = new Vector<Integer>(1); // Contains weight (specificity) for each selector path  

    public boolean isEmpty() {
        return (this.paths.size() == 0);
    }

    // Number of paths for selector
    public int getPathCount() {
        return this.paths.size();
    }

    // Number of Tags for path
    public int getTagCount(int path) {
        return this.paths.elementAt(path).size();
    }

    // Get specific Tag for the path
    public Tag getTag(int path, int tag) {
        Vector<Tag> data = this.paths.elementAt(path);

        if (tag >= 0 && tag < data.size())
            return data.elementAt(tag);
        else
            return null;
    }

    // Get weight for the path 
    public int getWeight(int path) {
        return this.weights.elementAt(path);
    }

    // Get the vector of rules  
    public Vector getRules() {
        return this.rules;
    }

    // Calculate the specificity for this selector and set the weight values for all the rules
    public void calcWeight() {
        int count = this.paths.size();
        if (count != 0) {
            int weight;

            for (int x = 0; x < count; x++) {
                weight = 0;

                Vector<Tag> path = this.paths.elementAt(x);
                for (int y = 0; y < path.size(); y++) {
                    Tag tag = path.elementAt(y);
                    weight += tag.getWeight();
                }

                this.weights.addElement(weight);
            }
        }
    }

    // Reuse this object to represent another parsed CSS selector rule
    public Tag nextSelector() {
        this.paths.removeAllElements();
        rules.removeAllElements();
        this.weights.setSize(0);

        lastPath = new Vector(4);
        this.paths.addElement(lastPath);

        lastTag = new Tag();
        lastPath.addElement(lastTag);

        return lastTag;
    }

    // Add another path to the selector.  Paths are separated by commas.
    public Tag nextPath() {
        // Add the first tag to the first path
        lastPath = new Vector(4);
        this.paths.addElement(lastPath);

        lastTag = null;

        return lastTag;
    }

    public Tag nextTag() {
        // Add the first tag to the first path
        lastTag = new Tag();
        lastPath.addElement(lastTag);

        return lastTag;
    }

    public void setTagName(String name) {
        // Periods and hashes are included within IDENTIFIER by the tokenizer, so for tag names, I need to check if 
        // they're present, and, if so, create separate tags with a class relationship for them.
        int found = name.indexOf('.');

        boolean foundClass = (found != -1);
        if (!foundClass)
            found = name.indexOf('#');

        if (found != -1) {
            if (found > 0)
                lastTag.name = name.substring(0, found);
            lastTag.relation = (foundClass ? Tag.RELATION_CLASS : Tag.RELATION_ID);
            nextTag();
            lastTag.name = name.substring(found + 1);
        } else {
            lastTag.name = name;
        }
    }

    public Rule nextRule() {
        // Add the first tag to the first path
        lastRule = new Rule();
        rules.addElement(lastRule);

        return lastRule;
    }

    public void print() {
        int count = this.paths.size();

        if (count != 0) {
            for (int x = 0; x < count; x++) {
                Vector path = this.paths.elementAt(x);

                for (int y = 0; y < path.size(); y++) {
                    Tag tag = (Tag) path.elementAt(y);
                    tag.print((y == path.size() - 1));
                }

                System.out.print("(" + this.weights.elementAt(x) + ")");

                if (x != count - 1)
                    System.out.print(", ");
            }

            System.out.println("");
            System.out.println("{");

            count = rules.size();
            for (int x = 0; x < count; x++) {
                Rule rule = (Rule) rules.elementAt(x);
                rule.print();
            }

            System.out.println("}");
            System.out.println("");
        }
    }
}