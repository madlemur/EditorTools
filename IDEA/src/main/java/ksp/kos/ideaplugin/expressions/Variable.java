package ksp.kos.ideaplugin.expressions;

import ksp.kos.ideaplugin.reference.context.LocalContext;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created on 29/01/16.
 *
 * @author ptasha
 */
public class Variable extends Atom {
    private final String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getText() {
        return name;
    }

    @Override
    public Expression differentiate(LocalContext context) {
        return new Variable(name+"_");
    }

    @Override
    public Expression inline(HashMap<String, Expression> args) {
        Expression expression = args.get(name);
        if (expression!=null) {
            return expression;
        }
        return this;
    }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visitVariable(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return Objects.equals(name, variable.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
