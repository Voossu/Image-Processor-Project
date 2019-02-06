package ImageProcessor;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Controller {

    public ImageView BaseImage;
    public ImageView FPTNImage;
    public ImageView FPTYImage;
    public ImageView OPTNImage;
    public ImageView OPTYImage;

    private Image OpenImageFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Image Files", "*.tif", "*.tiff", "*.png", "*.jpg", ".jpeg", "*.gif"),
                new ExtensionFilter("All Files", "*.*"));
        File file = fileChooser.showOpenDialog(null);
        return new Image(file.toURI().toString());
    }

    private void SaveImageFile(Image image) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            ImageIO.write(SwingFXUtils.fromFXImage(image,null), "png", file);
        }
    }

    private void InitImage(Image image) throws IOException {

        Image base = image;
        Image fptn = QImage.LinearQuantization(base, 256);
        Image optn = QImage.PopularityMethod(base,256);
        Image fpty = QImage.DoToning(base, fptn);
        Image opty = QImage.DoToning(base, optn);

        BaseImage.setImage(base);

        FPTNImage.setImage(fptn);
        OPTNImage.setImage(optn);
        FPTYImage.setImage(fpty);
        OPTYImage.setImage(opty);
    }


    public void QuitProgram(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void OpenImage(ActionEvent actionEvent) throws IOException {
        Image i = OpenImageFile();
        InitImage(i);
    }


}
