import java.util.*;
import java.io.File;
import java.io.FileWriter;

public class Parser {
	Token token;
	Lexer lexer;

	public Parser(Lexer ts) {
		lexer = ts;
		token = lexer.next();
	}

	private String match(TokenType t) {
		String value = token.value();
		if (token.type().equals(t))
			token = lexer.next();
		else
			error(t);
		return value;
	}

	private void error(TokenType tok) {
		System.err.println("Syntax error: expecting: " + tok + "; saw: " + token);
		System.exit(1);
	}

	private void error(String tok) {
		System.err.println("Syntax error: expecting: " + tok + "; saw: " + token);
		System.exit(1);
	}

	public Declarations declarations() {
		Declarations ds = new Declarations();
		while (!token.type().equals(TokenType.Eof)) {
			if (isType())
				declaration(ds);
			else
				token = lexer.next();
		}
		return ds;
	}

	private void declaration(Declarations ds) {
		Declaration d;
		Type t;
		Variable v;
		int x = -1;
		int y = -1;
		Expressions ex;
		
		t = type();
		v = new Variable(match(TokenType.Identifier));
		if (token.type().equals(TokenType.LeftBracket)) {
			match(TokenType.LeftBracket);
			x = Integer.parseInt(token.value());
			match(TokenType.IntLiteral);
			match(TokenType.RightBracket);
			if (token.type().equals(TokenType.LeftBracket)) {
				match(TokenType.LeftBracket);
				y = Integer.parseInt(token.value());
				match(TokenType.IntLiteral);
				match(TokenType.RightBracket);
			}
		}
		if (token.type().equals(TokenType.Assign)) {
			match(TokenType.Assign);
			ex = expressions(x, y);
			d = new Declaration(v, t, ex, x, y);
		} else {
			d = new Declaration(v, t, x, y);
		}
		ds.add(d);
		match(TokenType.Semicolon);
	}

	private Type type() {
		// Type --> int | bool | float | char
		Type t = null;
		if (token.type().equals(TokenType.Int)) {
			t = Type.INT;
		} else if (token.type().equals(TokenType.Bool)) {
			t = Type.BOOL;
		} else if (token.type().equals(TokenType.Float)) {
			t = Type.FLOAT;
		} else if (token.type().equals(TokenType.Char)) {
			t = Type.CHAR;
		} else if (token.type().equals(TokenType.String)) {
			t = Type.STRING;
		}
		token = lexer.next();
		return t;
	}

	private Statement statement() {
		Statement s = null;
		if (token.type().equals(TokenType.Semicolon)) {
			s = new Skip();
			match(TokenType.Semicolon);
		} else if (token.type().equals(TokenType.LeftBrace))
			s = statements();
		else if (token.type().equals(TokenType.If))
			s = ifStatement();
		else if (token.type().equals(TokenType.While))
			s = whileStatement();
		else if (token.type().equals(TokenType.Identifier)) {
			s = assignment();
		} else if (isExecution())
			s = exe();

		return s;
	}

	public Block statements() {
		Block b = new Block();
		Statement s;
		match(TokenType.LeftBrace);
		while (isStatement() || isType()) {
			if (isStatement()) {
				s = statement();
				b.list.add(s);
			} else {
				while (!token.type().equals(TokenType.Semicolon))
					token = lexer.next();
				token = lexer.next();
			}
		}
		match(TokenType.RightBrace);
		return b;
	}

	public Block block() {
		Block b = new Block();
		Statement s;
		while (isStatement() || isType()) {
			if (isStatement()) {
				s = statement();
				b.list.add(s);
			} else {
				while (!token.type().equals(TokenType.Semicolon))
					token = lexer.next();
				token = lexer.next();
			}
		}
		return b;
	}

	private Statement assignment() {
		Variable v;
		Expression e;
		String x = "-1";
		String y = "-1";

		String str = match(TokenType.Identifier);
		if (token.type().equals(TokenType.LeftBracket)) {
			match(TokenType.LeftBracket);
			x = token.value();
			if(token.type().equals(TokenType.IntLiteral))
				match(TokenType.IntLiteral);
			else if(token.type().equals(TokenType.Identifier))
				match(TokenType.Identifier);
			else
				return null;
			match(TokenType.RightBracket);
			if (token.type().equals(TokenType.LeftBracket)) {
				match(TokenType.LeftBracket);
				y = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				else
					return null;
				match(TokenType.RightBracket);
			}
		}
		v = new Variable(str, x, y);
		match(TokenType.Assign);
		if (isFunction()) {
			if (token.type().equals(TokenType.Sum)) {
				match(TokenType.Sum);
				match(TokenType.LeftParen);
				e = expression();
				match(TokenType.Comma);
				match(TokenType.LeftBracket);
				String a = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				if (token.type().equals(TokenType.RightBracket)) {
					match(TokenType.RightBracket);
					match(TokenType.Comma);
					match(TokenType.LeftBracket);
					String f = token.value();
					if(token.type().equals(TokenType.IntLiteral))
						match(TokenType.IntLiteral);
					else if(token.type().equals(TokenType.Identifier))
						match(TokenType.Identifier);
					match(TokenType.RightBracket);
					match(TokenType.RightParen);
					match(TokenType.Semicolon);
					return new Function_Sum(v, e, a, f);
				}
				match(TokenType.Comma);
				String b = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				match(TokenType.RightBracket);
				match(TokenType.Comma);
				match(TokenType.LeftBracket);
				String c = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				match(TokenType.Comma);
				String d = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				match(TokenType.RightBracket);
				match(TokenType.RightParen);
				match(TokenType.Semicolon);
				return new Function_Sum(v, e, a, b, c, d);
			} else if (token.type().equals(TokenType.Avg)) {
				match(TokenType.Avg);
				match(TokenType.LeftParen);
				e = expression();
				match(TokenType.Comma);
				match(TokenType.LeftBracket);
				String a = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				if (token.type().equals(TokenType.RightBracket)) {
					match(TokenType.RightBracket);
					match(TokenType.Comma);
					match(TokenType.LeftBracket);
					String f = token.value();
					if(token.type().equals(TokenType.IntLiteral))
						match(TokenType.IntLiteral);
					else if(token.type().equals(TokenType.Identifier))
						match(TokenType.Identifier);
					match(TokenType.RightBracket);
					match(TokenType.RightParen);
					match(TokenType.Semicolon);
					return new Function_Avg(v, e, a, f);
				}
				match(TokenType.Comma);
				String b = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				match(TokenType.RightBracket);
				match(TokenType.Comma);
				match(TokenType.LeftBracket);
				String c = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				match(TokenType.Comma);
				String d = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				match(TokenType.RightBracket);
				match(TokenType.RightParen);
				match(TokenType.Semicolon);
				return new Function_Avg(v, e, a, b, c, d);
			} else if (token.type().equals(TokenType.Min)) {
				match(TokenType.Min);
				match(TokenType.LeftParen);
				e = expression();
				match(TokenType.Comma);
				match(TokenType.LeftBracket);
				String a = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				if (token.type().equals(TokenType.RightBracket)) {
					match(TokenType.RightBracket);
					match(TokenType.Comma);
					match(TokenType.LeftBracket);
					String f = token.value();
					if(token.type().equals(TokenType.IntLiteral))
						match(TokenType.IntLiteral);
					else if(token.type().equals(TokenType.Identifier))
						match(TokenType.Identifier);
					match(TokenType.RightBracket);
					match(TokenType.RightParen);
					match(TokenType.Semicolon);
					return new Function_Min(v, e, a, f);
				}
				match(TokenType.Comma);
				String b = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				match(TokenType.RightBracket);
				match(TokenType.Comma);
				match(TokenType.LeftBracket);
				String c = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				match(TokenType.Comma);
				String d = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				match(TokenType.RightBracket);
				match(TokenType.RightParen);
				match(TokenType.Semicolon);
				return new Function_Min(v, e, a, b, c, d);
			} else if (token.type().equals(TokenType.Max)) {
				match(TokenType.Max);
				match(TokenType.LeftParen);
				e = expression();
				match(TokenType.Comma);
				match(TokenType.LeftBracket);
				String a = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				if (token.type().equals(TokenType.RightBracket)) {
					match(TokenType.RightBracket);
					match(TokenType.Comma);
					match(TokenType.LeftBracket);
					String f = token.value();
					if(token.type().equals(TokenType.IntLiteral))
						match(TokenType.IntLiteral);
					else if(token.type().equals(TokenType.Identifier))
						match(TokenType.Identifier);
					match(TokenType.RightBracket);
					match(TokenType.RightParen);
					match(TokenType.Semicolon);
					return new Function_Max(v, e, a, f);
				}
				match(TokenType.Comma);
				String b = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				match(TokenType.RightBracket);
				match(TokenType.Comma);
				match(TokenType.LeftBracket);
				String c = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				match(TokenType.Comma);
				String d = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				match(TokenType.RightBracket);
				match(TokenType.RightParen);
				match(TokenType.Semicolon);
				return new Function_Max(v, e, a, b, c, d);
			}
		} else {
			e = expression();
			match(TokenType.Semicolon);
			return new Assignment(v, e, x, y);
		}
		return null;
	}

	private Conditional ifStatement() {
		// IfStatement --> if ( Expression ) Statement [ else Statement ]
		Conditional c;
		Expression e;
		Statement s;

		match(TokenType.If);
		match(TokenType.LeftParen);
		e = expression();
		match(TokenType.RightParen);
		s = statement();
		if (token.type().equals(TokenType.Else)) {
			match(TokenType.Else);
			Statement es = statement();
			c = new Conditional(e, s, es);
		} else {
			c = new Conditional(e, s);
		}
		return c;
	}

	private Loop whileStatement() {
		// WhileStatement --> while ( Expression ) Statement
		Expression e;
		Statement s;

		match(TokenType.While);
		match(TokenType.LeftParen);
		e = expression();
		match(TokenType.RightParen);
		s = statement();
		return new Loop(e, s);
	}

	private Expressions expressions(int x, int y) {
		Expressions ex = new Expressions();
		if (x == -1) {
			ex.add(expression());
			return ex;
		} else if (y == -1) {
			match(TokenType.LeftBrace);
			for (int i = 0; i < x; i++) {
				ex.add(expression());
				if (i != x - 1) {
					match(TokenType.Comma);
				}
			}
			match(TokenType.RightBrace);
		} else {
			match(TokenType.LeftBrace);
			for (int i = 0; i < x; i++) {
				match(TokenType.LeftBrace);
				for (int j = 0; j < y; j++) {
					ex.add(expression());
					if (j != y - 1)
						match(TokenType.Comma);
				}
				match(TokenType.RightBrace);
				if (i != x - 1)
					match(TokenType.Comma);
			}
			match(TokenType.RightBrace);
		}
		return ex;
	}

	private Execution exe() {
		Variable v;
		Expression e;
		Operator op = new Operator(match(token.type()));
		if (op.toString().equals("show")) {
			match(TokenType.LeftParen);
			e = expression();
			if (token.type().equals(TokenType.Comma)) {
				match(TokenType.Comma);
				match(TokenType.LeftBracket);
				int a = Integer.parseInt(token.value());
				match(TokenType.IntLiteral);
				if (token.type().equals(TokenType.RightBracket)) {
					match(TokenType.RightBracket);
					match(TokenType.Comma);
					match(TokenType.LeftBracket);
					int f = Integer.parseInt(token.value());
					match(TokenType.IntLiteral);
					match(TokenType.RightBracket);
					match(TokenType.RightParen);
					match(TokenType.Semicolon);
					return new Execution_Show(e, a, f);
				}
				match(TokenType.Comma);
				int b = Integer.parseInt(token.value());
				match(TokenType.IntLiteral);
				match(TokenType.RightBracket);
				match(TokenType.Comma);
				match(TokenType.LeftBracket);
				int c = Integer.parseInt(token.value());
				match(TokenType.IntLiteral);
				match(TokenType.Comma);
				int d = Integer.parseInt(token.value());
				match(TokenType.IntLiteral);
				match(TokenType.RightBracket);
				match(TokenType.RightParen);
				match(TokenType.Semicolon);
				return new Execution_Show(e, a, b, c, d);
			}
			match(TokenType.RightParen);
			match(TokenType.Semicolon);
			return new Execution_Show(e);
		} else if (op.toString().equals("get")) {
			match(TokenType.LeftParen);
			if (token.type().equals(TokenType.Identifier)) {
				String x = "-1";
				String y = "-1";
				String str = match(TokenType.Identifier);
				if (token.type().equals(TokenType.LeftBracket)) {
					match(TokenType.LeftBracket);
					x = token.value();
					match(TokenType.IntLiteral);
					match(TokenType.RightBracket);
					if (token.type().equals(TokenType.LeftBracket)) {
						match(TokenType.LeftBracket);
						y = token.value();
						match(TokenType.IntLiteral);
						match(TokenType.RightBracket);
						v = new Variable(str, x, y);
					} else
						v = new Variable(str, x);
				} else if (token.type().equals(TokenType.Comma)) {
					match(TokenType.Comma);
					match(TokenType.LeftBracket);
					int a = Integer.parseInt(token.value());
					match(TokenType.IntLiteral);
					if (token.type().equals(TokenType.RightBracket)) {
						match(TokenType.RightBracket);
						match(TokenType.Comma);
						match(TokenType.LeftBracket);
						int f = Integer.parseInt(token.value());
						match(TokenType.IntLiteral);
						match(TokenType.RightBracket);
						match(TokenType.RightParen);
						match(TokenType.Semicolon);
						return new Execution_Get(new Variable(str), a, f);
					}
					match(TokenType.Comma);
					int b = Integer.parseInt(token.value());
					match(TokenType.IntLiteral);
					match(TokenType.RightBracket);
					match(TokenType.Comma);
					match(TokenType.LeftBracket);
					int c = Integer.parseInt(token.value());
					match(TokenType.IntLiteral);
					match(TokenType.Comma);
					int d = Integer.parseInt(token.value());
					match(TokenType.IntLiteral);
					match(TokenType.RightBracket);
					match(TokenType.RightParen);
					match(TokenType.Semicolon);
					return new Execution_Get(new Variable(str), a, b, c, d);
				} else
					v = new Variable(str);
				match(TokenType.RightParen);
				match(TokenType.Semicolon);
				return new Execution_Get(v);
			}
		} else if (op.toString().equals("sort")) {
			match(TokenType.LeftParen);
			if (token.type().equals(TokenType.Identifier)) {
				String str = match(TokenType.Identifier);
				v = new Variable(str);
				if (token.type().equals(TokenType.LeftBracket)) {
					match(TokenType.LeftBracket);
					String i = token.value();
					if(token.type().equals(TokenType.IntLiteral))
						match(TokenType.IntLiteral);
					else if(token.type().equals(TokenType.Identifier))
						match(TokenType.Identifier);
					match(TokenType.RightBracket);
					match(TokenType.RightParen);
					match(TokenType.Semicolon);
					return new Execution_Sort(v, i);
				}
				match(TokenType.RightParen);
				match(TokenType.Semicolon);
				return new Execution_Sort(v);
			} else
				return null;
		}
		return null;
	}

	private Expression expression() {
		// Expression --> Conjunction { || Conjunction }
		Expression e = conjunction();
		while (token.type().equals(TokenType.Or)) {
			Operator op = new Operator(match(token.type()));
			Expression t = expression();
			e = new Binary(op, e, t);
		}
		return e;
	}

	private Expression conjunction() {
		// Conjunction --> Equality { && Equality }
		Expression e = equality();
		while (token.type().equals(TokenType.And)) {
			Operator op = new Operator(match(token.type()));
			Expression t = conjunction();
			e = new Binary(op, e, t);
		}
		return e;
	}

	private Expression equality() {
		// Equality --> Relation [ EquOp Relation ]
		Expression e = relation();
		if (isEqualityOp()) {
			Operator op = new Operator(match(token.type()));
			Expression t = relation();
			e = new Binary(op, e, t);
		}
		return e;
	}

	private Expression relation() {
		// Relation --> Addition [RelOp Addition]
		Expression e = addition();
		if (isRelationalOp()) {
			Operator op = new Operator(match(token.type()));
			Expression t = addition();
			e = new Binary(op, e, t);
		}
		return e;
	}

	private Expression addition() {
		// Addition --> Term { AddOp Term }
		Expression e = term();
		while (isAddOp()) {
			Operator op = new Operator(match(token.type()));
			Expression t = term();
			e = new Binary(op, e, t);
		}
		return e;
	}

	private Expression term() {
		// Term --> Factor { MultiplyOp Factor }
		Expression e = square();
		while (isMultiplyOp()) {
			Operator op = new Operator(match(token.type()));
			Expression t = square();
			e = new Binary(op, e, t);
		}
		return e;
	}

	private Expression square() {
		// Term --> Factor {^ Factor}
		Expression e = factor();
		while (isSquareOp()) {
			Operator op = new Operator(match(token.type()));
			Expression t = factor();
			e = new Binary(op, e, t);
		}
		return e;
	}

	private Expression factor() {
		// Factor --> [ UnaryOp ] Primary
		if (isUnaryOp()) {
			Operator op = new Operator(match(token.type()));
			Expression term = primary();
			return new Unary(op, term);
		}
		return primary();
	}

	private Expression primary() {
		// Primary --> Identifier | Literal | ( Expression )
		String x, y;
		Expression e = null;
		if (token.type().equals(TokenType.LeftParen))
			e = expression_paren();
		else if (token.type().equals(TokenType.Identifier)) {
			String s = match(TokenType.Identifier);
			if (token.type().equals(TokenType.LeftBracket)) {
				match(TokenType.LeftBracket);
				x = token.value();
				if(token.type().equals(TokenType.IntLiteral))
					match(TokenType.IntLiteral);
				else if(token.type().equals(TokenType.Identifier))
					match(TokenType.Identifier);
				match(TokenType.RightBracket);
				if (token.type().equals(TokenType.LeftBracket)) {
					match(TokenType.LeftBracket);
					y = token.value();
					if(token.type().equals(TokenType.IntLiteral))
						match(TokenType.IntLiteral);
					else if(token.type().equals(TokenType.Identifier))
						match(TokenType.Identifier);
					match(TokenType.RightBracket);
					e = new Variable(s, x, y);
				} else
					e = new Variable(s, x);
			} else
				e = new Variable(s);
		} else if (isLiteral())
			e = literal();
		return e;
	}

	private Expression expression_paren() {
		match(TokenType.LeftParen);
		Expression_Paren e = new Expression_Paren(expression());
		match(TokenType.RightParen);
		return e;
	}

	private Value literal() {
		Value v = null;
		String s = token.value();
		if (token.type().equals(TokenType.IntLiteral)) {
			v = new IntValue(Integer.parseInt(s));
			token = lexer.next();
		} else if (token.type().equals(TokenType.FloatLiteral)) {
			v = new FloatValue(Float.parseFloat(s));
			token = lexer.next();
		} else if (token.type().equals(TokenType.CharLiteral)) {
			v = new CharValue(s.charAt(0));
			token = lexer.next();
		} else if (token.type().equals(TokenType.True)) {
			v = new BoolValue(true);
			token = lexer.next();
		} else if (token.type().equals(TokenType.False)) {
			v = new BoolValue(false);
			token = lexer.next();
		} else if (token.type().equals(TokenType.StringLiteral)) {
			v = new StringValue(s);
			token = lexer.next();
		}
		return v;
	}

	private boolean isAddOp() {
		return token.type().equals(TokenType.Plus) || token.type().equals(TokenType.Minus);
	}

	private boolean isMultiplyOp() {
		return token.type().equals(TokenType.Multiply) || token.type().equals(TokenType.Divide);
	}

	private boolean isSquareOp() {
		return token.type().equals(TokenType.Square) || token.type().equals(TokenType.Squareroot);
	}

	private boolean isUnaryOp() {
		return token.type().equals(TokenType.Not) || token.type().equals(TokenType.Minus);
	}

	private boolean isEqualityOp() {
		return token.type().equals(TokenType.Equals) || token.type().equals(TokenType.NotEqual);
	}

	private boolean isRelationalOp() {
		return token.type().equals(TokenType.Less) || token.type().equals(TokenType.LessEqual)
				|| token.type().equals(TokenType.Greater) || token.type().equals(TokenType.GreaterEqual);
	}

	private boolean isType() {
		return token.type().equals(TokenType.Int) || token.type().equals(TokenType.Bool)
				|| token.type().equals(TokenType.Float) || token.type().equals(TokenType.Char)
				|| token.type().equals(TokenType.String);
	}

	private boolean isLiteral() {
		return token.type().equals(TokenType.IntLiteral) || isBooleanLiteral()
				|| token.type().equals(TokenType.FloatLiteral) || token.type().equals(TokenType.CharLiteral)
				|| token.type().equals(TokenType.StringLiteral);
	}

	private boolean isBooleanLiteral() {
		return token.type().equals(TokenType.True) || token.type().equals(TokenType.False);
	}

	private boolean isStatement() {
		return token.type().equals(TokenType.Semicolon) || token.type().equals(TokenType.LeftBrace)
				|| token.type().equals(TokenType.Identifier) || token.type().equals(TokenType.If)
				|| token.type().equals(TokenType.While) || isExecution();
	}

	private boolean isFunction() {
		return token.type().equals(TokenType.Sum) || token.type().equals(TokenType.Avg)
				|| token.type().equals(TokenType.Min) || token.type().equals(TokenType.Max);
	}

	private boolean isExecution() {
		return token.type().equals(TokenType.Show) || token.type().equals(TokenType.Get)
				|| token.type().equals(TokenType.Sort);
	}

	private boolean isExpression() {
		return token.type().equals(TokenType.CharLiteral) || token.type().equals(TokenType.IntLiteral)
				|| token.type().equals(TokenType.FloatLiteral) || token.type().equals(TokenType.Identifier)
				|| isFunction();
	}
}