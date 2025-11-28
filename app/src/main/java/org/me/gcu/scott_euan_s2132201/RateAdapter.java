package org.me.gcu.scott_euan_s2132201;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class RateAdapter extends ArrayAdapter<CurrencyRate> {

    private final LayoutInflater inflater;

    public RateAdapter(@NonNull Context context, @NonNull List<CurrencyRate> objects) {
        super(context, 0, objects);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = inflater.inflate(R.layout.row_rate, parent, false);
        }

        TextView txt = row.findViewById(R.id.txtRateRow);
        CurrencyRate cr = getItem(position);

        if (cr != null) {
            String line = cr.getCode()
                    + " - "
                    + cr.getName()
                    + " : 1 GBP = "
                    + String.format("%.4f", cr.getRateToGbp());
            txt.setText(line);

            double rate = cr.getRateToGbp();

            if (rate > 100) {                                                    // against the pound
                txt.setBackgroundColor(Color.parseColor("#C8E6C9"));   // strong - green
            }
            else if (rate > 10) {
                txt.setBackgroundColor(Color.parseColor("#FFF9C4"));   // moderate - yellow
            }
            else if (rate > 1) {
                txt.setBackgroundColor(Color.parseColor("#FFE0B2"));   // weak - orange
            }
            else {
                txt.setBackgroundColor(Color.parseColor("#FFCDD2"));   // very weak - red
            }
        }

        return row;
    }
}
