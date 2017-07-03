package me.noverify.list;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import me.lpk.util.OpUtils;
import me.noverify.utils.DisplayUtils;
import me.noverify.utils.TextUtils;

public class TCBListEntry extends ListEntry {
	private ClassNode cn;
	private MethodNode mn;
	private String text;
	private TryCatchBlockNode tcbn;

	public TCBListEntry(ClassNode cn, MethodNode mn, TryCatchBlockNode tcbn) {
		super(TextUtils.toHtml(DisplayUtils.getDisplayType(tcbn.type != null ? tcbn.type : "") + ": label "
				+ OpUtils.getLabelIndex(tcbn.start) + " -> label " + OpUtils.getLabelIndex(tcbn.end) + " handler: label "
				+ (tcbn.handler == null ? "null" : OpUtils.getLabelIndex(tcbn.handler))));
		this.cn = cn;
		this.mn = mn;
		this.tcbn = tcbn;
	}

	public TryCatchBlockNode getTcbn() {
		return tcbn;
	}

	public void setTcbn(TryCatchBlockNode tcbn) {
		this.tcbn = tcbn;
	}

	public ClassNode getCn() {
		return cn;
	}

	public void setCn(ClassNode cn) {
		this.cn = cn;
	}

	public MethodNode getMn() {
		return mn;
	}

	public void setMn(MethodNode mn) {
		this.mn = mn;
	}

}
