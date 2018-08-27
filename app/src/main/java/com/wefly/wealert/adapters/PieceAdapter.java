package com.wefly.wealert.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.appizona.yehiahd.fastsave.FastSave;
import com.wefly.wealert.R;
import com.wefly.wealert.models.Piece;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PieceAdapter extends BaseAdapter {
    private Context context;
    private List<Piece> pieces;
    private LayoutInflater inflater;

    public PieceAdapter(Context ctx,List<Piece> pieceList) {
        this.context = ctx;
        this.pieces = pieceList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return pieces.size();
    }

    @Override
    public Object getItem(int i) {
        return pieces.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.piece_item, parent, false);
        ImageView image = view.findViewById(R.id.piece);
        ImageButton btnDel = view.findViewById(R.id.delete);
        if (pieces.size() > 0) {
            Piece p = pieces.get(i);
            if(!p.getUrl().isEmpty()) {
                if (p.getExtension(p.getUrl()).matches(".m4a")) {
                    image.setImageResource(R.drawable.microphone);
                } else {
                    image.setImageURI(Uri.fromFile(new File(p.getUrl().trim())));
                }
            }
            //Log.e("piece content uri", p.getContentUrl().toString());
            image.setTag(p.getIndex());
            image.setPadding(5, 5, 15, 5);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            btnDel.setTag(p.getIndex());
            btnDel.setOnClickListener(view1 -> {
                removeImage(view1);
            });
        }
        return view;
    }

    private void removeImage(View view) {
        String index = view.getTag().toString();
        List<Piece> tmp = new ArrayList<>();
        tmp.addAll(pieces);
        for (Piece p : tmp) {
            Log.e("clicked index", String.valueOf(index));
            Log.e("stored index", String.valueOf(p.getIndex()));
            if (p.getIndex().equals(index)) {
                pieces.remove(p);
                storePieces();
                notifyDataSetChanged();
            }
        }
        Log.v("piece size 2", String.valueOf(pieces.size()));
    }

    private void storePieces() {
        FastSave.getInstance().saveObjectsList("alertDataPieces",pieces);
    }

    protected List<Piece> getStoredPieces() {
        List<Piece> list = new ArrayList<>();
        if(FastSave.getInstance().isKeyExists("alertDataPieces")) {
            list = FastSave.getInstance().getObjectsList("alertDataPieces",Piece.class);
        }
        return list;
    }
}
