package gestion;
import com.formdev.flatlaf.extras.FlatSVGIcon;
public class TestSVG {
    public static void main(String[] args) {
        FlatSVGIcon icon = new FlatSVGIcon("/resources/icons/plus.svg");
        if (icon.hasFound()) {
            System.out.println("SVG trouvé par FlatSVGIcon !");
        } else {
            System.err.println("SVG NON TROUVÉ par FlatSVGIcon.");
        }
    }
}
