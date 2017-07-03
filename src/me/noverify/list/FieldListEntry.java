package me.noverify.list;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import me.noverify.utils.DisplayUtils;
import me.noverify.utils.TextUtils;

public class FieldListEntry extends ListEntry {
	private String text;
	private FieldNode fn;
	private ClassNode cn;

	public FieldListEntry(ClassNode cn, FieldNode fn) {
		super(TextUtils.toHtml(DisplayUtils.getDisplayAccess(fn.access) + " " + DisplayUtils.getDisplayType(fn.desc) + " "
				+ DisplayUtils.getDisplayClassRed(fn.name) + " = " + fn.value));
		this.fn = fn;
		this.cn = cn;
	}

	public FieldNode getFn() {
		return fn;
	}

	public void setFn(FieldNode fn) {
		this.fn = fn;
	}

	public ClassNode getCn() {
		return cn;
	}

	public void setCn(ClassNode cn) {
		this.cn = cn;
	}

}
