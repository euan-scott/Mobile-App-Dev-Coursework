package org.me.gcu.scott_euan_s2132201;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;


import androidx.core.view.WindowCompat;
import androidx.appcompat.app.AppCompatActivity;

public class CurrencyConvertor extends AppCompatActivity {

    private TextView txtCurrencyInfo;
    private TextView txtRateInfo;
    private EditText edtAmount;
    private RadioButton rdoGbpToCur;
    private RadioButton rdoCurToGbp;
    private Button btnConvert;
    private TextView txtResult;
    private Button btnBack;

    private String code;
    private String name;
    private double rateToGbp;   // 1 GBP = rateToGbp of this currency

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.currency_convertor_activity);

        View root = findViewById(R.id.currency_convertor_activity); // the ScrollView

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

        view.setPadding(
                view.getPaddingLeft(),
                systemBars.top,
                view.getPaddingRight(),
                view.getPaddingBottom()
        );

            return insets;
        });

        // Get data from MainActivity
        code = getIntent().getStringExtra("code");
        name = getIntent().getStringExtra("name");
        rateToGbp = getIntent().getDoubleExtra("rate", 0.0);

        txtCurrencyInfo = findViewById(R.id.txtCurrencyInfo);
        txtRateInfo = findViewById(R.id.txtRateInfo);
        edtAmount = findViewById(R.id.edtAmount);
        rdoGbpToCur = findViewById(R.id.rdoGbpToCur);
        rdoCurToGbp = findViewById(R.id.rdoCurToGbp);
        btnConvert = findViewById(R.id.btnConvert);
        txtResult = findViewById(R.id.txtResult);
        btnBack = findViewById(R.id.btnBack);


        txtCurrencyInfo.setText(name);
        txtRateInfo.setText("1 GBP = " + String.format("%.4f", rateToGbp) + " " + code);

        // Default: GBP → Currency
        rdoGbpToCur.setChecked(true);

        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performConversion();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();   // closes conversion screen
            }
        });

    }

    private void performConversion() {
        String text = edtAmount.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(text);
            double result;

            if (rdoGbpToCur.isChecked()) {
                // GBP → selected currency
                result = amount * rateToGbp;
                txtResult.setText(String.format("£%.2f GBP = %.2f %s", amount, result, code));
            } else {
                // selected currency → GBP
                if (rateToGbp == 0.0) {
                    Toast.makeText(this, "Invalid rate", Toast.LENGTH_SHORT).show();
                    return;
                }
                result = amount / rateToGbp;
                txtResult.setText(String.format("%.2f %s = £%.2f GBP", amount, code, result));
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
        }
    }
}
