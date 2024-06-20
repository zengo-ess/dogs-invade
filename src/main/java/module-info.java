module javafx.myjavafxproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens javafx.myjavafxproject to javafx.fxml;
    exports javafx.myjavafxproject;
}