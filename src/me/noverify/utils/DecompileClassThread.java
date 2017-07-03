package me.noverify.utils;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.noverify.JByteMod;

public class DecompileClassThread extends Thread {
	private ClassNode cn;

	public DecompileClassThread(ClassNode cn) {
		this.cn = cn;
	}

	@Override
	public void run() {
		if (cn.methods.size() < 100 || JByteMod.instance.decompileHuge()) {
			JByteMod.instance.getFernflowerArea().setText("Processing..");
			String decompiled = FernflowerDecompiler.decompile(cn);
			JByteMod.instance.getFernflowerArea().setText(decompiled);
		} else {
			JByteMod.instance.getFernflowerArea().setText("Class too large.");
		}
	}
}
