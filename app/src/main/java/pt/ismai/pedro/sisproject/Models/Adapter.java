package pt.ismai.pedro.sisproject.Models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pt.ismai.pedro.sisproject.R;

public class Adapter extends PagerAdapter {

    private List<TypeOfGame> typeOfGames;
    private LayoutInflater layoutInflater;
    private Context context;

    public Adapter(List<TypeOfGame> typeOfGames, Context context) {
        this.typeOfGames = typeOfGames;
        this.context = context;
    }

    @Override
    public int getCount() {
        return typeOfGames.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view.equals(o);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.game,container,false);

        ImageView imageView;
        TextView title;
        imageView = view.findViewById(R.id.image);
        title = view.findViewById(R.id.title);

        imageView.setImageResource(typeOfGames.get(position).getImage());
        title.setText(typeOfGames.get(position).getType());

        container.addView(view,0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);

    }
}
