import java.util.*;

public class Parser {

    public static Expression parse(String src) {
        Lexer lexer = new Lexer(src);
        Parser parser = new Parser(lexer);
        Node root = parser.parseExpr();
        if (lexer.current().type != TokenType.EOF) {
            throw new RuntimeException("Unexpected token: " + lexer.current().value);
        }
        return root::eval;
    }

    @FunctionalInterface
    public interface Expression {
        double eval(double x);
    }
    enum TokenType {
        NUMBER, IDENT, PLUS, MINUS, STAR, SLASH, CARET,
        LPAREN, RPAREN, EOF
    }
    record Token(TokenType type, String value) {}

    static class Lexer {
        private final String src;
        private int pos = 0;
        private Token cur;

        Lexer(String src) {
            this.src = src.trim();
            advance();
        }

        Token current() {
            return cur;
        }

        Token consume() {
            Token t = cur;
            advance();
            return t;
        }

        Token consume(TokenType expected) {
            if (cur.type != expected) {
                throw new RuntimeException("Expected " + expected + " but got " + cur.type + " ('" + cur.value + "')");
            }
            return consume();
        }

        private void advance() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;

            if (pos >= src.length()) { cur = new Token(TokenType.EOF, ""); return; }

            char c = src.charAt(pos);

            if (c == '+') { cur = new Token(TokenType.PLUS,   "+"); pos++; return; }
            if (c == '-') { cur = new Token(TokenType.MINUS,  "-"); pos++; return; }
            if (c == '*') { cur = new Token(TokenType.STAR,   "*"); pos++; return; }
            if (c == '/') { cur = new Token(TokenType.SLASH,  "/"); pos++; return; }
            if (c == '^') { cur = new Token(TokenType.CARET,  "^"); pos++; return; }
            if (c == '(') { cur = new Token(TokenType.LPAREN, "("); pos++; return; }
            if (c == ')') { cur = new Token(TokenType.RPAREN, ")"); pos++; return; }

            if (Character.isDigit(c) || c == '.') {
                int start = pos;
                while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) pos++;
                if (pos < src.length() && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
                    pos++;
                    if (pos < src.length() && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) pos++;
                    while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
                }
                cur = new Token(TokenType.NUMBER, src.substring(start, pos));
                return;
            }

            if (Character.isLetter(c) || c == '_') {
                int start = pos;
                while (pos < src.length() && (Character.isLetterOrDigit(src.charAt(pos)) || src.charAt(pos) == '_')) pos++;
                cur = new Token(TokenType.IDENT, src.substring(start, pos));
                return;
            }

            throw new RuntimeException("Unknown character: '" + c + "' at position " + pos);
        }
    }

    interface Node {
        double eval(double x);
    }

    record Num(double v) implements Node {
        public double eval(double x) {
            return v;
        }
    }
    record Var() implements Node {
        public double eval(double x) {
            return x;
        }
    }
    record BinOp(Node l, char op, Node r) implements Node {
        public double eval(double x) {
            return switch (op) {
                case '+' -> l.eval(x) + r.eval(x);
                case '-' -> l.eval(x) - r.eval(x);
                case '*' -> l.eval(x) * r.eval(x);
                case '/' -> l.eval(x) / r.eval(x);
                case '^' -> Math.pow(l.eval(x), r.eval(x));
                default  -> throw new RuntimeException("Unknown op: " + op);
            };
        }
    }
    record Negate(Node n) implements Node {
        public double eval(double x) { return -n.eval(x);
        }
    }
    record Func(String name, Node arg) implements Node {
        public double eval(double x) {
            double v = arg.eval(x);
            return switch (name) {
                case "sin"  -> Math.sin(v);
                case "cos"  -> Math.cos(v);
                case "tan"  -> Math.tan(v);
                case "asin" -> Math.asin(v);
                case "acos" -> Math.acos(v);
                case "atan" -> Math.atan(v);
                case "sqrt" -> Math.sqrt(v);
                case "exp"  -> Math.exp(v);
                case "ln"   -> Math.log(v);
                case "log"  -> Math.log10(v);
                case "abs"  -> Math.abs(v);
                default     -> throw new RuntimeException("Unknown function: " + name);
            };
        }
    }

    static class Parser {
        private final Lexer lex;

        Parser(Lexer lex) {
            this.lex = lex;
        }

        Node parseExpr() {
            Node n = parseTerm();
            while (lex.current().type == TokenType.PLUS || lex.current().type == TokenType.MINUS) {
                char op = lex.consume().value.charAt(0);
                n = new BinOp(n, op, parseTerm());
            }
            return n;
        }

        Node parseTerm() {
            Node n = parseUnary();
            while (lex.current().type == TokenType.STAR || lex.current().type == TokenType.SLASH) {
                char op = lex.consume().value.charAt(0);
                n = new BinOp(n, op, parseUnary());
            }
            return n;
        }

        Node parseUnary() {
            if (lex.current().type == TokenType.MINUS) {
                lex.consume();
                return new Negate(parsePower());
            }
            return parsePower();
        }

        Node parsePower() {
            Node base = parseAtom();
            if (lex.current().type == TokenType.CARET) {
                lex.consume();
                return new BinOp(base, '^', parseUnary());
            }
            return base;
        }

        Node parseAtom() {
            Token t = lex.current();

            if (t.type == TokenType.NUMBER) {
                lex.consume();
                return new Num(Double.parseDouble(t.value));
            }

            if (t.type == TokenType.LPAREN) {
                lex.consume();
                Node n = parseExpr();
                lex.consume(TokenType.RPAREN);
                return n;
            }

            if (t.type == TokenType.IDENT) {
                String name = t.value.toLowerCase();
                lex.consume();
                switch (name) {
                    case "x" -> {
                        return new Var();
                    }
                    case "pi" -> {
                        return new Num(Math.PI);
                    }
                    case "e" -> {
                        return new Num(Math.E);
                    }
                }

                Set<String> FUNCS = Set.of("sin","cos","tan","asin","acos","atan","sqrt","exp","ln","log","abs");
                if (FUNCS.contains(name)) {
                    lex.consume(TokenType.LPAREN);
                    Node arg = parseExpr();
                    lex.consume(TokenType.RPAREN);
                    return new Func(name, arg);
                }

                throw new RuntimeException("Unknown identifier: " + name);
            }

            throw new RuntimeException("Unexpected token: " + t.value + " (type=" + t.type + ")");
        }
    }
}
