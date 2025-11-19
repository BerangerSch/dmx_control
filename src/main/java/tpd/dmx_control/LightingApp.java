package tpd.dmx_control;

import tpd.dmx_control.controllers.DmxController;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import com.fazecast.jSerialComm.SerialPort;

public class LightingApp extends Application{
    
    // Reference to our DMX controller
    private final DmxController dmxController = new DmxController();

    // Interface components
    private Rectangle colorPreview;
    private Slider redSlider;
    private Slider greenSlider;
    private Slider blueSlider;

    @Override
    public void start(Stage stage) {
        // 1. Window config
        stage.setTitle("DMX Lighting Control");

        // 2. Connection zone (top)
        HBox connectionBox = createConnectionBox();

        // 3. Control zone (center)
        VBox controlBox = createControlBox();

        // 4. Main layout (root)
        BorderPane root = new BorderPane();
        root.setTop(connectionBox);
        root.setCenter(controlBox);
        root.setPadding(new Insets(20));

        // 5. Launch
        Scene scene = new Scene(root, 400, 500);
        stage.setScene(scene);
        stage.show();

        // Hook to close DMX port when app closes
        stage.setOnCloseRequest(event -> dmxController.stop());
    }

    private HBox createConnectionBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 0, 20, 0));

        // Ports drop-down list
        ComboBox<String> portList = new ComboBox<>();
        // We fill the list with available ports detected by jSerialComm
        for (SerialPort p : SerialPort.getCommPorts()) {
            portList.getItems().add(p.getSystemPortName());
        }
        if (!portList.getItems().isEmpty()) portList.getSelectionModel().select(0);

        Button btnConnect = new Button("Connecter");
        Label statusLabel = new Label("Déconnecté");
        statusLabel.setStyle("-fx-text-fill: red;");

        btnConnect.setOnAction(e -> {
            String selectedPort = portList.getValue();
            if (selectedPort != null) {
                boolean success = dmxController.connect(selectedPort);
                if (success) {
                    statusLabel.setText("Connecté !");
                    statusLabel.setStyle("-fx-text-fill: green;");
                    btnConnect.setDisable(true); // To avoid clicking again
                } else {
                    statusLabel.setText("Erreur de connexion");
                }
            }
        });
        box.getChildren().addAll(new Label("Port DMX :"), portList, btnConnect, statusLabel);
        return box;
    }

    private VBox createControlBox() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);

        // -- Color Preview --
        colorPreview = new Rectangle(300, 100, Color.BLACK);
        colorPreview.setStroke(Color.GRAY);

        // -- Color Picker (quick selector) --
        ColorPicker colorPicker = new ColorPicker(Color.BLACK);
        colorPicker.setOnAction(e -> {
            Color c = colorPicker.getValue();
            // Update sliders when color picker is used
            redSlider.setValue(c.getRed() * 255);
            greenSlider.setValue(c.getGreen() * 255);
            blueSlider.setValue(c.getBlue() * 255);
        });

        // -- Sliders (Red / Green / Blue) --
        redSlider = createSlider("Rouge", Color.RED);
        greenSlider = createSlider("Vert", Color.GREEN);
        blueSlider = createSlider("Bleu", Color.BLUE);

        // Add listeners on sliders
        // As slider values change, we send DMX updates and refresh preview
        redSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateDMXAndPreview());
        greenSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateDMXAndPreview());
        blueSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateDMXAndPreview());

        box.getChildren().addAll(
            colorPreview,
            new Label("Sélecteur rapide :"), colorPicker,
            new Separator(),
            new Label("Contrôle manuel Canaux RGB :"),
            new Label ("Rouge (Ch 1)"), redSlider,
            new Label ("Vert (Ch 2)"), greenSlider,
            new Label ("Bleu (Ch 3)"), blueSlider
        );
        return box;
    }

    private Slider createSlider(String label, Color color) {
        Slider slider = new Slider(0, 255, 0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(85);
        slider.setBlockIncrement(1);
        slider.setStyle(String.format("-fx-control-inner-background: rgb(%d,%d,%d);",
            (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255)));
        return slider;
    }
    
    private void updateDMXAndPreview() {
        int r = (int) redSlider.getValue();
        int g = (int) greenSlider.getValue();
        int b = (int) blueSlider.getValue();

        // 1. Visual preview update
        colorPreview.setFill(Color.rgb(r, g, b));

        // 2. DMX channels update
        // We suppose that address is 001. So Red=Ch1, Green=Ch2, Blue=Ch3
        dmxController.setChannel(1, r);
        dmxController.setChannel(2, g);
        dmxController.setChannel(3, b);
    }

    public static void main(String[] args) {
        launch(args); // to run JavaFX application : "mvn javafx:run"
    }
}

