package com.aartas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aartas.Interface.Api;
import com.aartas.modalclass.CardDetailModel;
import com.aartas.modalclass.DeleteCard.DeleteCardRespons;
import com.aartas.modalclass.DeleteShipingAdd.DeleteShippingAddressRespons;
import com.aartas.modalclass.EditCardDetail.EditCardDetailRespons;
import com.aartas.modalclass.MedicineOrderModel;
import com.aartas.modalclass.OrderModel;

import com.razorpay.PaymentResultListener;
import com.razorpay.Razorpay;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditCardPharmacyActivity extends AppCompatActivity implements PaymentResultListener {

    EditText etCardNumber, etCardHolderName, etCardMonth, etCardYear, etCVV;
    TextView tvTotalPrice, btnDonePay, txtTitle, tvOnEdit, txtBack, tv_delete_card;
    Retrofit retrofit;
    Api api;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;

    CardDetailModel.Datum datum = new CardDetailModel.Datum();
    ImageView imgCard;
    private static final String TAG = EditCardPharmacyActivity.class.getSimpleName();
    OrderModel orderModel = new OrderModel();
    private String p_UserID = "", speed = "Fast", ids = "", qty = "", shipping_id = "", orderID = "",
            total_price = "";
    RelativeLayout llSaveCard, rlBottomTotal, rlDeleteCardPayment;
    String id, edtCartNumForUpdate, OrderStr;
    Calendar currentYear;
    int year;
    GifImageView progressBar;
    RadioButton rbSaveCard;
    int appointmentId = 0;
    WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_card_option);

        init();
    }

    private void init() {
        retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build();
        api = retrofit.create(Api.class);
        progressBar = findViewById(R.id.imgLoader);
        progressBar.setVisibility(View.GONE);
//        sharedpreferences = getSharedPreferences("User_pref", Context.MODE_PRIVATE);
        sharedpreferences = getSharedPreferences("User_pref", Context.MODE_PRIVATE);
        id = getIntent().getStringExtra("id");
        editor = sharedpreferences.edit();
        p_UserID = sharedpreferences.getString("p_UserID", "No");

        txtBack = findViewById(R.id.lay_back);
        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        currentYear = Calendar.getInstance();
        year = currentYear.get(Calendar.YEAR);

        etCardNumber = findViewById(R.id.etCardNumber);
        etCardHolderName = findViewById(R.id.etCardHolderName);
        etCardMonth = findViewById(R.id.etCardMonth);
        etCardYear = findViewById(R.id.etCardYear);
        etCVV = findViewById(R.id.etCVV);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvOnEdit = findViewById(R.id.tvOnEdit);
        txtTitle = findViewById(R.id.txtTitle);
        btnDonePay = findViewById(R.id.btnDonePay);
        imgCard = findViewById(R.id.imgCard);
        rlBottomTotal = findViewById(R.id.rlBottomTotal);
        rlDeleteCardPayment = findViewById(R.id.rlDeleteCardPayment);
        llSaveCard = findViewById(R.id.llSaveCard);
        tv_delete_card = findViewById(R.id.tv_delete_card);
        rbSaveCard = findViewById(R.id.rbSaveCard);
        ids = getIntent().getStringExtra("ids");
        speed = getIntent().getStringExtra("speed");
        shipping_id = getIntent().getStringExtra("shipping_id");
        qty = getIntent().getStringExtra("qty");

        Intent intent = getIntent();
        if ((getIntent().getStringExtra("path_name") != null)) {
            appointmentId = getIntent().getIntExtra("appointmentId", 0);
            patch_color_change();
//            Appointment Id
            OrderStr = "Aartas-AppointmentorderNo_" + "\"" + appointmentId + "";
        }
        if (intent.getBundleExtra("BUNDLE") != null) {
            Bundle args = intent.getBundleExtra("BUNDLE");
            datum = (CardDetailModel.Datum) args.getSerializable("savedCard");
            showCardDetail();


            if (getIntent().getBooleanExtra("is_pay", false) == true) {
                btnDonePay.setText("PAY NOW");
                rlBottomTotal.setVisibility(View.VISIBLE);
                tvOnEdit.setVisibility(View.VISIBLE);
                rlDeleteCardPayment.setVisibility(View.GONE);


            } else {

                btnDonePay.setText("EDIT");
                etCardNumber.setEnabled(false);
                etCardHolderName.setEnabled(false);
                etCardMonth.setEnabled(false);
                etCardYear.setEnabled(false);
                btnDonePay.setBackground(null);
                btnDonePay.setTextColor(ContextCompat.getColor(EditCardPharmacyActivity.this, R.color.mango_yellow));
                tvOnEdit.setVisibility(View.GONE);
                rlBottomTotal.setVisibility(View.GONE);
                rlDeleteCardPayment.setVisibility(View.VISIBLE);
                etCVV.setText("");
                etCVV.setHint("");
            }

//            txtTitle.setText("CARD DETAILS");
//            tvOnEdit.setVisibility(View.VISIBLE);
//            llSaveCard.setVisibility(View.GONE);
//            rlDeleteCardPayment.setVisibility(View.VISIBLE);
//            rlBottomTotal.setVisibility(View.VISIBLE);
        } else {
            btnDonePay.setText("ADD");
            btnDonePay.setBackground(null);
            btnDonePay.setBackgroundResource(R.drawable.rect_grey);
            txtTitle.setText("ENTER NEW CARD DETAILS");
            tvOnEdit.setVisibility(View.GONE);
            rlDeleteCardPayment.setVisibility(View.GONE);
            llSaveCard.setVisibility(View.VISIBLE);
            rlBottomTotal.setVisibility(View.GONE);
        }

        rbSaveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!rbSaveCard.isSelected()) {
                    rbSaveCard.setChecked(true);
                    rbSaveCard.setSelected(true);
                } else {
                    rbSaveCard.setChecked(false);
                    rbSaveCard.setSelected(false);
                }
            }
        });


        total_price = getIntent().getStringExtra("total_price");
        tvTotalPrice.setText(total_price);
        etCardNumber.addTextChangedListener(new TextWatcher() {
            private static final int TOTAL_SYMBOLS = 19; // size of pattern 0000-0000-0000-0000
            private static final int TOTAL_DIGITS = 16; // max numbers of digits in pattern: 0000 x 4
            private static final int DIVIDER_MODULO = 5; // means divider position is every 5th symbol beginning with 1
            private static final int DIVIDER_POSITION = DIVIDER_MODULO - 1; // means divider position is every 4th symbol beginning with 0
            private static final char DIVIDER = ' ';

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {

                } else {
                    imgCard.setImageResource(0);
                    imgCard.setImageDrawable(null);
                    imgCard.setImageBitmap(null);
                }
//                Toast.makeText(EditCardPharmacyActivity.this, ""+s.length(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isInputCorrect(s, TOTAL_SYMBOLS, DIVIDER_MODULO, DIVIDER)) {
                    s.replace(0, s.length(), buildCorrectString(getDigitArray(s, TOTAL_DIGITS), DIVIDER_POSITION, DIVIDER));
                    if (s.length() > 14) {
                        if (getCreditCardType(s.toString()).equalsIgnoreCase("amex")) {
                            Picasso.with(EditCardPharmacyActivity.this)
                                    .load(R.drawable.icn_card_amex).fit()
                                    .into(imgCard);
                        } else if (getCreditCardType(s.toString()).equalsIgnoreCase("maestro")) {
                            Picasso.with(EditCardPharmacyActivity.this)
                                    .load(R.drawable.icn_card_maestro).fit()
                                    .into(imgCard);
                        } else if (getCreditCardType(s.toString()).equalsIgnoreCase("mastercard")) {
                            Picasso.with(EditCardPharmacyActivity.this)
                                    .load(R.drawable.icn_master_card).fit()
                                    .into(imgCard);
                        } else if (getCreditCardType(s.toString()).equalsIgnoreCase("visa")) {
                            Picasso.with(EditCardPharmacyActivity.this)
                                    .load(R.drawable.icn_visa).fit()
                                    .into(imgCard);
                        } else if (getCreditCardType(s.toString()).equalsIgnoreCase("")) {
                            Picasso.with(EditCardPharmacyActivity.this)
                                    .load(datum.getCardType()).fit()
                                    .into(imgCard);
                        }
                    } else {
                        imgCard.setImageResource(0);
                        imgCard.setImageDrawable(null);
                        imgCard.setImageBitmap(null);

                    }
                }
            }

            private boolean isInputCorrect(Editable s, int totalSymbols, int dividerModulo, char divider) {
                boolean isCorrect = s.length() <= totalSymbols; // check size of entered string
                for (int i = 0; i < s.length(); i++) { // check that every element is right
                    if (i > 0 && (i + 1) % dividerModulo == 0) {
                        isCorrect &= divider == s.charAt(i);
                    } else {
                        isCorrect &= Character.isDigit(s.charAt(i));
                    }
                }
                return isCorrect;
            }

            private String buildCorrectString(char[] digits, int dividerPosition, char divider) {
                final StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < digits.length; i++) {
                    if (digits[i] != 0) {
                        formatted.append(digits[i]);
                        if ((i > 0) && (i < (digits.length - 1)) && (((i + 1) % dividerPosition) == 0)) {
                            formatted.append(divider);
                        }
                    }
                }

                return formatted.toString();
            }

            private char[] getDigitArray(final Editable s, final int size) {
                char[] digits = new char[size];
                int index = 0;
                for (int i = 0; i < s.length() && index < size; i++) {
                    char current = s.charAt(i);
                    if (Character.isDigit(current)) {
                        digits[index] = current;
                        index++;
                    }
                }
                return digits;
            }
        });

        btnDonePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnDonePay.getText().toString().equalsIgnoreCase("PAY NOW")) {
                    CallMedicineReOrderAPI();
                } else if (btnDonePay.getText().toString().equalsIgnoreCase("ADD")) {
                    validate();
                } else if (btnDonePay.getText().toString().equalsIgnoreCase("EDIT")) {
                    btnDonePay.setText("UPDATE");
                    etCardNumber.setEnabled(true);
                    etCardHolderName.setEnabled(true);
                    etCardMonth.setEnabled(true);
                    etCardYear.setEnabled(true);

                } else if (btnDonePay.getText().toString().equalsIgnoreCase("UPDATE")) {
                    validate();
                }
            }
        });
        rlDeleteCardPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCardApiCall();
            }
        });
    }

    public void patch_color_change() {
        if (!getIntent().getStringExtra("path_name").equals("")) {
            btnDonePay.setBackgroundResource(R.drawable.rect_blue);
            txtBack.setTextColor(ContextCompat.getColor(this, R.color.cancelColor));
            tvTotalPrice.setTextColor(ContextCompat.getColor(this, R.color.cancelColor));
            txtTitle.setTextColor(ContextCompat.getColor(this, R.color.cancelColor));
            etCardNumber.setTextColor(ContextCompat.getColor(this, R.color.cancelColor));
            etCardHolderName.setTextColor(ContextCompat.getColor(this, R.color.cancelColor));
            etCardMonth.setTextColor(ContextCompat.getColor(this, R.color.cancelColor));
            etCardYear.setTextColor(ContextCompat.getColor(this, R.color.cancelColor));
            etCVV.setTextColor(ContextCompat.getColor(this, R.color.cancelColor));
            rbSaveCard.setButtonDrawable(R.drawable.custom_radiobutton_check_out);
        }
    }

    private void deleteCardApiCall() {
        progressBar.setVisibility(View.VISIBLE);
        Call<DeleteCardRespons> call = api.deleteCard(p_UserID, id);
        call.enqueue(new Callback<DeleteCardRespons>() {
            @Override
            public void onResponse(Call<DeleteCardRespons> call, Response<DeleteCardRespons> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    if (response.body().getStatus() == 1) {
                        Toast.makeText(EditCardPharmacyActivity.this, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditCardPharmacyActivity.this, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<DeleteCardRespons> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditCardPharmacyActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void validate() {
        Pattern regMonth = Pattern.compile("^([0-9]|1[012])$");

        if (etCardNumber.getText().toString().length() == 0) {
            Toast.makeText(this, R.string.enter_card_number, Toast.LENGTH_LONG).show();
//            btnDonePay.setEnabled(false);
            btnDonePay.setBackground(null);
            btnDonePay.setBackgroundResource(R.drawable.rect_grey);
        } else if (etCardNumber.getText().toString().length() < 15 || etCardNumber.getText().toString().length() < 16) {
            Toast.makeText(this, R.string.valid_card_number, Toast.LENGTH_LONG).show();
//            btnDonePay.setEnabled(false);
            btnDonePay.setBackground(null);
            btnDonePay.setBackgroundResource(R.drawable.rect_grey);
        } else if (etCardHolderName.getText().toString().length() == 0) {
            Toast.makeText(this, R.string.enter_card_name, Toast.LENGTH_LONG).show();
//            btnDonePay.setEnabled(false);
            btnDonePay.setBackground(null);
            btnDonePay.setBackgroundResource(R.drawable.rect_grey);
        } else if (etCardMonth.getText().toString().length() == 0) {
            Toast.makeText(this, R.string.enter_card_month, Toast.LENGTH_LONG).show();
//            btnDonePay.setEnabled(false);
            btnDonePay.setBackground(null);
            btnDonePay.setBackgroundResource(R.drawable.rect_grey);
        } else if (Integer.parseInt(etCardMonth.getText().toString()) <= 0 && Integer.parseInt(etCardMonth.getText().toString()) > 12) {
            Toast.makeText(this, R.string.valid_card_month, Toast.LENGTH_LONG).show();
//            btnDonePay.setEnabled(false);
            btnDonePay.setBackground(null);
            btnDonePay.setBackgroundResource(R.drawable.rect_grey);
        } else if ((etCardYear.getText().toString().length() == 0) && (Integer.valueOf(etCardMonth.getText().toString()) > year)) {
            Toast.makeText(this, R.string.enter_card_year, Toast.LENGTH_LONG).show();
//            btnDonePay.setEnabled(false);
            btnDonePay.setBackground(null);
            btnDonePay.setBackgroundResource(R.drawable.rect_grey);
        } else if (etCardYear.getText().toString().length() < 4) {
            Toast.makeText(this, R.string.valid_card_year, Toast.LENGTH_LONG).show();
//            btnDonePay.setEnabled(false);
            btnDonePay.setBackground(null);
            btnDonePay.setBackgroundResource(R.drawable.rect_grey);
        } else if (etCVV.getText().toString().length() == 0) {
            Toast.makeText(this, R.string.enter_card_code, Toast.LENGTH_LONG).show();
//            btnDonePay.setEnabled(false);
            btnDonePay.setBackground(null);
            btnDonePay.setBackgroundResource(R.drawable.rect_grey);
        } else if (etCVV.getText().toString().length() < 3) {
            Toast.makeText(this, R.string.valid_card_code, Toast.LENGTH_LONG).show();
//            btnDonePay.setEnabled(false);
            btnDonePay.setBackground(null);
            btnDonePay.setBackgroundResource(R.drawable.rect_grey);
        } else {
//            btnDonePay.setVisibility(View.VISIBLE);
            btnDonePay.setEnabled(true);
            if (getIntent().getStringExtra("path_name") != null) {
                btnDonePay.setBackgroundResource(R.drawable.rect_blue);
            }
            btnDonePay.setBackgroundResource(R.drawable.rect_yellow_tenradius);
            if (btnDonePay.getText().toString().equalsIgnoreCase("ADD")) {
                addCardAPICall();
            } else if (btnDonePay.getText().toString().equalsIgnoreCase("UPDATE")) {
                call_api_edit_cardDetail();
            }
        }
    }

    private void addCardAPICall() {
        progressBar.setVisibility(View.VISIBLE);

        Call<CardDetailModel> call = api.addBankDetails(p_UserID, etCardHolderName.getText().toString(),
                etCardMonth.getText().toString(), etCardYear.getText().toString(), etCVV.getText().toString(), etCardNumber.getText().toString().trim().replaceAll(" ", ""),
                getCreditCardType(etCardNumber.getText().toString()).toLowerCase());

        call.enqueue(new Callback<CardDetailModel>() {
            @Override
            public void onResponse(Call<CardDetailModel> call, Response<CardDetailModel> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    if (response.body().getStatus() == 1) {
                        Toast.makeText(EditCardPharmacyActivity.this, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditCardPharmacyActivity.this, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onFailure(Call<CardDetailModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditCardPharmacyActivity.this, "Fail to order medicine....", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void call_api_edit_cardDetail() {
        progressBar.setVisibility(View.VISIBLE);

        Call<EditCardDetailRespons> call = api.editCardDetails(p_UserID, id, etCardHolderName.getText().toString(),
                etCardMonth.getText().toString(), etCardYear.getText().toString(), etCVV.getText().toString(),
                edtCartNumForUpdate.replaceAll(" ", ""),
                getCreditCardType(edtCartNumForUpdate).toLowerCase());

        call.enqueue(new Callback<EditCardDetailRespons>() {
            @Override
            public void onResponse(Call<EditCardDetailRespons> call, Response<EditCardDetailRespons> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    if (response.body().getStatus() == 1) {
                        Toast.makeText(EditCardPharmacyActivity.this, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditCardPharmacyActivity.this, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onFailure(Call<EditCardDetailRespons> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditCardPharmacyActivity.this, "Fail to order medicine....", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void CallMedicineReOrderAPI() {
        retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build();
        api = retrofit.create(Api.class);

        progressBar.setVisibility(View.VISIBLE);

        Call<MedicineOrderModel> call = api.postMedicine(p_UserID, ids, speed, shipping_id, "15", qty);
        call.enqueue(new Callback<MedicineOrderModel>() {
            @Override
            public void onResponse(Call<MedicineOrderModel> call, Response<MedicineOrderModel> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    if (response.body().getStatus().equals(1)) {
                        createOrderToPay(response.body().getData().getOrderNo());

                    }
                }
            }

            @Override
            public void onFailure(Call<MedicineOrderModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditCardPharmacyActivity.this, "Fail to order medicine....", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public static String getAuthToken() {
        byte[] data = new byte[0];
        try {
            data = ("rzp_test_eEB5Fmw5bhIePn" + ":" + "xjix3hNi4aoeiwG2krjsIZBy").getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "Basic " + Base64.encodeToString(data, Base64.NO_WRAP);
    }

    private void createOrderToPay(String orderNo) {
        if ((getIntent().getStringExtra("path_name") != null)) {

        } else {
            OrderStr = "Aartas-MedicineorderNo_" + "\"" + orderNo;
        }
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.razorpay.com/v1/")
                .addConverterFactory(GsonConverterFactory.create()).build();
        api = retrofit.create(Api.class);

        progressBar.setVisibility(View.VISIBLE);

        double price = Double.parseDouble(total_price.replaceAll("₹", " ")) * 100;

        JSONObject paramObject = new JSONObject();
        try {
            paramObject.put("amount", price);
            paramObject.put("currency", "INR");
            paramObject.put("receipt", "");
            paramObject.put("payment_capture", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body =
                RequestBody.create(MediaType.parse("application/json; charset=utf-8"), paramObject.toString());

        String uName = "rzp_test_eEB5Fmw5bhIePn";
        String uPass = "xjix3hNi4aoeiwG2krjsIZBy";
        String uBase = uName + ":" + uPass;

        String authHeader = "Basic " + Base64.encodeToString(uBase.getBytes(), Base64.NO_WRAP);

        Call<OrderModel> call = api.generateOrder(getAuthToken(), body);

        call.enqueue(new Callback<OrderModel>() {
            @Override
            public void onResponse(Call<OrderModel> call, Response<OrderModel> response) {
                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    orderModel = response.body();
                    startPayment(orderModel);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditCardPharmacyActivity.this, "", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditCardPharmacyActivity.this, "Fail....", Toast.LENGTH_SHORT).show();
            }

        });

    }

    public void startPayment(OrderModel orderModel) {
        Razorpay razorpay = new Razorpay(EditCardPharmacyActivity.this, "rzp_test_eEB5Fmw5bhIePn");
        webview = findViewById(R.id.payment_webview);
// Hide the webview until the payment details are submitted
        webview.setVisibility(View.GONE);
        razorpay.setWebView(webview);

        try {
            JSONObject options = new JSONObject();

            options.put("order_id", orderModel.getId());//from response of step 3.
            options.put("currency", orderModel.getCurrency());
            options.put("amount", orderModel.getAmount());//pass amount in currency subunits
            options.put("card[name]", etCardHolderName.getText().toString());//pass amount in currency subunits
            options.put("card[number]", datum.getCardNumber().toString());//pass amount in currency subunits
            options.put("card[expiry_month]", etCardMonth.getText().toString());//pass amount in currency subunits
            options.put("card[expiry_year]", etCardYear.getText().toString());//pass amount in currency subunits
            options.put("card[cvv]", etCVV.getText().toString());//pass amount in currency subunits
            options.put("email", "" + sharedpreferences.getString("p_Email", "No"));//pass amount in currency subunits
            options.put("contact", "" + sharedpreferences.getString("p_Phone", "No"));//pass amount in currency subunits
            options.put("method", "card");//pass amount in currency subunits
            options.put("save", "1");//pass amount in currency subunits


            // Make webview visible before submitting payment details
            webview.setVisibility(View.VISIBLE);

            razorpay.submit(options, new PaymentResultListener() {
                @Override
                public void onPaymentSuccess(String razorpayPaymentId) {
                    // Razorpay payment ID is passed here after a successful payment
                    Toast.makeText(EditCardPharmacyActivity.this, "Success", Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onPaymentError(int code, String description) {
                    // Error code and description is passed here
                    Toast.makeText(EditCardPharmacyActivity.this, "failure", Toast.LENGTH_LONG).show();

                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e);
        }

        /**
         * Instantiate Checkout
         */
//        Checkout checkout = new Checkout();
//        checkout.setKeyID("rzp_test_eEB5Fmw5bhIePn");
////        checkout.setKeyID("xjix3hNi4aoeiwG2krjsIZBy");//secret key
//        /**
//         * Set your logo here
//         */
//        checkout.setImage(R.drawable.icon_application_aartas);
//
//        /**
//         * Reference to current activity
//         */
//        final Activity activity = this;
//
//        /**
//         * Pass your payment options to the Razorpay Checkout as a JSONObject
//         */
//        try {
//            JSONObject options = new JSONObject();
//
//            options.put("name", "" + sharedpreferences.getString("p_UserID", "No"));
////            options.put("description", "Reference No. #123456");
//            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
//            options.put("order_id", orderModel.getId());//from response of step 3.
//            options.put("theme.color", "#3399cc");
//            options.put("currency", orderModel.getCurrency());
//            options.put("amount", orderModel.getAmount());//pass amount in currency subunits
//            options.put("prefill.email", "" + sharedpreferences.getString("p_Email", "No"));
//            options.put("prefill.contact", "" + sharedpreferences.getString("p_Phone", "No"));
//            checkout.open(activity, options);
//
//
//        } catch (Exception e) {
//            Log.e(TAG, "Error in starting Razorpay Checkout", e);
//        }
    }

    public String getCreditCardType(String CreditCardNumber) {
        Log.e("getCreditCardType: ", CreditCardNumber + "");
        String creditType = "invalid";
        Pattern regVisa = Pattern.compile("^4[0-9]{6,}$");
        Pattern regMaster = Pattern.compile("^5[1-5][0-9]{5,}$");
        Pattern regExpress = Pattern.compile("^3[47][0-9]{13}$");
        Pattern regDiners = Pattern.compile("^3(?:0[0-5]|[68][0-9])[0-9]{11}$");
        Pattern regDiscover = Pattern.compile("^6(?:011|5[0-9]{2})[0-9]{12}$");
        Pattern regJCB = Pattern.compile("^(?:2131|1800|35\\d{3})\\d{11}$");
        Pattern regAMEX = Pattern.compile("^3[47][0-9]{5,}$");


        if (regVisa.matcher(CreditCardNumber.replaceAll(" ", "")).matches()) {
            creditType = "VISA";
            return creditType;
        } else if (regMaster.matcher(CreditCardNumber.replaceAll(" ", "")).find()) {
            creditType = "MASTERCARD";
            return creditType;
        } else if (regExpress.matcher(CreditCardNumber.replaceAll(" ", "")).find()) {
            creditType = "AEXPRESS";
            return creditType;
        } else if (regDiners.matcher(CreditCardNumber.replaceAll(" ", "")).find()) {
            creditType = "DINERS";
            return creditType;
        } else if (regDiscover.matcher(CreditCardNumber.replaceAll(" ", "")).find()) {
            creditType = "DISCOVERS";
            return creditType;
        } else if (regJCB.matcher(CreditCardNumber.replaceAll(" ", "")).find()) {
            creditType = "JCB";
            return creditType;
        } else if (regAMEX.matcher(CreditCardNumber.replaceAll(" ", "")).find()) {
            creditType = "AMEX";
            return creditType;
        } else
            return creditType;
    }

    private void showCardDetail() {

        StringBuilder s;
        s = new StringBuilder(datum.getCardNumber().toString());

        for (int i = 4; i < s.length(); i += 5) {
            s.insert(i, " ");
        }
        edtCartNumForUpdate = datum.getCardNumber().toString();
        String mask = datum.getCardNumber().toString().replaceAll("\\w(?=\\w{4})", "\u2022");
        etCardNumber.setText(mask);
//        etCardNumber.setText(s.toString().replaceAll("\\w(?=\\w{4})", "\u2022"));
//        etCardNumber.setText(datum.getCardNumber().replaceAll("\\w(?=\\w{4})", "\u2022"));
        etCardHolderName.setText(datum.getName());
        etCardMonth.setText(datum.getMonth());
        etCardYear.setText(datum.getYear());
//        etCVV.setText(datum.getSecurityCode());
        txtTitle.setText("");
        if (datum.getCardType().equals("amex")) {
            Picasso.with(this)
                    .load(R.drawable.icn_card_amex).fit()
                    .into(imgCard);
        } else if (datum.getCardType().equals("maestro")) {
            Picasso.with(this)
                    .load(R.drawable.icn_card_maestro).fit()
                    .into(imgCard);
        } else if (datum.getCardType().equals("mastercard")) {
            Picasso.with(this)
                    .load(R.drawable.icn_master_card).fit()
                    .into(imgCard);
        } else if (datum.getCardType().equals("visa")) {
            Picasso.with(this)
                    .load(R.drawable.icn_visa).fit()
                    .into(imgCard);
        } else if (datum.getCardType().equals("")) {
            Picasso.with(this)
                    .load(datum.getCardType()).fit()
                    .into(imgCard);
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        Toast.makeText(EditCardPharmacyActivity.this, "Success", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(EditCardPharmacyActivity.this, "failure", Toast.LENGTH_LONG).show();
    }
}
