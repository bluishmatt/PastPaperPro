package co.uk.mattelliot;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;


public class NewTopicController {
    public AnchorPane topicsPane;
    public Button closeButton;
    public Button minimizeButton;
    public MenuBar menuBar;
    private TableColumn<Topic, String> subtopicCol = new TableColumn("Subtopic");
    private TableColumn<Topic, String> numberCol = new TableColumn("#");
    private TableColumn<Topic, String> topicCol = new TableColumn("Topic");
    @FXML private TableView topicTable;
    ObservableList<Topic> topics = FXCollections.observableArrayList();
    private double xOffset = 0;
    private double yOffset = 0;



    public void initialize() throws IOException {

       numberCol.setCellValueFactory(new PropertyValueFactory<Topic,String>("number"));
       topicCol.setCellValueFactory(new PropertyValueFactory<Topic,String>("topic"));
       subtopicCol.setCellValueFactory(new PropertyValueFactory<Topic,String>( "subtopic"));

       numberCol.setSortable(true);
       topicCol.setSortable(false);
       subtopicCol.setSortable(false);

       numberCol.setPrefWidth(45);
       topicCol.setPrefWidth(200);
       subtopicCol.setPrefWidth(235);

        //open and read Json for any previously saved data.
        Gson gson = new Gson();
        try (Reader reader = new FileReader("topics.json")) {
            // Convert JSON File to Java Object
            ArrayList<Topic> imports = gson.fromJson(reader, new TypeToken<ArrayList<Topic>>() {}.getType());
            topics = FXCollections.observableArrayList(imports);
        } catch (IOException e) {
            e.printStackTrace();
        }

       topicTable.getColumns().addAll(numberCol, topicCol,subtopicCol);
       topicTable.setItems(topics);

       setEditable(); //lets you click the table instead of editing it.
       topicTable.setEditable(true);
   }

    private void initializeTopBar() {
        closeButton.styleProperty().bind(
                Bindings.when(
                        closeButton.hoverProperty()
                )
                        .then("-fx-text-fill: red;" +
                                "-fx-border-color: transparent;\n" +
                                "-fx-border-width: 0;\n" +
                                "-fx-background-radius: 0;\n" +
                                "-fx-background-color: transparent;")
                        .otherwise("-fx-text-fill: black;" +
                                "-fx-border-color: transparent;\n" +
                                "-fx-border-width: 0;\n" +
                                "-fx-background-radius: 0;\n" +
                                "-fx-background-color: transparent;")
        );
        minimizeButton.styleProperty().bind(
                Bindings.when(
                        minimizeButton.hoverProperty()
                )
                        .then("-fx-text-fill: #0096C9;" +
                                "-fx-border-color: transparent;\n" +
                                "-fx-border-width: 0;\n" +
                                "-fx-background-radius: 0;\n" +
                                "-fx-background-color: transparent;")
                        .otherwise("-fx-text-fill: black;" +
                                "-fx-border-color: transparent;\n" +
                                "-fx-border-width: 0;\n" +
                                "-fx-background-radius: 0;\n" +
                                "-fx-background-color: transparent;")
        );

        //grab your root here
        menuBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        //move around here
        menuBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                topicsPane.getScene().getWindow().setX(event.getScreenX() - xOffset);
                topicsPane.getScene().getWindow().setY(event.getScreenY() - yOffset);
            }
        });
    }

    public void saveObjects(ActionEvent actionEvent) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("topics.json")) {
            gson.toJson(topics, writer);
            System.out.println("Saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void addNewTopic(ActionEvent actionEvent) {
            topics.add(new Topic("0","topic", "subtopic"));
    }

    public void deleteSelectedRow(ActionEvent actionEvent) {
        // get Selected Item
        int currentTopic = topicTable.getSelectionModel().getSelectedIndex();
        //remove selected item from the table list
        topics.remove(currentTopic);
    }


    /** IS this necessary anymore? **/
    public void setEditable(){
        subtopicCol.setCellFactory(TextFieldTableCell.forTableColumn());
        subtopicCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Topic, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Topic, String> t) {
                        ((Topic) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setSubtopic(t.getNewValue());
                    }
                }
        );
        numberCol.setCellFactory(TextFieldTableCell.forTableColumn());
        numberCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Topic, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Topic, String> t) {
                        ((Topic) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setNumber(t.getNewValue());
                    }
                }
        );
        topicCol.setCellFactory(TextFieldTableCell.forTableColumn());
        topicCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Topic, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Topic, String> t) {
                        ((Topic) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setTopic(t.getNewValue());
                        System.out.println(topics);
                    }

                }
        );

    }
    //MENU BUTTONS
    public void close(ActionEvent actionEvent) {
        Stage stage = (Stage) topicsPane.getScene().getWindow();
        stage.close();
    }
    public void readme(ActionEvent actionEvent) {
        try {
            Desktop desk=Desktop.getDesktop();
            desk.browse(new URI("https://github.com/bluishmatt/PastPaperPro"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        //https://github.com/bluishmatt/PastPaperPro
    }
    public void reportIssue(ActionEvent actionEvent) {
        try {
            Desktop desk=Desktop.getDesktop();
            desk.browse(new URI("https://github.com/bluishmatt/PastPaperPro/issues"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        //https://github.com/bluishmatt/PastPaperPro/issues
    }
    public void about(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("About");
        alert.setHeaderText("About Past Paper Pro");
        alert.setContentText("Past Paper pro was designed and created by Matt Elliot as a means to organize past Papers and help improve student learning. \n\rIf this program was useful please send me a line, I would love to hear how/where this is being used. \n\rPlease report any issues or feature requests on github.");
        alert.showAndWait();
    }
    public void minimize(ActionEvent actionEvent) {
        Stage stage = (Stage) topicsPane.getScene().getWindow();
        stage.setIconified(true);
    }


}
