package ImageProcessor;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class QImage {

    /**
     *
     * @param cl Color for compare
     * @param cr Color for compare
     * @return Evaluation of the discrepancy
     */
    public static double ColorEST(Color cl, Color cr) {
        return Math.pow((cl.getRed() - cr.getRed()) * 255, 2) + Math.pow((cl.getGreen() - cr.getGreen()) * 255, 2) + Math.pow((cl.getBlue() - cr.getBlue()) * 255, 2) + Math.pow((cl.getOpacity() - cr.getOpacity()) * 255, 2);
    }

    /**
     *
     * @param il Color for compare
     * @param ir Color for compare
     * @return Evaluation of the discrepancy
     */
    public static double ImageEST(Image il, Image ir) {
        if (il.getWidth() != ir.getWidth() || il.getHeight() != ir.getHeight()) throw new IllegalArgumentException("Images must be the same size!");
        double error = 0;
        PixelReader rl = il.getPixelReader();
        PixelReader rr = ir.getPixelReader();
        for (int x = 0; x < il.getWidth(); x++) {
            for (int y = 0; y < il.getHeight(); y++) {
                error += ColorEST(rl.getColor(x, y), rr.getColor(x, y));
            }
        }
        return error;
    }

    /**
     *
      * @param image image for palette application
     * @param palette palette for application
     * @return image with applied palette
     */
    public static Image ApplyPalette(Image image, ArrayList<Color> palette) {
        PixelReader reader = image.getPixelReader();
        WritableImage edit = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter writer = edit.getPixelWriter();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color nowColor = reader.getColor(x, y);
                Color newColor = palette.get(0);
                double est = ColorEST(newColor, nowColor);
                for (Color tryColor: palette) {
                    double tryEST = ColorEST(nowColor,tryColor);
                    if (tryEST < est) {
                        newColor = tryColor;
                        est = tryEST;
                    }
                }
                writer.setColor(x,y,newColor);
            }
        }
        return edit;
    }

    /**
     *
     * @param image
     * @param numberOfColors
     * @return
     */
    public static Image LinearQuantization(Image image, int numberOfColors) {
        PixelReader reader = image.getPixelReader();
        WritableImage edit = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter writer = edit.getPixelWriter();
        byte trimDegree = (byte)Math.pow(2, ((24 - (byte)(Math.log(numberOfColors) / Math.log(2))) / 3));
        for (int x = 0; x < edit.getWidth(); x++) {
            for (int y = 0; y < edit.getHeight(); y++) {
                Color color = reader.getColor(x, y);
                writer.setColor(x, y, Color.color(color.getRed() - color.getRed() * 255 % trimDegree / 255, color.getGreen() - color.getGreen() * 255 % trimDegree / 255, color.getBlue() - color.getBlue() * 255 % trimDegree / 255, color.getOpacity()));
            }
        }
        return edit;
    }

    /**
     *
     * @param image
     * @param numberOfColors
     * @return ArrayList
     */
    public static ArrayList<Color> GetPopularColors(Image image, long numberOfColors) {
        PixelReader reader = image.getPixelReader();
        HashMap<Color,Long> colorPallet = new HashMap<>();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = reader.getColor(x, y);
                colorPallet.put(color, colorPallet.getOrDefault(color, 0L) + 1L);
            }
        }
        ArrayList<Color> popularColors = new ArrayList<>();
        colorPallet.entrySet().stream().sorted(Map.Entry.<Color,Long>comparingByValue().reversed()).limit(numberOfColors).forEach(colorLongEntry -> popularColors.add(colorLongEntry.getKey()));
        return popularColors;
    }

    /**
     *
     * @param baseImage
     * @param editImage
     * @return
     */
    public static Image DoToning(Image baseImage, Image editImage) {
        if (baseImage.getWidth() != editImage.getWidth() || baseImage.getHeight() != editImage.getHeight()) throw new IllegalArgumentException("Images must be the same size!");

        PixelReader baseReader = baseImage.getPixelReader();
        PixelReader editReader = editImage.getPixelReader();

        WritableImage writeImage = new WritableImage((int) baseImage.getWidth(), (int) baseImage.getHeight());
        PixelWriter writer = writeImage.getPixelWriter();

        for (int x = 0; x < baseImage.getWidth(); x++)
        {
            writer.setColor(x,0,editReader.getColor(x,0));
        }


        for (int y = 0; y < baseImage.getHeight(); y++) {

            for (int x = 0; x < baseImage.getWidth(); x++) {

                Color baseColor = baseReader.getColor(x, y);
                Color editColor = editReader.getColor(x, y);
                double errorR = baseColor.getRed() - editColor.getRed();
                double errorG = baseColor.getGreen() - editColor.getGreen();
                double errorB = baseColor.getBlue() - editColor.getBlue();

                if (y != baseImage.getHeight() - 1) {
                    if (x != 0) {
                        writer.setColor(x - 1,y + 1, Color.color(editColor.getRed() + 0.25 * errorR, editColor.getGreen() + 0.25 * errorG, editColor.getBlue() + 0.25 * errorB, editColor.getOpacity()));
                    }
                    writer.setColor(x, y + 1, Color.color(editColor.getRed() + 0.25 * errorR, editColor.getGreen() + 0.25 * errorG, editColor.getBlue() + 0.25 * errorB, editColor.getOpacity()));
                }
                if (x != baseImage.getWidth() - 1) {
                    if (y != baseImage.getHeight() - 1) {
                        writer.setColor(x + 1, y + 1, Color.color(editColor.getRed() + 0.25 * errorR, editColor.getGreen() + 0.25 * errorG, editColor.getBlue() + 0.25 * errorB, editColor.getOpacity()));
                    }
                    writer.setColor(x + 1, y, Color.color(editColor.getRed() + 0.25 * errorR, editColor.getGreen() + 0.25 * errorG, editColor.getBlue() + 0.25 * errorB, editColor.getOpacity()));
                }
            }
        }
        return writeImage;
    }

    /**
     *
     * @param image
     * @param numberOfColors
     * @return
     */
    public static Image PopularityMethod(Image image, int numberOfColors) {
        return ApplyPalette(image, GetPopularColors(image, numberOfColors));
    }

}
