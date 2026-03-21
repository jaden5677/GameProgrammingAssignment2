package ImageManager;
import javax.swing.ImageIcon;
import java.awt.Image;

public class ImageManager{
    public ImageManager(){}

    public static Image loadImage(String filename){
        return new ImageIcon(filename).getImage();
    }
}