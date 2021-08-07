package co.uk.mattelliot;

import java.util.ArrayList;

public class Topic {
    String number;
    String topic;
    String subtopic;
    String link;

    ArrayList<Question> questions = new ArrayList<>();

    public Topic(String number, String topic, String subtopic) {
        this.number = number;
        this.topic = topic;
        this.subtopic = subtopic;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public void addQuestion(String paper, String questionNumber, Double scrollLocation, Double scrollLocationMS) {
        this.questions.add(new Question(paper, questionNumber,scrollLocation, scrollLocationMS));
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String getSubtopic() {
        return subtopic;
    }
    public void setSubtopic(String subtopic) {
        this.subtopic = subtopic;
    }

    @Override
    public String toString() {
        return number+ ": "+ topic + " - "+subtopic;
    }
}
