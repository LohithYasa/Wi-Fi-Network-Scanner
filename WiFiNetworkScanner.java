
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class WiFiNetworkScanner {

    private static final Scanner INPUT = new Scanner(System.in);
    private static final List<WiFiNetwork> HISTORY = new ArrayList<>();

    static class WiFiNetwork {
        String ssid;
        String bssid;
        String signal;
        String radioType;
        String channel;
        String authentication;
        String encryption;

        WiFiNetwork(String ssid, String bssid, String signal,
                    String radioType, String channel,
                    String authentication, String encryption) {
            this.ssid = ssid;
            this.bssid = bssid;
            this.signal = signal;
            this.radioType = radioType;
            this.channel = channel;
            this.authentication = authentication;
            this.encryption = encryption;
        }

        int getSignalValue() {
            try {
                return Integer.parseInt(
                    signal.replace("%", "").trim()
                );
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        @Override
        public String toString() {
            return String.format(
                "%-22s %-20s %-9s %-12s %-9s %-18s %-12s",
                ssid, bssid, signal, radioType,
                channel, authentication, encryption
            );
        }
    }

    static class CommandResult {
        String output;
        int exitCode;

        CommandResult(String output, int exitCode) {
            this.output = output;
            this.exitCode = exitCode;
        }
    }

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("       WI-FI NETWORK SCANNER");
            System.out.println("========================================");
            System.out.println("1. Scan nearby Wi-Fi networks");
            System.out.println("2. Sort networks by signal strength");
            System.out.println("3. Search network by SSID");
            System.out.println("4. View scan history");
            System.out.println("5. Inspect Wi-Fi interface");
            System.out.println("6. Export networks to CSV");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");

            String choice = INPUT.nextLine().trim();

            switch (choice) {
                case "1":
                    scanNetworks();
                    break;
                case "2":
                    sortNetworks();
                    break;
                case "3":
                    searchNetwork();
                    break;
                case "4":
                    viewHistory();
                    break;
                case "5":
                    inspectInterface();
                    break;
                case "6":
                    exportCSV();
                    break;
                case "7":
                    System.out.println("Exiting Wi-Fi Scanner.");
                    INPUT.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static CommandResult runCommand(String... command)
            throws IOException, InterruptedException {

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);

        Process process = builder.start();
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    process.getInputStream(),
                    StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        boolean finished = process.waitFor(15, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            throw new IOException("Command timed out.");
        }

        return new CommandResult(
            output.toString(), process.exitValue()
        );
    }

    private static void scanNetworks() {
        System.out.println("\nScanning nearby Wi-Fi networks...");

        try {
            CommandResult result = runCommand(
                "cmd", "/c", "netsh wlan show networks mode=bssid"
            );

            if (result.exitCode != 0) {
                System.out.println("Network scan failed.");
                System.out.println(result.output);
                return;
            }

            List<WiFiNetwork> networks =
                parseNetworks(result.output);

            if (networks.isEmpty()) {
                System.out.println(
                    "No networks found. Check your Wi-Fi adapter."
                );
                System.out.println(
                    "Note: This parser expects English Windows output."
                );
                return;
            }

            HISTORY.addAll(networks);
            displayNetworks(networks);

            System.out.println("\nNetworks found: " + networks.size());
            System.out.println("Scan time: " + LocalDateTime.now());

        } catch (IOException e) {
            System.out.println("Scanning failed: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Scan interrupted.");
        }
    }

    private static List<WiFiNetwork> parseNetworks(String output) {
        List<WiFiNetwork> networks = new ArrayList<>();

        String currentSSID = "";
        String authentication = "Unknown";
        String encryption = "Unknown";
        String radioType = "Unknown";
        String channel = "Unknown";

        String currentBSSID = "";
        String signal = "Unknown";

        for (String rawLine : output.split("\\R")) {
            String line = rawLine.trim();

            if (line.startsWith("SSID ") && line.contains(":")) {
                if (!currentBSSID.isEmpty()) {
                    networks.add(new WiFiNetwork(
                        currentSSID, currentBSSID, signal,
                        radioType, channel, authentication, encryption
                    ));
                    currentBSSID = "";
                }

                int colon = line.indexOf(':');
                currentSSID = line.substring(colon + 1).trim();
                authentication = "Unknown";
                encryption = "Unknown";
                radioType = "Unknown";
                channel = "Unknown";
                signal = "Unknown";

            } else if (line.startsWith("Authentication")
                    && line.contains(":")) {
                authentication = valueAfterColon(line);

            } else if (line.startsWith("Encryption")
                    && line.contains(":")) {
                encryption = valueAfterColon(line);

            } else if (line.startsWith("BSSID ")
                    && line.contains(":")) {
                if (!currentBSSID.isEmpty()) {
                    networks.add(new WiFiNetwork(
                        currentSSID, currentBSSID, signal,
                        radioType, channel, authentication, encryption
                    ));
                }

                int colon = line.indexOf(':');
                currentBSSID = line.substring(colon + 1).trim();
                signal = "Unknown";
                radioType = "Unknown";
                channel = "Unknown";

            } else if (line.startsWith("Signal")
                    && line.contains(":")) {
                signal = valueAfterColon(line);

            } else if (line.startsWith("Radio type")
                    && line.contains(":")) {
                radioType = valueAfterColon(line);

            } else if (line.startsWith("Channel")
                    && line.contains(":")) {
                channel = valueAfterColon(line);
            }
        }

        if (!currentBSSID.isEmpty()) {
            networks.add(new WiFiNetwork(
                currentSSID, currentBSSID, signal,
                radioType, channel, authentication, encryption
            ));
        }

        return networks;
    }

    private static String valueAfterColon(String line) {
        int colon = line.indexOf(':');
        return colon >= 0
            ? line.substring(colon + 1).trim()
            : "Unknown";
    }

    private static void displayNetworks(List<WiFiNetwork> networks) {
        System.out.printf(
            "%-22s %-20s %-9s %-12s %-9s %-18s %-12s%n",
            "SSID", "BSSID", "Signal", "Radio Type",
            "Channel", "Authentication", "Encryption"
        );

        System.out.println(
            "--------------------------------------------------------------------------------------------------------------"
        );

        for (WiFiNetwork network : networks) {
            System.out.println(network);
        }
    }

    private static void sortNetworks() {
        List<WiFiNetwork> networks = getLatestNetworks();

        if (networks.isEmpty()) {
            System.out.println("Scan networks first.");
            return;
        }

        networks.sort(
            Comparator.comparingInt(WiFiNetwork::getSignalValue)
                      .reversed()
        );

        System.out.println("\nNetworks sorted by signal strength:");
        displayNetworks(networks);
    }

    private static List<WiFiNetwork> getLatestNetworks() {
        List<WiFiNetwork> networks = new ArrayList<>();

        if (HISTORY.isEmpty()) {
            return networks;
        }

        // History stores every discovered access point.
        // Use the most recent scan results where available.
        networks.addAll(HISTORY);
        return networks;
    }

    private static void searchNetwork() {
        System.out.print("Enter SSID or part of SSID: ");
        String query = INPUT.nextLine().trim();

        if (query.isEmpty()) {
            System.out.println("Search text cannot be empty.");
            return;
        }

        List<WiFiNetwork> matches = new ArrayList<>();

        for (WiFiNetwork network : HISTORY) {
            if (network.ssid.toLowerCase()
                    .contains(query.toLowerCase())) {
                matches.add(network);
            }
        }

        if (matches.isEmpty()) {
            System.out.println(
                "No matching network found. Scan networks first."
            );
        } else {
            displayNetworks(matches);
        }
    }

    private static void viewHistory() {
        if (HISTORY.isEmpty()) {
            System.out.println("No scan history available.");
            return;
        }

        System.out.println("\nPreviously discovered access points:");
        displayNetworks(HISTORY);
        System.out.println("Total recorded entries: " + HISTORY.size());
    }

    private static void inspectInterface() {
        System.out.println("\nWi-Fi Interface Information:");

        try {
            CommandResult result = runCommand(
                "cmd", "/c", "netsh wlan show interfaces"
            );

            System.out.println(result.output);

            if (result.exitCode != 0) {
                System.out.println(
                    "Unable to retrieve interface information."
                );
            }

        } catch (IOException e) {
            System.out.println(
                "Interface inspection failed: " + e.getMessage()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Interface inspection interrupted.");
        }
    }

    private static void exportCSV() {
        if (HISTORY.isEmpty()) {
            System.out.println("No network data to export.");
            return;
        }

        String filename = "wifi_networks.csv";

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(
                "SSID,BSSID,Signal,Radio Type,Channel,"
                + "Authentication,Encryption\n"
            );

            for (WiFiNetwork network : HISTORY) {
                writer.write(
                    csv(network.ssid) + ","
                    + csv(network.bssid) + ","
                    + csv(network.signal) + ","
                    + csv(network.radioType) + ","
                    + csv(network.channel) + ","
                    + csv(network.authentication) + ","
                    + csv(network.encryption) + "\n"
                );
            }

            System.out.println(
                "Data exported successfully to " + filename
            );

        } catch (IOException e) {
            System.out.println(
                "CSV export failed: " + e.getMessage()
            );
        }
    }

    private static String csv(String value) {
        if (value == null) {
            return "\"\"";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}