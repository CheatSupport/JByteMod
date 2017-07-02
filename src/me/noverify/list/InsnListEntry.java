package me.noverify.list;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.noverify.utils.DisplayUtils;

public class InsnListEntry extends ListEntry {
	protected AbstractInsnNode node;
	private MethodNode method;

	public MethodNode getMethod() {
		return method;
	}

	public InsnListEntry(MethodNode mn, AbstractInsnNode node) {
		super("null");
		this.method = mn;
		this.node = node;
	}

	public AbstractInsnNode getNode() {
		return node;
	}

	public void setNode(AbstractInsnNode node) {
		this.node = node;
	}

	@Override
	public String getText() {
		return DisplayUtils.getNodeText(node);
	}
}
