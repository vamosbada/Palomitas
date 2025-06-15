package palomitas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizWindow extends JDialog {
    private Quiz quizLogic;
    private JLabel questionLabel;
    private JTextField answerField;
    private JPanel choicesPanel;
    private List<JButton> choiceButtons;

    private JLabel timerLabel;
    private JLabel feedbackLabel;
    private JButton submitButton;
    private JButton showAnswerButton;
    private JButton nextButton;

    private Timer questionTimer;
    private static final int TIME_LIMIT_SECONDS = 15;
    private int timeLeft;

    public QuizWindow(Frame owner, Quiz quiz) {
        super(owner, "퀴즈 진행", true);
        this.quizLogic = quiz;
        initUI();
        if (this.quizLogic.getTotalQuestionsInQuiz() > 0) {
            loadNextQuestionOrShowResults();
        } else {
            JOptionPane.showMessageDialog(this, "출제할 문제가 없습니다.", "퀴즈 오류", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> dispose());
        }
    }

    private void initUI() {
        setSize(600, 450);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        questionLabel = new JLabel("문제 로딩 중...", SwingConstants.CENTER);
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        topPanel.add(questionLabel, BorderLayout.CENTER);

        timerLabel = new JLabel("시간: " + TIME_LIMIT_SECONDS + "초", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        topPanel.add(timerLabel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanelContainer = new JPanel(new GridBagLayout());
        GridBagConstraints gbcCenter = new GridBagConstraints();
        gbcCenter.gridx = 0;
        gbcCenter.gridy = 0;
        gbcCenter.weightx = 1;
        gbcCenter.weighty = 1;
        gbcCenter.anchor = GridBagConstraints.CENTER;
        gbcCenter.fill = GridBagConstraints.NONE;

        answerField = new JTextField(25);
        answerField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        answerField.setHorizontalAlignment(JTextField.CENTER);
        answerField.addActionListener(e -> handleSubmitOrMCQAction());

        choicesPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        choiceButtons = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            JButton choiceBtn = new JButton();
            choiceBtn.setFont(new Font("SansSerif", Font.PLAIN, 16));
            choiceBtn.setPreferredSize(new Dimension(200, 60));
            choiceBtn.addActionListener(e -> handleMCQChoiceSelection(choiceBtn.getText()));
            choiceButtons.add(choiceBtn);
            choicesPanel.add(choiceBtn);
        }

        if (quizLogic.getQuizType() == Quiz.QuizType.SPANISH_TO_KOREAN_SHORT_ANSWER ||
            quizLogic.getQuizType() == Quiz.QuizType.KOREAN_TO_SPANISH_SHORT_ANSWER) {
            centerPanelContainer.add(answerField, gbcCenter);
        } else {
            centerPanelContainer.add(choicesPanel, gbcCenter);
        }
        add(centerPanelContainer, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));

        feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
        feedbackLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        bottomPanel.add(feedbackLabel, BorderLayout.NORTH);

        JPanel buttonControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        submitButton = new JButton("제출");
        showAnswerButton = new JButton("정답 보기");
        nextButton = new JButton("다음 문제");
        nextButton.setVisible(false);

        submitButton.addActionListener(e -> handleSubmitOrMCQAction());
        showAnswerButton.addActionListener(e -> handleShowAnswer());
        nextButton.addActionListener(e -> loadNextQuestionOrShowResults());

        if (quizLogic.getQuizType() == Quiz.QuizType.SPANISH_TO_KOREAN_SHORT_ANSWER ||
            quizLogic.getQuizType() == Quiz.QuizType.KOREAN_TO_SPANISH_SHORT_ANSWER) {
            buttonControlPanel.add(submitButton);
        }
        buttonControlPanel.add(showAnswerButton);
        buttonControlPanel.add(nextButton);
        bottomPanel.add(buttonControlPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        questionTimer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("시간: " + timeLeft + "초");
            if (timeLeft <= 5) {
                timerLabel.setForeground(Color.RED);
            }
            if (timeLeft <= 0) {
                handleTimeOut();
            }
        });
    }

    private void loadNextQuestionOrShowResults() {
        if (quizLogic.hasNextQuestion()) {
            Word currentQ = quizLogic.nextQuestion();
            questionLabel.setText("<html><div style='text-align: center;'>" + getQuestionText(currentQ) + "</div></html>");
            feedbackLabel.setText(" ");
            timerLabel.setForeground(Color.BLACK);
            answerField.setText("");
            answerField.setEditable(true);
            submitButton.setEnabled(true);
            showAnswerButton.setEnabled(true);
            nextButton.setText("다음 문제");
            nextButton.setVisible(false);

            if (quizLogic.getQuizType() == Quiz.QuizType.SPANISH_TO_KOREAN_MULTIPLE_CHOICE ||
                quizLogic.getQuizType() == Quiz.QuizType.KOREAN_TO_SPANISH_MULTIPLE_CHOICE) {
                submitButton.setVisible(false);
                setupMultipleChoiceOptions();
            } else {
                submitButton.setVisible(true);
                answerField.requestFocusInWindow();
            }

            timeLeft = TIME_LIMIT_SECONDS;
            timerLabel.setText("시간: " + timeLeft + "초");
            questionTimer.start();
        } else {
            showResults();
        }
    }

    private String getQuestionText(Word q) {
        if (q == null) return "문제를 가져올 수 없습니다.";
        switch (quizLogic.getQuizType()) {
            case SPANISH_TO_KOREAN_SHORT_ANSWER:
            case SPANISH_TO_KOREAN_MULTIPLE_CHOICE:
                return "다음 스페인어 단어의 뜻은?<br><br><b>" + q.getSpanish() + "</b>";
            case KOREAN_TO_SPANISH_SHORT_ANSWER:
            case KOREAN_TO_SPANISH_MULTIPLE_CHOICE:
                return "다음 한국어 뜻의 스페인어 단어는?<br><br><b>" + q.getKorean() + "</b>";
            default:
                return "알 수 없는 퀴즈 유형입니다.";
        }
    }

    private void setupMultipleChoiceOptions() {
        List<String> choices = quizLogic.generateMultipleChoices();
        for (int i = 0; i < choiceButtons.size(); i++) {
            if (i < choices.size()) {
                choiceButtons.get(i).setText(choices.get(i));
                choiceButtons.get(i).setEnabled(true);
                choiceButtons.get(i).setVisible(true);
            } else {
                choiceButtons.get(i).setText("");
                choiceButtons.get(i).setVisible(false);
            }
        }
    }

    private void handleSubmitOrMCQAction() {
        if (quizLogic.getQuizType() == Quiz.QuizType.SPANISH_TO_KOREAN_SHORT_ANSWER ||
            quizLogic.getQuizType() == Quiz.QuizType.KOREAN_TO_SPANISH_SHORT_ANSWER) {
            questionTimer.stop();
            String userAnswer = answerField.getText();
            boolean isCorrect = quizLogic.checkAnswer(userAnswer);
            showFeedback(isCorrect, getCorrectAnswerText());
            prepareForNextQuestion();
        }
    }

    private void handleMCQChoiceSelection(String selectedChoiceText) {
        questionTimer.stop();
        boolean isCorrect = quizLogic.checkAnswerMcq(selectedChoiceText);
        showFeedback(isCorrect, getCorrectAnswerText());
        prepareForNextQuestion();
    }


    private void handleShowAnswer() {
        questionTimer.stop();
        quizLogic.passQuestion();
        feedbackLabel.setText("<html><font color='blue'>정답: " + getCorrectAnswerText() + "</font></html>");
        feedbackLabel.setForeground(Color.BLUE);
        prepareForNextQuestion();
    }

    private void handleTimeOut() {
        questionTimer.stop();
        timerLabel.setText("시간 종료!");
        timerLabel.setForeground(Color.RED);
        quizLogic.passQuestion();
        feedbackLabel.setText("<html><font color='orange'>시간 초과!</font> 정답: " + getCorrectAnswerText() + "</html>");
        feedbackLabel.setForeground(Color.ORANGE);
        prepareForNextQuestion();
    }

    private void showFeedback(boolean isCorrect, String correctAnswer) {
        if (isCorrect) {
            feedbackLabel.setText("<html><font color='green'>정답입니다!</font></html>");
            feedbackLabel.setForeground(Color.GREEN);
        } else {
            feedbackLabel.setText("<html><font color='red'>오답입니다.</font> 정답: " + correctAnswer + "</html>");
            feedbackLabel.setForeground(Color.RED);
        }
    }

    private String getCorrectAnswerText() {
        Word currentQ = quizLogic.getCurrentQuestion();
        if (currentQ == null) return "";
        switch (quizLogic.getQuizType()) {
            case SPANISH_TO_KOREAN_SHORT_ANSWER:
            case SPANISH_TO_KOREAN_MULTIPLE_CHOICE:
                return currentQ.getKorean();
            case KOREAN_TO_SPANISH_SHORT_ANSWER:
            case KOREAN_TO_SPANISH_MULTIPLE_CHOICE:
                return currentQ.getSpanish();
            default:
                return "";
        }
    }

    private void prepareForNextQuestion() {
        answerField.setEditable(false);
        submitButton.setEnabled(false);
        showAnswerButton.setEnabled(false);
        for (JButton btn : choiceButtons) {
            btn.setEnabled(false);
        }
        nextButton.setVisible(true);
        if (!quizLogic.hasNextQuestion()) {
            nextButton.setText("결과 보기");
        }
        nextButton.requestFocusInWindow();
    }

    // <<<--- 여기가 수정되었습니다! JOptionPane 대신 새로 만든 QuizResultDialog를 띄웁니다.
    private void showResults() {
        questionTimer.stop();
        
        // 새로 만든 결과/오답노트 창을 띄웁니다.
        QuizResultDialog resultDialog = new QuizResultDialog(
            (Frame) getOwner(),
            quizLogic.getScore(),
            quizLogic.getTotalQuestionsInQuiz(),
            quizLogic.getPassedCount(),
            quizLogic.getWrongAnswers()
        );
        
        dispose(); // 현재 퀴즈 창을 닫고
        resultDialog.setVisible(true); // 새 결과 창을 보여줍니다.
    }

    @Override
    public void dispose() {
        if (questionTimer != null && questionTimer.isRunning()) {
            questionTimer.stop();
        }
        super.dispose();
    }
}