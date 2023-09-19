package me.hostadam.mcstatus;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MCStatusController {

    @FXML
    private ChoiceBox<String> typeInput;
    @FXML
    private TextField ipInput;
    @FXML
    private TextField scannedPorts;
    @FXML
    private TextArea outputArea;
    @FXML
    private ProgressBar bar;

    public void initialize() {
        bar.setVisible(false);
        scannedPorts.setVisible(false);
        typeInput.setItems(FXCollections.observableArrayList("Ports", "Server"));
        typeInput.getSelectionModel().selectFirst();
    }

    public void scanButtonClicked() {
        String type = typeInput.getValue().toLowerCase();
        String ip = ipInput.getText();
        if(ip.isEmpty()) {
            this.output("IP cannot be empty.");
            return;
        }

        if (type.equals("ports") || type.equals("port")) {
            int portMin = Integer.parseInt(showInputDialog("Enter the lowest port"));
            int portMax = Integer.parseInt(showInputDialog("Enter the highest port"));
            if (portMax <= portMin) {
                this.output("Highest port must be higher than lowest.");
                return;
            }

            int totalPorts = (portMax - portMin);
            this.output("Scanning " + totalPorts + " port(s).");
            this.output("Estimated duration: " + TimeUnit.MILLISECONDS.toSeconds(30L * totalPorts) + " seconds");
            bar.setVisible(true);
            scannedPorts.setVisible(true);
            scanPorts(ip, portMin, portMax, 0, 0, totalPorts);
        } else if (type.equals("server")) {
            int port = Integer.parseInt(showInputDialog("Enter the port"));
            this.output("Scanning " + ip + ":" + port);
            scanServer(ip, port);
        } else {
            this.output("Invalid.");
        }
    }

    private void scanPorts(String ip, int port, int portMax, int failed, int counter, int total) {
        if (port > portMax) {
            bar.setVisible(false);
            scannedPorts.setVisible(false);
            this.output("Scanning completed.");
            return;
        }

        bar.setProgress((double) counter / (double) total);
        scannedPorts.setText("Port " + counter + " out of " + total + " (" + port + ")");

        CompletableFuture.runAsync(() -> {
            try {
                Socket socket = new Socket();
                long start = System.currentTimeMillis();
                socket.connect(new InetSocketAddress(ip, port), 50);

                Platform.runLater(() -> {
                    this.output("✅ Found connection on port " + port + " in " + (System.currentTimeMillis() - start) + "ms");
                });

                socket.close();
            } catch (IOException e) {
            }

        }).thenRunAsync(() -> scanPorts(ip, port + 1, portMax, failed + 1, counter + 1, total));
    }

    private void scanServer(String ip, int port) {
        try {
            String urlString = "https://api.mcstatus.io/v2/status/java/" + ip + ":" + port + "?query=true";
            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                MCServer server = new MCServer(line);
                this.output(" ");
                this.output("Server Data");
                this.output(" - SRV: " + server.getSrvHost() + " (" + server.getSrvPort() + ")");
                this.output(" - Online: " + server.isOnline());
                this.output(" - Blocked: " + server.isEulaBlocked());
                this.output(" - Version: " + server.getVersionName() + " (" + server.getVersionProtocol() + ")");
                this.output(" - Players: " + server.getOnlinePlayers() + " / " + server.getMaxPlayers());
                this.output(" - Motd: " + server.getMotd());
                this.output(" - Software: " + server.getSoftware());
                this.output(" - Plugins: " + (server.getPlugins().isEmpty() ? "None" : String.join(", ", server.getPlugins())));
                this.output(" ");
            }
            Platform.runLater(() -> this.output("Scanning completed."));
        } catch (Exception exception) {
            Platform.runLater(() -> this.output("Failed to scan " + ip + ":" + port + "!"));
            exception.printStackTrace();
        }
    }

    public void output(String output) {
        this.outputArea.appendText(output + "\n");
    }

    private String showInputDialog(String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(prompt);
        dialog.showAndWait();
        return dialog.getEditor().getText();
    }
}