package me.noverify.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.Manifest;

import org.jetbrains.java.decompiler.main.decompiler.BaseDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;


/**
 * A FernFlower wrapper with all the options (except 2)
 *
 * @author Konloch
 * @author WaterWolf
 */
public class FernflowerDecompiler {
	/*
	 * @author noverify
	 */
	public static String decompileMethod(MethodNode mn) {
		ClassNode cn = new ClassNode(Opcodes.ASM5);
		cn.access = 1;
		cn.name = mn.owner;
		cn.version = 49;
		cn.methods.add(mn);
		return decompile(cn);
	}

	public static String decompile(ClassNode cn) { //cn only contains method to decompile
		try {
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			cn.accept(cw);
			final byte[] bytesToUse = cw.toByteArray();

			Map<String, Object> options = new HashMap<>();
			options.put("hdc", "0");
			final AtomicReference<String> result = new AtomicReference<String>();
			result.set(null);

			BaseDecompiler baseDecompiler = new BaseDecompiler(new IBytecodeProvider() {
				@Override
				public byte[] getBytecode(String s, String s1) throws IOException {
					byte[] clone = new byte[bytesToUse.length];
					System.arraycopy(bytesToUse, 0, clone, 0, bytesToUse.length);
					return clone;
				}
			}, new IResultSaver() {
				@Override
				public void saveFolder(String s) {

				}

				@Override
				public void copyFile(String s, String s1, String s2) {

				}

				@Override
				public void saveClassFile(String s, String s1, String s2, String s3, int[] ints) {
					result.set(s3);
				}

				@Override
				public void createArchive(String s, String s1, Manifest manifest) {

				}

				@Override
				public void saveDirEntry(String s, String s1, String s2) {

				}

				@Override
				public void copyEntry(String s, String s1, String s2, String s3) {

				}

				@Override
				public void saveClassEntry(String s, String s1, String s2, String s3, String s4) {
				}

				@Override
				public void closeArchive(String s, String s1) {

				}
			}, options, new PrintStreamLogger(System.out));

			baseDecompiler.addSpace(new File(cn.name.hashCode() + ".class"), true); //no tricks here
			baseDecompiler.decompileContext();
			while (true) {
				if (result.get() != null) {
					break;
				}
			}
			return result.get();
		} catch (Exception e) {
			return e.toString();
		}
	}
}
