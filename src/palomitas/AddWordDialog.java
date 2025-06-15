package palomitas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddWordDialog extends JDialog {
    private JTextField spanishField;
    private JTextField koreanField;
    private JTextField exampleField;
    private JButton saveButton;
    private JButton cancelButton;
    private Word currentWord; // 수정 시 사용될 단어
    private WordManager wordManager;
    private Runnable onSaveAction; // 저장 후 실행될 액션 (예: 테이블 새로고침)

    // 단어 추가용 생성자
    public AddWordDialog(Frame owner, WordManager wordManager, Runnable onSave) {
        super(owner, "단어 추가", true); // true: Modal 다이얼로그
        this.wordManager = wordManager;
        this.onSaveAction = onSave;
        this.currentWord = null; // 새 단어 추가 모드
        initUI();
    }

    // 단어 수정용 생성자
    public AddWordDialog(Frame owner, WordManager wordManager, Word wordToEdit, Runnable onSave) {
        super(owner, "단어 수정", true);
        this.wordManager = wordManager;
        this.currentWord = wordToEdit;
        this.onSaveAction = onSave;
        initUI();
    }


    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 220); // 높이 약간 늘림
        setLocationRelativeTo(getOwner()); // 부모 창 중앙에 표시

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("스페인어:"));
        spanishField = new JTextField();
        formPanel.add(spanishField);

        formPanel.add(new JLabel("한국어 뜻:"));
        koreanField = new JTextField();
        formPanel.add(koreanField);

        formPanel.add(new JLabel("예문 (선택):"));
        exampleField = new JTextField();
        formPanel.add(exampleField);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        saveButton = new JButton("저장");
        cancelButton = new JButton("취소");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveWord();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 다이얼로그 창 닫기
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 수정 모드일 때 필드 채우기 및 창 제목 변경
        if (currentWord != null) {
            setTitle("단어 수정"); // 창 제목 변경
            spanishField.setText(currentWord.getSpanish());
            // 수정 모드에서는 스페인어 단어(PK)는 변경할 수 없도록 하는 것이 일반적입니다.
            // spanishField.setEditable(false); // 필요에 따라 주석 해제
            koreanField.setText(currentWord.getKorean());
            exampleField.setText(currentWord.getExample());
        } else {
            setTitle("단어 추가");
        }
    }

    private void saveWord() {
        String spanish = spanishField.getText().trim();
        String korean = koreanField.getText().trim();
        String example = exampleField.getText().trim();

        if (spanish.isEmpty() || korean.isEmpty()) {
            JOptionPane.showMessageDialog(this, "스페인어와 한국어 뜻은 필수 입력 항목입니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (currentWord == null) { // 새 단어 추가 모드
            // 추가하려는 스페인어 단어가 이미 존재하는지 확인
            if (wordManager.getAllWords().stream().anyMatch(w -> w.getSpanish().equalsIgnoreCase(spanish))) {
                JOptionPane.showMessageDialog(this, "이미 존재하는 스페인어 단어입니다.", "중복 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Word newWord = new Word(spanish, korean, example);
            wordManager.addWord(newWord);
        } else { // 기존 단어 수정 모드
            String oldSpanish = currentWord.getSpanish(); // 원래 스페인어 단어 (PK)

            // 스페인어 단어(PK)가 변경되었는지 확인
            if (!oldSpanish.equalsIgnoreCase(spanish)) {
                // PK가 변경되었다면, 변경하려는 새 스페인어 단어가 다른 단어와 중복되는지 확인
                 if (wordManager.getAllWords().stream().anyMatch(w -> w.getSpanish().equalsIgnoreCase(spanish))) {
                    JOptionPane.showMessageDialog(this, "수정하려는 스페인어 단어 '" + spanish + "'가 이미 다른 단어로 존재합니다.", "중복 오류", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            // Word 객체 정보 업데이트
            currentWord.setSpanish(spanish); // spanishField가 editable=true일 때만 의미 있음
            currentWord.setKorean(korean);
            currentWord.setExample(example);
            wordManager.updateWord(oldSpanish, currentWord); // WordManager를 통해 업데이트
        }

        if (onSaveAction != null) {
            onSaveAction.run(); // 저장 후 실행할 동작 (예: 테이블 새로고침)
        }
        dispose(); // 다이얼로그 창 닫기
    }
}