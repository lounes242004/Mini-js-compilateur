import java.util.List;

public class AnalyseurSyntaxique2 {

    private final List<AnalyseurLexicale.Token> tokens;
    private int index = 0;
    private AnalyseurLexicale.Token courant;

    public AnalyseurSyntaxique2(List<AnalyseurLexicale.Token> tokens) {
        this.tokens = tokens;
        this.courant = tokens.get(0);
    }

    
    private void avancer() {
        if (index < tokens.size() - 1) {
            index++;
            courant = tokens.get(index);
        }
    }

    
    private void skipInsignificant() {
        while (courant.type == AnalyseurLexicale.TokenType.WHITESPACE
                || courant.type == AnalyseurLexicale.TokenType.COMMENT) {
            avancer();
        }
    }

    private boolean match(String lexeme) {
        skipInsignificant();
        if (courant.lexeme.equals(lexeme)) {
            avancer();
            return true;
        }
        return false;
    }

    private boolean type(AnalyseurLexicale.TokenType t) {
        skipInsignificant();
        if (courant.type == t) {
            avancer();
            return true;
        }
        return false;
    }

    private void erreur(String msg) {
        throw new RuntimeException(
                "Erreur syntaxique ligne " + courant.line + ", colonne " + courant.column + " : " + msg +
                        " (trouvé : '" + courant.lexeme + "')");
    }

    
    
    

    
    public void Programme() {
        while (true) {
            skipInsignificant();
            if (courant.type == AnalyseurLexicale.TokenType.EOF)
                break;
            Instruction();
        }
    }

    
    
    public void Instruction() {
        skipInsignificant();

        if (courant.lexeme.equals("var") || courant.lexeme.equals("let") || courant.lexeme.equals("const")) {
            DeclarationVar();
            return;
        }

        if (courant.type == AnalyseurLexicale.TokenType.IDENTIFIER) {
            
            
            Affectation();
            return;
        }

        if (courant.lexeme.equals("class")) {
            DeclarationClasse();
            return;
        }

        if (courant.lexeme.equals("try")) {
            TryCatch();
            return;
        }

        
        if (courant.lexeme.equals("while") || courant.lexeme.equals("for") || courant.lexeme.equals("if")) {
            InstructionIgnoree();
            return;
        }

        erreur("Instruction invalide");
    }

    
    public void DeclarationVar() {
        if (!(courant.lexeme.equals("var") || courant.lexeme.equals("let") || courant.lexeme.equals("const")))
            erreur("Déclaration de variable attendue");

        avancer(); 

        if (!type(AnalyseurLexicale.TokenType.IDENTIFIER))
            erreur("Identifiant attendu après déclaration");

        if (match("=")) {
            Expression();
        }

        if (!match(";"))
            erreur("';' attendu après déclaration");
    }

    
    public void Affectation() {

        if (!type(AnalyseurLexicale.TokenType.IDENTIFIER))
            erreur("Identifiant attendu");

        if (!match("="))
            erreur("'=' attendu dans l'affectation");

        Expression();

        if (!match(";"))
            erreur("';' attendu après affectation");
    }

    
    public void Expression() {
        Primaire();
        SuiteComparaison();
    }

    public void SuiteComparaison() {
        skipInsignificant();
        while (courant.type == AnalyseurLexicale.TokenType.OPERATOR && (courant.lexeme.equals("==")
                || courant.lexeme.equals("!=") || courant.lexeme.equals("===") || courant.lexeme.equals("!==") ||
                courant.lexeme.equals("<=") || courant.lexeme.equals(">=") || courant.lexeme.equals("<")
                || courant.lexeme.equals(">") ||
                courant.lexeme.equals("+") || courant.lexeme.equals("-") || courant.lexeme.equals("*")
                || courant.lexeme.equals("/") || courant.lexeme.equals("%") ||
                courant.lexeme.equals("&&") || courant.lexeme.equals("||"))) {
            avancer(); 
            skipInsignificant();
            Primaire();
            skipInsignificant();
        }
    }

    
    public void Primaire() {
        if (type(AnalyseurLexicale.TokenType.IDENTIFIER))
            return;
        if (type(AnalyseurLexicale.TokenType.NUMBER))
            return;
        if (type(AnalyseurLexicale.TokenType.STRING))
            return;

        erreur("Expression primaire attendue");
    }

    
    public void DeclarationClasse() {
        if (!match("class"))
            erreur("'class' attendu");

        if (!type(AnalyseurLexicale.TokenType.IDENTIFIER))
            erreur("Nom de classe attendu");

        if (!match("{"))
            erreur("'{' attendu");

        ListeMethodes();

        if (!match("}"))
            erreur("'}' attendu");
    }

    public void ListeMethodes() {
        skipInsignificant();
        while (courant.type == AnalyseurLexicale.TokenType.IDENTIFIER) {
            Methode();
            skipInsignificant();
        }
    }

    public void Methode() {

        if (!type(AnalyseurLexicale.TokenType.IDENTIFIER))
            erreur("Nom de méthode attendu");

        if (!match("("))
            erreur("'(' attendu");
        if (!match(")"))
            erreur("')' attendu");

        if (!match("{"))
            erreur("'{' attendu");
        if (!match("}"))
            erreur("'}' attendu");
    }

    
    public void TryCatch() {

        if (!match("try"))
            erreur("'try' attendu");

        Bloc();

        if (!match("catch"))
            erreur("'catch' attendu");

        if (!match("("))
            erreur("'(' attendu après catch");
        if (!type(AnalyseurLexicale.TokenType.IDENTIFIER))
            erreur("Identifiant attendu après catch(");
        if (!match(")"))
            erreur("')' attendu après catch( ident");

        Bloc();

        if (match("finally")) {
            Bloc();
        }
    }

    public void Bloc() {
        if (!match("{"))
            erreur("'{' attendu pour ouvrir un bloc");

        skipInsignificant();
        while (!(courant.type == AnalyseurLexicale.TokenType.PUNCTUATION && courant.lexeme.equals("}"))
                && courant.type != AnalyseurLexicale.TokenType.EOF) {
            Instruction();
            skipInsignificant();
        }

        if (!match("}"))
            erreur("'}' attendu pour fermer le bloc");
    }

    public void InstructionIgnoree() {
        
        avancer();
    }

    
    public static void analyser(String code) {

        AnalyseurLexicale.Lexer lx = new AnalyseurLexicale.Lexer(code);
        List<AnalyseurLexicale.Token> tks = lx.analyserLexicale();

        AnalyseurSyntaxique2 parser = new AnalyseurSyntaxique2(tks);

        parser.Programme();

        System.out.println("✔ Programme syntaxiquement correct !");
    }

    public static void main(String[] args) {
        String code = """
                var x = 10;
                let y = x + 5;
                const z = "Hello";

                class MaClasse {
                    maMethode() {
                    }
    }

                try {
                    
                } catch (e) {
                    
                } finally {
                    
                }
                """;

        analyser(code);
    }
}
