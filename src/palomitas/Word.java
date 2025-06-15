package palomitas;

public class Word {
    private String spanish;
    private String korean;
    private String example; // 예문은 선택 사항이므로 null일 수 있음

    public Word(String spanish, String korean, String example) {
        this.spanish = spanish;
        this.korean = korean;
        this.example = (example == null || example.trim().isEmpty()) ? "" : example; // 빈 문자열이나 null이면 빈칸으로
    }

    // Getter 메소드들 (단어 정보를 가져올 때 사용)
    public String getSpanish() {
        return spanish;
    }

    public String getKorean() {
        return korean;
    }

    public String getExample() {
        return example;
    }

    // Setter 메소드들 (단어 정보를 수정할 때 사용)
    public void setSpanish(String spanish) {
        this.spanish = spanish;
    }

    public void setKorean(String korean) {
        this.korean = korean;
    }

    public void setExample(String example) {
        this.example = (example == null || example.trim().isEmpty()) ? "" : example;
    }

    // CSV 파일 저장을 위한 문자열 변환 (쉼표로 구분)
    public String toCsvString() {
        // 예문이 없으면 빈 칸으로 저장
        String ex = (example == null || example.isEmpty()) ? "" : example;
        // CSV 내부의 쉼표나 줄바꿈 문자를 고려해야 하지만, MVP에서는 단순하게 갑니다.
        return escapeCsv(spanish) + "," + escapeCsv(korean) + "," + escapeCsv(ex);
    }

    // CSV 문자열에서 Word 객체 생성 (정적 메소드)
    public static Word fromCsvString(String csvLine) {
        String[] parts = csvLine.split(",", -1); // -1을 주어 빈 문자열도 유지
        if (parts.length >= 2) {
            String spanish = unescapeCsv(parts[0]);
            String korean = unescapeCsv(parts[1]);
            String example = (parts.length > 2) ? unescapeCsv(parts[2]) : "";
            return new Word(spanish, korean, example);
        }
        return null; // 형식이 맞지 않으면 null 반환
    }

    // CSV 특수 문자 처리 (간단 버전)
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains("\"") || value.contains(",") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String unescapeCsv(String value) {
        if (value == null) return "";
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1); // 양쪽 큰따옴표 제거
            return value.replace("\"\"", "\""); // 이중 큰따옴표를 단일 큰따옴표로
        }
        return value;
    }

    @Override
    public String toString() { // 객체를 문자열로 표현할 때 (디버깅용)
        return "Spanish: " + spanish + ", Korean: " + korean + ", Example: " + example;
    }
}