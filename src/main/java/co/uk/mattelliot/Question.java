package co.uk.mattelliot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Question implements Comparable<Question>{

    String paper;
    String questionNumber;
    Double scrollLocation;
    Double scrollLocationMS;

    public Question(String paper, String questionNumber, Double scrollLocation, Double scrollLocationMS){
        this.paper = paper;
        this.questionNumber = questionNumber;
        this.scrollLocation = scrollLocation;
        this.scrollLocationMS = scrollLocationMS;
    }

    public String getPaper() {
        return paper;
    }

    public void setPaper(String paper) {
        this.paper = paper;
    }

    public String getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(String questionNumber) {
        this.questionNumber = questionNumber;
    }

    public double getScrollLocation() {
        return this.scrollLocation;
    }

    public void setScrollLocation(Double scrollLocation) {
        this.scrollLocation = scrollLocation;
    }

    public double getScrollLocationMS() {
        return this.scrollLocationMS;
    }

    public void setScrollLocationMS(Double scrollLocationMS) {
        this.scrollLocationMS = scrollLocationMS;
    }


    @Override
    public String toString() {
        Pattern pattern = Pattern.compile("paper.*png", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(paper);
        if (matcher.find()) {
            return questionNumber + ". " +paper.substring(0,9) + matcher.group(); // you can get it from desired index as well
        } else {
            return null;
        }
    }

    @Override
    public int compareTo(Question o) {
        return this.getQuestionNumber().compareTo(o.getQuestionNumber());
    }

}
