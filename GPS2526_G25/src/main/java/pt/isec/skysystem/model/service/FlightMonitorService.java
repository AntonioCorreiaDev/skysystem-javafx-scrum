package pt.isec.skysystem.model.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import pt.isec.skysystem.model.DataFacade;
import pt.isec.skysystem.model.data.flights.Trip;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FlightMonitorService extends Thread {

    private final ScheduledExecutorService scheduler;
    private final AmadeusService amadeusService;
    private final DataFacade dataFacade;

    private static final int CHECK_INTERVAL_MINUTES = 60;
    private boolean isTestMode = false;

    @Override
    public void run() {
        startMonitoring();
    }

    // --- NOVO: Para evitar repetir o mesmo lembrete várias vezes ---
    private final Set<String> sentReminders = new HashSet<>();

    public FlightMonitorService(DataFacade dataFacade, AmadeusService amadeusService) {
        this.dataFacade = dataFacade;
        this.amadeusService = amadeusService;

        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    public void setTestMode(boolean active) {
        this.isTestMode = active;
        System.out.println("[Monitor] Modo de Teste: " + (active ? "ATIVADO" : "DESATIVADO"));
    }

    public void startMonitoring() {
        System.out.println("[Monitor] Serviço iniciado.");
        long interval = isTestMode ? 15 : (CHECK_INTERVAL_MINUTES * 60);
        scheduler.scheduleAtFixedRate(this::checkAllTrips, 5, interval, TimeUnit.SECONDS);
    }

    public void stopMonitoring() {
        scheduler.shutdownNow();
        System.out.println("[Monitor] Serviço parado.");
    }

    private void checkAllTrips() {
        if (!dataFacade.hasCurrentUser()) return;

        List<Trip> savedTrips = dataFacade.getSavedFlights();
        if (savedTrips == null || savedTrips.isEmpty()) return;

        System.out.println("[Monitor] A verificar " + savedTrips.size() + " viagens...");

        for (Trip savedTrip : savedTrips) {
            if (!isTestMode) {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }

            Trip latestTrip = amadeusService.getLatestTripStatus(savedTrip, isTestMode);

            if (latestTrip != null) {
                List<String> changes = detectChanges(savedTrip, latestTrip);
                if (!changes.isEmpty()) {
                    notifyChanges(savedTrip, changes);
                }

                checkReminders(latestTrip);
            }
        }
    }

    private void checkReminders(Trip trip) {
        try {
            LocalDateTime departureDate = LocalDateTime.parse(trip.getOutboundFlight().getDepartureTime());
            LocalDateTime now = LocalDateTime.now();

            long daysUntil = ChronoUnit.DAYS.between(now.toLocalDate(), departureDate.toLocalDate());

            // Lógica de simulação para Teste
            if (isTestMode) {
                String mockKey = trip.getId() + "_mock_reminder";
                if (!sentReminders.contains(mockKey)) {
                    showSimpleToast("Lembrete Simulado 📅", "Faltam 2 dias para a viagem a " + trip.getDestination());
                    sentReminders.add(mockKey);
                }
            }

            if (daysUntil <= 2 && daysUntil >= 0) {
                String reminderKey = trip.getId() + "_reminder_" + daysUntil;

                if (!sentReminders.contains(reminderKey)) {
                    String msg;
                    if (daysUntil == 0) {
                        msg = "É hoje! Boa viagem para " + trip.getDestination() + " ✈️";
                    } else if (daysUntil == 1) {
                        msg = "Falta 1 dia para o voo de " + trip.getDestination() + " ⏳";
                    } else {
                        msg = "Faltam " + daysUntil + " dias para a viagem a " + trip.getDestination();
                    }

                    showSimpleToast("Lembrete de Viagem 📅", msg);
                    sentReminders.add(reminderKey);
                    System.out.println("[LEMBRETE] " + msg);
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao verificar datas para lembrete: " + e.getMessage());
        }
    }

    private List<String> detectChanges(Trip oldTrip, Trip newTrip) {
        List<String> changes = new ArrayList<>();

        double oldPrice = oldTrip.getTotalPrice();
        double newPrice = newTrip.getTotalPrice();

        if (Math.abs(newPrice - oldPrice) > 0.01) {
            if (newPrice < oldPrice) {
                changes.add(String.format("📉 Preço desceu! (%.2f€ -> %.2f€)", oldPrice, newPrice));
            } else {
                changes.add(String.format("📈 Preço subiu (%.2f€ -> %.2f€)", oldPrice, newPrice));
            }
        }

        if (!isSameTime(oldTrip.getOutboundFlight().getDepartureTime(), newTrip.getOutboundFlight().getDepartureTime())) {
            changes.add("🕒 Ida: Hora alterada para " + extractTime(newTrip.getOutboundFlight().getDepartureTime()));
        }

        if (oldTrip.isRoundTrip() && newTrip.isRoundTrip()) {
            if (!isSameTime(oldTrip.getReturnFlight().getDepartureTime(), newTrip.getReturnFlight().getDepartureTime())) {
                changes.add("🕒 Volta: Hora alterada para " + extractTime(newTrip.getReturnFlight().getDepartureTime()));
            }
        }

        return changes;
    }

    private void notifyChanges(Trip trip, List<String> changes) {
        String title = "Atualização de Voo ⚠️";
        StringBuilder sb = new StringBuilder();
        sb.append(trip.getOrigin()).append(" -> ").append(trip.getDestination()).append("\n");

        for (String c : changes) {
            sb.append(c).append("\n");

            // --- ALTERAÇÃO AQUI ---
            // Guardar na Base de Dados e Log para debug
            System.out.println("[DEBUG] A guardar notificação para: " + trip.getOutboundFlight().getFlightNumber());
            dataFacade.addTripNotification(trip, c);
        }

        System.out.println("[TOAST MUDANÇA] " + title);
        showSimpleToast(title, sb.toString());
    }

    // --- Toast Interno ---
    private void showSimpleToast(String title, String message) {
        Platform.runLater(() -> {
            try {
                Stage toastStage = new Stage();
                toastStage.initStyle(StageStyle.TRANSPARENT);
                toastStage.setAlwaysOnTop(true);

                VBox root = new VBox(5);
                // Azul escuro moderno
                root.setStyle(
                        "-fx-background-color: rgba(44, 62, 80, 0.95);" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 15;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);" +
                                "-fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 10;"
                );
                root.setMinWidth(300);
                root.setMaxWidth(350);

                Label titleLabel = new Label(title);
                titleLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-weight: bold; -fx-font-size: 14px;");

                Label msgLabel = new Label(message);
                msgLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 12px;");
                msgLabel.setWrapText(true);

                root.getChildren().addAll(titleLabel, msgLabel);

                Scene scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);
                toastStage.setScene(scene);

                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                toastStage.setOpacity(0);
                toastStage.show();

                toastStage.setX(screenBounds.getMaxX() - toastStage.getWidth() - 20);
                toastStage.setY(screenBounds.getMaxY() - toastStage.getHeight() - 20);

                toastStage.setOpacity(1);

                Timeline timeline = new Timeline(new KeyFrame(
                        Duration.seconds(5),
                        evt -> toastStage.close()
                ));
                timeline.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private boolean isSameTime(String t1, String t2) {
        return (t1 == null && t2 == null) || (t1 != null && t1.equals(t2));
    }

    private String extractTime(String isoDateTime) {
        try {
            if (isoDateTime != null && isoDateTime.contains("T")) {
                return isoDateTime.split("T")[1].substring(0, 5);
            }
        } catch (Exception e) {}
        return isoDateTime;
    }
}