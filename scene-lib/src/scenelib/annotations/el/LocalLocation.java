package scenelib.annotations.el;

import org.objectweb.asm.Label;

import java.lang.reflect.Field;
import java.util.Arrays;


/**
 * A {@link LocalLocation} holds location information for a local
 * variable: slot index, scope start, and scope end.
 */
public final class LocalLocation {

    public final Label[] start;
    public final Label[] end;
    public final int[] index;
    public final int scopeStart;
    public final int scopeLength;

    public LocalLocation(Label[] start, Label[] end, int[] index) {
        this.start = start;
        this.end = end;
        this.index = index;
        this.scopeStart = start[0].getOffset();
        this.scopeLength = end[end.length - 1].getOffset() - start[0].getOffset(); // FIXME
    }

    public LocalLocation(int index, int scopeStart, int scopeLength) {
        // Changes values reflectively.
        this.scopeStart = scopeStart;
        this.scopeLength = scopeLength;
        this.index = new int[] {index};
        this.start = new Label[] {new Label()};
        this.end = new Label[] {new Label()};

        try {
            Field flagsField = Label.class.getDeclaredField("flags");
            Field bytecodeOffsetField = Label.class.getDeclaredField("bytecodeOffset");
            Field FLAG_RESOLVED_FIELD = Label.class.getDeclaredField("FLAG_RESOLVED");

            flagsField.setAccessible(true);
            bytecodeOffsetField.setAccessible(true);
            FLAG_RESOLVED_FIELD.setAccessible(true);

            int FLAG_RESOLVED = (Integer) FLAG_RESOLVED_FIELD.get(null);

            short flagsStart = (Short) flagsField.get(start[0]);
            short flagsEnd = (Short) flagsField.get(end[0]);
            flagsStart |= FLAG_RESOLVED;
            flagsEnd |= FLAG_RESOLVED;

            flagsField.set(start[0], flagsStart);
            bytecodeOffsetField.set(start[0], scopeStart);

            flagsField.set(end[0], flagsEnd);
            bytecodeOffsetField.set(end[0], scopeStart + scopeLength);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalLocation that = (LocalLocation) o;
        return Arrays.equals(start, that.start) &&
            Arrays.equals(end, that.end) &&
            Arrays.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(start);
        result = 31 * result + Arrays.hashCode(end);
        result = 31 * result + Arrays.hashCode(index);
        return result;
    }

    @Override
    public String toString() {
        return "LocalLocation{" +
            "start=" + Arrays.toString(start) +
            ", end=" + Arrays.toString(end) +
            ", index=" + Arrays.toString(index) +
            '}';
    }
}


//    /**
//     * The slot index of the local variable.
//     */
//    public final int index;
//
//    /**
//     * The start of the local variable's scope (or live range), as an offset
//     * from the beginning of the method code in bytes.
//     */
//    public final int scopeStart;
//
//    /**
//     * The length of the local variable's scope (or live range), in bytes.
//     */
//    public final int scopeLength;
//
//    public final String varName;
//    public final int varIndex;
//
//    /**
//     * Constructs a new {@link LocalLocation}; the arguments are assigned to
//     * the fields of the same names.
//     */
//    public LocalLocation(int index, int scopeStart, int scopeLength) {
//        this.index = index;
//        this.scopeStart = scopeStart;
//        this.scopeLength = scopeLength;
//        this.varName = null;
//        this.varIndex = -1;
//    }
//
//    public LocalLocation(String varName, int varIndex) {
//        this.index = -1;
//        this.scopeStart = -1;
//        this.scopeLength = -1;
//        this.varName = varName;
//        this.varIndex = varIndex;
//    }


//    /**
//     * Returns whether this {@link LocalLocation} equals <code>o</code>; a
//     * slightly faster variant of {@link #equals(Object)} for when the argument
//     * is statically known to be another nonnull {@link LocalLocation}.
//     */
//    public boolean equals(LocalLocation l) {
//        return index == l.index && scopeStart == l.scopeStart
//                && scopeLength == l.scopeLength &&
//                (varName==null || varName.equals(l.varName)) &&
//                varIndex==l.varIndex;
//    }

//    /**
//     * This {@link LocalLocation} equals <code>o</code> if and only if
//     * <code>o</code> is another nonnull {@link LocalLocation} and
//     * <code>this</code> and <code>o</code> have equal {@link #index},
//     * {@link #scopeStart}, and {@link #scopeLength}.
//     */
//    @Override
//    public boolean equals(Object o) {
//        return o instanceof LocalLocation
//                && equals((LocalLocation) o);
//    }

//    @Override
//    public int hashCode() {
//        if (varName==null) {
//            return Objects.hash(index, scopeStart, scopeLength);
//        } else {
//            return Objects.hash(varName, varIndex);
//        }
//    }
//
//    @Override
//    public String toString() {
//        if (varName==null) {
//            return "LocalLocation(" + index + ", " + scopeStart + ", " + scopeLength + ")";
//        } else {
//            return "LocalLocation(\"" + varName + "\" #" + varIndex + ")";
//        }
//    }
