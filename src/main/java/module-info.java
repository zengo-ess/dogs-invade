module javafx.myjavafxproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens javafx.myjavafxproject to javafx.fxml;
    exports javafx.myjavafxproject;
}