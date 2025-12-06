 import javax.swing.SwingUtilities;
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MiniCompilerUI ui = new MiniCompilerUI();
            ui.setVisible(true);
        });
    }
}
