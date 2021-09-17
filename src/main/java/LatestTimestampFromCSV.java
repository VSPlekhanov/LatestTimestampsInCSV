import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class LatestTimestampFromCSV {
    private final char separatorChar;
    private final char escapeChar;
    private final String headerName;
    private final int capacity;

    private String[] headers;

    public LatestTimestampFromCSV(char separatorChar, char escapeChar, String headerName, int capacity) {
        this.separatorChar = separatorChar;
        this.escapeChar = escapeChar;
        this.headerName = headerName;
        this.capacity = capacity;
    }

    public Map<String, String> parseRecord(String line) {
        Map<String, String> result = new HashMap<>();
        char[] arr = new char[line.length()];
        line.getChars(0, line.length(), arr, 0);
        boolean escapeOn = false;
        boolean prevCharEscape = false;
        boolean prevPrevCharEscape = false;
        int currCell = 0;
        StringBuilder currVal = new StringBuilder();
        for (char c : arr) {
            if (c == escapeChar) {
                if (!prevCharEscape) {
                    prevCharEscape = true;
                } else if (prevPrevCharEscape) {
                    currVal.append(escapeChar);
                    escapeOn = !escapeOn;
                    prevPrevCharEscape = false;
                    prevCharEscape =false;
                } else {
                    prevPrevCharEscape = true;
                }
            } else {
                if (prevPrevCharEscape){
                    prevPrevCharEscape = false;
                    prevCharEscape = false;
                    currVal.append(escapeChar);
                }
                if (prevCharEscape) {
                    prevCharEscape = false;
                    escapeOn = !escapeOn;
                }
                if (escapeOn || c != separatorChar) {
                    currVal.append(c);
                } else {
                    if (currCell == headers.length - 1)
                        throw new IllegalStateException("Incorrect format: too many commas");
                    result.put(headers[currCell++], currVal.toString());
                    currVal = new StringBuilder();
                }
            }
        }
        result.put(headers[currCell], currVal.toString());
        return result;
    }

    public void getLatestTimestamps(String pathToCSV) throws IOException {

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(pathToCSV))) {
            String firstLine = bufferedReader.readLine();
            int timestampIndex = -1;
            headers = firstLine.split(String.valueOf(separatorChar));
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i];
                if (headerName.equals(header)) {
                    timestampIndex = i;
                }
            }
            if (timestampIndex == -1) {
                throw new IllegalArgumentException("No timestamp header");
            }
            PriorityQueue<Map<String, String>> queue =
                    new PriorityQueue<>(Comparator.comparingInt(record -> Integer.parseInt(record.get(headerName))));

            String line;
            int iter = capacity;
            while ((line = bufferedReader.readLine()) != null) {
                queue.add(parseRecord(line));
                if (iter > 0) {
                    --iter;
                } else {
                    queue.poll();
                }
            }


            Map<String, String> record;
            while ((record = queue.poll()) != null) {
                System.out.println(record);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        LatestTimestampFromCSV latestTimestampFromCSV =
                new LatestTimestampFromCSV(',', '"', "timestamp", 10);
        latestTimestampFromCSV.getLatestTimestamps(args[0]);
    }
}
