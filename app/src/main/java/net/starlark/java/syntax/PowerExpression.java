package net.starlark.java.syntax;

/* Power expression. 'x ** y' */
public final class PowerExpression extends Expression {
    private final Expression x;
    private final Expression y;
    private final int opOffset;

    PowerExpression(FileLocations locs, Expression x, int opOffset, Expression y) {
        super(locs);
        this.x = x;
        this.opOffset = opOffset;
        this.y = y;
    }

    public Expression getX() {
        return x;
    }
    public Expression getY() {
        return y;
    }
    public Location getOperatorLocation() {
        return locs.getLocation(opOffset);
    }


    @Override
    public Kind kind() { return Kind.POWER; }

    @Override
    public int getStartOffset() { return x.getStartOffset(); }

    @Override
    public int getEndOffset() {
        return y.getEndOffset();
    }

    @Override
    public void accept(NodeVisitor visitor) { visitor.visit(this); }

    @Override
    public String toString() {
        // may be not correct for omitting parentheses in some case.
        return x + " ** " + y;
    }

}
