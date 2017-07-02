package me.noverify.utils;

import javax.swing.tree.DefaultMutableTreeNode;

public class TreeUtils {

	public static DefaultMutableTreeNode sort(DefaultMutableTreeNode node) {

		for (int i = 0; i < node.getChildCount() - 1; i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			String nt = child.getUserObject().toString();

			for (int j = i + 1; j <= node.getChildCount() - 1; j++) {
				DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) node.getChildAt(j);
				String np = prevNode.getUserObject().toString();

				if (nt.compareToIgnoreCase(np) > 0) {
					node.insert(child, j);
					node.insert(prevNode, i);
				}
			}
			if (child.getChildCount() > 0) {
				sort(child);
			}
		}

		for (int i = 0; i < node.getChildCount() - 1; i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			for (int j = i + 1; j <= node.getChildCount() - 1; j++) {
				DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) node.getChildAt(j);

				if (!prevNode.isLeaf() && child.isLeaf()) {
					node.insert(child, j);
					node.insert(prevNode, i);
				}
			}
		}

		return node;

	}
}
