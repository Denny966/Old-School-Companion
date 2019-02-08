package com.dennyy.oldschoolcompanion.adapters;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dennyy.oldschoolcompanion.R;
import com.dennyy.oldschoolcompanion.interfaces.WorldmapCityClickListener;
import com.dennyy.oldschoolcompanion.models.Worldmap.City;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class WorldmapCitiesAdapter extends GenericAdapter<City> {
    private WorldmapCityClickListener listener;

    public WorldmapCitiesAdapter(Context context, WorldmapCityClickListener listener) {
        super(context, getCities());
        this.listener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.worldmap_city_row, null);

            viewHolder = new ViewHolder();
            viewHolder.name = convertView.findViewById(R.id.worldmap_city_name);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final City city = getItem(position);
        viewHolder.name.setText(city.name);
        viewHolder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onWorldmapCityClick(position, city);
                }
            }
        });
        return convertView;
    }

    private static class ViewHolder {
        public TextView name;
    }

    private static ArrayList<City> getCities() {
        ArrayList<City> cities = new ArrayList<>();

        cities.add(new City("Al Kharid", new Point(6466, 2769)));
        cities.add(new City("Ardougne", new Point(4454, 2418)));
        cities.add(new City("Barbarian Village", new Point(5819, 2043)));
        cities.add(new City("Brimhaven", new Point(4934, 2753)));
        cities.add(new City("Burgh de Rott", new Point(7089, 2636)));
        cities.add(new City("Burthope", new Point(5294, 1668)));
        cities.add(new City("Camelot", new Point(4862, 1835)));
        cities.add(new City("Chambers of Xeric", new Point(273, 1630)));
        cities.add(new City("Canifis", new Point(7069, 1843)));
        cities.add(new City("Catherby", new Point(5054, 1998)));
        cities.add(new City("Draynor Village", new Point(5894, 2523)));
        cities.add(new City("Edgeville", new Point(5864, 1843)));
        cities.add(new City("Falador", new Point(5584, 2223)));
        cities.add(new City("Grand Tree", new Point(3979, 1828)));
        cities.add(new City("Jatizso", new Point(3804, 893)));
        cities.add(new City("Lumbridge", new Point(6294, 2626)));
        cities.add(new City("Miscellania", new Point(4219, 688)));
        cities.add(new City("Mort'ton", new Point(7054, 2463)));
        cities.add(new City("Musa Point", new Point(5304, 2828)));
        cities.add(new City("Nardah", new Point(6874, 3583)));
        cities.add(new City("Neitiznot", new Point(3579, 893)));
        cities.add(new City("Pollnivneach", new Point(6654, 3388)));
        cities.add(new City("Port Khazard", new Point(4549, 2818)));
        cities.add(new City("Port Phasmatys", new Point(7614, 1848)));
        cities.add(new City("Port Sarim", new Point(5664, 2633)));
        cities.add(new City("Rellekka", new Point(4554, 1278)));
        cities.add(new City("Rimmington", new Point(5449, 2643)));
        cities.add(new City("Seers' Village", new Point(4709, 1858)));
        cities.add(new City("Shilo Village", new Point(5124, 3383)));
        cities.add(new City("Sophanem", new Point(6479, 3968)));
        cities.add(new City("Tai Bwo Wannai", new Point(4964, 3113)));
        cities.add(new City("Taverley", new Point(5284, 1978)));
        cities.add(new City("Tirannwn", new Point(2551, 2535)));
        cities.add(new City("Tutorial Island", new Point(5904, 3013)));
        cities.add(new City("Varrock", new Point(6219, 1998)));
        cities.add(new City("Waterbirth Island", new Point(4179, 1083)));
        cities.add(new City("Yanille", new Point(4314, 3038)));
        cities.add(new City("Arceuus House", new Point(1583, 1025)));
        cities.add(new City("Hosidius House", new Point(1750, 1635)));
        cities.add(new City("Lovakengj House", new Point(1000, 975)));
        cities.add(new City("Piscarilius House", new Point(1986, 1071)));
        cities.add(new City("Shayzien House", new Point(1130, 1562)));
        cities.add(new City("Wintertodt", new Point(1470, 287)));
        cities.add(new City("Lithkren", new Point(7266, 303)));
        cities.add(new City("Fossil Island", new Point(7751, 892)));
        cities.add(new City("Theatre of Blood", new Point(7557, 2655)));
        cities.add(new City("Slepe", new Point(7723, 2314)));
        cities.add(new City("Tollheim", new Point(5237, 1280)));
        cities.add(new City("Weiss", new Point(5202, 490)));
        cities.add(new City("Myths' Guild", new Point(3942, 3766)));
        cities.add(new City("Corsair Cove", new Point(4273, 3738)));
        cities.add(new City("Entrana", new Point(5084, 2237)));
        cities.add(new City("Grand Exchange", new Point(6065, 1838)));
        cities.add(new City("Clan Wars", new Point(6673, 2835)));
        cities.add(new City("Menaphos", new Point(6260, 3965)));
        cities.add(new City("Mage Arena", new Point(5883, 514)));
        cities.add(new City("Fountain of Rune", new Point(6669, 652)));
        cities.add(new City("Lava Dragon Isle", new Point(6168, 828)));
        cities.add(new City("Lava Maze", new Point(5801, 751)));
        cities.add(new City("LLetya", new Point(3580, 2795)));
        cities.add(new City("Zul-Andra", new Point(3161, 3139)));
        cities.add(new City("Isafdar", new Point(3292, 2752)));
        cities.add(new City("Prifddinas", new Point(3282, 2383)));
        cities.add(new City("Castle Wars", new Point(3859, 2977)));
        cities.add(new City("West Ardougne", new Point(4151, 2386)));
        cities.add(new City("Barrows", new Point(7266, 2439)));
        cities.add(new City("Duel Arena", new Point(6650, 2610)));
        cities.add(new City("Kebos Lowlands", new Point(358, 1358)));
        cities.add(new City("Farming Guild", new Point(308, 1092)));
        cities.add(new City("Mount Karuulm", new Point(504, 890)));
        cities.add(new City("The Forsaken Tower", new Point(716, 832)));
        cities.add(new City("Molch", new Point(500, 1308)));

        Collections.sort(cities, new Comparator<City>() {
            @Override
            public int compare(City o1, City o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        return cities;
    }
}
