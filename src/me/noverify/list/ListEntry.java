package me.noverify.list;

import org.objectweb.asm.tree.AbstractInsnNode;

public class ListEntry {
	private String text;

	public ListEntry(String text) {
		super();
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return getText();
	}
}
