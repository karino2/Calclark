// Copyright 2021 karino2. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
