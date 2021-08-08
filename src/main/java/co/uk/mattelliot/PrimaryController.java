package co.uk.mattelliot;

import java.awt.*;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.print.Paper;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import javax.imageio.ImageIO;


public class PrimaryController {

    @FXML public MenuBar menuBar;
    @FXML public Button closeButton;
    @FXML public Button minimizeButton;
    @FXML public javafx.scene.image.ImageView imageView;
    @FXML public javafx.scene.image.ImageView imageViewMS;
    @FXML public ScrollPane scrollPane;
    @FXML public ScrollPane scrollPaneMS;
    @FXML public ChoiceBox QuestionTopicsChoiceBox;
    @FXML private ListView mpaperSelectListView;
    @FXML private ListView mtopicSelectListView;
    @FXML private ChoiceBox mpaperChoiceBox;
    @FXML private AnchorPane modifyPaperPane;
    @FXML private ChoiceBox mtopicsChoiceBox;

    public AtomicBoolean selectAndChangePaper = new AtomicBoolean(true);

    //Empty list for all topics
    private static ObservableList<Topic> topics = FXCollections.observableArrayList();
    private static ObservableList<Question> listViewTopicQ = FXCollections.observableArrayList();
    private static ObservableList<Question> listViewPaperQ = FXCollections.observableArrayList();
    private static ObservableList<String> questionsTopicsList = FXCollections.observableArrayList(); //to show topics this question is in.

    private ListView lastSelectedList = mpaperSelectListView;
    private double xOffset = 0;
    private double yOffset = 0;
    public String markschemeString = "";

    public void pdftoimage(String pdf){
        ArrayList<BufferedImage> images = new ArrayList<>();
        int heightTotal = 0;

        try (final PDDocument document = PDDocument.load(new File(pdf))){
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page)
            {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 100, ImageType.RGB);
                images.add(bim);
                heightTotal += bim.getHeight();
                //String fileName = OUTPUT_DIR + "image-" + page + ".png";
                //ImageIOUtil.writeImage(bim, fileName, 300);
            }

            int heightCurr = 0;
            BufferedImage concatImage = new BufferedImage(images.get(0).getWidth(), heightTotal, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = concatImage.createGraphics();
            for(int j = 0; j < images.size(); j++) {
                g2d.drawImage(images.get(j), 0, heightCurr, null);
                heightCurr += images.get(j).getHeight();
            }
            g2d.dispose();

            ImageIO.write(concatImage, "png", new File(pdf+".png")); // export concat image


            document.close();
        } catch (IOException e){
            System.err.println("Exception while trying to create pdf document - " + e);
        }
    }

    public void setImage(String paper, String markscheme){
        Image paperImage = new Image(paper);
        imageView.setImage(paperImage);
        imageView.setFitHeight(paperImage.getHeight());
        imageView.setFitWidth(paperImage.getWidth());

        Image markschemeImage = new Image(markscheme);
        imageViewMS.setImage(markschemeImage);
        imageViewMS.setFitHeight(markschemeImage.getHeight());
        imageViewMS.setFitWidth(markschemeImage.getWidth());
    }




    public void initialize() throws IOException {
        initializeTopBar();
        //a list of Paper names for the paper choicebox.
        ObservableList<String> PaperNames = FXCollections.observableArrayList();
        ObservableList<String> PaperToConvert = FXCollections.observableArrayList();

        VBox loadingPane = new VBox();
        Label exp = new Label("Converting papers, please wait \n\r" +
                "This process should take a minute or two.");
        ProgressBar bar = new ProgressBar();
        loadingPane.getChildren().addAll(exp, bar);
        loadingPane.setLayoutX(250);
        loadingPane.setLayoutY(250);
        modifyPaperPane.getChildren().add(loadingPane);

        Task task = new Task<Void>() {
            @Override public Void call() throws IOException {

                ArrayList<String> exclude = new ArrayList<>();
                exclude.add("paper_3"); //read from json

                markschemeString = "_markscheme"; //read from json.

                java.nio.file.Files.walk(
                            Paths.get("Papers/"))
                            .filter(java.nio.file.Files::isRegularFile)
                            .filter(name -> !name.toString().contains("paper_3")) //not paper 3
                            .filter(name -> name.toString().contains("pdf")) //not paper 3
                            .filter(name -> !name.toString().contains("png")) //not paper 3
                            .forEach(file -> PaperToConvert.add(String.valueOf(file).substring(7))); //adds the found filename to list, without proceeding /Papers/ folder so it looks nicer in choicebox.

                for (String s: PaperToConvert) {
                    for (String e: exclude) {
                        if(s.contains(e)){
                            PaperToConvert.remove(s);
                        }
                    }
                }

                int complete = 0;
                for (String file: PaperToConvert) {

                    updateProgress(complete, PaperToConvert.size());
                   // System.out.println("Iteration " + complete);

                    File f = new File(System.getProperty("user.dir") + "/Papers/" + file + ".png");
                    //System.out.println(f + " " + f.exists());
                    if (!f.exists()) {
                        pdftoimage(System.getProperty("user.dir") + "/Papers/" + file);
                    }
                    complete++;
                }

                java.nio.file.Files.walk(
                        Paths.get("Papers/"))
                        .filter(java.nio.file.Files::isRegularFile)
                        .filter(name -> !name.toString().contains(markschemeString)) //not list markschemes
                        .filter(name -> name.toString().endsWith("png")) //ignore the pdfs because we will work with jpegs.
                        .forEach(file -> PaperNames.add(String.valueOf(file).substring(7))); //adds the found filename to list, without proceeding Papers folder so it looks nice in choicebox.

                loadingPane.setVisible(false);
                //System.out.println(PaperNames);
                    if(PaperNames.size() == 0){
                        loadingPane.setVisible(true);
                        Label l = (Label) loadingPane.getChildren().get(0);
                        l.setText("Papers folder is empty. Add Papers using the file structure: \n\n" +
                                "E.g for IB CS -  Papers/<year>/<month>/*.pdf \n\n" +
                                "You can check the structure in the topics.json file to make sure.\n\n" +
                                "See help/guide file for more details.");
                    }
                    FXCollections.reverse(PaperNames); //newest Papers at top of list.

                return null;
            }
        };
        ProgressBar pb = (ProgressBar) loadingPane.getChildren().get(1);
        pb.progressProperty().bind(task.progressProperty());
        pb.setPrefWidth(200);
        new Thread(task).start();



        //open and read Json for any previously saved data.
        readJson();

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        imageViewMS.setPreserveRatio(true);
        imageViewMS.setSmooth(true);
        imageViewMS.setCache(true);
        scrollPaneMS.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        //a flag to stop the mtopicsChoiceBox action triggering and loading a new paper when you click on a question in the currently loaded Paper paper

        QuestionTopicsChoiceBox.setItems(questionsTopicsList);
        QuestionTopicsChoiceBox.setOnAction(event -> {
            QuestionTopicsChoiceBox.getSelectionModel().selectFirst();
        });

        mtopicsChoiceBox.setItems(topics);
        mtopicsChoiceBox.setOnAction(event -> {
            if(selectAndChangePaper.get() == true ) {
                try {
                    //sets the topic questions listview.
                    setTopicQuestionsList(mtopicsChoiceBox.getSelectionModel().getSelectedIndex());
                    //selects the right paper (this loads the paper too).
                    mpaperChoiceBox.getSelectionModel().select(topics.get(mtopicsChoiceBox.getSelectionModel().getSelectedIndex()).getQuestions().get(0).getPaper());
                   //fix - should do this separately.

                    //scroll to the first question/markscheme of whatever topic was chosen.
                    scrollTo(scrollPane, topics.get(mtopicsChoiceBox.getSelectionModel().getSelectedIndex()).getQuestions().get(0).getScrollLocation());
                    scrollTo(scrollPaneMS, topics.get(mtopicsChoiceBox.getSelectionModel().getSelectedIndex()).getQuestions().get(0).getScrollLocationMS());

                }catch(Exception e){
                   //customAlert("No questions have been added to this topic. Select a paper first and find add question related to this topic.");
                }
            }
        });
        
        mpaperChoiceBox.setItems(PaperNames);
        mpaperChoiceBox.setOnAction(event -> {
            try {
                File paperFile = new File( System.getProperty("user.dir") + "/Papers/" + mpaperChoiceBox.getValue().toString());
                File markschemeFile = new File( System.getProperty("user.dir") + "/Papers/" +mpaperChoiceBox.getValue().toString().substring(0,mpaperChoiceBox.getValue().toString().length()-8)+markschemeString+".pdf.png");
                
                setImage(paperFile.toURI().toString(), markschemeFile.toURI().toString());

                setPaperQuestionsList(mpaperChoiceBox.getValue().toString());

                String paper = mpaperChoiceBox.getValue().toString();

                int sp = 0;
                String questionN = null;
                if(lastSelectedList == mtopicSelectListView) {
                    sp = mtopicSelectListView.getSelectionModel().getSelectedItem().toString().indexOf(" ")-1;
                    questionN = mtopicSelectListView.getSelectionModel().getSelectedItem().toString().substring(0,sp);
                }else{
                    sp = mpaperSelectListView.getItems().get(0).toString().indexOf(" ")-1;
                    questionN = mpaperSelectListView.getItems().get(0).toString().substring(0,sp);
                }

                for (Topic t : topics) {
                    for (Question q : t.getQuestions()) {
                        if (q.getPaper().equals(paper) && q.getQuestionNumber().equals(questionN)) {
                           /// if (q.equals(lastQuestionClicked)) { //found the topic for last question clicked...
                            setTopicQuestionsList(topics.indexOf(t));
                            scrollTo(scrollPane, q.getScrollLocation());
                            scrollTo(scrollPaneMS, q.getScrollLocationMS());
                            selectAndChangePaper.set(false);
                            mtopicsChoiceBox.getSelectionModel().select(t);
                            selectAndChangePaper.set(true);
                            mpaperSelectListView.getSelectionModel().selectFirst();
                            setQuestionsTopicsChoiceBox();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });



        mpaperSelectListView.setOnMouseClicked(event -> {
            lastSelectedList = mpaperSelectListView;
       
            setQuestionsTopicsChoiceBox();

            Question lastQuestionClicked = getQuestionByLastClicked();
            for (Topic t : topics) {
                for (Question q : t.getQuestions()) {
                    if (q.equals(lastQuestionClicked)) { //found the topic for last question clicked...
                        setTopicQuestionsList(topics.indexOf(t));
                        scrollTo(scrollPane, q.getScrollLocation());
                        scrollTo(scrollPaneMS, q.getScrollLocationMS());
                        selectAndChangePaper.set(false);
                        mtopicsChoiceBox.getSelectionModel().select(t);
                        selectAndChangePaper.set(true);
                    }
                }
            }
        });

        mtopicSelectListView.setOnMouseClicked(event -> {
            lastSelectedList = mtopicSelectListView;
            setQuestionsTopicsChoiceBox();
            Question lastQuestionClicked = getQuestionByLastClicked();
            
            if(!lastQuestionClicked.getPaper().equals(mpaperChoiceBox.getSelectionModel().getSelectedItem().toString())) {
                mpaperChoiceBox.getSelectionModel().select(PaperNames.indexOf(lastQuestionClicked.getPaper()));
                scrollTo(scrollPane, lastQuestionClicked.getScrollLocation());
                scrollTo(scrollPaneMS, lastQuestionClicked.getScrollLocationMS());
                mtopicSelectListView.getSelectionModel().select(lastQuestionClicked);
            }else{
                scrollTo(scrollPane, lastQuestionClicked.getScrollLocation());
                scrollTo(scrollPaneMS, lastQuestionClicked.getScrollLocationMS());
            }
        });

        mtopicSelectListView.setItems(listViewTopicQ);
        mpaperSelectListView.setItems(listViewPaperQ);
    }

    public Question getQuestionByLastClicked(){
        for (Topic t:topics) {
            for (Question q: t.getQuestions()) {
                if (q.toString().equals(lastSelectedList.getFocusModel().getFocusedItem().toString())){
                 return q;
                }
            }
        }
        System.out.println("Last clicked question not found.");
        return null;
    }
    public void setQuestionsTopicsChoiceBox(){
        questionsTopicsList.clear();
        ArrayList<Integer> topicIndex = new ArrayList();
        int i = 0;
        try{
            for (Topic t : topics) {
                for (Question q : t.getQuestions()) {
                    if (q.toString().equals(lastSelectedList.getSelectionModel().getSelectedItem().toString())) {
                        topicIndex.add(i);
                    }
                }
                i++;
            }
        }catch(Exception e){
            //System.out.println("no last selected list");
            for (Topic t : topics) {
                for (Question q : t.getQuestions()) {
                    if (q.toString().equals(mpaperSelectListView.getItems().get(0).toString())) {
                        topicIndex.add(i);
                    }
                }
                i++;
            }
        }

        questionsTopicsList.add("This question is found in "+topicIndex.size()+" topics.");
        for (int index: topicIndex) {
            questionsTopicsList.add(topics.get(index).toString());
        }
        QuestionTopicsChoiceBox.getSelectionModel().selectFirst();
    }
    public void initializeTopBar(){
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
                modifyPaperPane.getScene().getWindow().setX(event.getScreenX() - xOffset);
                modifyPaperPane.getScene().getWindow().setY(event.getScreenY() - yOffset);
            }
        });
    }


    public void setPaperQuestionsList(String paperName){
        listViewPaperQ.clear();
        ArrayList<Question> tempList = new ArrayList<>();
        for (Topic t: topics) {
            for (Question q : t.getQuestions()) {
                if(q.getPaper().equals(paperName)){
                    tempList.add(q);
                }
            }
        }

        tempList.sort(Comparator.comparing(Question::getQuestionNumber));

        for(int i = 0; i < tempList.size(); i++){
            if(i==0){
                listViewPaperQ.add(tempList.get(i));
            }else{
                String current = tempList.get(i).getQuestionNumber();
                String previous = tempList.get(i-1).getQuestionNumber();
                if (current.equals(previous)){
                    //do nothing since it is duplicate. (some questions have multiple topics, they show up for each topic).
                }else{
                    listViewPaperQ.add(tempList.get(i));
                }
            }
        }
        listViewPaperQ.sort(Comparator.comparing(Question::getQuestionNumber));
    }

    public void setTopicQuestionsList(int topicIndex){
        listViewTopicQ.clear();
        for (Question question: topics.get(topicIndex).getQuestions()) {
            listViewTopicQ.add(question);
        }
        listViewTopicQ.sort(Comparator.comparing(Question::getQuestionNumber));
    }

    public void getPaperTopicsList(){
        Dialog<Topic> dialog = new Dialog<>();
        dialog.initModality(Modality.NONE);
        dialog.setTitle("Topics for this paper");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK);

        Label paperLabel = new Label("Question Paper:");
        Label paper = new Label(mpaperChoiceBox.getSelectionModel().getSelectedItem().toString());

        Label topicsLabel = new Label("Topics:");

        ArrayList<String> topicsListArray = new ArrayList<>();
        for (Topic t:topics) {
            for (Question q: t.getQuestions()) {
                if (q.getPaper().equals(mpaperChoiceBox.getSelectionModel().getSelectedItem().toString())){
                    topicsListArray.add(t.toString());
                }
            }
        }

        topicsListArray = removeDuplicates(topicsListArray);
        TextArea topicsList = new TextArea();
        for(String a : topicsListArray){
            topicsList.appendText(a + "\n");
        }

        dialogPane.setContent(new VBox(8, paperLabel, paper,topicsLabel,topicsList));

        Optional<Topic> optionalResult = dialog.showAndWait();
    }

    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {
        // Create a new LinkedHashSet
        Set<T> set = new LinkedHashSet<>();

        // Add the elements to set
        set.addAll(list);

        // Clear the list
        list.clear();

        // add the elements of set
        // with no duplicates to the list
        list.addAll(set);

        // return the list
        return list;
    }


    /**
     * Modify Paper
     */

    public void questionDialog(String QuestionS,ArrayList<Integer> topicIndex){
        Dialog<Topic> dialog = new Dialog<>();
        dialog.initModality(Modality.NONE);
        Stage stage = (Stage) modifyPaperPane.getScene().getWindow();
        dialog.initOwner(stage);

        dialog.setTitle("Add new Question");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Label paperLabel = new Label("Question Paper:");
        Label paper = null;
        try{
        paper = new Label(this.mpaperChoiceBox.getSelectionModel().getSelectedItem().toString());
        }catch (NullPointerException e){
            customAlert("Make sure you select a question paper at the bottom of the main window before adding a question.");
        }

        Label questionLabel = new Label("Question number:");
        TextField Question = new TextField(QuestionS);

        FlowPane flowPane = new FlowPane();
        flowPane.prefWidth(288);
        Label scrollInfo = new Label("Paper anchor: \n(Scroll to the position of the question)");
        scrollInfo.setPrefWidth(288);
        TextField scroll = new TextField();
        scroll.setText(Double.toString(getVScrollValue(scrollPane)));
        scroll.setEditable(false);
        Button scrollBtn = new Button("Update");

        Label scrollInfo2 = new Label("Markscheme anchor: \n(Scroll to the position of the answer)");
        scrollInfo2.setPrefWidth(288);
        TextField scrollMS = new TextField();
        scrollMS.setText(Double.toString(getVScrollValue(scrollPaneMS)));
        scrollMS.setEditable(false);
        Button scrollMSBtn = new Button("Update");

        scrollBtn.setOnAction(event -> scroll.setText(Double.toString(getVScrollValue(scrollPane))));
        scrollMSBtn.setOnAction(event -> scrollMS.setText(Double.toString(getVScrollValue(scrollPaneMS))));

        flowPane.getChildren().addAll(scrollInfo, scroll, scrollBtn, scrollInfo2, scrollMS, scrollMSBtn);

        Label topicLabel = new Label("Topic/s:" );
        ListView topicList = new ListView();
        topicList.setMaxHeight(300);

        ObservableList<CheckBox> topicsCB = FXCollections.observableArrayList();
        int i=0;
        for (Topic t:topics) {
            CheckBox cb=new CheckBox(t.toString());
            try {
                for (int ti : topicIndex) {
                    if (ti == i) {
                        cb.setSelected(true);
                    }
                }
            }catch(Exception e){
                System.out.println("no topic chosen, must be a new question?");
            }
            i++;
            topicsCB.add(cb);
        }
        topicList.setItems(topicsCB);

        ArrayList selectedTopics = new ArrayList();
        dialogPane.setContent(new VBox(8, paperLabel, paper, questionLabel,Question,flowPane,topicLabel,topicList));
        Platform.runLater(Question::requestFocus);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Label finalPaper = paper;
        btOk.addEventFilter(
                ActionEvent.ACTION,
                event -> {

                    for (CheckBox cb: topicsCB) {
                        if(cb.isSelected()){
                            selectedTopics.add(cb.getText());
                        }
                    }
                    if(!Question.getText().equals("")&&!scroll.getText().equals("")&&!selectedTopics.isEmpty()) {
                        //delete the question from all topics.
                        ArrayList<Question> found = new ArrayList<>();
                        try {
                            for (int ti : topicIndex) {
                                for (Question q : topics.get(ti).getQuestions()) {
                                    //find the original question.
                                    if (q.getQuestionNumber().equals(QuestionS) && q.getPaper().equals(finalPaper.getText())) {
                                        found.add(q);
                                    }
                                    //find the question it might replace. (if the question number entered already exists in the paper)
                                    if (q.getQuestionNumber().equals(Question.getText()) && q.getPaper().equals(finalPaper.getText())) {
                                        found.add(q);
                                    }
                                }
                                topics.get(ti).questions.removeAll(found);
                            }
                        }catch(Exception e){
                            System.out.println("No topicIndex, must be a new question.");
                        }

                        //Create the new question
                        for (Object item:selectedTopics) {
                            for (Topic t:topics){
                                if(item.equals(t.toString())){
                                    if(Question.getText().length()<2 || !Character.isDigit(Question.getText().charAt(1))){
                                        Question.setText("0" + Question.getText());
                                    }
                                    t.addQuestion(finalPaper.getText(), Question.getText(),Double.parseDouble(scroll.getText()), Double.parseDouble(scrollMS.getText()));
                                    Collections.sort(t.questions);

                                    setPaperQuestionsList(mpaperChoiceBox.getValue().toString());
                                    setTopicQuestionsList(topics.indexOf(t));
                                    selectAndChangePaper.set(false);
                                    mtopicsChoiceBox.getSelectionModel().select(t);
                                    selectAndChangePaper.set(true);
                                    try {
                                        saveJson(new ActionEvent());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }else{
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Incorrect input");
                        alert.setHeaderText(null);
                        alert.setContentText("Make sure everything is filled in correctly.");
                        alert.showAndWait();
                        event.consume();
                    }
                }
        );
        Optional<Topic> optionalResult = dialog.showAndWait();
    }

    public void customAlert(String s){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Alert!");
        alert.setHeaderText(null);
        alert.setContentText(s);
        alert.showAndWait();
    }

    public void ModifyQuestionBtn(ActionEvent actionEvent) {
        try{
            //find first space in the question string to retrieve the question number.
            int sp = lastSelectedList.getFocusModel().getFocusedItem().toString().indexOf(" ")-1;
            String questionN = lastSelectedList.getFocusModel().getFocusedItem().toString().substring(0,sp);

            ArrayList<Integer> topicIndex = new ArrayList();
            int i = 0;
            for (Topic t: topics) {
                for (Question q: t.getQuestions()) { //if question is the same as last selected question
                    if(q.toString().equals(lastSelectedList.getFocusModel().getFocusedItem().toString())){
                       //if the paper of the question found above is the same as the current question paper
                        if(q.getPaper().equals(mpaperChoiceBox.getSelectionModel().getSelectedItem().toString()))
                        topicIndex.add(i);
                    }
                }
                i++;
            }
            questionDialog(questionN,topicIndex);
        }catch (NullPointerException e){
            customAlert("Make sure you select a question on the right of the main window before modifying a question.");
        }
    }

    public void addQuestion(ActionEvent actionEvent) {
        ArrayList<Integer> topicIndex = new ArrayList();
        questionDialog("",topicIndex) ;
    }

    /**
     * pop up window for adding/modifying topics and/or adding more papers
     */
    public void modifyTopicsBtn(ActionEvent actionEvent) throws IOException, UnsupportedFlavorException {

        final Stage topicsStage = new Stage();
        topicsStage.initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) modifyPaperPane.getScene().getWindow();
        topicsStage.initStyle(StageStyle.TRANSPARENT);

        topicsStage.initOwner(stage);
        Scene dialogScene = new Scene(loadFXML("newTopic"));
        dialogScene.setFill(Color.TRANSPARENT); //transparent for custom menu

        topicsStage.setScene(dialogScene);
        topicsStage.setX( stage.getX() + 25);
        topicsStage.setY(stage.getY() + 25);
        topicsStage.showAndWait();

        //reload json after returning from modify topics window
        readJson();
        mtopicsChoiceBox.setItems(topics);
    }

    public void saveJson(ActionEvent actionEvent) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("topics.json")) {
            gson.toJson(topics, writer);
            System.out.println("Saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readJson() throws IOException {
        Gson gson = new Gson();
        try (Reader reader = new FileReader("topics.json")) {
            // Convert JSON File to Java Object
            ArrayList<Topic> imports = gson.fromJson(reader, new TypeToken<ArrayList<Topic>>() {}.getType());
            topics = FXCollections.observableArrayList(imports);
        } catch (IOException e) {
            e.printStackTrace();
            customAlert("topics.json not found in root folder. Creating a new empty one. If you wish to import one, read the readme file for details.");
            File myObj = new File("topics.json");
            myObj.createNewFile();
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    /** Scoll functions **/
    public void scrollTo(ScrollPane pane, double y) {
        pane.setVvalue(y);
    }

    public Double getVScrollValue(ScrollPane pane) {
        return pane.getVvalue();
    }

    /** MENU BUTTONS **/
    public void close(ActionEvent actionEvent) {
        System.exit(0);
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

        TextFlow contents = new TextFlow();
        contents.getChildren().add(new Text("Past Paper pro was designed and created by"));
        Text name =  new Text(" Matt Elliot ");
        name.setStyle("-fx-font-weight: bold");
        contents.getChildren().add(name);
        contents.getChildren().add(new Text("as a means to organize past Papers and help improve student learning. \n\r"));
        contents.getChildren().add(new Text("If this program was useful please send me a line, I would love to hear how/where this is being used. \n\r"));
        contents.getChildren().add(new Text("If it is super useful, buy me a drink to show your appreciation: "));
        contents.getChildren().add(new Hyperlink("Paypal (to be added) bluishmatt@gmail.com"));
        contents.getChildren().add(new Text("\n\rPlease report any issues or feature requests on github."));

        alert.getDialogPane().setContent(contents);
        alert.showAndWait();
    }
    public void minimize(ActionEvent actionEvent) {
        Stage stage = (Stage) modifyPaperPane.getScene().getWindow();
        stage.setIconified(true);
    }


    public void deleteQuestion(ActionEvent actionEvent) {
        try{
            String paper = mpaperChoiceBox.getSelectionModel().getSelectedItem().toString();
            //find first space in the question string to retrieve the question number.
            int sp = lastSelectedList.getFocusModel().getFocusedItem().toString().indexOf(" ")-1;
            String questionN = lastSelectedList.getFocusModel().getFocusedItem().toString().substring(0,sp);

            ArrayList<Integer> topicIndex = new ArrayList();
            int i = 0;
            for (Topic t: topics) {
                for (Question q: t.getQuestions()) {
                    if(q.getQuestionNumber().equals(questionN)){
                        topicIndex.add(i);
                    }
                }
                i++;
            }

            //delete the question from all topics.
            ArrayList<Question> found = new ArrayList<>();
            for (int ti : topicIndex) {
                for (Question q : topics.get(ti).getQuestions()) {
                    //find the original question.
                    if (q.getQuestionNumber().equals(questionN) && q.getPaper().equals(paper)) {
                        found.add(q);
                    }
                }
                topics.get(ti).questions.removeAll(found);
            }

        }catch (Exception e){
            customAlert("error deleting question");
            System.out.println(e);
        }

       // UPDATE LISTS.
        setTopicQuestionsList(mtopicsChoiceBox.getSelectionModel().getSelectedIndex());
        setPaperQuestionsList(mpaperChoiceBox.getValue().toString());

        try {
            saveJson(new ActionEvent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

