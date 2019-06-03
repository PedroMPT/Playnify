package pt.ismai.pedro.sisproject.Models;

public class TypeOfGame {

    private int image;
    private String type;

    public TypeOfGame(int image, String type) {
        this.image = image;
        this.type = type;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
