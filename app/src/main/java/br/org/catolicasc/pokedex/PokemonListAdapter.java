package br.org.catolicasc.pokedex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class PokemonListAdapter extends ArrayAdapter {
    private static final String TAG = "PokemonListAdapter";
    private final int layoutResource;
    private final LayoutInflater layoutInflater;
    private List<Pokemon> pokemons;

    public PokemonListAdapter(Context context, int resource, List<Pokemon> pokemons) {
        super(context, resource);
        this.layoutResource = resource;
        this.layoutInflater = LayoutInflater.from(context);
        this.pokemons = pokemons;
    }

    @Override
    public int getCount() {
        return pokemons.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            Log.d(TAG, "getView: chamada com um convertView null");
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            Log.d(TAG, "getView: recebeu um convertView");
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Pokemon pokemonAtual = pokemons.get(position);

        viewHolder.tvNome.setText(pokemonAtual.getNome());
        viewHolder.tvNumero.setText(pokemonAtual.getNumero());
        viewHolder.tvPeso.setText(pokemonAtual.getPeso());
        viewHolder.tvAltura.setText(pokemonAtual.getAltura());

        String tipos = "";
        for (String t : pokemonAtual.getTipo()) {
            tipos += t + '\n';
        }

        viewHolder.tvTipo.setText(tipos);

        new DownloadImageTask(viewHolder.ivPokemonImg).execute(pokemonAtual.getImageUrl());

        return convertView;
    }

    private class ViewHolder {
        final TextView tvNome;
        final TextView tvNumero;
        final TextView tvPeso;
        final TextView tvAltura;
        final TextView tvTipo;
        final ImageView ivPokemonImg;

        ViewHolder(View v) {
            this.tvNome = v.findViewById(R.id.tvNome);
            this.tvNumero = v.findViewById(R.id.tvNumero);
            this.tvPeso = v.findViewById(R.id.tvPeso);
            this.tvAltura = v.findViewById(R.id.tvAltura);
            this.ivPokemonImg = v.findViewById(R.id.ivPokemonImg);
            this.tvTipo = v.findViewById(R.id.tvTipo);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            URL url = null;
            try {
                url = new URL(urls[0]);
                Bitmap bmp = null;
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int resposta = connection.getResponseCode();
                Log.d(TAG, "downloadJson: O código de resposta foi: " + resposta);

                if (resposta != HttpURLConnection.HTTP_OK) { // se resposta não foi OK
                    if (resposta == HttpURLConnection.HTTP_MOVED_TEMP  // se for um redirect
                            || resposta == HttpURLConnection.HTTP_MOVED_PERM
                            || resposta == HttpURLConnection.HTTP_SEE_OTHER) {
                        // pegamos a nova URL e abrimos nova conexão!
                        String novaUrl = connection.getHeaderField("Location");
                        connection = (HttpURLConnection) new URL(novaUrl).openConnection();
                    }
                }

                InputStream inputStream = connection.getInputStream();

                return BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
