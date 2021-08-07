package co.uk.mattelliot;

import javafx.scene.control.Alert;

public class LoadingPapers {
    public Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
    public int progress = 0;

    public LoadingPapers(){
            loadingAlert.setTitle("Alert!");
            loadingAlert.setHeaderText("Converting past papers into PNG files.");
            loadingAlert.setContentText(progress +"% complete.");
            loadingAlert.show();
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress += progress;
    }
}
