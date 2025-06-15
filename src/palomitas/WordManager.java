package palomitas;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // 이 줄이 있는지 확인해주세요!

public class WordManager {
    private List<Word> wordList;
    private static final String DEFAULT_FILE_NAME = "words.csv"; // 기본 파일 이름
    private String filePath; // 파일 경로

    public WordManager() {
        this.wordList = new ArrayList<>();
        // 이클립스에서는 프로젝트 루트에 resources 폴더를 만들고 그 안에 저장됩니다.
        Path resourceDir = Paths.get("resources");
        try {
            if (!Files.exists(resourceDir)) {
                Files.createDirectories(resourceDir);
                System.out.println("'resources' 디렉토리를 생성했습니다.");
            }
            this.filePath = resourceDir.resolve(DEFAULT_FILE_NAME).toString();
            System.out.println("단어 파일 경로: " + this.filePath);
            loadWordsFromFile();
        } catch (IOException e) {
            System.err.println("'resources' 디렉토리 생성 또는 파일 경로 설정 오류: " + e.getMessage());
            // 대체 경로로 프로젝트 루트에 바로 저장
            this.filePath = DEFAULT_FILE_NAME;
            System.out.println("대체 단어 파일 경로: " + this.filePath);
            loadWordsFromFile();
        }
    }

    public WordManager(String customFilePath) {
        this.wordList = new ArrayList<>();
        this.filePath = customFilePath;
        loadWordsFromFile();
    }

    public List<Word> getAllWords() {
        return new ArrayList<>(wordList); // 외부에서 리스트를 직접 수정하지 못하도록 복사본 반환
    }

    public void addWord(Word word) {
        boolean isDuplicate = wordList.stream()
                                      .anyMatch(w -> w.getSpanish().equalsIgnoreCase(word.getSpanish()));
        if (!isDuplicate) {
            wordList.add(word);
            saveWordsToFile();
        } else {
            System.out.println("중복된 단어입니다 (추가 안됨): " + word.getSpanish());
            // GUI에서는 사용자에게 알림창을 띄워주는 것이 좋습니다.
            // JOptionPane.showMessageDialog(null, "이미 존재하는 스페인어 단어입니다.", "중복 오류", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean removeWord(String spanishWord) {
        Optional<Word> wordToRemove = wordList.stream()
                .filter(word -> word.getSpanish().equalsIgnoreCase(spanishWord))
                .findFirst();

        if (wordToRemove.isPresent()) {
            wordList.remove(wordToRemove.get());
            saveWordsToFile();
            return true;
        }
        return false;
    }

    public void updateWord(String oldSpanish, Word newWord) {
        for (int i = 0; i < wordList.size(); i++) {
            if (wordList.get(i).getSpanish().equalsIgnoreCase(oldSpanish)) {
                // 새 스페인어 단어가 기존 다른 단어와 중복되는지 확인 (자기 자신은 제외)
                final int currentIndex = i;
                boolean isDuplicateWithOther = wordList.stream()
                    .filter(w -> !w.getSpanish().equalsIgnoreCase(oldSpanish)) // 자기 자신 제외
                    .anyMatch(w -> w.getSpanish().equalsIgnoreCase(newWord.getSpanish()));

                if (newWord.getSpanish().equalsIgnoreCase(oldSpanish) || !isDuplicateWithOther) {
                    wordList.set(i, newWord);
                    saveWordsToFile();
                    return;
                } else {
                    System.out.println("수정하려는 스페인어 단어 '" + newWord.getSpanish() + "'가 이미 다른 단어로 존재합니다. (수정 안됨)");
                    // JOptionPane.showMessageDialog(null, "수정하려는 스페인어 단어가 이미 다른 단어로 존재합니다.", "중복 오류", JOptionPane.WARNING_MESSAGE);
                    return; // 중복이면 수정하지 않음
                }
            }
        }
    }


    public void loadWordsFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("'" + filePath + "' 파일이 존재하지 않습니다. (새로 사용 시 생성 예정)");
            return;
        }

        wordList.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Word word = Word.fromCsvString(line);
                    if (word != null) {
                        boolean isDuplicate = wordList.stream()
                                .anyMatch(w -> w.getSpanish().equalsIgnoreCase(word.getSpanish()));
                        if (!isDuplicate) {
                            wordList.add(word);
                        } else {
                            System.out.println("파일 로드 중 중복된 단어 발견 (무시됨): " + word.getSpanish());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("파일에서 단어 로드 중 오류 발생: " + e.getMessage());
        }
    }

    public void saveWordsToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            for (Word word : wordList) {
                pw.println(word.toCsvString());
            }
        } catch (IOException e) {
            System.err.println("파일에 단어 저장 중 오류 발생: " + e.getMessage());
        }
    }

    public List<String> importWordsFromCsv(String importFilePath) {
        List<String> importMessages = new ArrayList<>();
        File file = new File(importFilePath);
        if (!file.exists()) {
            importMessages.add("'" + importFilePath + "' 파일이 존재하지 않습니다.");
            return importMessages;
        }

        int importedCount = 0;
        int duplicateCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Word word = Word.fromCsvString(line);
                    if (word != null) {
                        boolean isDuplicate = wordList.stream()
                                .anyMatch(w -> w.getSpanish().equalsIgnoreCase(word.getSpanish()));
                        if (!isDuplicate) {
                            wordList.add(word);
                            importedCount++;
                        } else {
                            duplicateCount++;
                        }
                    }
                }
            }
            if (importedCount > 0) { // 실제로 추가된 단어가 있을 때만 저장
                saveWordsToFile();
            }
            importMessages.add(importedCount + "개의 단어를 가져왔습니다.");
            if (duplicateCount > 0) {
                importMessages.add(duplicateCount + "개의 중복된 단어는 건너뛰었습니다.");
            }

        } catch (IOException e) {
            importMessages.add("CSV 파일 가져오기 중 오류 발생: " + e.getMessage());
        }
        return importMessages;
    }

    public boolean exportWordsToCsv(String exportFilePath) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(exportFilePath, StandardCharsets.UTF_8))) {
            for (Word word : wordList) {
                pw.println(word.toCsvString());
            }
            return true;
        } catch (IOException e) {
            System.err.println("CSV 파일 내보내기 중 오류 발생: " + e.getMessage());
            return false;
        }
    }
}