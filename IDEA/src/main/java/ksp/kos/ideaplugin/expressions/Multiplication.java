package ksp.kos.ideaplugin.expressions;

import ksp.kos.ideaplugin.psi.KerboScriptMultdivExpr;
import ksp.kos.ideaplugin.reference.context.LocalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created on 30/01/16.
 *
 * @author ptasha
 */
public class Multiplication extends MultiExpression<Multiplication.Op, Element> {
    @SuppressWarnings("unused")
    public Multiplication() {
        super();
    }

    protected Multiplication(List<Item<Op, Element>> items) {
        super(items);
    }

    public Multiplication(Expression... expressions) {
        super(expressions);
    }

    public Multiplication(KerboScriptMultdivExpr muldiv) throws SyntaxException {
        super(muldiv);
    }

    @Override
    public MultiExpressionBuilder<Op, Element> createBuilder() {
        return new MultiplicationBuilder();
    }

    @Override
    public Expression differentiate(LocalContext context) {
        Expression diff = Number.ZERO;
        for (Item<Op, Element> item : items) {
            Expression id = Number.ONE;
            for (Item<Op, Element> ditem : items) {
                if (item!=ditem) {
                    if (ditem.getOperation()==Op.MUL) {
                        id = id.multiply(ditem.getExpression());
                    } else {
                        id = id.divide(ditem.getExpression());
                    }
                } else {
                    if (ditem.getOperation()==Op.MUL) {
                        id = id.multiply(ditem.getExpression().differentiate(context));
                    } else {
                        id = id.multiply(ditem.getExpression().power(Number.create(-1)).differentiate(context));
                    }
                }
            }
            diff = diff.plus(id);
        }
        return diff;
    }

    @Override
    public Expression multiply(Expression expression) {
        return addExpression(expression);
    }

    @Override
    public Expression multiplyLeft(Expression expression) {
        return addExpressionLeft(expression);
    }

    @Override
    public Expression divide(Expression expression) {
        return addExpression(Op.DIV, expression);
    }

    @Override
    public Expression minus() {
        boolean first = true;
        MultiExpressionBuilder<Op, Element> builder = createBuilder();
        for (Item<Op, Element> item : items) {
            if (first) {
                item = builder.createItem(item.getOperation(), item.getExpression().minus());
                first = false;
            }
            builder.addItem(item);
        }
        return builder.createExpression();
    }

    private static class NormalForm {
        private final Expression mulNumber;
        private final Expression divNumber;
        private final Set<Item<Op, Element>> items;

        public NormalForm(Expression mulNumber, Expression divNumber, Set<Item<Op, Element>> items) {
            this.mulNumber = mulNumber;
            this.divNumber = divNumber;
            this.items = items;
        }

        public static NormalForm toNormal(Multiplication multiplication) {
            Expression mulNumber = Number.ONE;
            Expression divNumber = Number.ONE;
            HashSet<Item<Op, Element>> items = new HashSet<>();
            for (Item<Op, Element> item : multiplication.getItems()) {
                if (item.getExpression().isNumber()) {
                    if (item.getOperation()==Op.MUL) {
                        mulNumber = mulNumber.multiply(item.getExpression());
                    } else {
                        divNumber = divNumber.multiply(item.getExpression());
                    }
                } else if (item.getExpression().isNegative()) {
                    mulNumber = mulNumber.minus();
                    items.add(new Item<>(item.getOperation(), Element.toElement(item.getExpression().minus())));
                } else {
                    items.add(item);
                }
            }
            return new NormalForm(mulNumber, divNumber, items);
        }

        public Expression plus(NormalForm normalForm) {
            if (!this.items.equals(normalForm.items)) {
                return null;
            }
            MultiplicationBuilder builder = new MultiplicationBuilder();
            builder.addExpression(
                    this.mulNumber.multiply(normalForm.divNumber)
                            .plus(normalForm.mulNumber.multiply(this.divNumber))
                            .divide(this.divNumber).divide(normalForm.divNumber));
            builder.addItems(items);
            return builder.createExpression();
        }
    }

    @Override
    @Nullable
    public Expression simplePlus(Expression expression) {
        if (expression instanceof Multiplication) {
            return NormalForm.toNormal(this).plus(NormalForm.toNormal((Multiplication) expression));
        } else {
            ArrayList<Item<Op, Element>> items = new ArrayList<>();
            items.add(new Item<>(Op.MUL, Element.toElement(expression)));
            return simplePlus(new Multiplication(items));
        }
    }

    @Override
    public Expression normalize() {
        MultiplicationBuilder builder = new MultiplicationBuilder();
        for (Item<Op, Element> item : items) {
            builder.addExpression(item.getOperation(), item.getExpression().normalize().simplify());
        }
        return builder.createExpression();
    }

    @Override
    public Expression divisor() {
        MultiplicationBuilder builder = new MultiplicationBuilder();
        for (Item<Op, Element> item : items) {
            if (item.getOperation()==Op.DIV) {
                builder.addExpression(Op.MUL, item.getExpression());
            }
        }
        return builder.createExpression();
    }

    @Override
    public Expression distribute() {
        Expression addition = Number.ONE.distribute();
        MultiplicationBuilder divisor = new MultiplicationBuilder();
        for (Item<Op, Element> item : items) {
            if (item.getOperation()==Op.MUL) {
                addition = addition.distribute(item.getExpression().distribute());
            } else {
                divisor.addExpression(Op.MUL, item.getExpression());
            }
        }
        return addition.divide(divisor.createExpression());
    }

    @Override
    public boolean isNegative() {
        return items.get(0).getExpression().isNegative();
    }

    public enum Op implements MultiExpression.Op {
        MUL("*", Expression::multiply),
        DIV("/", Expression::divide);

        private final String text;
        private final MultiExpression.Operation operation;

        Op(String text, Operation operation) {
            this.text = text;
            this.operation = operation;
        }

        @Override
        public String getText() {
            return text;
        }

        public String toString() {
            return text;
        }

        @Override
        public Expression apply(Expression exp1, Expression exp2) {
            return operation.apply(exp1, exp2);
        }
    }

    public static class MultiplicationBuilder extends MultiExpressionBuilder<Op, Element> implements ConsumeSupported<Op, Element> {
        public MultiplicationBuilder() {
            super(Multiplication.class);
        }

        @Override
        protected Expression zero() {
            return Number.ZERO;
        }

        @Override
        protected Expression one() {
            return Number.ONE;
        }

        @Override
        protected Expression singleItemExpression(Item<Op, Element> item) {
            if (item.getOperation()== Op.MUL) {
                Element expression = item.getExpression();
                if (expression.isSimple()) {
                    return expression.getAtom().simplify();
                }
                return expression;
            } else {
                items.add(0, createItem(Op.MUL, toElement(Number.ONE)));
                return createExpression();
            }
        }

        @NotNull
        @Override
        protected Op[] operators() {
            return Op.values();
        }

        @Override
        protected Element toElement(Expression expression) {
            return Element.toElement(expression);
        }

        @Override
        protected void normalize() {
            int sign = 1;
            LinkedList<Item<Op, Element>> mult = new LinkedList<>();
            LinkedList<Item<Op, Element>> div = new LinkedList<>();
            for (Item<Op, Element> item : this.items) {
                if (item.getExpression().isNegative()) {
                    item = createItem(item.getOperation(), item.getExpression().minus());
                    sign*=-1;
                }
                if (item.getOperation() == Op.MUL) {
                    mult.add(item);
                } else {
                    div.add(item);
                }
            }
            Expression muln = normalize(mult);
            Expression divn = normalize(div);
            Number root = Number.root(muln, divn);
            if (!root.equals(Number.ONE)) {
                muln= muln.divide(root);
                divn= divn.divide(root);
            }
            if (!muln.equals(Number.ONE)) {
                mult.addFirst(createItem(Op.MUL, toElement(muln)));
            } else if (mult.isEmpty()) {
                mult.add(createItem(Op.MUL, toElement(Number.ONE)));
            }
            if (!divn.equals(Number.ONE)) {
                div.addFirst(createItem(Op.DIV, toElement(divn)));
            }
            if (sign<0) {
                mult.set(0, createItem(Op.MUL, mult.get(0).getExpression().minus()));
            }
            this.items.clear();
            this.items.addAll(mult);
            this.items.addAll(div);
        }

        private Expression normalize(LinkedList<Item<Op, Element>> items) {
            Expression number = Number.ONE;
            int index = -1;
            float score = 0;
            int i = 0;
            for (Iterator<Item<Op, Element>> iterator = items.iterator(); iterator.hasNext(); i++) {
                Item<Op, Element> item = iterator.next();
                Element expression = item.getExpression();
                if (expression.isNumber()) {
                    number = number.multiply(expression).simplify();
                    iterator.remove();
                    i--;
                } else if (expression.isAddition()) {
                    float newScore = getNumbersScore(getAddition(expression));
                    if (newScore>score) {
                        index = i;
                        score = newScore;
                    }
                }
            }
            if (!number.equals(Number.ONE)) {
                if (score>0.99) {
                    Item<Op, Element> addition = items.get(index);
                    Expression expression = getAddition(addition.getExpression()).multiplyItems(number);
                    items.set(index, createItem(addition.getOperation(), toElement(expression)));
                    number = Number.ONE;
                }
            }
            return number;
        }

        private float getNumbersScore(Addition addition) {
            float numbers = 0;
            for (Item<Addition.Op, Expression> item : addition.items) {
                Expression expression = item.getExpression();
                if (expression instanceof Number) {
                    numbers++;
                } else if (expression instanceof Multiplication) {
                    for (Item<Op, Element> mulItem : ((Multiplication) expression).items) {
                        if (mulItem.getExpression().isNumber()) {
                            numbers++;
                            break;
                        }
                    }
                }
            }
            return numbers/addition.items.size();
        }

        private Addition getAddition(Element expression) {
            return (Addition)((Escaped)expression.getAtom()).getExpression();
        }

        @Override
        protected MultiExpression<Op, Element> createExpression(List<Item<Op, Element>> items) {
            return new Multiplication(items);
        }

        @Override
        public Item<Op, Element> consume(Item<Op, Element> item, Item<Op, Element> newItem) {
            if (item.getExpression().canMultiply(newItem.getExpression())) {
                Expression merged = merge(item.getOperation(), newItem.getOperation()).apply(item.getExpression(), newItem.getExpression());
                return createItem(item.getOperation(), toElement(merged));
            }
            return null;
        }
    }
}
