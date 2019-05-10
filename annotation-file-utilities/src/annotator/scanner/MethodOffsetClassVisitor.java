package annotator.scanner;

import org.objectweb.asm.*;

import scenelib.annotations.io.classfile.CodeOffsetAdapter;

import com.sun.tools.javac.util.Pair;


/**
 * MethodOffsetClassVisitor is a class visitor that should be passed to
 * ASM's ClassReader in order to retrieve extra information about method
 * offsets needed by all of the annotator.scanner classes.  This visitor
 * should visit every class that is to be annotated, and should be done
 * before trying to match elements in the tree to the various criterion.
 */
// Note: in order to ensure all labels are visited, this class
// needs to extend ClassWriter and not other class visitor classes.
// There is no good reason why this is the case with ASM.
public class MethodOffsetClassVisitor extends ClassVisitor {
  CodeOffsetAdapter codeOffsetAdapter;
  MethodVisitor methodCodeOffsetAdapter;

  // This field should be set by entry on a method through visitMethod,
  // and so all the visit* methods in LocalVariableMethodVisitor
  private String methodName;

  public MethodOffsetClassVisitor(int api, ClassReader classReader, ClassWriter classWriter) {
    super(api, classWriter);
    this.methodName = "LocalVariableVisitor: DEFAULT_METHOD";
    codeOffsetAdapter = new CodeOffsetAdapter(api, classReader);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    methodName = name + desc.substring(0, desc.indexOf(")") + 1);
    methodCodeOffsetAdapter = codeOffsetAdapter.visitMethod(access, name, desc, signature, exceptions);
    return new MethodOffsetMethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions));
  }

  /**
   * MethodOffsetMethodVisitor is the method visitor that
   * MethodOffsetClassVisitor uses to visit particular methods and gather
   * all the offset information by calling the appropriate static
   * methods in annotator.scanner classes.
   */
  private class MethodOffsetMethodVisitor extends MethodVisitor {
    private Label lastLabel;

    public MethodOffsetMethodVisitor(int api, MethodVisitor mv) {
      super(api, mv);
      lastLabel = null;
    }

    private int labelOffset() {
      try {
        return lastLabel.getOffset();
      } catch (Exception ex) {
        return 0;  // TODO: find a better default?
      }
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
      super.visitLocalVariable(name, desc, signature, start, end, index);
      LocalVariableScanner.addToMethodNameIndexMap(Pair.of(methodName, Pair.of(index, start.getOffset())), name);
      LocalVariableScanner.addToMethodNameCounter(methodName, name, start.getOffset());
      methodCodeOffsetAdapter.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitLabel(Label label) {
      super.visitLabel(label);
      lastLabel = label;
      methodCodeOffsetAdapter.visitLabel(label);
    }

    @Override
    public void visitTypeInsn(int opcode,  String desc)   {
      super.visitTypeInsn(opcode, desc);
      switch (opcode) {
      case Opcodes.CHECKCAST:
        // CastScanner.addCastToMethod(methodName, labelOffset() + 1);
        CastScanner.addCastToMethod(methodName, codeOffsetAdapter.getMethodCodeOffset());
        break;
      case Opcodes.NEW:
      case Opcodes.ANEWARRAY:
        NewScanner.addNewToMethod(methodName, labelOffset());
        break;
      case Opcodes.INSTANCEOF:
        InstanceOfScanner.addInstanceOfToMethod(methodName, labelOffset() + 1);
        break;
      }
      methodCodeOffsetAdapter.visitTypeInsn(opcode, desc);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims)  {
      super.visitMultiANewArrayInsn(desc, dims);
      NewScanner.addNewToMethod(methodName, labelOffset());
      methodCodeOffsetAdapter.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitIntInsn(int opcode, int operand)  {
      super.visitIntInsn(opcode, operand);
      if (opcode == Opcodes.NEWARRAY) {
        NewScanner.addNewToMethod(methodName, labelOffset());
      }
      methodCodeOffsetAdapter.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
      super.visitMethodInsn(opcode, owner, name, desc);
      switch (opcode) {
        case Opcodes.INVOKEINTERFACE:
        case Opcodes.INVOKESTATIC:
        case Opcodes.INVOKEVIRTUAL:
          MethodCallScanner.addMethodCallToMethod(methodName, labelOffset());
          break;
        default:
          break;
      }
      methodCodeOffsetAdapter.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
      super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
      LambdaScanner.addLambdaExpressionToMethod(methodName, labelOffset());
      methodCodeOffsetAdapter.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitCode() {
      super.visitCode();
      methodCodeOffsetAdapter.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
      super.visitInsn(opcode);
      methodCodeOffsetAdapter.visitInsn(opcode);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
      super.visitVarInsn(opcode, var);
      methodCodeOffsetAdapter.visitVarInsn(opcode, var);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
      super.visitFieldInsn(opcode, owner, name, desc);
      methodCodeOffsetAdapter.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
      super.visitJumpInsn(opcode, label);
      methodCodeOffsetAdapter.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object cst) {
      super.visitLdcInsn(cst);
      methodCodeOffsetAdapter.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
      super.visitIincInsn(var, increment);
      methodCodeOffsetAdapter.visitIincInsn(var, increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
      super.visitTableSwitchInsn(min, max, dflt, labels);
      methodCodeOffsetAdapter.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
      super.visitLookupSwitchInsn(dflt, keys, labels);
      methodCodeOffsetAdapter.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitEnd() {
      super.visitEnd();
      methodCodeOffsetAdapter.visitEnd();
    }
  }
}
