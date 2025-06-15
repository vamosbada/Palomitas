package palomitas;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*; // <<<--- 단축키 때문에 KeyEvent 추가
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MainApp {
    private JFrame frame;
    private WordManager wordManager;
    private JTable wordTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    public MainApp() {
        wordManager = new WordManager();
        initializeUI();
        loadWordsToTable();
    }

    private void initializeUI() {
        frame = new JFrame("Palomitas - 단어장 관리");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // --- 메뉴 바 ---
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("파일");
        JMenuItem importCsvItem = new JMenuItem("CSV 가져오기...");
        JMenuItem exportCsvItem = new JMenuItem("CSV 내보내기...");
        JMenuItem exitItem = new JMenuItem("종료");

        // <<<--- 단축키 설정 추가
        int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        importCsvItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutMask)); // Cmd/Ctrl + O
        exportCsvItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcutMask)); // Cmd/Ctrl + S
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, shortcutMask));      // Cmd/Ctrl + Q

        importCsvItem.addActionListener(e -> importCsv());
        exportCsvItem.addActionListener(e -> exportCsv());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(importCsvItem);
        fileMenu.add(exportCsvItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);
        
        // --- 상단 패널 ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton addButton = new JButton("단어 추가");
        JButton quizButton = new JButton("퀴즈 시작");
        JButton deleteButton = new JButton("선택 단어 삭제");
        
        // <<<--- '단어 추가' 버튼에 단축키 툴팁 추가
        addButton.setToolTipText("새 단어를 추가합니다. (단축키: Cmd/Ctrl + N)");

        addButton.addActionListener(e -> openAddOrEditWordDialog(null));
        quizButton.addActionListener(e -> openQuizSetupDialog());
        deleteButton.addActionListener(e -> deleteSelectedWord());
        
        // <<<--- '단어 추가' 버튼에 실제 단축키 기능 연결
        KeyStroke addKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcutMask);
        addButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(addKeyStroke, "addWordAction");
        addButton.getActionMap().put("addWordAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAddOrEditWordDialog(null);
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(quizButton);
        buttonPanel.add(deleteButton);
        topPanel.add(buttonPanel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchPanel.add(new JLabel("검색:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.EAST);

        frame.add(topPanel, BorderLayout.NORTH);

        // --- 중앙 패널 ---
        String[] columnNames = {"스페인어", "한국어 뜻", "예문"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        wordTable = new JTable(tableModel);
        wordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wordTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        wordTable.setRowHeight(25);
        wordTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        wordTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table = (JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);
                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int modelRow = table.convertRowIndexToModel(row);
                    String spanishToEdit = (String) tableModel.getValueAt(modelRow, 0);
                    Word wordToEdit = wordManager.getAllWords().stream()
                            .filter(w -> w.getSpanish().equals(spanishToEdit))
                            .findFirst().orElse(null);
                    if (wordToEdit != null) {
                        openAddOrEditWordDialog(wordToEdit);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(wordTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        // <<<--- 테이블 정렬 기능은 이 코드로 이미 구현되어 있습니다!
        // 컬럼 헤더를 클릭하면 정렬이 됩니다.
        sorter = new TableRowSorter<>(tableModel);
        wordTable.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void insertUpdate(DocumentEvent e) { filterTable(); }
        });

        frame.setVisible(true);
    }
    
    // 이 아래 메소드들은 변경사항 없습니다.
    private void filterTable() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            try {
                List<RowFilter<Object, Object>> rfs = new ArrayList<>();
                rfs.add(RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 0));
                rfs.add(RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 1));
                rfs.add(RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 2));
                sorter.setRowFilter(RowFilter.orFilter(rfs));
            } catch (PatternSyntaxException e) {
                System.err.println("검색어 정규식 오류: " + e.getMessage());
                sorter.setRowFilter(null);
            }
        }
    }

    private void loadWordsToTable() {
        tableModel.setRowCount(0);
        List<Word> words = wordManager.getAllWords();
        for (Word word : words) {
            tableModel.addRow(new Object[]{word.getSpanish(), word.getKorean(), word.getExample()});
        }
    }

    private void openAddOrEditWordDialog(Word wordToEdit) {
        AddWordDialog dialog;
        if (wordToEdit == null) {
            dialog = new AddWordDialog(frame, wordManager, this::loadWordsToTable);
        } else {
            dialog = new AddWordDialog(frame, wordManager, wordToEdit, this::loadWordsToTable);
        }
        dialog.setVisible(true);
    }

    private void deleteSelectedWord() {
        int selectedViewRow = wordTable.getSelectedRow();
        if (selectedViewRow >= 0) {
            int modelRow = wordTable.convertRowIndexToModel(selectedViewRow);
            String spanishWord = (String) tableModel.getValueAt(modelRow, 0);
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "'" + spanishWord + "' 단어를 정말 삭제하시겠습니까?",
                    "단어 삭제 확인",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                if (wordManager.removeWord(spanishWord)) {
                    loadWordsToTable();
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "삭제할 단어를 테이블에서 선택해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openQuizSetupDialog() {
        List<Word> allWords = wordManager.getAllWords();
        if (allWords.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "퀴즈를 시작하려면 단어장에 단어가 하나 이상 있어야 합니다.", "퀴즈 불가", JOptionPane.WARNING_MESSAGE);
            return;
        }
        QuizSetupDialog setupDialog = new QuizSetupDialog(frame, allWords);
        setupDialog.setVisible(true);
        if (setupDialog.didUserStartQuiz() && setupDialog.getSelectedQuestionCount() > 0) {
            Quiz.QuizType type = setupDialog.getSelectedQuizType();
            int count = setupDialog.getSelectedQuestionCount();
            Quiz quiz = new Quiz(allWords, type, count);
            if (quiz.getTotalQuestionsInQuiz() > 0) {
                 QuizWindow quizWindow = new QuizWindow(frame, quiz);
                 quizWindow.setVisible(true);
            }
        }
    }

    private void importCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("가져올 CSV 파일 선택");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV 파일 (*.csv)", "csv"));
        int userSelection = fileChooser.showOpenDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToImport = fileChooser.getSelectedFile();
            List<String> messages = wordManager.importWordsFromCsv(fileToImport.getAbsolutePath());
            loadWordsToTable();
            JOptionPane.showMessageDialog(frame, String.join("\n", messages), "CSV 가져오기 결과", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportCsv() {
        if (wordManager.getAllWords().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "내보낼 단어가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("내보낼 CSV 파일 위치 및 이름 지정");
        fileChooser.setSelectedFile(new File("palomitas_단어장.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV 파일 (*.csv)", "csv"));
        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                fileToSave = new File(filePath + ".csv");
            }
            if (fileToSave.exists()) {
                int response = JOptionPane.showConfirmDialog(frame,
                        "이미 파일이 존재합니다. 덮어쓰시겠습니까?",
                        "파일 덮어쓰기 확인",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            if (wordManager.exportWordsToCsv(fileToSave.getAbsolutePath())) {
                JOptionPane.showMessageDialog(frame, "단어장이 성공적으로 내보내졌습니다.", "CSV 내보내기 성공", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "단어장 내보내기에 실패했습니다.", "CSV 내보내기 오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        try {
             System.setProperty("apple.awt.application.name", "Palomitas");
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            try {
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("시스템 LookAndFeel 설정 실패: " + e.getMessage());
            }
            new WelcomeWindow();
        });
    }
}