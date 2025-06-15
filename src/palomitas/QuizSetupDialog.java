package palomitas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List; // List import 확인

public class QuizSetupDialog extends JDialog {
    private JComboBox<String> quizTypeComboBox;
    private JSpinner questionCountSpinner;
    private JButton startButton;
    private JButton cancelButton;

    private Quiz.QuizType selectedQuizType; // 사용자가 선택한 퀴즈 유형
    private int selectedQuestionCount;    // 사용자가 선택한 문제 수
    private boolean quizStarted = false; // 사용자가 "퀴즈 시작"을 눌렀는지 여부

    private List<Word> allWordsForQuiz; // 퀴즈 생성을 위해 전달받은 전체 단어 목록

    public QuizSetupDialog(Frame owner, List<Word> allWords) {
        super(owner, "퀴즈 설정", true); // Modal 다이얼로그
        this.allWordsForQuiz = allWords;
        this.selectedQuestionCount = 0; // 초기화
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(380, 220); // 크기 약간 조정
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // X 버튼으로 닫힐 때 리소스 해제

        JPanel panel = new JPanel(new GridBagLayout()); // GridBagLayout으로 변경
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 컴포넌트 간 간격
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 퀴즈 유형 레이블
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("퀴즈 유형:"), gbc);

        // 퀴즈 유형 콤보박스
        String[] quizTypes = {
                "스페인어 제시 → 한국어 뜻 (주관식)",
                "스페인어 제시 → 한국어 뜻 (객관식)",
                "한국어 뜻 제시 → 스페인어 단어 (주관식)",
                "한국어 뜻 제시 → 스페인어 단어 (객관식)"
        };
        quizTypeComboBox = new JComboBox<>(quizTypes);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // 가로 공간을 채우도록
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(quizTypeComboBox, gbc);

        // 문제 개수 레이블
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("문제 개수:"), gbc);

        // 문제 개수 스피너
        int maxQuestions = allWordsForQuiz.isEmpty() ? 1 : allWordsForQuiz.size();
        // 기본값은 10개 또는 최대 문제 수 중 작은 값, 최소 1개
        int initialValue = Math.min(10, maxQuestions);
        if (initialValue == 0 && maxQuestions > 0) initialValue = 1; // 단어가 하나라도 있으면 최소 1개
        if (maxQuestions == 0) initialValue = 0; // 단어가 없으면 0

        SpinnerModel spinnerModel = new SpinnerNumberModel(initialValue, (maxQuestions > 0 ? 1 : 0), maxQuestions, 1);
        questionCountSpinner = new JSpinner(spinnerModel);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(questionCountSpinner, gbc);
        
        // 최대 문제 개수 안내 레이블 (부가 정보)
        JLabel infoLabel = new JLabel("(단어장에 총 " + allWordsForQuiz.size() + "개 단어)");
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.ITALIC, 10f));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST; // 왼쪽 위에 붙도록
        gbc.gridwidth = GridBagConstraints.REMAINDER; // 남은 공간 모두 사용
        panel.add(infoLabel, gbc);


        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        startButton = new JButton("퀴즈 시작");
        cancelButton = new JButton("취소");

        startButton.addActionListener(e -> handleStartQuiz());
        cancelButton.addActionListener(e -> {
            quizStarted = false; // 취소 시 플래그 false
            dispose();
        });

        buttonPanel.add(startButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 단어가 없으면 퀴즈 시작 버튼 등 비활성화
        if (allWordsForQuiz.isEmpty()) {
            startButton.setEnabled(false);
            quizTypeComboBox.setEnabled(false);
            questionCountSpinner.setEnabled(false);
            // 사용자에게 알림 (생성자에서 호출하거나, setVisible 전에 호출)
            // JOptionPane.showMessageDialog(this, "단어장에 단어가 없습니다. 먼저 단어를 추가해주세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleStartQuiz() {
        int selectedIndex = quizTypeComboBox.getSelectedIndex();
        switch (selectedIndex) {
            case 0: selectedQuizType = Quiz.QuizType.SPANISH_TO_KOREAN_SHORT_ANSWER; break;
            case 1: selectedQuizType = Quiz.QuizType.SPANISH_TO_KOREAN_MULTIPLE_CHOICE; break;
            case 2: selectedQuizType = Quiz.QuizType.KOREAN_TO_SPANISH_SHORT_ANSWER; break;
            case 3: selectedQuizType = Quiz.QuizType.KOREAN_TO_SPANISH_MULTIPLE_CHOICE; break;
            default: // 이론상 발생 안함
                JOptionPane.showMessageDialog(this, "올바른 퀴즈 유형을 선택해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
        }
        selectedQuestionCount = (Integer) questionCountSpinner.getValue();

        if (selectedQuestionCount <= 0 && !allWordsForQuiz.isEmpty()) { // 단어가 있는데 문제수가 0이면
            JOptionPane.showMessageDialog(this, "문제 개수는 1 이상이어야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
         if (allWordsForQuiz.isEmpty() && selectedQuestionCount > 0) { // 단어가 없는데 문제수가 0이 아니면 (사실상 스피너에서 막힘)
             JOptionPane.showMessageDialog(this, "단어장에 단어가 없어 퀴즈를 시작할 수 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
             return;
        }
         if (allWordsForQuiz.isEmpty() && selectedQuestionCount == 0) { //단어도 없고 문제수도 0이면 그냥 닫기
             quizStarted = false;
             dispose();
             return;
         }

        quizStarted = true; // "퀴즈 시작" 버튼 누름
        dispose(); // 설정 창 닫기
    }

    // MainApp에서 사용자가 선택한 설정을 가져갈 수 있도록 getter 메소드 추가
    public Quiz.QuizType getSelectedQuizType() {
        return selectedQuizType;
    }

    public int getSelectedQuestionCount() {
        return selectedQuestionCount;
    }

    public boolean didUserStartQuiz() { // 사용자가 "퀴즈 시작"을 눌렀는지 확인
        return quizStarted;
    }
}