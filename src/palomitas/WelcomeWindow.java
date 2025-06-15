package palomitas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class WelcomeWindow extends JFrame {

    public WelcomeWindow() {
        setTitle("Palomitas에 오신 것을 환영합니다!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 300);
        setLocationRelativeTo(null); // 화면 중앙에 표시
        
        // 전체 레이아웃 설정
        setLayout(new BorderLayout(20, 20));
        
        // 상단 제목 레이블
        JLabel titleLabel = new JLabel("무엇을 하시겠어요?", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // 위쪽 여백
        add(titleLabel, BorderLayout.NORTH);

        // 중앙 버튼 패널
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // 1행 2열, 버튼 사이 수평 간격 20
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 60, 40)); // 패널 내부 여백 (상, 좌, 하, 우)

        JButton startQuizButton = new JButton("퀴즈 풀기");
        JButton manageWordsButton = new JButton("단어장 관리");

        // 버튼 폰트 및 크기 설정
        Font buttonFont = new Font("SansSerif", Font.PLAIN, 18);
        startQuizButton.setFont(buttonFont);
        manageWordsButton.setFont(buttonFont);

        // 버튼에 액션 리스너 추가
        startQuizButton.addActionListener(e -> openQuizDialog());
        manageWordsButton.addActionListener(e -> openWordManager());

        buttonPanel.add(startQuizButton);
        buttonPanel.add(manageWordsButton);
        add(buttonPanel, BorderLayout.CENTER);

        // 창을 보이게 함
        setVisible(true);
    }

    private void openQuizDialog() {
        // 퀴즈를 시작하기 위해 단어 목록을 불러옵니다.
        WordManager wordManager = new WordManager();
        List<Word> allWords = wordManager.getAllWords();

        if (allWords.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "퀴즈를 시작하려면 단어장에 단어가 하나 이상 있어야 합니다.", 
                "퀴즈 불가", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // MainApp에 있던 퀴즈 시작 로직을 그대로 가져옵니다.
        QuizSetupDialog setupDialog = new QuizSetupDialog(this, allWords);
        setupDialog.setVisible(true);

        if (setupDialog.didUserStartQuiz() && setupDialog.getSelectedQuestionCount() > 0) {
            Quiz.QuizType type = setupDialog.getSelectedQuizType();
            int count = setupDialog.getSelectedQuestionCount();

            Quiz quiz = new Quiz(allWords, type, count);
            if (quiz.getTotalQuestionsInQuiz() > 0) {
                 QuizWindow quizWindow = new QuizWindow(this, quiz);
                 quizWindow.setVisible(true);
            } else {
                 JOptionPane.showMessageDialog(this, 
                    "퀴즈에 출제할 문제가 없습니다. 단어 수나 문제 설정을 확인해주세요.", 
                    "퀴즈 오류", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void openWordManager() {
        // 단어장 관리 창(MainApp)을 엽니다.
        new MainApp();
        // 그리고 현재 시작 화면은 닫습니다.
        dispose();
    }
}