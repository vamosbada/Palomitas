package palomitas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Quiz {
    public enum QuizType {
        SPANISH_TO_KOREAN_SHORT_ANSWER,
        SPANISH_TO_KOREAN_MULTIPLE_CHOICE,
        KOREAN_TO_SPANISH_SHORT_ANSWER,
        KOREAN_TO_SPANISH_MULTIPLE_CHOICE
    }

    private List<Word> allWords;
    private List<Word> quizWords;
    private List<Word> wrongAnswers; // <<<--- 틀린 단어를 저장할 리스트 추가
    private QuizType quizType;
    private int numberOfQuestions;
    private int currentQuestionIndex;
    private int score;
    private int passedCount;

    private static final int MCQ_CHOICES_COUNT = 4;

    public Quiz(List<Word> allWordsFromManager, QuizType quizType, int requestedNumQuestions) {
        this.allWords = new ArrayList<>(allWordsFromManager);
        this.quizType = quizType;
        this.numberOfQuestions = Math.min(requestedNumQuestions, this.allWords.size());
        this.quizWords = new ArrayList<>();
        this.wrongAnswers = new ArrayList<>(); // <<<--- 리스트 초기화
        this.currentQuestionIndex = -1;
        this.score = 0;
        this.passedCount = 0;
        setupQuizWords();
    }

    private void setupQuizWords() {
        if (allWords.isEmpty() || numberOfQuestions == 0) {
            return;
        }
        Collections.shuffle(allWords);
        for (int i = 0; i < numberOfQuestions; i++) {
            quizWords.add(allWords.get(i));
        }
    }

    public boolean hasNextQuestion() {
        return currentQuestionIndex < quizWords.size() - 1;
    }

    public Word nextQuestion() {
        if (hasNextQuestion()) {
            currentQuestionIndex++;
            return quizWords.get(currentQuestionIndex);
        }
        return null;
    }

    public Word getCurrentQuestion() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < quizWords.size()) {
            return quizWords.get(currentQuestionIndex);
        }
        return null;
    }
    
    // <<<--- 오답 기록을 위해 checkAnswer 메소드들 수정
    public boolean checkAnswer(String userAnswer) {
        Word currentWord = getCurrentQuestion();
        if (currentWord == null || userAnswer == null) return false;

        boolean isCorrect;
        String correctAnswer = "";

        switch (quizType) {
            case SPANISH_TO_KOREAN_SHORT_ANSWER:
                correctAnswer = currentWord.getKorean();
                break;
            case KOREAN_TO_SPANISH_SHORT_ANSWER:
                correctAnswer = currentWord.getSpanish();
                break;
            default:
                return false;
        }
        isCorrect = correctAnswer.equalsIgnoreCase(userAnswer.trim());
        if (isCorrect) {
            score++;
        } else {
            wrongAnswers.add(currentWord); // 틀렸을 때 리스트에 추가
        }
        return isCorrect;
    }

    public boolean checkAnswerMcq(String selectedChoiceText) {
        Word currentWord = getCurrentQuestion();
        if (currentWord == null || selectedChoiceText == null) return false;

        String correctAnswerText = "";
         switch (quizType) {
            case SPANISH_TO_KOREAN_MULTIPLE_CHOICE:
                correctAnswerText = currentWord.getKorean();
                break;
            case KOREAN_TO_SPANISH_MULTIPLE_CHOICE:
                correctAnswerText = currentWord.getSpanish();
                break;
            default:
                return false;
        }

        boolean isCorrect = correctAnswerText.equalsIgnoreCase(selectedChoiceText.trim());
        if (isCorrect) {
            score++;
        } else {
            wrongAnswers.add(currentWord); // 틀렸을 때 리스트에 추가
        }
        return isCorrect;
    }


    public void passQuestion() {
        passedCount++;
        // '패스'한 문제도 오답으로 간주하여 기록하고 싶다면 아래 주석을 해제
        // if (getCurrentQuestion() != null) {
        //     wrongAnswers.add(getCurrentQuestion());
        // }
    }

    public int getScore() { return score; }
    public int getTotalQuestionsInQuiz() { return quizWords.size(); }
    public int getPassedCount() { return passedCount; }
    public QuizType getQuizType() { return quizType; }
    public List<Word> getWrongAnswers() { return wrongAnswers; } // <<<--- 오답 리스트를 반환하는 getter 추가

    public List<String> generateMultipleChoices() {
        Word correctAnswerWord = getCurrentQuestion();
        if (correctAnswerWord == null) return new ArrayList<>();

        List<String> choices = new ArrayList<>();
        String correctChoiceText;

        if (quizType == QuizType.SPANISH_TO_KOREAN_MULTIPLE_CHOICE) {
            correctChoiceText = correctAnswerWord.getKorean();
        } else if (quizType == QuizType.KOREAN_TO_SPANISH_MULTIPLE_CHOICE) {
            correctChoiceText = correctAnswerWord.getSpanish();
        } else {
            return choices;
        }
        choices.add(correctChoiceText);

        List<Word> otherWords = new ArrayList<>(this.allWords);
        otherWords.remove(correctAnswerWord);
        Collections.shuffle(otherWords);

        for (Word otherWord : otherWords) {
            if (choices.size() >= MCQ_CHOICES_COUNT) break;

            String wrongChoiceText = "";
            if (quizType == QuizType.SPANISH_TO_KOREAN_MULTIPLE_CHOICE) {
                wrongChoiceText = otherWord.getKorean();
            } else if (quizType == QuizType.KOREAN_TO_SPANISH_MULTIPLE_CHOICE) {
                wrongChoiceText = otherWord.getSpanish();
            }

            if (!wrongChoiceText.equalsIgnoreCase(correctChoiceText) && !choices.contains(wrongChoiceText) && !wrongChoiceText.trim().isEmpty()) {
                choices.add(wrongChoiceText);
            }
        }
        
        Collections.shuffle(choices);
        return choices;
    }
}