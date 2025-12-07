import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class MiniCompilerUI extends JFrame {

    private JTextArea sourceArea;
    private JTextArea resultArea;
    private JTable tokenTable;
    private TokenTableModel tokenTableModel;
    private JLabel statusLabel;

    public MiniCompilerUI() {
        super("Compilateur Try-catch - LL(1)");

        initLookAndFeel();
        initComponents();
        initLayout();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null); 
    }

    
    private void initLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void initComponents() {
        
        sourceArea = new JTextArea();
        sourceArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        sourceArea.setTabSize(4);
        sourceArea.setText("""
                try {
                   let y = 5;
                } catch(exption){
                 
                }

                """);

        
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultArea.setBackground(new Color(245, 245, 245));

        
        tokenTableModel = new TokenTableModel();
        tokenTable = new JTable(tokenTableModel);
        tokenTable.setFillsViewportHeight(true);
        tokenTable.setRowHeight(22);

        
        statusLabel = new JLabel("Prêt.");
        statusLabel.setBorder(new EmptyBorder(4, 8, 4, 8));

        
        JButton analyseButton = new JButton("Analyser");
        analyseButton.setFocusPainted(false);
        analyseButton.addActionListener(this::onAnalyse);

        
        JPanel topBar = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Compilateur Try-Catch - LL(1)");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        topBar.add(title, BorderLayout.WEST);
        topBar.add(analyseButton, BorderLayout.EAST);
        topBar.setBorder(new EmptyBorder(6, 6, 6, 6));

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(createCenterSplitPane(), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private Component createCenterSplitPane() {
        
        JScrollPane sourceScroll = new JScrollPane(sourceArea);
        sourceScroll.setBorder(BorderFactory.createTitledBorder("Code source"));

        
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Résultat analyse"));

        
        JScrollPane tokenScroll = new JScrollPane(tokenTable);
        tokenScroll.setBorder(BorderFactory.createTitledBorder("Tokens"));

        
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tokenScroll, resultScroll);
        rightSplit.setResizeWeight(0.5);

        
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sourceScroll, rightSplit);
        mainSplit.setResizeWeight(0.5);

        return mainSplit;
    }

    private void initLayout() {
        
    }

    
    private void onAnalyse(ActionEvent e) {
        String src = sourceArea.getText();
        resultArea.setText("");
        tokenTableModel.setTokens(new ArrayList<>());
        statusLabel.setText("Analyse en cours...");

        try {
            
            AnalyseurLexicale.Lexer lexer = new AnalyseurLexicale.Lexer(src);
            java.util.List<AnalyseurLexicale.Token> tokens = lexer.analyserLexicale();

            
            tokenTableModel.setTokens(tokens);

            
            resultArea.append("Analyse lexicale terminée ✅\n");
            statusLabel.setText("Analyse lexicale terminée.");

            
            javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    
                    AnalyseurSyntaxique2 parser = new AnalyseurSyntaxique2(tokens);
                    parser.Programme();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); 
                        resultArea.append("Analyse syntaxique terminée ✅\n");
                        statusLabel.setText("Analyse terminée.");
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        resultArea.append("❌ Erreur syntaxique :\n" + cause.getMessage() + "\n");
                        statusLabel.setText("Erreur pendant l'analyse syntaxique.");
                    }
                }
            };
            worker.execute();
        } catch (RuntimeException ex) {
            
            resultArea.append("❌ Erreur :\n");
            resultArea.append(ex.getMessage() + "\n");
            statusLabel.setText("Erreur pendant l'analyse.");
        }
    }

    
    private static class TokenTableModel extends javax.swing.table.AbstractTableModel {
        private final String[] columns = { "#", "Type", "Lexème", "Ligne", "Colonne" };
        private java.util.List<AnalyseurLexicale.Token> data = new ArrayList<>();

        public void setTokens(java.util.List<AnalyseurLexicale.Token> tokens) {
            this.data = tokens;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            AnalyseurLexicale.Token t = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> rowIndex;
                case 1 -> t.type.toString();
                case 2 -> t.lexeme;
                case 3 -> t.line;
                case 4 -> t.column;
                default -> "";
            };
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0, 3, 4 -> Integer.class;
                default -> String.class;
            };
        }
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MiniCompilerUI ui = new MiniCompilerUI();
            ui.setVisible(true);
        });
    }
}
