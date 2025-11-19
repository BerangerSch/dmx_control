package tpd.dmx_control;

import tpd.dmx_control.controllers.DmxController;
import tpd.dmx_control.device.StairvilleLedBar;
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
    
    // -- Class variables --
    private final DmxController dmxController = new DmxController();
    private StairvilleLedBar myBar;

    // Interface components
    private Rectangle colorPreview;
    private Slider redSlider;
    private Slider greenSlider;
    private Slider blueSlider;
    private Slider dimmerSlider;
    private Slider strobeSlider;

    // Star DMX Address for the Stairville LED Bar
    private static final int BAR_START_ADDRESS = 1;

    @Override
    public void start(Stage stage) {
        // 1. Window config
        stage.setTitle("DMX Lighting Control");

        // 2. Control zones
        HBox connectionBox = createConnectionBox();
        VBox controlBox = createControlBox();

        // 4. Main layout (root)
        BorderPane root = new BorderPane();
        root.setTop(connectionBox);
        root.setCenter(controlBox);
        root.setPadding(new Insets(20));

        // 5. Launch
        Scene scene = new Scene(root, 400, 600);
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
                    myBar = new StairvilleLedBar(BAR_START_ADDRESS, dmxController);

                    statusLabel.setText("Connecté !");
                    statusLabel.setStyle("-fx-text-fill: green;");
                    btnConnect.setDisable(true); // To avoid clicking again

                    // We make sure the dimmer is at max on connect
                    myBar.setDimmer((int) dimmerSlider.getValue());
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
        dimmerSlider = createSlider("Dimmer", Color.GRAY);
        dimmerSlider.setValue(255); // Default to max
        strobeSlider = createSlider("Strobe", Color.GRAY);
        strobeSlider.setValue(0); // Default to off

        // Add listeners on sliders
        // As slider values change, we send DMX updates and refresh preview
        redSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateDMXAndPreview());
        greenSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateDMXAndPreview());
        blueSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateDMXAndPreview());
        dimmerSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateDMXAndPreview());
        strobeSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateDMXAndPreview());

        box.getChildren().addAll(
            colorPreview,
            new Label("Sélecteur rapide :"), colorPicker,
            new Separator(),
            new Label("Contrôle manuel Canaux RGB :"),
            new Label ("Rouge"), redSlider,
            new Label ("Vert"), greenSlider,
            new Label ("Bleu"), blueSlider,
            new Separator(),
            new Label ("Dimmer (Intensité Globale)"),
            dimmerSlider,
            new Label ("Strobe (Vitesse)"),
            strobeSlider
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
        int d = (int) dimmerSlider.getValue();
        int s = (int) strobeSlider.getValue();

        // 1. Visual preview update
        colorPreview.setFill(Color.rgb(r, g, b));

        // 2. DMX channels update
        // We suppose that address is 001. So Red=Ch1, Green=Ch2, Blue=Ch3
        if (myBar != null) {
            myBar.setColor(Color.rgb(r, g, b));
            myBar.setDimmer(d);
            myBar.setStrobeSpeed(s);
        }
    }

    public static void main(String[] args) {
        launch(args); // to run JavaFX application : "mvn javafx:run"
    }
}

