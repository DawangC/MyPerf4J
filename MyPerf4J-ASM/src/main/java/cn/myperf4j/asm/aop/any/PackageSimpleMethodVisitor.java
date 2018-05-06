package cn.myperf4j.asm.aop.any;

import cn.myperf4j.asm.ASMRecorderMaintainer;
import cn.myperf4j.asm.aop.ProfilingAspect;
import cn.myperf4j.core.AbstractRecorderMaintainer;
import cn.myperf4j.core.ProfilerParams;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by LinShunkang on 2018/4/15
 */
public class PackageSimpleMethodVisitor extends LocalVariablesSorter {

    private AbstractRecorderMaintainer maintainer = ASMRecorderMaintainer.getInstance();

    private String tag;

    private int startTimeIdentifier;

    public PackageSimpleMethodVisitor(int access,
                                      String name,
                                      String desc,
                                      MethodVisitor mv,
                                      String className) {
        super(ASM5, access, desc, mv);
        this.tag = className + "." + name;
    }

    /**
     * 此方法在访问方法的头部时被访问到，仅被访问一次
     */
    @Override
    public void visitCode() {
        super.visitCode();
        maintainer.addRecorder(tag, ProfilerParams.of(false, 300, 10));

        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
        startTimeIdentifier = newLocal(Type.LONG_TYPE);
        mv.visitVarInsn(LSTORE, startTimeIdentifier);
    }

    /**
     * 此方法可以获取方法中每一条指令的操作类型，被访问多次
     * 如应在方法结尾处添加新指令，则应判断opcode的类型
     */
    @Override
    public void visitInsn(int opcode) {
        if ((IRETURN <= opcode && opcode <= RETURN) || opcode == ATHROW) {
            mv.visitVarInsn(LLOAD, startTimeIdentifier);
            mv.visitLdcInsn(tag);
            mv.visitMethodInsn(INVOKESTATIC, ProfilingAspect.class.getName().replace(".", "/"), "profiling", "(JLjava/lang/String;)V", false);
        }
        super.visitInsn(opcode);
    }
}