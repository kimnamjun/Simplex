import java.io.*;

public class Lexer {
	private boolean isEof = false;
	private char ch = ' ';
	private BufferedReader input;
	private String line = "";
	private int lineno = 0;
	private int col = 1;
	private final String letters = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "_";
	private final String digits = "0123456789";
	private final char eolnCh = '\n';
	private final char eofCh = '\004';
	private String str = "";

	public Lexer(String fileName) {
		try {
			input = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + fileName);
			System.exit(1);
		}
	}

	private char nextChar() {
		if (ch == eofCh)
			error("Attempt to read past end of file");
		col++;
		if (col >= line.length()) {
			try {
				line = input.readLine();
			} catch (IOException e) {
				System.err.println(e);
				System.exit(1);
			}
			if (line == null)
				line = "" + eofCh;
			else {
				lineno++;
				line += eolnCh;
			}
			col = 0;
		}
		return line.charAt(col);
	}

	public Token next() {
		do {
			if (isLetter(ch)) { // ident or keyword
				String spelling = concat(letters + digits);
				return Token.keyword(spelling);
			} else if (isDigit(ch)) { // int or float literal
				String number = concat(digits);
				if (ch != '.') // int Literal
					return Token.mkIntLiteral(number);
				number += concat(digits);
				return Token.mkFloatLiteral(number);
			} else if (ch == '\'') {
				char c = nextChar();
				nextChar();
				ch = nextChar();
				return Token.mkCharLiteral(Character.toString(c));
			}

			else if (ch == '"') {
				String str = "";
				ch = nextChar();
				while (ch != '"') {
					str += Character.toString(ch);
					ch = nextChar();
				}
				ch = nextChar();
				return Token.mkStringLiteral(str);
			} else
				switch (ch) {
				case ' ':
				case '\t':
				case '\r':
				case eolnCh:
					ch = nextChar();
					break;
					
				case eofCh:
					return Token.eofTok;
				case '+':
					ch = nextChar();
					return Token.plusTok;
				case '-':
					ch = nextChar();
					return Token.minusTok;
				case '*':
					ch = nextChar();
					return Token.multiplyTok;
				case '/':
					ch = nextChar();
					return Token.divideTok;
				case '(':
					ch = nextChar();
					return Token.leftParenTok;
				case ')':
					ch = nextChar();
					return Token.rightParenTok;
				case '{':
					ch = nextChar();
					return Token.leftBraceTok;
				case '}':
					ch = nextChar();
					return Token.rightBraceTok;
				case ',':
					ch = nextChar();
					return Token.commaTok;
				case ';':
					ch = nextChar();
					return Token.semicolonTok;
				case '&':
					ch = nextChar();
					return Token.andTok;
				case '|':
					ch = nextChar();
					return Token.orTok;
				case ':':
					ch = nextChar();
					return Token.assignTok;
				case '=':
					ch = nextChar();
					return Token.eqeqTok;
				case '<':
					return chkOpt('=', Token.ltTok, Token.lteqTok);
				case '>':
					return chkOpt('=', Token.gtTok, Token.gteqTok);
				case '!':
					return chkOpt('=', Token.notTok, Token.noteqTok);
				case '^':
					return chkOpt('/', Token.squareTok, Token.squarerootTok);

				case '[':
					ch = nextChar();
					return Token.leftBracketTok;
				case ']':
					ch = nextChar();
					return Token.rightBracketTok;

				case '@':
					do {
						ch = nextChar();
					} while (ch != eolnCh);
					ch = nextChar();
					break;

				default:
					error("Illegal character " + ch);
				} // switch
		} while (true);
	} // next

	private boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_');
	}

	private boolean isDigit(char c) {
		return (c >= '0' && c <= '9');
	}

	private void check(char c) {
		ch = nextChar();
		if (ch != c)
			error("Illegal character, expecting " + c);
		ch = nextChar();
	}

	private Token chkOpt(char c, Token one, Token two) {
		ch = nextChar();
		if (c != ch)
			return one;
		ch = nextChar();
		return two;
	}

	private String concat(String set) {
		String r = "";
		do {
			r += ch;
			ch = nextChar();
		} while (set.indexOf(ch) >= 0);
		return r;
	}

	public void error(String msg) {
		System.err.print(line);
		System.err.println("Error: column " + col + " " + msg);
		System.exit(1);
	}

	static public void main(String[] argv) {
		Lexer lexer = new Lexer("text.txt");
		Token tok = lexer.next();
		while (tok != Token.eofTok) {
			System.out.println(tok.toString());
			tok = lexer.next();
		}
	}
}