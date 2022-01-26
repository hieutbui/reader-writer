package com.example.btlhdh;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.ListView;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.Semaphore;

public class Controller {

    @FXML
    private Button addReader;

    @FXML
    private Button addWriter;

    @FXML
    private Button start;

    @FXML
    private ListView<String> screen = new ListView<>();

    @FXML
    private HBox serviceArea;

    @FXML
    private HBox waiting;

    private Queue<Integer> numReader = new LinkedList<>();
    private Queue<Integer> numWriter = new LinkedList<>();
    public List<ReaderAndWriter> stack = new ArrayList<>();
    private Queue<Integer> stackReader = new LinkedList<>();
    private Queue<Integer> stackWriter = new LinkedList<>();
    static int readCount = 0;
    static Semaphore mutex = new Semaphore(1);
    static Semaphore database = new Semaphore(1);

    static class ReaderAndWriter implements Runnable{
        public int check() {
            return 0;
        }
        @Override
        public void run() {

        }
    }

    class Read extends ReaderAndWriter implements Runnable {
        @Override
        public int check() {
            return 1;
        }

        @Override
        public void run() {
            try {
                mutex.acquire();
                readCount++;
                if (readCount == 1) {
                    database.acquire();
                }
                mutex.release();
                int num = numReader.remove();
                Platform.runLater(() -> {
                    screen.getItems().add("Reader " + num + " is READING");
                });
                Label reader = new Label();
                reader.setStyle("-fx-background-color: #204429;-fx-padding:50px 10px 50px 10px;-fx-background-radius:4px;-fx-border-color: #636f44;-fx-border-radius:100px;");
                reader.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 17));
                Platform.runLater(() -> {
                    int index = stackReader.remove();
                    stack.remove(index);
                    waiting.getChildren().remove(index);
                    stackReader.clear();
                    stackWriter.clear();
                    for (ReaderAndWriter i : stack){
                        if (i.check() == 1){
                            stackReader.add(stack.indexOf(i));
                        } else {
                            stackWriter.add(stack.indexOf(i));
                        }
                    }
                    TranslateTransition transition = new TranslateTransition();
                    transition.setNode(reader);
                    transition.setByX(470);
                    transition.setDuration(Duration.millis(2000));
                    transition.play();
                    serviceArea.getChildren().addAll(reader);
                });
                Thread.sleep(2500);
                Platform.runLater(() -> {
                    serviceArea.getChildren().removeAll(reader);
                    screen.getItems().addAll("Reader " + num + " has FINISHED READING");
                });

                mutex.acquire();
                readCount--;
                if (readCount == 0) {
                    database.release();
                }
                mutex.release();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    class Write extends ReaderAndWriter implements Runnable {
        @Override
        public int check() {
            return 2;
        }

        @Override
        public void run() {
            try {
                database.acquire();
                int num = numWriter.remove();
                Platform.runLater(() -> {
                    screen.getItems().add("Writer " + num + " is WRITING");
                });
                Label writer = new Label();
                writer.setStyle("-fx-background-color: #a63d4c;-fx-padding:50px 10px 50px 10px;-fx-background-radius:4px;-fx-border-color: #c8777b;-fx-border-radius:100px;");
                writer.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 17));
                Thread.sleep(500);
                Platform.runLater(() -> {
                    int index = stackWriter.remove();
                    stack.remove(index);
                    waiting.getChildren().remove(index);
                    stackReader.clear();
                    stackWriter.clear();
                    for (ReaderAndWriter i : stack) {
                        if (i.check() == 1){
                            stackReader.add(stack.indexOf(i));
                        } else {
                            stackWriter.add(stack.indexOf(i));
                        }
                    }
                    TranslateTransition transition = new TranslateTransition();
                    transition.setNode(writer);
                    transition.setByX(470);
                    transition.setDuration(Duration.millis(2500));
                    transition.play();
                    serviceArea.getChildren().addAll(writer);
                });
                Thread.sleep(3000);
                Platform.runLater(() ->{
                    serviceArea.getChildren().removeAll(writer);
                    screen.getItems().add("Writer " + num + " has FINISHED WRITING");
                });
                database.release();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @FXML
    public void setAddReader () {
        int num = stackReader.size() + 1;
        stackReader.add(num);
        numReader.add(num);
        ReaderAndWriter read = new Read();
        stack.add(read);
        Label reader = new Label("" + num);
        reader.setStyle("-fx-background-color: #204429;-fx-padding:50px 10px 50px 10px;-fx-background-radius:4px;-fx-border-color: #636f44;-fx-border-radius:100px;");
        reader.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 17));
        waiting.getChildren().add(stack.indexOf(read), reader);
    }

    @FXML
    public void setAddWriter (){
        int num = stackWriter.size() + 1;
        stackWriter.add(num);
        numWriter.add(num);
        ReaderAndWriter write = new Write();
        stack.add(write);
        Label writer = new Label(""+num);
        writer.setStyle("-fx-background-color: #a63d4c;-fx-padding:50px 10px 50px 10px;-fx-background-radius:4px;-fx-border-color: #c8777b;-fx-border-radius:100px;");
        writer.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 17));
        waiting.getChildren().add(stack.indexOf(write), writer);
    }

    @FXML
    public void setStart (){
        screen.getItems().clear();
        stackReader.clear();
        stackWriter.clear();
        for (ReaderAndWriter i : stack){
            if (i.check() == 1){
                stackReader.add(stack.indexOf(i));
            } else {
                stackWriter.add(stack.indexOf(i));
            }
            Thread thread =new Thread(i);
            thread.start();
        }
    }
}
