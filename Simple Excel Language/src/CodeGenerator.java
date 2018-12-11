import java.io.File;
import java.io.FileWriter;
import java.util.*;

import java.io.IOException;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class CodeGenerator {
	
	public static TypeMap typing(Declarations d) {
		TypeMap map = new TypeMap();
		map.put(new Variable("sheet","100","100"), Type.INT);
		for (Declaration di : d)
			map.put(di.v, di.t);
		return map;
	}

	public static void check(boolean test, String msg) {
		if (test)
			return;
		System.err.println(msg);
		System.exit(1);
	}

	public static void V(Declarations d) {
		for (int i = 0; i < d.size() - 1; i++)
			for (int j = i + 1; j < d.size(); j++) {
				Declaration di = d.get(i);
				Declaration dj = d.get(j);
				check(!(di.v.equals(dj.v)), "duplicate declaration: " + dj.v);
			}
	}

	public static void V(Program p) {
		V(p.ds);
		V(p.b, typing(p.ds));
	}

	public static Type typeOf(Expression e, TypeMap tm) { // tm = TypeMap which is a tuple (v, k)
		if (e instanceof Value)
			return ((Value) e).type;
		if (e instanceof Variable) {
			Variable v = (Variable) e; // containsKey is a hashMap method which takes an object and returns a bool
			//if(tm.containsKey(v))
			check(tm.containsKey(v), "undefined variable: " + v);
			return (Type) tm.get(v);
		}
		if (e instanceof Binary) {
			Binary b = (Binary) e;
			if (b.o.ArithmeticOp())
				if (typeOf(b.e1, tm) == Type.FLOAT)
					return (Type.FLOAT);
				else
					return (Type.INT);
			if (b.o.RelationalOp() || b.o.BooleanOp())
				return (Type.BOOL);
		}
		if (e instanceof Unary) {
			Unary u = (Unary) e;
			if (u.o.NotOp())
				return (Type.BOOL);
			else if (u.o.NegateOp())
				return typeOf(u.e, tm);
			else if (u.o.intOp())
				return (Type.INT);
			else if (u.o.floatOp())
				return (Type.FLOAT);
			else if (u.o.charOp())
				return (Type.CHAR);
		}
		throw new IllegalArgumentException("should never reach here");
	}

	public static void V(Expression e, TypeMap tm) {
		if (e instanceof Value)
			return;
		if (e instanceof Variable) {
			Variable v = (Variable) e;
			check(tm.containsKey(v), "undeclared variable: " + v);
			return;
		}
		if (e instanceof Binary) {
			Binary b = (Binary) e;
			Type typ1 = typeOf(b.e1, tm);
			Type typ2 = typeOf(b.e2, tm);
			V(b.e1, tm);
			V(b.e2, tm);
			if (b.o.ArithmeticOp())
				check(typ1 == typ2 && (typ1 == Type.INT || typ1 == Type.FLOAT), "type error for " + b.o);
			else if (b.o.RelationalOp())
				check(typ1 == typ2, "type error for " + b.o);
			else if (b.o.BooleanOp())
				check(typ1 == Type.BOOL && typ2 == Type.BOOL, b.o + ": non-bool operand");
			else
				throw new IllegalArgumentException("should never reach here BinaryOp error");
			return;
		}

		// begin
		if (e instanceof Unary) {
			Unary u = (Unary) e;
			Type type = typeOf(u.e, tm); // start here
			V(u.e, tm);
			if (u.o.NotOp()) {
				check((type == Type.BOOL), "type error for NotOp " + u.o);
			} else if (u.o.NegateOp()) {
				check((type == (Type.INT) || type == (Type.FLOAT)), "type error for NegateOp " + u.o);
			} else {
				throw new IllegalArgumentException("should never reach here UnaryOp error");
			}
			return;
		}
		// end
		throw new IllegalArgumentException("should never reach here");
	}
	
	public static void V(Statement s, TypeMap tm) {
		if (s == null)
			throw new IllegalArgumentException("AST error: null statement");
		else if (s instanceof Skip)
			return;
		else if (s instanceof Assignment) {
			Assignment a = (Assignment) s;
			if(!tm.containsKey(a.v)) {
				
			}
			check(tm.containsKey(a.v), " undefined target in assignment: " + a.v);
			V(a.e, tm);

			Type ttype = (Type)tm.get(a.v);
			Type srctype = typeOf(a.e, tm); // scrtype = source type; sources are Expressions or Statements which are
											// not in the TypeMap
			
			if (ttype != srctype) {
				if (ttype == Type.FLOAT)
					check(srctype == Type.INT, "mixed mode assignment to " + a.v);
				else if (ttype == Type.INT)
					check(srctype == Type.FLOAT, "mixed mode assignment to " + a.v);
				else if (ttype == Type.INT)
					check(srctype == Type.CHAR, "mixed mode assignment to " + a.v);
				else
					check(false, "mixed mode assignment to " + a.v + "  " + a.e.coding(tm) + "\n" + a.v.getType(tm) + " " + a.e.getType(tm));
			}
			return;
		}

		// begin
		else if (s instanceof Conditional) {
			Conditional c = (Conditional) s;
			V(c.e, tm);
			Type testtype = typeOf(c.e, tm);
			if (testtype == Type.BOOL) {
				V(c.s, tm);
				V(c.es, tm);
				return;
			} else {
				check(false, "poorly typed if in Conditional: " + c.e);
			}
		} else if (s instanceof Loop) {
			Loop l = (Loop) s;
			V(l.e, tm);
			Type testtype = typeOf(l.e, tm);
			if (testtype == Type.BOOL || testtype == Type.INT || testtype == Type.FLOAT ) {
				V(l.s, tm);
			} else {
				check(false, "poorly typed test in while Loop in Conditional: " + l.e);
			}
		} else if (s instanceof Block) {
			Block b = (Block) s;
			for (Statement i : b.list) {
				V(i, tm);
			}
		} else if(s instanceof Execution_Show) {
			Execution_Show e = (Execution_Show) s;
			V(e.e, tm);
		} else if(s instanceof Execution_Get) {
			Execution_Get e = (Execution_Get) s;
			V(e.v, tm);
		} else if(s instanceof Function_Sum) {
			Function_Sum f = (Function_Sum) s;
			V(f.v, tm);
			Type testtype = typeOf(f.e, tm);
			if(testtype == Type.INT || testtype == Type.FLOAT) {
				V(f.e, tm);
			} else {
				check(false, "poorly typed test in while Loop in Conditional: " + f.e);
			}
		} else if(s instanceof Execution_Sort) {
			Execution_Sort e = (Execution_Sort) s;
			V(e.v, tm);
		}
		else if(s instanceof Function_Avg) {
			Function_Avg f = (Function_Avg) s;
			V(f.v, tm);
			Type testtype = typeOf(f.e, tm);
			if(testtype == Type.INT || testtype == Type.FLOAT) {
				V(f.e, tm);
			} else {
				check(false, "poorly typed test in while Loop in Conditional: " + f.e);
			}
		}
		else if(s instanceof Function_Min) {
			Function_Min f = (Function_Min) s;
			V(f.v, tm);
			Type testtype = typeOf(f.e, tm);
			if(testtype == Type.INT || testtype == Type.FLOAT) {
				V(f.e, tm);
			} else {
				check(false, "poorly typed test in while Loop in Conditional: " + f.e);
			}
		}
		else if(s instanceof Function_Max) {
			Function_Max f = (Function_Max) s;
			V(f.v, tm);
			Type testtype = typeOf(f.e, tm);
			if(testtype == Type.INT || testtype == Type.FLOAT) {
				V(f.e, tm);
			} else {
				check(false, "poorly typed test in while Loop in Conditional: " + f.e);
			}
		}
		else {
			// end
			throw new IllegalArgumentException("should never reach here");
		}
	}
	
	public static void main(String args[]) throws IOException {
		Parser ps1 = new Parser(new Lexer("text.txt"));
		Parser ps2 = new Parser(new Lexer("text.txt"));
		ReadExcel test = new ReadExcel();
		test.setInputFile("C:\\Users\\NamJun\\eclipse-workspace\\Simple Excel Language\\src\\Excel_File.xls");
		
		Declarations ds = ps1.declarations();
        V(ds);
		TypeMap map = typing(ds);
        Block b = ps2.block();
        V(b, map);
		
		String head =
				"import java.util.*;\r\n" +
				"import java.io.File; \r\n" + 
				"import jxl.Workbook; \r\n" + 
				"import jxl.write.*;\r\n" +
				"import jxl.write.Number;\r\n\n" +
				"public class Simple_Excel_Language {\r\n" + 
				"	public static void main(String args[]) throws InterruptedException {\r\n" + 
				"		Scanner scan = new Scanner(System.in);\r\n" +
				"		float TEMP;\r\n\n";
		String excelRead = test.read();
		String excelWrite = "WritableWorkbook wworkbook;\r\n" + 
				"		try {\r\n" + 
				"			wworkbook = Workbook.createWorkbook(\r\n" + 
				"					new File(\"C:\\\\Users\\\\NamJun\\\\eclipse-workspace\\\\Simple Excel Language\\\\src\\\\Excel_File.xls\"));\r\n" + 
				"			WritableSheet wsheet = wworkbook.createSheet(\"Simplex_Sheet\", 0);\r\n" + 
				"			for (int O = 0; O < 100; O++) {\r\n" + 
				"				for (int P = 0; P < 100; P++) {\r\n" +
				"					if(sheet[O][P] != 0) {\r\n" + 
				"					Number label = new Number(O, P, sheet[O][P]);\r\n" + 
				"					wsheet.addCell(label);\r\n" +
				"					}\r\n" + 
				"				}\r\n" + 
				"			}\r\n" + 
				"			wworkbook.write();\r\n" + 
				"			wworkbook.close();\r\n" + 
				"		} catch (Exception e) {\r\n" + 
				"			System.out.println(e);\r\n" + 
				"		}";
        String decl = ds.coding(map);
        String block = b.coding_(map);
        String tail = "\t}\n}";		
        
        String fileName = "C:\\Users\\NamJun\\eclipse-workspace\\Simple Excel Language\\src\\Simple_Excel_Language.txt";
        try {
          File file = new File(fileName);
          FileWriter clear = new FileWriter(file);
          FileWriter fileWrite = new FileWriter(file,true);
          fileWrite.write(head + excelRead + decl + block + excelWrite + tail);
          fileWrite.flush();
          fileWrite.close();
          System.out.println("Success");
       }
       catch(Exception e){
          e.printStackTrace();
       }
	}
}