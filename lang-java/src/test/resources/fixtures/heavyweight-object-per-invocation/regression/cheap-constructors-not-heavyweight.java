public class CheapConstructors {
    // DecimalFormat is cheap enough to construct inline
    public String formatPrice(double price) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(price);
    }

    // File is a path wrapper, no IO in constructor
    public boolean exists(String path) {
        File f = new File(path);
        return f.exists();
    }
}
