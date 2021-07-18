package shell.app.jfx;

import android.widget.Space;

import com.mindrex.android.appsupport.ContextUtils;
import com.mindrex.shell.R;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import shell.app.Splash;

public final class Test extends Application {
    public Test() {
    }

    @Override
    public void start(Stage stage) {
        final ImageView iv = new ImageView();
        iv.setImage(new Image("https://upload.wikimedia.org/wikipedia/en/c/cc/JavaFX_Logo.png"));
        iv.setPreserveRatio(true);
        iv.setSmooth(false);
        final Text title = new Text(ContextUtils.getResources().getString(R.string.app_name_short));
        title.setFill(Color.BLACK);
        title.setStyle("-fx-font: 24 arial;");
        final Text subtitle = new Text(Splash.getRandom().toString());
        subtitle.setFill(Color.GRAY);
        subtitle.setStyle("-fx-font: 16 arial;");
        final HBox a = new HBox(title, subtitle);
        a.setAlignment(Pos.BOTTOM_CENTER);
        final Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        final Scene scene = new Scene(new StackPane(iv, a), bounds.getWidth(), bounds.getHeight());
        iv.fitWidthProperty().bind(scene.widthProperty().subtract(20));
        stage.setScene(scene);
        stage.show();
    }
}