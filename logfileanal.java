import java.io.*;
import java.util.*;
import java.util.regex.*;

public class logfileanal {

    static final String LOG_PATTERN =
            "^(\\S+) \\S+ \\S+ \\[.*?\\] \"(\\S+) (\\S+) \\S+\" (\\d{3}) (\\d+|-)";

    public static void main(String[] args) throws IOException {

        String logFile = "access.log";

        Map<String, Integer> errorCount = new HashMap<>();
        Map<String, Integer> ipCount = new HashMap<>();
        Map<String, Integer> endpointCount = new HashMap<>();

        int totalLines = 0;

        Pattern pattern = Pattern.compile(LOG_PATTERN);

        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {

            String line;

            while ((line = br.readLine()) != null) {

                totalLines++;

                Matcher m = pattern.matcher(line);

                if (m.find()) {

                    String ip = m.group(1);
                    String method = m.group(2);
                    String endpoint = m.group(3);
                    String status = m.group(4);

                    if (status.startsWith("4") || status.startsWith("5")) {
                        errorCount.merge(status, 1, Integer::sum);
                    }

                    ipCount.merge(ip, 1, Integer::sum);
                    endpointCount.merge(endpoint, 1, Integer::sum);
                }
            }
        }

        System.out.println("=== LOG ANALYSIS REPORT ===");
        System.out.println("Total Lines Processed: " + totalLines);

        System.out.println("\n--- HTTP Errors ---");
        errorCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> System.out.println(e.getKey() + " : " + e.getValue() + " times"));

        System.out.println("\n--- Top 5 IP Addresses ---");
        ipCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> System.out.println(e.getKey() + " : " + e.getValue() + " requests"));

        System.out.println("\n--- Top 5 Endpoints ---");
        endpointCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> System.out.println(e.getKey() + " : " + e.getValue() + " hits"));

        exportCSV(errorCount, ipCount, endpointCount);

        System.out.println("\nReport saved to: report.csv");
    }

    static void exportCSV(Map<String, Integer> errors,
                          Map<String, Integer> ips,
                          Map<String, Integer> endpoints) throws IOException {

        try (FileWriter fw = new FileWriter("report.csv")) {

            fw.write("Type,Key,Count\n");

            for (Map.Entry<String, Integer> e : errors.entrySet())
                fw.write("Error," + e.getKey() + "," + e.getValue() + "\n");

            for (Map.Entry<String, Integer> e : ips.entrySet())
                fw.write("IP," + e.getKey() + "," + e.getValue() + "\n");

            for (Map.Entry<String, Integer> e : endpoints.entrySet())
                fw.write("Endpoint," + e.getKey() + "," + e.getValue() + "\n");
        }
    }
}