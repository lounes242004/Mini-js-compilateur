import java.util.*;

public class AnalyseurLexicale {

    enum TokenType {
        KEYWORD,
        IDENTIFIER,
        NUMBER,
        STRING,
        OPERATOR,
        PUNCTUATION,
        COMMENT,
        WHITESPACE,
        UNKNOWN,
        EOF
    }

    static class Token {
        TokenType type;
        String lexeme;
        int line;
        int column;

        Token(TokenType type, String lexeme, int line, int column) {
            this.type = type;
            this.lexeme = lexeme;
            this.line = line;
            this.column = column;
        }

        @Override
        public String toString() {
            return String.format("%-12s %-20s (ligne %d, col %d)", type, "\"" + lexeme + "\"", line, column);
        }
    }

    static class Lexer {
        private final String input;
        private int pos = 0;
        private int line = 1;
        private int col = 1;

        private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
                "break", "case", "catch", "class", "const", "continue", "debugger", "default", "delete", "do", "else",
                "export", "extends", "finally", "for", "function", "if", "import", "in", "instanceof", "let", "new",
                "return", "super", "switch", "this", "throw", "try", "typeof", "var", "void", "while", "with", "yield",
                "Belmokhtar", "Lounes"));

        private static final String OPERATORS = "+-*/%=&|^!<>?:";
        private static final String PUNCTUATIONS = "(){},;[].";

        Lexer(String input) {
            this.input = input;
        }

        private boolean estFin() {
            return pos >= input.length();
        }

        private char regarder() {
            return estFin() ? '#' : input.charAt(pos);
        }

        private char regarderSuivant() {
            return (pos + 1) >= input.length() ? '#' : input.charAt(pos + 1);
        }

        private char avancer() {
            char c = regarder();
            pos++;
            if (c == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
            return c;
        }

        List<Token> analyserLexicale() {
            List<Token> tokens = new ArrayList<>();

            while (!estFin()) {
                char c = regarder();

                int startLine = line;
                int startCol = col;

                if (Character.isWhitespace(c)) {
                    StringBuilder sb = new StringBuilder();
                    while (Character.isWhitespace(regarder()) && !estFin()) {
                        sb.append(avancer());
                    }
                    tokens.add(new Token(TokenType.WHITESPACE, sb.toString(), startLine, startCol));
                    continue;
                }

                if (c == '/' && regarderSuivant() == '/') {

                    StringBuilder sb = new StringBuilder();
                    sb.append(avancer());
                    sb.append(avancer());
                    while (regarder() != '\n' && !estFin()) {
                        sb.append(avancer());
                    }
                    tokens.add(new Token(TokenType.COMMENT, sb.toString(), startLine, startCol));
                    continue;
                }
                if (c == '/' && regarderSuivant() == '*') {

                    StringBuilder sb = new StringBuilder();
                    sb.append(avancer());
                    sb.append(avancer());
                    while (!estFin()) {
                        if (regarder() == '*' && regarderSuivant() == '/') {
                            sb.append(avancer());
                            sb.append(avancer());
                            break;
                        } else {
                            sb.append(avancer());
                        }
                    }
                    tokens.add(new Token(TokenType.COMMENT, sb.toString(), startLine, startCol));
                    continue;
                }

                if (c == '"' || c == '\'' || c == '`') {
                    char quote = c;
                    StringBuilder sb = new StringBuilder();
                    sb.append(avancer());
                    boolean escaped = false;
                    while (!estFin()) {
                        char ch = avancer();
                        sb.append(ch);
                        if (ch == '\n' && quote != '`') {
                            break;
                        }
                        if (ch == '\\' && !escaped) {
                            escaped = true;
                        } else {
                            if (ch == quote && !escaped) {
                                break;
                            }
                            escaped = false;
                        }
                    }
                    tokens.add(new Token(TokenType.STRING, sb.toString(), startLine, startCol));
                    continue;
                }

                if (Character.isDigit(c)) {
                    StringBuilder sb = new StringBuilder();
                    while (Character.isDigit(regarder()))
                        sb.append(avancer());
                    if (regarder() == '.' && Character.isDigit(regarderSuivant())) {
                        sb.append(avancer()); // '.'
                        while (Character.isDigit(regarder()))
                            sb.append(avancer());
                    }

                    if (regarder() == 'e' || regarder() == 'E') {
                        sb.append(avancer());
                        if (regarder() == '+' || regarder() == '-')
                            sb.append(avancer());
                        while (Character.isDigit(regarder()))
                            sb.append(avancer());
                    }
                    tokens.add(new Token(TokenType.NUMBER, sb.toString(), startLine, startCol));
                    continue;
                }

                if (Character.isLetter(c) || c == '_' || c == '$') {
                    StringBuilder sb = new StringBuilder();
                    while (Character.isLetterOrDigit(regarder()) || regarder() == '_' || regarder() == '$') {
                        sb.append(avancer());
                    }
                    String lexeme = sb.toString();
                    if (KEYWORDS.contains(lexeme)) {
                        tokens.add(new Token(TokenType.KEYWORD, lexeme, startLine, startCol));
                    } else {
                        tokens.add(new Token(TokenType.IDENTIFIER, lexeme, startLine, startCol));
                    }
                    continue;
                }

                String two = "" + c + regarderSuivant();
                String three = pos + 2 < input.length() ? input.substring(pos, Math.min(pos + 3, input.length()))
                        : null;

                if (three != null && (three.equals("===") || three.equals("!=="))) {
                    avancer();
                    avancer();
                    avancer();
                    tokens.add(new Token(TokenType.OPERATOR, three, startLine, startCol));
                    continue;
                }
                if (two.equals("==") || two.equals("!=") || two.equals("<=") || two.equals(">=") ||
                        two.equals("&&") || two.equals("||") || two.equals("++") || two.equals("--") ||
                        two.equals("+=") || two.equals("-=") || two.equals("*=") || two.equals("/=") ||
                        two.equals("=>") || two.equals("<<") || two.equals(">>") || two.equals("**")) {
                    avancer();
                    avancer();
                    tokens.add(new Token(TokenType.OPERATOR, two, startLine, startCol));
                    continue;
                }

                if (OPERATORS.indexOf(c) >= 0) {
                    tokens.add(new Token(TokenType.OPERATOR, "" + avancer(), startLine, startCol));
                    continue;
                }

                if (PUNCTUATIONS.indexOf(c) >= 0) {
                    tokens.add(new Token(TokenType.PUNCTUATION, "" + avancer(), startLine, startCol));
                    continue;
                }

                tokens.add(new Token(TokenType.UNKNOWN, "" + avancer(), startLine, startCol));
            }
            tokens.add(new Token(TokenType.EOF, "#", line, col));
            return tokens;
        }
    }

    public static void main(String[] args) {
        String example = ""
                + "// Exemple de programme JS\n"
                + "/* commentaire multi-lignes\n"
                + "   ignoré par le lexer */\n"
                + "function test(x, y) {\n"
                + "  let sum = x ~` y;\n"
                + "  Belmokhtar(); // mot-clé personnalisé utilisé comme identifiant\n"
                + "  if (sum > 10) {\n"
                + "    Lounes = 'grande';\n"
                + "  } else {\n"
                + "    // rien\n"
                + "    return 0;\n"
                + "  }\n"
                + "  // template string\n"
                + "  const msg = `résultat: ${sum}`;\n"
                + "  return msg;\n"
                + "}\n";

        Lexer lexer = new Lexer(example);
        List<Token> allTokens = lexer.analyserLexicale();

        System.out.println("Tokens (commentaires affichés séparément et peuvent être ignorés):\n");
        for (Token t : allTokens) {

            if (t.type == TokenType.WHITESPACE)
                continue;
            System.out.println(t);
        }

        System.out.println("\nTokens (sans commentaires) :");
        for (Token t : allTokens) {
            if (t.type == TokenType.WHITESPACE || t.type == TokenType.COMMENT)
                continue;
            System.out.println(t);
        }
    }
}
