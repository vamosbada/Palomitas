package palomitas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class QuizResultDialog extends JDialog {

    public QuizResultDialog(Frame owner, int score, int totalQuestions, int passedCount, List<Word> wrongAnswers) {
        super(owner, "퀴즈 결과", true); // Modal 다이얼로그
        setSize(500, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // 1. 상단: 결과 요약 패널
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        int attempted = totalQuestions - passedCount;
        double accuracy = (attempted > 0) ? ((double) score / attempted) * 100 : 0;
        if (score > 0 && attempted == 0) accuracy = 100; // 모든 문제를 맞히고 나머지는 패스한 경우

        String summaryText = String.format(
            "총 문제: %d | 맞춘 개수: %d | 패스: %d | 정답률(패스 제외): %.1f%%",
            totalQuestions, score, passedCount, accuracy
        );
        topPanel.add(new JLabel(summaryText));
        add(topPanel, BorderLayout.NORTH);

        // 2. 중앙: 오답 목록 테이블
        String[] columnNames = {"틀린 단어 (스페인어)", "정답 (한국어 뜻)"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Word word : wrongAnswers) {
            tableModel.addRow(new Object[]{word.getSpanish(), word.getKorean()});
        }

        JTable wrongWordsTable = new JTable(tableModel);
        wrongWordsTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(wrongWordsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("오답 노트")); // 제목 추가
        add(scrollPane, BorderLayout.CENTER);

        // 3. 하단: 닫기 버튼
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JButton closeButton = new JButton("닫기");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}