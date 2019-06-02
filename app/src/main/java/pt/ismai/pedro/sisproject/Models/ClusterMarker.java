package pt.ismai.pedro.sisproject.Models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {

    private LatLng position;
    private String title;
    private String snippet;
    private int iconePicture;

    public ClusterMarker(LatLng position, String title, String snippet, int iconePicture) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.iconePicture = iconePicture;
    }

    public ClusterMarker() { }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public int getIconePicture() {
        return iconePicture;
    }

    public void setIconePicture(int iconePicture) {
        this.iconePicture = iconePicture;
    }

}
