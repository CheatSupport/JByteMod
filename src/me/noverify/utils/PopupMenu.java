package me.noverify.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.noverify.JByteMod;
import me.noverify.list.FieldListEntry;
import me.noverify.list.InsnListEntry;
import me.noverify.list.ListEntry;

public class PopupMenu {
	public static void showPopupInsn(MouseEvent e, JList codeList) {
		codeList.setSelectedIndex(codeList.locationToIndex(e.getPoint()));
		ListEntry entr = (ListEntry) codeList.getSelectedValue();
		if (entr instanceof InsnListEntry) {
			InsnListEntry insn = (InsnListEntry) entr;
			AbstractInsnNode ain = insn.getNode();
			MethodNode mn = insn.getMethod();
			
			JPopupMenu menu = new JPopupMenu();
			JMenuItem insert = new JMenuItem("Insert after");
			insert.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						EditDialogue.createInsertInsnDialog(mn, ain);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			menu.add(insert);
			JMenuItem edit = new JMenuItem("Edit");
			edit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						EditDialogue.createEditInsnDialog(mn, ain);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			menu.add(edit);
			JMenuItem up = new JMenuItem("Move up");
			up.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AbstractInsnNode node = ain.getPrevious();
					mn.instructions.remove(node);
					mn.instructions.insert(ain, node);
					JByteMod.instance.reloadList(mn);
				}
			});
			menu.add(up);
			JMenuItem down = new JMenuItem("Move down");
			down.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AbstractInsnNode node = ain.getNext();
					mn.instructions.remove(node);
					mn.instructions.insertBefore(ain, node);
					JByteMod.instance.reloadList(mn);
				}
			});
			menu.add(down);
			JMenuItem remove = new JMenuItem("Remove");
			remove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mn.instructions.remove(ain);
					JByteMod.instance.reloadList(mn);
				}
			});
			menu.add(remove);
			menu.addPopupMenuListener(new PopupMenuListener() {
				public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
					codeList.setFocusable(true);
				}

				public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
					codeList.setFocusable(true);
				}

				public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
					codeList.setFocusable(false);
				}
			});
			menu.show(JByteMod.instance, (int) JByteMod.instance.getMousePosition().getX(), (int) JByteMod.instance.getMousePosition().getY());
		} else if(entr instanceof FieldListEntry) {
			FieldListEntry fle = (FieldListEntry) entr;
			JPopupMenu menu = new JPopupMenu();
			JMenuItem edit = new JMenuItem("Edit");
			edit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						EditDialogue.createEditFieldDialog(fle.getCn(), fle.getFn());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			menu.add(edit);
			menu.show(JByteMod.instance, (int) JByteMod.instance.getMousePosition().getX(), (int) JByteMod.instance.getMousePosition().getY());
		}
	}
}
