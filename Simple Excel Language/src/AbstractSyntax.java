import java.util.*;

class Program {
	Declarations ds;
	Block b;

	Program(Declarations d_, Block b_) {
		ds = d_;
		b = b_;
	}
}

class Expressions extends ArrayList<Expression> {
	int x, y;

	Expressions() {
		x = -1;
		y = -1;
	}

	Expressions(int x_) {
		x = x_;
		y = -1;
	}

	Expressions(int x_, int y_) {
		x = x_;
		y = y_;
	}

	public void set(int x_, int y_) {
		x = x_;
		y = y_;
	}

	public String coding(TypeMap tm) {
		String code = "";
		if (x == -1) {
			code += get(0).coding(tm);
		} else if (y == -1) {
			code += "{ ";
			for (int i = 0; i < x; i++) {
				code += get(i).coding(tm);
				if (i != size() - 1) {
					code += ", ";
				}
			}
			code += " }";
		} else {
			code += "{\n";
			for (int i = 0; i < x; i++) {
				code += "{ ";
				for (int j = 0; j < y; j++) {
					code += get(i * y + j).coding(tm);
					if (j != y - 1)
						code += ", ";
				}
				code += " }";
				if (i != x - 1)
					code += ",\n";
			}
			code += "\n}";
		}
		return code;
	}
}

class Declarations extends ArrayList<Declaration> {
	public String coding(TypeMap tm) {
		String code = "";
		for (int i = 0; i < size(); i++) {
			code += get(i).coding(tm);
		}
		return code;
	}
}

class Declaration {
	Expressions ex = null;
	Variable v = null;
	Type t = null;
	int x = -1;
	int y = -1;

	Declaration(Variable var, Type type, int size1, int size2) {
		v = var;
		t = type;
		x = size1;
		y = size2;
	}

	Declaration(Variable var, Type type, Expressions ex_, int size1, int size2) {
		v = var;
		t = type;
		ex = ex_;
		x = size1;
		y = size2;
	}

	public String coding(TypeMap tm) {
		if (ex == null) {
			if (x == -1)
				return t.toString() + " " + v.toString() + ";\n";
			else if (y == -1)
				return t.toString() + " " + v.toString() + "[] = new " + t.toString() + "[" + x + "];\n";
			else
				return t.toString() + " " + v.toString() + "[][] = new " + t.toString() + "[" + x + "][" + y + "];\n";
		} else {
			ex.set(x, y);
			if (x == -1)
				return t.toString() + " " + v.toString() + " = " + ex.coding(tm) + ";\n";
			else if (y == -1)
				return t.toString() + " " + v.toString() + "[] = " + ex.coding(tm) + ";\n";
			else
				return t.toString() + " " + v.toString() + "[][] = " + ex.coding(tm) + ";\n";
		}
	}
}

class Type {
	// Type = int | bool | char | float
	final static Type INT = new Type("int");
	final static Type BOOL = new Type("boolean");
	final static Type CHAR = new Type("char");
	final static Type FLOAT = new Type("float");
	final static Type STRING = new Type("String");
	// final static Type UNDEFINED = new Type("undef");

	private String id;

	private Type(String t) {
		id = t;
	}

	public String toString() {
		return id;
	}
}

abstract class Statement {
	public String coding(TypeMap tm) {
		return null;
	}
}

class Skip extends Statement {
	public String coding(TypeMap tm) {
		return ";\n";
	}
}

class Block extends Statement {
	public ArrayList<Statement> list = new ArrayList<Statement>();

	public String coding_(TypeMap tm) {
		String code = "\n";
		for (int i = 0; i < list.size(); i++) {
			code += list.get(i).coding(tm);
		}
		code += "\n";
		return code;
	}

	public String coding(TypeMap tm) {
		String code = "{\n";
		for (int i = 0; i < list.size(); i++) {
			code += list.get(i).coding(tm);
		}
		code += "}\n";
		return code;
	}
}

class Assignment extends Statement {
	Variable v;
	Expression e;
	String x;
	String y;

	Assignment(Variable v_, Expression e_, String x_, String y_) {
		v = v_;
		e = e_;
		x = x_;
		y = y_;
	}

	public String coding(TypeMap tm) {
		if (x == "-1")
			return v.coding(tm) + " = " + e.coding(tm) + ";\n";
		else if (y == "-1")
			return v.coding(tm) + "[" + x + "] = " + e.coding(tm) + ";\n";
		else
			return v.coding(tm) + "[" + x + "][" + y + "] = " + e.coding(tm) + ";\n";
	}
}

class Conditional extends Statement {
	Expression e;
	Statement s, es;

	Conditional(Expression e_, Statement s_) {
		e = e_;
		s = s_;
		es = new Skip();
	}

	Conditional(Expression e_, Statement s_, Statement es_) {
		e = e_;
		s = s_;
		es = es_;
	}

	public String coding(TypeMap tm) {
		if (es instanceof Skip)
			return "if(" + e.coding(tm) + ")\n" + s.coding(tm) + "\n";
		else
			return "if(" + e.coding(tm) + ")\n" + s.coding(tm) + "\nelse " + es.coding(tm) + "\n";
	}
}

class Loop extends Statement {
	Expression e;
	Statement s;

	Loop(Expression e_, Statement s_) {
		e = e_;
		s = s_;
	}

	public String coding(TypeMap tm) {
		if (e.getType(tm).equals("int") || e.getType(tm).equals("float"))
			return "for(int E = 0; E < " + e.toString() + "; E++)\n" + s.coding(tm);
		else
			return "while(" + e.coding(tm) + ")\n" + s.coding(tm);
	}
}

class Decl extends Statement {
	int x, y;
	Variable v;
	Expressions ex;

	Decl(Variable v_, Expressions ex_) {
		v = v_;
		ex = ex_;
	}

	Decl() {
		v = null;
		ex = null;
		x = -1;
		y = -1;
	}

	public String coding(TypeMap tm) {
		if (v == null) {
			return "";
		} else if (x == -1) {
			return v.coding(tm) + " = " + ex.coding(tm) + ";\n";
		} else if (y == -1) {
			return v.coding(tm) + "[] = " + ex.coding(tm) + ";\n";
		} else
			return v.coding(tm) + "[][] = " + ex.coding(tm) + ";\n";
	}
}

abstract class Expression {
	// Expression = Variable | Value | Binary | Unary
	public String coding(TypeMap tm) {
		return null;
	}

	public String getType(TypeMap tm) {
		return null;
	}
}

class Expression_Paren extends Expression {
	Expression e;

	Expression_Paren(Expression e_) {
		e = e_;
	}

	public String coding(TypeMap tm) {
		return "(" + e.coding(tm) + ")";
	}

	public String getType(TypeMap tm) {
		return e.getType(tm);
	}
}

class Variable extends Expression {
	public String id = null;
	public String x = "-1";
	public String y = "-1";

	Variable() {
	}
	
	Variable(String s) {
		id = s;
	}

	Variable(String s, String x_) {
		id = s;
		x = x_;
	}

	Variable(String s, String x_, String y_) {
		id = s;
		x = x_;
		y = y_;
	}

	public String toString() {
		if (x == "-1")
			return id;
		else if (y == "-1")
			return id + "[" + x + "]";
		else
			return id + "[" + x + "][" + y + "]";
	}

	public boolean equals(Object obj) {
		String s = ((Variable) obj).id;
		return id.equals(s); // case-sensitive identifiers
	}

	public int hashCode() {
		return id.hashCode();
	}

	public String coding(TypeMap tm) {
		return toString();
	}

	public String getType(TypeMap tm) {
		return tm.get(this).toString();
	}
}

abstract class Value extends Expression {
	// Value = IntValue | BoolValue |
	// CharValue | FloatValue
	protected Type type;
	protected boolean undef = true;

	int intValue() {
		assert false : "should never reach here";
		return 0;
	}

	boolean boolValue() {
		assert false : "should never reach here";
		return false;
	}

	char charValue() {
		assert false : "should never reach here";
		return ' ';
	}

	float floatValue() {
		assert false : "should never reach here";
		return 0.0f;
	}

	String StirngValue() {
		assert false : "should never reach here";
		return "";
	}

	boolean isUndef() {
		return undef;
	}

	Type type() {
		return type;
	}

	static Value mkValue(Type type) {
		if (type == Type.INT)
			return new IntValue();
		if (type == Type.BOOL)
			return new BoolValue();
		if (type == Type.CHAR)
			return new CharValue();
		if (type == Type.FLOAT)
			return new FloatValue();
		if (type == Type.STRING)
			return new StringValue();
		throw new IllegalArgumentException("Illegal type in mkValue");
	}

	public String getType(TypeMap tm) {
		if (type == Type.INT)
			return "int";
		if (type == Type.BOOL)
			return "boolean";
		if (type == Type.CHAR)
			return "char";
		if (type == Type.FLOAT)
			return "float";
		if (type == Type.STRING)
			return "String";
		return null;
	}
}

class IntValue extends Value {
	private int value = 0;

	IntValue() {
		type = Type.INT;
	}

	IntValue(int v) {
		this();
		value = v;
		undef = false;
	}

	int intValue() {
		assert !undef : "reference to undefined int value";
		return value;
	}

	public String toString() {
		if (undef)
			return "undef";
		return "" + value;
	}

	public String coding(TypeMap tm) {
		return toString();
	}
}

class BoolValue extends Value {
	private boolean value = false;

	BoolValue() {
		type = Type.BOOL;
	}

	BoolValue(boolean v) {
		this();
		value = v;
		undef = false;
	}

	boolean boolValue() {
		assert !undef : "reference to undefined bool value";
		return value;
	}

	int intValue() {
		assert !undef : "reference to undefined bool value";
		return value ? 1 : 0;
	}

	public String toString() {
		if (undef)
			return "undef";
		return "" + value;
	}

	public String coding(TypeMap tm) {
		return toString();
	}
}

class CharValue extends Value {
	private char value = ' ';

	CharValue() {
		type = Type.CHAR;
	}

	CharValue(char v) {
		this();
		value = v;
		undef = false;
	}

	char charValue() {
		assert !undef : "reference to undefined char value";
		return value;
	}

	public String toString() {
		if (undef)
			return "undef";
		return "" + value;
	}

	public String coding(TypeMap tm) {
		return "'" + toString() + "'";
	}
}

class FloatValue extends Value {
	private float value = 0;

	FloatValue() {
		type = Type.FLOAT;
	}

	FloatValue(float v) {
		this();
		value = v;
		undef = false;
	}

	float floatValue() {
		assert !undef : "reference to undefined float value";
		return value;
	}

	public String toString() {
		if (undef)
			return "undef";
		return "" + value;
	}

	public String coding(TypeMap tm) {
		return toString() + "f";
	}
}

class StringValue extends Value {
	private String value = "";

	StringValue() {
		type = Type.STRING;
	}

	StringValue(String v) {
		this();
		value = v;
		undef = false;
	}

	String stringValue() {
		assert !undef : "reference to undefined String value";
		return value;
	}

	public String toString() {
		if (undef)
			return "undef";
		return "" + value;
	}

	public String coding(TypeMap tm) {
		return "\"" + toString() + "\"";
	}
}

class Binary extends Expression {
	// Binary = Operator op; Expression term1, term2
	Operator o;
	Expression e1, e2;

	Binary(Operator o_, Expression e1_, Expression e2_) {
		o = o_;
		e1 = e1_;
		e2 = e2_;
	}

	public String coding(TypeMap tm) {
		if (o.toString().equals("="))
			return e1.coding(tm) + " == " + e2.coding(tm);
		else if (o.toString().equals("|"))
			return e1.coding(tm) + " || " + e2.coding(tm);
		else if (o.toString().equals("&"))
			return e1.coding(tm) + " && " + e2.coding(tm);
		else if (o.toString().equals("^"))
			return "(int)Math.pow(" + e1.coding(tm) + ", " + e2.coding(tm) + ")";
		else if (o.toString().equals("^/"))
			return "(int)Math.pow(" + e1.coding(tm) + ", 1 / " + e2.coding(tm) + ")";
		else
			return e1.coding(tm) + " " + o + " " + e2.coding(tm);
	}

	public String getType(TypeMap tm) {
		if (o.ArithmeticOp())
			return "float";
		if (o.RelationalOp() || o.BooleanOp())
			return "boolean";
		return null;
	}
}

class Unary extends Expression {
	Operator o;
	Expression e;

	Unary(Operator o_, Expression e_) {
		o = o_;
		e = e_;
	}

	public String coding(TypeMap tm) {
		return o + e.coding(tm);
	}

	public String getType(TypeMap tm) {
		if (o.NotOp())
			return "bool";
		else if (o.NegateOp()) {
			Variable v = (Variable) e;
			if (tm.containsKey(v))
				return tm.get(v).toString();
		} else if (o.intOp())
			return "int";
		else if (o.floatOp())
			return "float";
		else if (o.charOp())
			return "char";
		return null;
	}
}

class Function_Sum extends Statement {
	Variable v;
	Expression e;
	String a = "-1", b = "-1", c = "-1", d = "-1";

	Function_Sum(Variable v_, Expression e_, String a_, String b_) {
		v = v_;
		e = e_;
		a = a_;
		b = b_;
	}

	Function_Sum(Variable v_, Expression e_, String a_, String b_, String c_, String d_) {
		v = v_;
		e = e_;
		a = a_;
		b = b_;
		c = c_;
		d = d_;
	}

	public String coding(TypeMap tm) {
		String temp;
		if (c == "-1") {
			if (Integer.parseInt(a) > Integer.parseInt(b)) {
				temp = a;
				a = b;
				b = temp;
			}
			return "TEMP = 0;\r\n" + "for(int I = " + a + "; I <= " + b + "; I++){\r\n" + "TEMP += " + e.coding(tm)
					+ "[I];\r\n" + "}\r\n" + v.coding(tm) + " = (int)TEMP;\r\n";
		} else {
			if (Integer.parseInt(a) > Integer.parseInt(c)) {
				temp = a;
				a = c;
				c = temp;
			}
			if (Integer.parseInt(b) > Integer.parseInt(d)) {
				temp = b;
				b = d;
				d = temp;
			}
			return "TEMP = 0;\r\n" + "for(int I = " + a + "; I <= " + c + "; I++){\r\n" + "for(int J = " + b + "; J <= "
					+ d + "; J++){\r\n" + "TEMP += " + e.coding(tm) + "[I][J];\r\n" + "}\r\n" + "}\r\n" + v.coding(tm)
					+ " = (int)TEMP;\r\n";
		}
	}
}

class Function_Avg extends Statement {
	Variable v;
	Expression e;
	String a = "-1", b = "-1", c = "-1", d = "-1";

	Function_Avg(Variable v_, Expression e_, String a_, String b_) {
		v = v_;
		e = e_;
		a = a_;
		b = b_;
	}

	Function_Avg(Variable v_, Expression e_, String a_, String b_, String c_, String d_) {
		v = v_;
		e = e_;
		a = a_;
		b = b_;
		c = c_;
		d = d_;
	}

	public String coding(TypeMap tm) {
		String temp;
		if (c == "-1") {
			if (Integer.parseInt(a) > Integer.parseInt(b)) {
				temp = a;
				a = b;
				b = temp;
			}
			return "TEMP = 0;\r\n" + "for(int I = " + a + "; I <= " + b + "; I++){\r\n" + "TEMP += " + e.coding(tm)
					+ "[I];\r\n" + "}\r\n" + v.coding(tm) + " = (int)TEMP / " + (Integer.parseInt(b) - Integer.parseInt(a) + 1) + ";\r\n";
		} else {
			if (Integer.parseInt(a) > Integer.parseInt(c)) {
				temp = a;
				a = c;
				c = temp;
			}
			if (Integer.parseInt(b) > Integer.parseInt(d)) {
				temp = b;
				b = d;
				d = temp;
			}
			return "TEMP = 0;\r\n" + "for(int I = " + a + "; I <= " + c + "; I++){\r\n" + "for(int J = " + b + "; J <= "
					+ d + "; J++){\r\n" + "TEMP += " + e.coding(tm) + "[I][J];\r\n" + "}\r\n" + "}\r\n" + v.coding(tm)
					+ " = (int)TEMP / " + (Integer.parseInt(c) - Integer.parseInt(a) + 1) * (Integer.parseInt(d) - Integer.parseInt(b) + 1) + ";\r\n";
		}
	}
}

class Function_Min extends Statement {
	Variable v;
	Expression e;
	String a = "-1", b = "-1", c = "-1", d = "-1";

	Function_Min(Variable v_, Expression e_, String a_, String b_) {
		v = v_;
		e = e_;
		a = a_;
		b = b_;
	}

	Function_Min(Variable v_, Expression e_, String a_, String b_, String c_, String d_) {
		v = v_;
		e = e_;
		a = a_;
		b = b_;
		c = c_;
		d = d_;
	}

	public String coding(TypeMap tm) {
		String temp;
		if (c == "-1") {
			if (Integer.parseInt(a) > Integer.parseInt(b)) {
				temp = a;
				a = b;
				b = temp;
			}
			return "TEMP = " + e.coding(tm) + "[" + a + "];\r\n" + "for(int I = " + a + "; I <= " + b + "; I++){\r\n"
					+ "		if(TEMP > " + e.coding(tm) + "[I])\r\n" + "			TEMP = " + e.coding(tm) + "[I];\r\n"
					+ "}\r\n" + v.coding(tm) + " = (int)TEMP;\r\n";
		} else {
			if (Integer.parseInt(a) > Integer.parseInt(c)) {
				temp = a;
				a = c;
				c = temp;
			}
			if (Integer.parseInt(b) > Integer.parseInt(d)) {
				temp = b;
				b = d;
				d = temp;
			}
			return "TEMP = " + e.coding(tm) + "[" + a + "][" + b + "];\r\n" + "for(int I = " + a + "; I <= " + c
					+ "; I++){\r\n" + "for(int J = " + b + "; J <= " + d + "; J++){\r\n" + "		if(TEMP > "
					+ e.coding(tm) + "[I][J])\r\n" + "			TEMP = " + e.coding(tm) + "[I][J];\r\n" + "}\r\n"
					+ "}\r\n" + v.coding(tm) + " = (int)TEMP;\r\n";
		}
	}
}

class Function_Max extends Statement {
	Variable v;
	Expression e;
	String a = "-1", b = "-1", c = "-1", d = "-1";

	Function_Max(Variable v_, Expression e_, String a_, String b_) {
		v = v_;
		e = e_;
		a = a_;
		b = b_;
	}

	Function_Max(Variable v_, Expression e_, String a_, String b_, String c_, String d_) {
		v = v_;
		e = e_;
		a = a_;
		b = b_;
		c = c_;
		d = d_;
	}

	public String coding(TypeMap tm) {
		String temp;
		if (c == "-1") {
			if (Integer.parseInt(a) > Integer.parseInt(b)) {
				temp = a;
				a = b;
				b = temp;
			}
			return "TEMP = " + e.coding(tm) + "[" + a + "];\r\n" + "for(int I = " + a + "; I <= " + b + "; I++){\r\n"
					+ "		if(TEMP < " + e.coding(tm) + "[I])\r\n" + "			TEMP = " + e.coding(tm) + "[I];\r\n"
					+ "}\r\n" + v.coding(tm) + " = (int)TEMP;\r\n";
		} else {
			if (Integer.parseInt(a) > Integer.parseInt(c)) {
				temp = a;
				a = c;
				c = temp;
			}
			if (Integer.parseInt(b) > Integer.parseInt(d)) {
				temp = b;
				b = d;
				d = temp;
			}
			return "TEMP = " + e.coding(tm) + "[" + a + "][" + b + "];\r\n" + "for(int I = " + a + "; I <= " + c
					+ "; I++){\r\n" + "for(int J = " + b + "; J <= " + d + "; J++){\r\n" + "		if(TEMP < "
					+ e.coding(tm) + "[I][J])\r\n" + "			TEMP = " + e.coding(tm) + "[I][J];\r\n" + "}\r\n"
					+ "}\r\n" + v.coding(tm) + " = (int)TEMP;\r\n";
		}
	}
}

abstract class Execution extends Statement {
}

class Execution_Show extends Execution {
	Expression e;
	int a = -1, b = -1, c = -1, d = -1;

	Execution_Show(Expression e_) {
		e = e_;
	}

	Execution_Show(Expression e_, int a_, int b_) {
		e = e_;
		a = a_;
		b = b_;
	}

	Execution_Show(Expression e_, int a_, int b_, int c_, int d_) {
		e = e_;
		a = a_;
		b = b_;
		c = c_;
		d = d_;
	}

	public String coding(TypeMap tm) {
		int temp;
		if (a == -1)
			return "System.out.println(" + e.coding(tm) + ");\n";
		else if (c == -1) {
			if (a > b) {
				temp = a;
				a = b;
				b = temp;
			}
			return "for(int I = " + a + "; I <= " + b + "; I++)\r\n" + "System.out.println(" + e.coding(tm)
					+ "[I]);\r\n";
		} else {
			if (a > c) {
				temp = a;
				a = c;
				c = temp;
			}
			if (b > d) {
				temp = b;
				b = d;
				d = temp;
			}
			return "for(int I = " + a + "; I <= " + c + "; I++){\r\n" + "for(int J = " + b + "; J <= " + d
					+ "; J++){\r\n" + "System.out.print(" + e.coding(tm) + "[I][J] + \" \t\");\r\n"
					+ "} System.out.println();\r\n" + "}\r\n";
		}
	}
}

class Execution_Get extends Execution {
	Variable v;
	int a = -1, b = -1, c = -1, d = -1;

	Execution_Get(Variable v_) {
		v = v_;
	}

	Execution_Get(Variable v_, int a_, int b_) {
		v = v_;
		a = a_;
		b = b_;
	}

	Execution_Get(Variable v_, int a_, int b_, int c_, int d_) {
		v = v_;
		a = a_;
		b = b_;
		c = c_;
		d = d_;
	}

	public String coding(TypeMap tm) {
		if (a == -1) {
			if (tm.get(v).equals(Type.INT))
				return v.coding(tm) + " = scan.nextInt();\n";
			else if (tm.get(v).equals(Type.FLOAT))
				return v.coding(tm) + " = scan.nextFloat();\n";
			else if (tm.get(v).equals(Type.CHAR))
				return v.coding(tm) + " = scan.nextChar();\n";
			else if (tm.get(v).equals(Type.BOOL))
				return v.coding(tm) + " = scan.nextBoolean();\n";
			else if (tm.get(v).equals(Type.STRING))
				return v.coding(tm) + " = scan.nextln();\n";
		}
		if (c == -1) {
			if (tm.get(v).equals(Type.INT))
				return "for(int Q = " + a + "; Q <= " + b + "; Q++)\r\n" + v.coding(tm) + "[Q] = scan.nextInt();\r\n";
			else if (tm.get(v).equals(Type.FLOAT))
				return "for(int Q = " + a + "; Q <= " + b + "; Q++)\r\n" + v.coding(tm) + "[Q] = scan.nextFloat();\r\n";
			else if (tm.get(v).equals(Type.CHAR))
				return "for(int Q = " + a + "; Q <= " + b + "; Q++)\r\n" + v.coding(tm) + "[Q] = scan.nextChar();\r\n";
			else if (tm.get(v).equals(Type.BOOL))
				return "for(int Q = " + a + "; Q <= " + b + "; Q++)\r\n" + v.coding(tm)
						+ "[Q] = scan.nextBoolean();\r\n";
			else if (tm.get(v).equals(Type.STRING))
				return "for(int Q = " + a + "; Q <= " + b + "; Q++)\r\n" + v.coding(tm) + "[Q] = scan.nextln();\r\n";
		} else {
			if (tm.get(v).equals(Type.INT))
				return "for(int Q = " + a + "; Q <= " + c + "; Q++){\r\n" + "for(int W = " + b + "; W <= " + d
						+ "; W++){\r\n" + v.coding(tm) + "[Q][W] = scan.nextInt();\r\n" + "}\r\n" + "}\r\n";
			else if (tm.get(v).equals(Type.FLOAT))
				return "for(int Q = " + a + "; Q <= " + c + "; Q++){\r\n" + "for(int W = " + b + "; W <= " + d
						+ "; W++){\r\n" + v.coding(tm) + "[Q][W] = scan.nextFloat();\r\n" + "}\r\n" + "}\r\n";
			else if (tm.get(v).equals(Type.CHAR))
				return "for(int Q = " + a + "; Q <= " + c + "; Q++){\r\n" + "for(int W = " + b + "; W <= " + d
						+ "; W++){\r\n" + v.coding(tm) + "[Q][W] = scan.nextChar();\r\n" + "}\r\n" + "}\r\n";
			else if (tm.get(v).equals(Type.BOOL))
				return "for(int Q = " + a + "; Q <= " + c + "; Q++){\r\n" + "for(int W = " + b + "; W <= " + d
						+ "; W++){\r\n" + v.coding(tm) + "[Q][W] = scan.nextBoolean();\r\n" + "}\r\n" + "}\r\n";
			else if (tm.get(v).equals(Type.STRING))
				return "for(int Q = " + a + "; Q <= " + c + "; Q++){\r\n" + "for(int W = " + b + "; W <= " + d
						+ "; W++){\r\n" + v.coding(tm) + "[Q][W] = scan.nextln();\r\n" + "}\r\n" + "}\r\n";
		}
		return null;
	}
}

class Execution_Sort extends Execution {
	Variable v;
	String i = "-1";

	public Execution_Sort(Variable v_) {
		v = v_;
	}

	public Execution_Sort(Variable v_, String i_) {
		v = v_;
		i = i_;
	}

	public String coding(TypeMap tm) {
		if (i == "-1")
			return "Arrays.sort(" + v.id + ");\n";
		else
			return "Arrays.sort(" + v.id + "[" + i + "]);\n";
	}
}

class Operator {
	// Operator = BooleanOp | RelationalOp | ArithmeticOp | UnaryOp
	// BooleanOp = & | |
	final static String AND = "&";
	final static String OR = "|";
	// RelationalOp = < | <= | == | != | >= | >
	final static String LT = "<";
	final static String LE = "<=";
	final static String EQ = "=";
	final static String NE = "!=";
	final static String GT = ">";
	final static String GE = ">=";
	// ArithmeticOp = + | - | * | /
	final static String PLUS = "+";
	final static String MINUS = "-";
	final static String TIMES = "*";
	final static String DIV = "/";
	// UnaryOp = !
	final static String NOT = "!";
	final static String NEG = "-";
	// CastOp = int | float | char
	final static String INT = "int";
	final static String FLOAT = "float";
	final static String CHAR = "char";
	// Typed Operators
	// RelationalOp = < | <= | == | != | >= | >
	final static String INT_LT = "INT<";
	final static String INT_LE = "INT<=";
	final static String INT_EQ = "INT=";
	final static String INT_NE = "INT!=";
	final static String INT_GT = "INT>";
	final static String INT_GE = "INT>=";
	// ArithmeticOp = + | - | * | /
	final static String INT_PLUS = "INT+";
	final static String INT_MINUS = "INT-";
	final static String INT_TIMES = "INT*";
	final static String INT_DIV = "INT/";
	// UnaryOp = !
	final static String INT_NEG = "-";
	// RelationalOp = < | <= | == | != | >= | >
	final static String FLOAT_LT = "FLOAT<";
	final static String FLOAT_LE = "FLOAT<=";
	final static String FLOAT_EQ = "FLOAT=";
	final static String FLOAT_NE = "FLOAT!=";
	final static String FLOAT_GT = "FLOAT>";
	final static String FLOAT_GE = "FLOAT>=";
	// ArithmeticOp = + | - | * | /
	final static String FLOAT_PLUS = "FLOAT+";
	final static String FLOAT_MINUS = "FLOAT-";
	final static String FLOAT_TIMES = "FLOAT*";
	final static String FLOAT_DIV = "FLOAT/";
	// UnaryOp = !
	final static String FLOAT_NEG = "-";
	// RelationalOp = < | <= | == | != | >= | >
	final static String CHAR_LT = "CHAR<";
	final static String CHAR_LE = "CHAR<=";
	final static String CHAR_EQ = "CHAR=";
	final static String CHAR_NE = "CHAR!=";
	final static String CHAR_GT = "CHAR>";
	final static String CHAR_GE = "CHAR>=";
	// RelationalOp = < | <= | == | != | >= | >
	final static String BOOL_LT = "BOOL<";
	final static String BOOL_LE = "BOOL<=";
	final static String BOOL_EQ = "BOOL=";
	final static String BOOL_NE = "BOOL!=";
	final static String BOOL_GT = "BOOL>";
	final static String BOOL_GE = "BOOL>=";
	// Type specific cast
	final static String I2F = "I2F";
	final static String F2I = "F2I";
	final static String C2I = "C2I";
	final static String I2C = "I2C";

	// Function
	final static String SUM = "sum";
	final static String AVG = "avg";
	final static String SHOW = "show";
	final static String Get = "get";

	String val;

	Operator(String s) {
		val = s;
	}

	public String toString() {
		return val;
	}

	public boolean equals(Object obj) {
		return val.equals(obj);
	}

	boolean BooleanOp() {
		return val.equals(AND) || val.equals(OR);
	}

	boolean RelationalOp() {
		return val.equals(LT) || val.equals(LE) || val.equals(EQ) || val.equals(NE) || val.equals(GT) || val.equals(GE);
	}

	boolean ArithmeticOp() {
		return val.equals(PLUS) || val.equals(MINUS) || val.equals(TIMES) || val.equals(DIV);
	}

	boolean NotOp() {
		return val.equals(NOT);
	}

	boolean NegateOp() {
		return val.equals(NEG);
	}

	boolean intOp() {
		return val.equals(INT);
	}

	boolean floatOp() {
		return val.equals(FLOAT);
	}

	boolean charOp() {
		return val.equals(CHAR);
	}

	final static String intMap[][] = { { PLUS, INT_PLUS }, { MINUS, INT_MINUS }, { TIMES, INT_TIMES }, { DIV, INT_DIV },
			{ EQ, INT_EQ }, { NE, INT_NE }, { LT, INT_LT }, { LE, INT_LE }, { GT, INT_GT }, { GE, INT_GE },
			{ NEG, INT_NEG }, { FLOAT, I2F }, { CHAR, I2C } };

	final static String floatMap[][] = { { PLUS, FLOAT_PLUS }, { MINUS, FLOAT_MINUS }, { TIMES, FLOAT_TIMES },
			{ DIV, FLOAT_DIV }, { EQ, FLOAT_EQ }, { NE, FLOAT_NE }, { LT, FLOAT_LT }, { LE, FLOAT_LE },
			{ GT, FLOAT_GT }, { GE, FLOAT_GE }, { NEG, FLOAT_NEG }, { INT, F2I } };

	final static String charMap[][] = { { EQ, CHAR_EQ }, { NE, CHAR_NE }, { LT, CHAR_LT }, { LE, CHAR_LE },
			{ GT, CHAR_GT }, { GE, CHAR_GE }, { INT, C2I } };

	final static String boolMap[][] = { { EQ, BOOL_EQ }, { NE, BOOL_NE }, { LT, BOOL_LT }, { LE, BOOL_LE },
			{ GT, BOOL_GT }, { GE, BOOL_GE }, };

	final static private Operator map(String[][] tmap, String op) {
		for (int i = 0; i < tmap.length; i++)
			if (tmap[i][0].equals(op))
				return new Operator(tmap[i][1]);
		assert false : "should never reach here";
		return null;
	}

	final static public Operator intMap(String op) {
		return map(intMap, op);
	}

	final static public Operator floatMap(String op) {
		return map(floatMap, op);
	}

	final static public Operator charMap(String op) {
		return map(charMap, op);
	}

	final static public Operator boolMap(String op) {
		return map(boolMap, op);
	}
}