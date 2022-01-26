module com.example.btlhdh {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.btlhdh to javafx.fxml;
    exports com.example.btlhdh;
}