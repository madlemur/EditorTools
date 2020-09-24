package ksp.kos.ideaplugin.dataflow;

import ksp.kos.ideaplugin.psi.KerboScriptDeclareParameterClause;
import ksp.kos.ideaplugin.reference.context.LocalContext;

import java.util.Objects;

/**
 * Created on 17/03/16.
 *
 * @author ptasha
 */
public class ParameterFlow extends BaseFlow<ParameterFlow> implements NamedFlow<ParameterFlow> {
    private final String name;

    public ParameterFlow(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean addContext(ContextBuilder context) {
        context.getMap().put(getName(), this);
        return true;
    }

    @Override
    public ParameterFlow differentiate(LocalContext context, ContextBuilder contextBuilder) {
        contextBuilder.add(new ParameterFlow(name));
        ParameterFlow diff = differentiate(context);
        contextBuilder.add(diff);
        return diff;
    }

    @Override
    public ParameterFlow differentiate(LocalContext context) {
        return new ParameterFlow(name+"_");
    }

    @Override
    public String getText() {
        return "parameter "+name+".";
    }

    public static ParameterFlow parse(KerboScriptDeclareParameterClause parameterClause) {
        return new ParameterFlow(parameterClause.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterFlow that = (ParameterFlow) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
