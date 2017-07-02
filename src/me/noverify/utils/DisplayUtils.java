package me.noverify.utils;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import me.lpk.util.OpUtils;

public class DisplayUtils {
	public static String getNodeText(AbstractInsnNode ain) {
		String opc = TextUtils.toBold(OpUtils.getOpcodeText(ain.getOpcode()).toLowerCase()) + " ";
		switch (ain.getType()) {
		case AbstractInsnNode.LABEL:
			opc = TextUtils.toLight("label " + OpUtils.getLabelIndex((LabelNode) ain));
			break;
		case AbstractInsnNode.LINE:
			opc = TextUtils.toLight("line " + ((LineNumberNode) ain).line);
			break;
		case AbstractInsnNode.FIELD_INSN:
			FieldInsnNode fin = (FieldInsnNode) ain;
			opc += getDisplayType(TextUtils.escape(fin.desc)) + " " + getDisplayClassRed(TextUtils.escape(fin.owner)) + "." + fin.name;
			break;
		case AbstractInsnNode.METHOD_INSN:
			MethodInsnNode min = (MethodInsnNode) ain;
			opc += getDisplayType(min.desc.split("\\)")[1]) + " " + getDisplayClassRed(TextUtils.escape(min.owner)) + "."
					+ TextUtils.escape(min.name) + "(" + getDisplayArgs(TextUtils.escape(min.desc)) + ")";
			break;
		case AbstractInsnNode.VAR_INSN:
			VarInsnNode vin = (VarInsnNode) ain;
			opc += vin.var;
			break;
		case AbstractInsnNode.TYPE_INSN:
			TypeInsnNode tin = (TypeInsnNode) ain;
			opc += getDisplayType(TextUtils.escape(tin.desc));
			break;
		case AbstractInsnNode.JUMP_INSN:
			JumpInsnNode jin = (JumpInsnNode) ain;
			opc += OpUtils.getLabelIndex(jin.label);
			break;
		case AbstractInsnNode.LDC_INSN:
			LdcInsnNode ldc = (LdcInsnNode) ain;
			opc += TextUtils.addTag(ldc.cst.getClass().getSimpleName(), "font color=#557799") + " ";
			if (ldc.cst instanceof String)
				opc += TextUtils.addTag("\"" + ldc.cst.toString() + "\"", "font color=#559955");
			else {
				opc += ldc.cst.toString();
			}
			break;
		case AbstractInsnNode.INT_INSN:
			opc += OpUtils.getIntValue(ain);
			break;
		case AbstractInsnNode.IINC_INSN:
			IincInsnNode iinc = (IincInsnNode) ain;
			opc += iinc.var + " " + iinc.incr;
			break;
		case AbstractInsnNode.FRAME:
			FrameNode fn = (FrameNode) ain;
			opc = TextUtils.toLight(OpUtils.getOpcodeText(fn.type).toLowerCase() + " " + fn.local.size() + " " + fn.stack.size());
			break;
		}
		return TextUtils.toHtml(opc);
	}

	public static String getDisplayClass(String str) {
		String[] spl = str.split("/"); 
		if (spl.length > 1) {
			return TextUtils.addTag(spl[spl.length - 1], "font color=#557799");
		}
		return TextUtils.addTag(str, "font color=#557799");
	}

	public static String getDisplayClassRed(String str) {
		String[] spl = str.split("/");
		if (spl.length > 1) {
			return TextUtils.addTag(spl[spl.length - 1], "font color=#995555");
		}
		return TextUtils.addTag(str, "font color=#995555");
	}

	public static String getDisplayArgs(String rawType) {
		return getDisplayType(rawType.split("\\)")[0].substring(1));
	}

	public static String getDisplayType(String rawType) {
		String result = "";
		String tmpArg = "";
		String argSuffix = "";
		boolean isFullyQualifiedClass = false;
		for (char chr : rawType.toCharArray()) {
			if (isFullyQualifiedClass) {
				if (chr == ';') {
					String[] spl = tmpArg.split("/");
					result += spl[spl.length - 1] + argSuffix + ", ";
					argSuffix = "";
					isFullyQualifiedClass = false;
					tmpArg = "";
				} else {
					tmpArg += chr;
				}
			} else if (chr == '[') {
				argSuffix += "[]";
			} else if (chr == 'L') {
				isFullyQualifiedClass = true;
			} else {
				if (chr == 'Z') {
					result += "boolean";
				} else if (chr == 'B') {
					result += "byte";
				} else if (chr == 'C') {
					result += "char";
				} else if (chr == 'S') {
					result += "short";
				} else if (chr == 'I') {
					result += "int";
				} else if (chr == 'J') {
					result += "long";
				} else if (chr == 'F') {
					result += "float";
				} else if (chr == 'D') {
					result += "double";
				} else if (chr == 'V') {
					result += "void";
				} else {
					isFullyQualifiedClass = true;
					continue;
				}

				result += argSuffix;
				argSuffix = "";
				result += ", ";
			}
		}

		if (tmpArg.length() != 0) {
			String[] spl = tmpArg.split("/");
			result += spl[spl.length - 1] + argSuffix + ", ";
		}

		if (result.length() >= 2) {
			result = result.substring(0, result.length() - 2);
		}
		return TextUtils.addTag(result, "font color=#557799");
	}
}
