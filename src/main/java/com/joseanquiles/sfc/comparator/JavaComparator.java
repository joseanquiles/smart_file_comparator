package com.joseanquiles.sfc.comparator;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class JavaComparator implements SFCComparator {

	@Override
	public void setParameters(Map<String, String> params) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> run(List<String> leftLines, List<String> rightLines) {
		CompilationUnit cu = null;
		try {
			cu = StaticJavaParser.parse("class A { public int get() { return 1; }");
		} catch (Exception x) {
			// handle parse exceptions here.
		}
		return null;
	}

	public static void main(String[] args) {
		CompilationUnit cu = null;
		CompilationUnit cu2 = null;
		try {
			cu = StaticJavaParser.parse("class A { public int get() { return 1; } public void set() { return; } }");
			cu2 = StaticJavaParser.parse("class A { public void set() { return; } public int get() { return 1; } }");
			System.out.println(cu.equals(cu2));
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	
}

class MethodVisitor extends VoidVisitorAdapter {
	public void visit(MethodDeclaration n, Object arg) {
		// extract method information here.
		// put in to hashmap
	}
}
