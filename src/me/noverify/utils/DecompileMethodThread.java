package me.noverify.utils;

import org.objectweb.asm.tree.MethodNode;

import me.noverify.JByteMod;

public class DecompileMethodThread extends Thread {
	private MethodNode mn;

	public DecompileMethodThread(MethodNode mn) {
		this.mn = mn;
	}

	@Override
	public void run() {
		if (mn.instructions.size() < 4000 || JByteMod.instance.decompileHuge()) {
			JByteMod.instance.getFernflowerArea().setText("Processing..");
			String decompiled = FernflowerDecompiler.decompileMethod(mn);
			JByteMod.instance.getFernflowerArea().setText(decompiled);
		} else {
			JByteMod.instance.getFernflowerArea().setText("Method too large.");
		}
	}
}
