package me.noverify.utils;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import me.lpk.util.OpUtils;
import me.noverify.JByteMod;

public class EditDialogue {
	private static final HashMap<String, String[]> opc = new HashMap<>();

	static {
		opc.put(InsnNode.class.getSimpleName(),
				new String[] { "nop", "aconst_null", "iconst_m1", "iconst_0", "iconst_1", "iconst_2", "iconst_3", "iconst_4", "iconst_5",
						"lconst_0", "lconst_1", "fconst_0", "fconst_1", "fconst_2", "dconst_0", "dconst_1", "iaload", "laload", "faload",
						"daload", "aaload", "baload", "caload", "saload", "iastore", "lastore", "fastore", "dastore", "aastore", "bastore",
						"castore", "sastore", "pop", "pop2", "dup", "dup_x1", "dup_x2", "dup2", "dup2_x1", "dup2_x2", "swap", "iadd", "ladd",
						"fadd", "dadd", "isub", "lsub", "fsub", "dsub", "imul", "lmul", "fmul", "dmul", "idiv", "ldiv", "fdiv", "ddiv", "irem",
						"lrem", "frem", "drem", "ineg", "lneg", "fneg", "dneg", "ishl", "lshl", "ishr", "lshr", "iushr", "lushr", "iand", "land",
						"ior", "lor", "ixor", "lxor", "i2l", "i2f", "i2d", "l2i", "l2f", "l2d", "f2i", "f2l", "f2d", "d2i", "d2l", "d2f", "i2b",
						"i2c", "i2s", "lcmp", "fcmpl", "fcmpg", "dcmpl", "dcmpg", "ireturn", "lreturn", "freturn", "dreturn", "areturn", "return",
						"arraylength", "athrow", "monitorenter", "monitorexit" });
		opc.put(MethodInsnNode.class.getSimpleName(), new String[] { "invokestatic", "invokevirtual", "invokespecial", "invokeinterface" }); //TODO: invokedynamic
		opc.put(FieldInsnNode.class.getSimpleName(), new String[] { "getstatic", "putstatic", "getfield", "putfield" });
		opc.put(VarInsnNode.class.getSimpleName(),
				new String[] { "iload", "lload", "fload", "dload", "aload", "istore", "lstore", "fstore", "dstore", "astore", "ret" });
		opc.put(TypeInsnNode.class.getSimpleName(), new String[] { "new", "anewarray", "checkcast", "instanceof" });
		opc.put(LdcInsnNode.class.getSimpleName(), new String[] { "ldc" });
		opc.put(IincInsnNode.class.getSimpleName(), new String[] { "iinc" });
		opc.put(JumpInsnNode.class.getSimpleName(), new String[] { "ifeq", "ifne", "iflt", "ifge", "ifgt", "ifle", "if_icmpeq", "if_icmpne",
				"if_icmplt", "if_icmpge", "if_icmpgt", "if_icmple", "if_acmpeq", "if_acmpne", "goto", "jsr", "ifnull", "ifnonnull" });
		opc.put(IntInsnNode.class.getSimpleName(), new String[] { "bipush", "sipush", "newarray" });
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public static void createEditInsnDialog(MethodNode mn, AbstractInsnNode ain) throws Exception {
		final JPanel panel = new JPanel(new BorderLayout(5, 5));
		final JPanel input = new JPanel(new GridLayout(0, 1));
		final JPanel labels = new JPanel(new GridLayout(0, 1));
		panel.add(labels, "West");
		panel.add(input, "Center");
		JComboBox<String> opcode = null;
		LinkedHashMap<String, String> fieldNames = new LinkedHashMap<>();
		if (ain.getOpcode() != -1) {
			fieldNames.put("opcode", "");
			labels.add(new JLabel("Insert Opcode: "));
			String[] arr = opc.get(ain.getClass().getSimpleName());
			if (arr == null) {
				error(ain.getClass().getSimpleName() + "s aren\'t supported yet");
				return;
			}
			opcode = new JComboBox<String>(arr);
			opcode.setSelectedItem(OpUtils.getOpcodeText(ain.getOpcode()).toLowerCase());
			input.add(opcode);
		}
		for (Field f : ain.getClass().getDeclaredFields()) {
			if (f.getGenericType().getTypeName().equals("java.lang.String")) {
				fieldNames.put(f.getName(), "String");
				labels.add(new JLabel("Insert " + toUp(f.getName()) + ": "));
				final JTextField value = new JTextField((String) f.get(ain));
				input.add(value);
			} else if (f.getGenericType().getTypeName().equals("int")) {
				fieldNames.put(f.getName(), "int");
				labels.add(new JLabel("Insert " + toUp(f.getName()) + ": "));
				NumberFormat format = NumberFormat.getInstance();
				format.setGroupingUsed(false);
				NumberFormatter formatter = new NumberFormatter(format);
				formatter.setValueClass(Integer.class);
				formatter.setMinimum(0);
				formatter.setMaximum(Integer.MAX_VALUE);
				formatter.setAllowsInvalid(false);
				formatter.setCommitsOnValidEdit(true);
				formatter.setOverwriteMode(true);
				JFormattedTextField field = new JFormattedTextField(formatter);
				field.setValue(f.get(ain));
				input.add(field);
			} else {
				System.out.println("Unallowed edit:" + f.getName() + " " + f.getGenericType().getTypeName());
			}
			//Special case for ldc
			if (ain.getClass().getSimpleName().equals(LdcInsnNode.class.getSimpleName())) {
				LdcInsnNode ldc = (LdcInsnNode) ain;
				fieldNames.put("ldctype", "");
				labels.add(new JLabel("Insert Ldc Type: "));
				JComboBox<String> ldctype = new JComboBox<String>(new String[] { "String", "float", "double", "int", "long" });
				System.out.println("LDC: " + ldc.cst.getClass().getName());
				if (ldc.cst instanceof String) {
					ldctype.setSelectedItem("String");
				} else if (ldc.cst instanceof Float) {
					ldctype.setSelectedItem("float");
				} else if (ldc.cst instanceof Double) {
					ldctype.setSelectedItem("double");
				} else if (ldc.cst instanceof Long) {
					ldctype.setSelectedItem("long");
				} else if (ldc.cst instanceof Integer) {
					ldctype.setSelectedItem("int");
				} else {
					error("Unsupported LDC Type: " + ldc.cst.getClass().getName());
				}
				input.add(ldctype);
				fieldNames.put("ldcvalue", "");
				labels.add(new JLabel("Insert Ldc Value: "));
				input.add(new JTextField(ldc.cst.toString()));
			}
			//Special case for jump
			if (ain.getClass().getSimpleName().equals(JumpInsnNode.class.getSimpleName())) {
				ArrayList<LabelNode> ln = new ArrayList<>();
				for (AbstractInsnNode nod : mn.instructions.toArray()) {
					if (nod instanceof LabelNode) {
						ln.add((LabelNode) nod);
					}
				}
				JumpInsnNode jin = (JumpInsnNode) ain;
				fieldNames.put("jumplabel", "");
				labels.add(new JLabel("Jump to Label: "));
				JComboBox<LabelNode> jcb = new JComboBox<>(ln.toArray(new LabelNode[0]));
				jcb.setSelectedItem(jin.label);
				input.add(jcb);
			}
		}

		if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Edit " + ain.getClass().getSimpleName(), 2) == JOptionPane.OK_OPTION) {
			int i = 0;
			for (String fn : fieldNames.keySet()) {
				System.out.println(fn);
				if (fn.equals("opcode")) {
					JComboBox<String> jcb = (JComboBox<String>) input.getComponent(i);
					ain.setOpcode(OpUtils.getOpcodeIndex(jcb.getSelectedItem().toString().toUpperCase()));
					i++;
				} else if (fn.equals("ldctype")) {
					LdcInsnNode ldc = (LdcInsnNode) ain;
					JComboBox<String> jcb = (JComboBox<String>) input.getComponent(i);
					JTextField cst = (JTextField) input.getComponent(i + 1);
					try {
						switch (jcb.getSelectedItem().toString()) {
						case "String":
							ldc.cst = cst.getText();
							break;
						case "float":
							ldc.cst = Float.parseFloat(cst.getText());
							break;
						case "double":
							ldc.cst = Double.parseDouble(cst.getText());
							break;
						case "long":
							ldc.cst = Long.parseLong(cst.getText());
							break;
						case "int":
							ldc.cst = Integer.parseInt(cst.getText());
							break;
						}
					} catch (Exception e) {
						error("Value not capable for type " + jcb.getSelectedItem().toString() + ": \"" + cst.getText() + "\"");
					}
					i++;
				} else if (fn.equals("jumplabel")) {
					JComboBox<String> jcb = (JComboBox<String>) input.getComponent(i);
					JumpInsnNode jin = (JumpInsnNode) ain;
					jin.label = (LabelNode) jcb.getSelectedItem();
					i++;
				} else {
					String type = fieldNames.get(fn);
					if (type.equals("String")) {
						JTextField jtf = (JTextField) input.getComponent(i);
						String text = jtf.getText();
						Field f = ain.getClass().getDeclaredField(fn);
						f.setAccessible(true);
						f.set(ain, text);
						i++;
					} else if (type.equals("int")) {
						JFormattedTextField jtf = (JFormattedTextField) input.getComponent(i);
						int val = (int) jtf.getValue();
						Field f = ain.getClass().getDeclaredField(fn);
						f.setAccessible(true);
						f.set(ain, val);
						i++;
					}
				}
			}

		}
	}

	private static String toUp(String name) {
		if (name.isEmpty()) {
			return name;
		}
		String after = name.substring(1);
		String f = name.substring(0, 1);
		return f.toUpperCase() + after;
	}

	public static void error(String str) {
		JOptionPane.showMessageDialog(JByteMod.instance, str, "Error", 0);
	}

	public static void createClassDialogue(ClassNode cn) {
		final JPanel panel = new JPanel(new BorderLayout(5, 5));
		final JPanel input = new JPanel(new GridLayout(0, 1));
		final JPanel labels = new JPanel(new GridLayout(0, 1));
		panel.add(labels, "West");
		panel.add(input, "Center");
		panel.add(new JLabel("Warning: References will not be updated!"), "South");
		labels.add(new JLabel("Class Name:"));
		JTextField name = new JTextField(cn.name);
		input.add(name);
		labels.add(new JLabel("Class SourceFile:"));
		JTextField sf = new JTextField(cn.sourceFile);
		input.add(sf);
		labels.add(new JLabel("Class Access:"));
		NumberFormat format = NumberFormat.getInstance();
		format.setGroupingUsed(false);
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Integer.class);
		formatter.setMinimum(0);
		formatter.setMaximum(Integer.MAX_VALUE);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);
		formatter.setOverwriteMode(true);
		JFormattedTextField access = new JFormattedTextField(formatter);
		access.setValue(cn.access);
		input.add(access);
		String cname = cn.name;
		if (cn.name.contains("/")) {
			String[] spl = cn.name.split("/");
			cname = spl[spl.length - 1];
		}
		if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Edit Class " + cname, 2) == JOptionPane.OK_OPTION) {
			cn.name = name.getText();
			cn.sourceFile = sf.getText();
			cn.access = (int) access.getValue();
			JByteMod.instance.updateFileTree();
		}
	}

	public static void createMethodDialogue(MethodNode mn) {
		final JPanel panel = new JPanel(new BorderLayout(5, 5));
		final JPanel input = new JPanel(new GridLayout(0, 1));
		final JPanel labels = new JPanel(new GridLayout(0, 1));
		panel.add(labels, "West");
		panel.add(input, "Center");
		panel.add(new JLabel("Warning: References will not be updated!"), "South");
		labels.add(new JLabel("Method Name:"));
		JTextField name = new JTextField(mn.name);
		input.add(name);
		labels.add(new JLabel("Method Desc:"));
		JTextField desc = new JTextField(mn.desc);
		input.add(desc);
		labels.add(new JLabel("Method Access:"));
		NumberFormat format = NumberFormat.getInstance();
		format.setGroupingUsed(false);
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Integer.class);
		formatter.setMinimum(0);
		formatter.setMaximum(Integer.MAX_VALUE);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);
		formatter.setOverwriteMode(true);
		JFormattedTextField access = new JFormattedTextField(formatter);
		access.setValue(mn.access);
		input.add(access);
		labels.add(new JLabel("Method MaxLocals:"));
		JFormattedTextField maxL = new JFormattedTextField(formatter);
		maxL.setValue(mn.maxLocals);
		input.add(maxL);
		labels.add(new JLabel("Method MaxStack:"));
		JFormattedTextField maxS = new JFormattedTextField(formatter);
		maxS.setValue(mn.maxLocals);
		input.add(maxS);
		if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Edit Method " + mn.name, 2) == JOptionPane.OK_OPTION) {
			mn.name = name.getText();
			mn.desc = desc.getText();
			mn.access = (int) access.getValue();
			mn.maxLocals = (int) maxL.getValue();
			mn.maxStack = (int) maxS.getValue();
			JByteMod.instance.updateFileTree();
		}
	}
}
