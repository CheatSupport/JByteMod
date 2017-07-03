package me.noverify;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import me.lpk.util.JarUtils;
import me.noverify.list.CellRenderer;
import me.noverify.list.FieldListEntry;
import me.noverify.list.InsnListEntry;
import me.noverify.list.ListEntry;
import me.noverify.list.SearchListEntry;
import me.noverify.list.TCBListEntry;
import me.noverify.utils.DecompileClassThread;
import me.noverify.utils.DecompileMethodThread;
import me.noverify.utils.EditDialogue;
import me.noverify.utils.MethodUtils;
import me.noverify.utils.PopupMenu;
import me.noverify.utils.SortedTreeNode;

public class JByteMod extends JFrame {

	private JPanel contentPane;
	private JTree fileTree;
	private JTabbedPane rightSide;
	private JPanel leftSide;
	private JList<ListEntry> codeList;
	private JList<SearchListEntry> searchList;
	public static JByteMod instance;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenu mnTools;
	private JMenuItem mntmSave;
	private JMenuItem mntmSaveAs;
	private JMenuItem mntmLoad;
	private JMenuItem mntmSearch;
	private JMenuItem mntmClose;
	private Map<String, ClassNode> classes;
	private Map<String, byte[]> output;
	private JLabel rightDesc;
	private JLabel searchDesc;
	private File opened;
	private JMenu mnSettings;
	private JCheckBoxMenuItem chckbxmntmSortMethods;
	private JLabel tcbDesc;
	private JList<TCBListEntry> tcbList;
	private JMenu mnNewMenu;
	private JMenuItem mntmSelectClassBy;
	private JMenuItem mntmSelectClassBy_1;
	private JMenuItem mntmFindMainClasses;
	private RSyntaxTextArea ffArea;
	private JCheckBoxMenuItem chckbxmntmDecompile;
	private JCheckBoxMenuItem chckbxmntmDecompileHugeCode;
	private JMenu mnDecompiler;
	private JCheckBoxMenuItem chckbxmntmRefreshDecompiler;
	private JCheckBoxMenuItem chckbxmntmDeclarationTreeSelection;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				try {
					for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
						if ("Nimbus".equals(info.getName())) {
							UIManager.setLookAndFeel(info.getClassName());
							break;
						}
					}
					instance = new JByteMod();
					instance.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * Create the frame.
	 */
	public JByteMod() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				if (JOptionPane.showConfirmDialog(instance, "Do you really want to exit? All unsaved changes will be lost.", "Are you sure?",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});
		setBounds(100, 100, 1280, 720);
		setTitle("JByteMod v0.5.0");

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (opened != null) {
					saveJarFile(opened);
				}
			}
		});
		mntmSave.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mnFile.add(mntmSave);

		mntmSaveAs = new JMenuItem("Save As..");
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (opened != null) {
					saveAsFileChooser();
				}
			}
		});
		mnFile.add(mntmSaveAs);

		mntmLoad = new JMenuItem("Load");
		mntmLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openFileChooserLoad();
			}
		});
		mntmLoad.setAccelerator(KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mnFile.add(mntmLoad);

		mntmClose = new JMenuItem("Close");
		mntmClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (opened == null || JOptionPane.showConfirmDialog(instance, "Do you really want to exit? All unsaved changes will be lost.",
						"Are you sure?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});
		mntmClose.setAccelerator(KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mnFile.add(mntmClose);

		mnTools = new JMenu("Tools");
		menuBar.add(mnTools);

		mntmSearch = new JMenuItem("Search");
		mntmSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (classes == null) {
					EditDialogue.error("Open a jar file first.");
					return;
				}
				final JPanel panel = new JPanel(new BorderLayout(5, 5));
				final JPanel input = new JPanel(new GridLayout(0, 1));
				final JPanel labels = new JPanel(new GridLayout(0, 1));
				panel.add(labels, "West");
				panel.add(input, "Center");
				panel.add(new JLabel("Warning: This could take some time\n on short strings!"), "South");
				labels.add(new JLabel("String Constant:"));
				JTextField cst = new JTextField();
				input.add(cst);
				JCheckBox exact = new JCheckBox("Exact");
				JCheckBox snstv = new JCheckBox("Case sensitive");
				labels.add(exact);
				input.add(snstv);
				if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Search LDC", 2) == JOptionPane.OK_OPTION
						&& !cst.getText().isEmpty()) {
					searchForCst(cst.getText(), exact.isSelected(), snstv.isSelected());
				}
			}
		});
		mntmSearch.setAccelerator(KeyStroke.getKeyStroke('H', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mnTools.add(mntmSearch);

		mnNewMenu = new JMenu("Utils");
		mnTools.add(mnNewMenu);

		mntmSelectClassBy = new JMenuItem("Select Class by SourceFile");
		mntmSelectClassBy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (classes == null)
					return;
				final JPanel panel = new JPanel(new BorderLayout(5, 5));
				final JPanel input = new JPanel(new GridLayout(0, 1));
				final JPanel labels = new JPanel(new GridLayout(0, 1));
				panel.add(labels, "West");
				panel.add(input, "Center");
				panel.add(new JLabel("Warning: This could take some time\n on big jars!"), "South");
				labels.add(new JLabel("SourceFile:"));
				JTextField cst = new JTextField();
				input.add(cst);
				if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Select Class by SourceFile", 2) == JOptionPane.OK_OPTION
						&& !cst.getText().isEmpty()) {
					for (ClassNode cn : classes.values()) {
						if (cn.sourceFile != null) {
							if (cn.sourceFile.equals(cst.getText())) {
								selectTreeClass(cn);
								break;
							}
						}
					}
				}
			}
		});
		mnNewMenu.add(mntmSelectClassBy);

		mntmSelectClassBy_1 = new JMenuItem("Select Class by Name");
		mntmSelectClassBy_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (classes == null)
					return;
				final JPanel panel = new JPanel(new BorderLayout(5, 5));
				final JPanel input = new JPanel(new GridLayout(0, 1));
				final JPanel labels = new JPanel(new GridLayout(0, 1));
				panel.add(labels, "West");
				panel.add(input, "Center");
				panel.add(new JLabel("Warning: This could take some time\n on big jars!"), "South");
				labels.add(new JLabel("Class Name:"));
				JTextField cst = new JTextField();
				input.add(cst);
				if (JOptionPane.showConfirmDialog(JByteMod.instance, panel, "Select Class by Name", 2) == JOptionPane.OK_OPTION
						&& !cst.getText().isEmpty()) {
					for (ClassNode cn : classes.values()) {
						if (cn.name != null) {
							if (cn.name.equals(cst.getText())) {
								selectTreeClass(cn);
								break;
							}
						}
					}
				}
			}
		});
		mnNewMenu.add(mntmSelectClassBy_1);

		mntmFindMainClasses = new JMenuItem("Find Main Classes");
		mntmFindMainClasses.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchForMainClasses();
			}
		});
		mnNewMenu.add(mntmFindMainClasses);

		mnSettings = new JMenu("Settings");
		menuBar.add(mnSettings);

		chckbxmntmSortMethods = new JCheckBoxMenuItem("Sort Methods");
		chckbxmntmSortMethods.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setupTree();
			}
		});
		chckbxmntmSortMethods.setSelected(true);
		mnSettings.add(chckbxmntmSortMethods);

		chckbxmntmDeclarationTreeSelection = new JCheckBoxMenuItem("Declaration Tree Selection");
		chckbxmntmDeclarationTreeSelection.setSelected(true);
		mnSettings.add(chckbxmntmDeclarationTreeSelection);

		mnDecompiler = new JMenu("Decompiler");
		mnSettings.add(mnDecompiler);

		chckbxmntmDecompile = new JCheckBoxMenuItem("Decompile");
		mnDecompiler.add(chckbxmntmDecompile);
		chckbxmntmDecompile.setSelected(true);

		chckbxmntmDecompileHugeCode = new JCheckBoxMenuItem("Decompile Huge Code");
		mnDecompiler.add(chckbxmntmDecompileHugeCode);

		chckbxmntmRefreshDecompiler = new JCheckBoxMenuItem("Refresh Decompiler");
		mnDecompiler.add(chckbxmntmRefreshDecompiler);
		chckbxmntmDecompile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ffArea.setText("");
			}
		});
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		SortedTreeNode root = new SortedTreeNode("", null, null);
		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		fileTree = new JTree(treeModel);
		fileTree.setRootVisible(false);
		fileTree.setShowsRootHandles(true);
		fileTree.setCellRenderer(new CellRenderer());
		fileTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				SortedTreeNode node = (SortedTreeNode) fileTree.getLastSelectedPathComponent();
				if (node == null)
					return;
				System.out.println(node.getCn());
				if (node.getCn() != null && node.getMn() != null) {
					decompileMethod(node.getCn(), node.getMn());
				} else if (node.getCn() != null) {
					decompileClass(node.getCn());
				} else {
					fileTree.clearSelection();
					if (node.isLeaf()) {
						return;
					}
					if (fileTree.isExpanded(e.getPath())) {
						fileTree.collapsePath(e.getPath());
					} else {
						fileTree.expandPath(e.getPath());
					}
				}
			}
		});
		rightSide = new JTabbedPane();
		leftSide = new JPanel();
		leftSide.setLayout(new BorderLayout(0, 0));
		//		leftSide.setLayout(new BoxLayout(leftSide, BoxLayout.Y_AXIS));
		leftSide.add(new JLabel(" Jar File"), BorderLayout.NORTH);
		leftSide.add(new JScrollPane(fileTree), BorderLayout.CENTER);
		setupTabs();
		JPanel border = new JPanel();
		border.setBorder(new LineBorder(Color.GRAY));
		border.setLayout(new GridLayout());
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSide, rightSide);
		splitPane.setDividerLocation(150);
		splitPane.setContinuousLayout(true);
		JPanel b2 = new JPanel();
		b2.setBorder(new EmptyBorder(5, 0, 5, 0));
		b2.setLayout(new GridLayout());
		b2.add(splitPane);
		border.add(b2);
		contentPane.add(border, BorderLayout.CENTER);
	}

	protected void searchForMainClasses() {
		if (classes == null)
			return;
		searchDesc.setText("Main Classes");
		rightSide.setSelectedIndex(3);
		DefaultListModel<SearchListEntry> lm = (DefaultListModel<SearchListEntry>) searchList.getModel();
		lm.clear();
		for (ClassNode c : classes.values()) {
			for (MethodNode m : c.methods) {
				if (m.name.equals("main") && m.desc.equals("([Ljava/lang/String;)V")) {
					lm.addElement(new SearchListEntry(c, m));
				}
			}
		}
	}

	protected void searchForCst(String search, boolean exact, boolean caseSens) {
		searchDesc.setText("Results for \"" + search + "\"");
		rightSide.setSelectedIndex(4);
		DefaultListModel<SearchListEntry> lm = (DefaultListModel<SearchListEntry>) searchList.getModel();
		lm.clear();
		if (!caseSens) {
			search = search.toLowerCase();
		}
		for (ClassNode c : classes.values()) {
			for (MethodNode m : c.methods) {
				for (AbstractInsnNode ain : m.instructions.toArray()) {
					if (ain instanceof LdcInsnNode) {
						String cst = ((LdcInsnNode) ain).cst.toString();
						if (!caseSens) {
							cst = cst.toLowerCase();
						}
						if (!exact && cst.toString().contains(search) || cst.toString().equals(search)) {
							lm.addElement(new SearchListEntry(c, m, cst));
						}
					}
				}
			}
		}
	}

	public void decompileClass(ClassNode cn) {
		DefaultListModel<ListEntry> lm = (DefaultListModel<ListEntry>) codeList.getModel();
		lm.clear();
		rightDesc.setText(cn.name + " fields");
		for (FieldNode fn : cn.fields) {
			lm.addElement(new FieldListEntry(cn, fn));
		}
		ffArea.setText("");
		if (chckbxmntmDecompile.isSelected()) {
			new DecompileClassThread(cn).start();
		}
	}

	protected void decompileMethod(ClassNode cn, MethodNode mn) {
		DefaultListModel<ListEntry> lm = (DefaultListModel<ListEntry>) codeList.getModel();
		lm.clear();
		rightDesc.setText(cn.name + "." + mn.name + mn.desc);
		for (AbstractInsnNode ain : mn.instructions.toArray()) {
			lm.addElement(new InsnListEntry(mn, ain));
		}

		DefaultListModel<TCBListEntry> lm2 = (DefaultListModel<TCBListEntry>) tcbList.getModel();
		lm2.clear();
		tcbDesc.setText("Try Catch Blocks: " + cn.name + "." + mn.name + mn.desc);
		for (TryCatchBlockNode tcbn : mn.tryCatchBlocks) {
			lm2.addElement(new TCBListEntry(cn, mn, tcbn));
		}
		if (chckbxmntmDecompile.isSelected()) {
			new DecompileMethodThread(mn).start();
		}
	}

	public boolean decompileHuge() {
		return chckbxmntmDecompileHugeCode.isSelected();
	}

	protected void openFileChooserLoad() {
		JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.home") + "/Desktop"));
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.setFileFilter(new FileNameExtensionFilter("Java Archives", "jar", "zip"));
		int result = jfc.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File input = jfc.getSelectedFile();
			this.opened = input;
			System.out.println("Selected input jar: " + input.getAbsolutePath());
			loadJarFile(input);
		}
	}

	protected void saveAsFileChooser() {
		JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.home") + "/Desktop"));
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.setFileFilter(new FileNameExtensionFilter("Java Archives", "jar", "zip"));
		int result = jfc.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File output = jfc.getSelectedFile();
			System.out.println("Selected output jar: " + output.getAbsolutePath());
			saveJarFile(output);
		}
	}

	private void saveJarFile(File op) {
		System.out.println("Writing..");
		for (String s : classes.keySet()) {
			ClassNode node = classes.get(s);
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			node.accept(writer);
			this.output.put(s, writer.toByteArray());
		}
		System.out.println("Saving..");
		JarUtils.saveAsJar(output, op.getAbsolutePath());
		System.out.println("Done!");
	}

	private void loadJarFile(File input) {
		try {
			classes = JarUtils.loadClasses(input);
			output = JarUtils.loadNonClassEntries(input);
			setupTree();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void setupTree() {
		DefaultTreeModel tm = (DefaultTreeModel) fileTree.getModel();
		SortedTreeNode root = (SortedTreeNode) tm.getRoot();
		root.removeAllChildren();
		tm.reload();

		for (ClassNode c : classes.values()) {
			for (MethodNode m : c.methods) {
				String name = c.name + ".class/" + m.name;
				if (name.isEmpty())
					continue;
				if (!name.contains("/")) {
					root.add(new SortedTreeNode(name, c, m));
				} else {
					String[] names = name.split("/");
					SortedTreeNode node = root;
					int i = 1;
					for (String n : names) {
						SortedTreeNode newnode = new SortedTreeNode(n, i >= names.length - 1 ? c : null, null);
						if (i == names.length) {
							newnode.setMn(m);
							node.add(newnode);
							tm.getChildCount(node);
						} else {
							SortedTreeNode extnode = addUniqueNode(tm, node, newnode);
							if (extnode != null) {
								node = extnode;
							} else {
								node = newnode;
							}
						}
						i++;
					}
				}
			}
		}
		sort(tm, root);
		tm.reload();
		fileTree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				if (SwingUtilities.isRightMouseButton(me)) {
					TreePath tp = fileTree.getPathForLocation(me.getX(), me.getY());
					if (tp != null && tp.getParentPath() != null) {
						fileTree.setSelectionPath(tp);
						if (fileTree.getLastSelectedPathComponent() == null) {
							return;
						}
						MethodNode mn = ((SortedTreeNode) fileTree.getLastSelectedPathComponent()).getMn();
						ClassNode cn = ((SortedTreeNode) fileTree.getLastSelectedPathComponent()).getCn();
						if (mn != null) {
							JPopupMenu menu = new JPopupMenu();
							JMenuItem edit = new JMenuItem("Edit");
							edit.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									EditDialogue.createMethodDialogue(mn);
								}
							});
							menu.add(edit);
							JMenuItem clear = new JMenuItem("Clear");
							clear.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									if (JOptionPane.showConfirmDialog(JByteMod.instance, "Are you sure you want to clear that method?", "Confirm",
											JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
										MethodUtils.clear(mn);
										decompileMethod(cn, mn);
									}
								}
							});
							menu.add(clear);
							menu.show(fileTree, me.getX(), me.getY());
						} else if (cn != null) {
							JPopupMenu menu = new JPopupMenu();
							JMenuItem edit = new JMenuItem("Edit");
							edit.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									EditDialogue.createClassDialogue(cn);
								}
							});
							menu.add(edit);
							menu.show(fileTree, me.getX(), me.getY());
						}
					}
				}
			}
		});
	}

	public SortedTreeNode addUniqueNode(DefaultTreeModel model, SortedTreeNode node, SortedTreeNode childNode) {
		for (int i = 0; i < model.getChildCount(node); i++) {
			Object compUserObj = ((SortedTreeNode) model.getChild(node, i)).getUserObject();
			if (compUserObj.equals(childNode.getUserObject())) {
				return (SortedTreeNode) model.getChild(node, i);
			}
		}
		node.add(childNode);
		return null;
	}

	public void sort(DefaultTreeModel model, SortedTreeNode node) {
		if (!node.isLeaf() && (chckbxmntmSortMethods.isSelected() ? true : (!node.getUserObject().toString().endsWith(".class")))) {
			node.sort();
			for (int i = 0; i < model.getChildCount(node); i++) {
				SortedTreeNode child = ((SortedTreeNode) model.getChild(node, i));
				sort(model, child);
			}
		}
	}

	private void setupTabs() {
		codeList = new JList<ListEntry>(new DefaultListModel());
		codeList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
		codeList.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					PopupMenu.showPopupInsn(e, codeList);
				}
			}
		});
		tcbList = new JList<TCBListEntry>(new DefaultListModel());
		tcbList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
		tcbList.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					JPopupMenu menu = new JPopupMenu();
					JMenuItem remove = new JMenuItem("Remove");
					remove.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							ClassNode cn = tcbList.getSelectedValue().getCn();
							MethodNode mn = tcbList.getSelectedValue().getMn();
							mn.tryCatchBlocks.remove(tcbList.getSelectedValue().getTcbn());
							decompileMethod(cn, mn);
						}
					});
					menu.add(remove);
					menu.show(tcbList, e.getX(), e.getY());
				}
			}
		});
		searchList = new JList<SearchListEntry>(new DefaultListModel());
		searchList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
		searchList.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					JPopupMenu menu = new JPopupMenu();
					JMenuItem decl = new JMenuItem("Go to declaration");
					decl.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							ClassNode cn = searchList.getSelectedValue().getCn();
							MethodNode mn = searchList.getSelectedValue().getMn();
							if (!chckbxmntmDeclarationTreeSelection.isSelected()) {
								decompileMethod(cn, mn);
							} else {
								selectTreeMethod(cn, mn);
							}
						}

					});
					menu.add(decl);
					menu.show(searchList, e.getX(), e.getY());
				}
			}
		});
		JPanel code = new JPanel();
		code.setLayout(new BorderLayout(0, 0));
		JPanel lpad = new JPanel();
		lpad.setBorder(new EmptyBorder(1, 5, 0, 5));
		lpad.setLayout(new GridLayout());
		rightDesc = new JLabel(" ");
		lpad.add(rightDesc);
		code.add(lpad, BorderLayout.NORTH);
		code.add(new JScrollPane(codeList), BorderLayout.CENTER);
		rightSide.addTab("Code", code);

		JPanel tcb = new JPanel();
		tcb.setLayout(new BorderLayout(0, 0));
		JPanel lpad3 = new JPanel();
		lpad3.setBorder(new EmptyBorder(1, 5, 0, 5));
		lpad3.setLayout(new GridLayout());
		tcbDesc = new JLabel(" ");
		lpad3.add(tcbDesc);
		tcb.add(lpad3, BorderLayout.NORTH);
		tcb.add(new JScrollPane(tcbList), BorderLayout.CENTER);
		rightSide.addTab("Try Catch Blocks", tcb);
		ffArea = new RSyntaxTextArea();
		ffArea.setSyntaxEditingStyle("text/java");
		ffArea.setCodeFoldingEnabled(true);
		ffArea.setAntiAliasingEnabled(true);
		ffArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		ffArea.setEditable(false);
		//change theme for fernflower 
		try {
			Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml"));
			theme.apply(ffArea);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		RTextScrollPane scp = new RTextScrollPane(ffArea);
		scp.setColumnHeaderView(new JLabel("Fernflower Decompiler"));
		rightSide.addTab("Decompiler", scp);

		JPanel search = new JPanel();
		search.setLayout(new BorderLayout(0, 0));
		JPanel lpad2 = new JPanel();
		lpad2.setBorder(new EmptyBorder(1, 5, 0, 5));
		lpad2.setLayout(new GridLayout());
		searchDesc = new JLabel(" ");
		lpad2.add(searchDesc);
		search.add(lpad2, BorderLayout.NORTH);
		search.add(new JScrollPane(searchList), BorderLayout.CENTER);
		rightSide.addTab("Search", search);
	}

	public RSyntaxTextArea getFernflowerArea() {
		return ffArea;
	}

	private void selectTreeMethod(ClassNode cn, MethodNode mn) {
		new Thread(() -> {
			DefaultTreeModel tm = (DefaultTreeModel) fileTree.getModel();
			selectEntry(mn, tm, (SortedTreeNode) tm.getRoot());
		}).start();
	}

	private void selectTreeClass(ClassNode cn) {
		new Thread(() -> {
			DefaultTreeModel tm = (DefaultTreeModel) fileTree.getModel();
			selectEntry(cn, tm, (SortedTreeNode) tm.getRoot());
		}).start();
	}

	private void selectEntry(MethodNode mn, DefaultTreeModel tm, SortedTreeNode node) {
		for (int i = 0; i < tm.getChildCount(node); i++) {
			SortedTreeNode child = (SortedTreeNode) tm.getChild(node, i);
			if (child.getMn() != null && child.getMn().equals(mn)) {
				fileTree.setSelectionPath(new TreePath(tm.getPathToRoot(child)));
				break;
			}
			if (!child.isLeaf()) {
				selectEntry(mn, tm, child);
			}
		}
	}

	private void selectEntry(ClassNode cn, DefaultTreeModel tm, SortedTreeNode node) {
		for (int i = 0; i < tm.getChildCount(node); i++) {
			SortedTreeNode child = (SortedTreeNode) tm.getChild(node, i);
			if (child.getCn() != null && child.getMn() == null && child.getCn().equals(cn)) {
				fileTree.setSelectionPath(new TreePath(tm.getPathToRoot(child)));
				break;
			}
			if (!child.isLeaf()) {
				selectEntry(cn, tm, child);
			}
		}
	}

	public void reloadList(MethodNode mn) {
		DefaultListModel<ListEntry> lm = (DefaultListModel<ListEntry>) codeList.getModel();
		lm.clear();
		for (AbstractInsnNode ain : mn.instructions.toArray()) {
			lm.addElement(new InsnListEntry(mn, ain));
		}
		if (chckbxmntmRefreshDecompiler.isSelected()) {
			if (chckbxmntmDecompile.isSelected()) {
				new DecompileMethodThread(mn).start();
			}
		}
	}

	public void updateFileTree() {
		SortedTreeNode s = (SortedTreeNode) fileTree.getSelectionPath().getLastPathComponent();
		ClassNode cn = s.getCn();
		MethodNode mn = s.getMn();
		if (mn != null) {
			s.setUserObject(mn.name);
			((DefaultTreeModel) fileTree.getModel()).nodeChanged(s);
			decompileMethod(cn, mn);
		} else {
			String cname = cn.name;
			if (cn.name.contains("/")) {
				String[] spl = cn.name.split("/");
				cname = spl[spl.length - 1];
			}
			s.setUserObject(cname + ".class");
			((DefaultTreeModel) fileTree.getModel()).nodeChanged(s);
			for (String key : classes.keySet()) {//update mah shit
				if (classes.get(key).equals(cn)) {
					classes.remove(key);
					break;
				}
			}
			classes.put(cn.name, cn);
			decompileClass(cn);
		}
	}

}
