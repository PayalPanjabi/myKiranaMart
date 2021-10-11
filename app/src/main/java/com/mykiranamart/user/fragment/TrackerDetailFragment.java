package com.mykiranamart.user.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.mykiranamart.user.R;
import com.mykiranamart.user.adapter.ItemsAdapter;
import com.mykiranamart.user.helper.ApiConfig;
import com.mykiranamart.user.helper.Constant;
import com.mykiranamart.user.helper.Session;
import com.mykiranamart.user.model.OrderTracker;

public class TrackerDetailFragment extends Fragment {
    @SuppressLint("StaticFieldLeak")
    public static ProgressBar pBar;
    @SuppressLint("StaticFieldLeak")
    public static Button btnCancel;
    Button btnReorder;
    @SuppressLint("StaticFieldLeak")
    public static LinearLayout lytTracker;
    View root;
    OrderTracker order;
    TextView tvOrderOTP, tvItemTotal, tvDeliveryCharge, tvTotal, tvPromoCode, tvPCAmount, tvWallet, tvFinalTotal, tvDPercent, tvDAmount;
    TextView tvCancelDetail, tvOtherDetails, tvOrderID, tvOrderDate;
    RecyclerView recyclerView;
    View l4;
    RelativeLayout relativeLyt;
    LinearLayout returnLyt, lytPromo, lytWallet, lytPriceDetail, lytOTP;
    double totalAfterTax = 0.0;
    Activity activity;
    String id;
    Session session;
    HashMap<String, String> hashMap;
    LinearLayout lytMainTracker;
    ScrollView scrollView;
    private ShimmerFrameLayout mShimmerViewContainer;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_tracker_detail, container, false);
        activity = getActivity();
        session = new Session(activity);

        pBar = root.findViewById(R.id.pBar);
        lytPriceDetail = root.findViewById(R.id.lytPriceDetail);
        lytPromo = root.findViewById(R.id.lytPromo);
        lytWallet = root.findViewById(R.id.lytWallet);
        tvItemTotal = root.findViewById(R.id.tvItemTotal);
        tvDeliveryCharge = root.findViewById(R.id.tvDeliveryCharge);
        tvDAmount = root.findViewById(R.id.tvDAmount);
        tvDPercent = root.findViewById(R.id.tvDPercent);
        tvTotal = root.findViewById(R.id.tvTotal);
        tvPromoCode = root.findViewById(R.id.tvPromoCode);
        tvPCAmount = root.findViewById(R.id.tvPCAmount);
        tvWallet = root.findViewById(R.id.tvWallet);
        tvFinalTotal = root.findViewById(R.id.tvFinalTotal);
        tvOrderID = root.findViewById(R.id.tvOrderID);
        tvOrderDate = root.findViewById(R.id.tvOrderDate);
        relativeLyt = root.findViewById(R.id.relativeLyt);
        tvOtherDetails = root.findViewById(R.id.tvOtherDetails);
        tvCancelDetail = root.findViewById(R.id.tvCancelDetail);
        lytTracker = root.findViewById(R.id.lytTracker);
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setNestedScrollingEnabled(false);
        btnCancel = root.findViewById(R.id.btnCancel);
        btnReorder = root.findViewById(R.id.btnReorder);
        l4 = root.findViewById(R.id.l4);
        returnLyt = root.findViewById(R.id.returnLyt);
        tvOrderOTP = root.findViewById(R.id.tvOrderOTP);
        lytOTP = root.findViewById(R.id.lytOTP);
        lytMainTracker = root.findViewById(R.id.lytMainTracker);
        scrollView = root.findViewById(R.id.scrollView);
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);
        hashMap = new HashMap<>();

        assert getArguments() != null;
        id = getArguments().getString("id");
        if (id.equals("")) {
            order = (OrderTracker) getArguments().getSerializable("model");
            id = order.getOrder_id();
            SetData(order);
        } else {
            getOrderDetails(id);
        }


        setHasOptionsMenu(true);

        btnReorder.setOnClickListener(view -> new AlertDialog.Builder(activity)
                .setTitle(getString(R.string.re_order))
                .setMessage(getString(R.string.reorder_msg))
                .setPositiveButton(getString(R.string.proceed), (dialog, which) -> {
                    if (activity != null) {
                        GetReOrderData();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss()).show());

        btnCancel.setOnClickListener(view -> {

            final Map<String, String> params = new HashMap<>();
            params.put(Constant.UPDATE_ORDER_STATUS, Constant.GetVal);
            params.put(Constant.ID, order.getOrder_id());
            params.put(Constant.STATUS, Constant.CANCELLED);
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            // Setting Dialog Message
            alertDialog.setTitle(activity.getResources().getString(R.string.cancel_order));
            alertDialog.setMessage(activity.getResources().getString(R.string.cancel_msg));
            alertDialog.setCancelable(false);
            final AlertDialog alertDialog1 = alertDialog.create();

            // Setting OK Button
            alertDialog.setPositiveButton(activity.getResources().getString(R.string.yes), (dialog, which) -> {
                if (pBar != null)
                    pBar.setVisibility(View.VISIBLE);
                ApiConfig.RequestToVolley((result, response) -> {
                    // System.out.println("================= " + response);
                    if (result) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean(Constant.ERROR)) {
                                btnCancel.setVisibility(View.GONE);
                                ApiConfig.getWalletBalance(activity, new Session(activity));
                            }
                            Toast.makeText(activity, object.getString(Constant.MESSAGE), Toast.LENGTH_LONG).show();
                            if (pBar != null)
                                pBar.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, activity, Constant.ORDER_PROCESS_URL, params, false);

            });
            alertDialog.setNegativeButton(activity.getResources().getString(R.string.no), (dialog, which) -> alertDialog1.dismiss());
            // Showing Alert Message
            alertDialog.show();
        });

        return root;
    }

    public void GetReOrderData() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_REORDER_DATA, Constant.GetVal);
        params.put(Constant.ID, id);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONObject(Constant.DATA).getJSONArray(Constant.ITEMS);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        hashMap.put(jsonArray.getJSONObject(i).getString(Constant.PRODUCT_VARIANT_ID), jsonArray.getJSONObject(i).getString(Constant.QUANTITY));
                    }
                    ApiConfig.AddMultipleProductInCart(session, activity, hashMap);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false);
    }

    public void getOrderDetails(String id) {
        scrollView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ORDERS, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.ORDER_ID, id);

        //  System.out.println("=====params " + params.toString());
        ApiConfig.RequestToVolley((result, response) -> {

            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        SetData(ApiConfig.GetOrders(jsonObject.getJSONArray(Constant.DATA)).get(0));
                    } else {
                        scrollView.setVisibility(View.VISIBLE);
                        mShimmerViewContainer.setVisibility(View.GONE);
                        mShimmerViewContainer.stopShimmer();
                    }
                } catch (JSONException e) {
                    scrollView.setVisibility(View.VISIBLE);
                    mShimmerViewContainer.setVisibility(View.GONE);
                    mShimmerViewContainer.stopShimmer();
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false);
    }

    @SuppressLint("SetTextI18n")
    public void SetData(OrderTracker order) {
        String[] date = order.getDate_added().split("\\s+");
        tvOrderID.setText(order.getOrder_id());
        if (order.getOtp().equals("0")) {
            lytOTP.setVisibility(View.GONE);
        } else {
            tvOrderOTP.setText(order.getOtp());
        }
        tvOrderDate.setText(date[0]);
        tvOtherDetails.setText(getString(R.string.name_1) + order.getUsername() + getString(R.string.mobile_no_1) + order.getMobile() + getString(R.string.address_1) + order.getAddress());
        totalAfterTax = (Double.parseDouble(order.getTotal()) + Double.parseDouble(order.getDelivery_charge()) + Double.parseDouble(order.getTax_amt()));
        tvItemTotal.setText(session.getData(Constant.currency) + ApiConfig.StringFormat(order.getTotal()));
        tvDeliveryCharge.setText("+ " + session.getData(Constant.currency) + ApiConfig.StringFormat(order.getDelivery_charge()));
        tvDPercent.setText(getString(R.string.discount) + "(" + order.getdPercent() + "%) :");
        tvDAmount.setText("- " + session.getData(Constant.currency) + ApiConfig.StringFormat(order.getdAmount()));
        tvTotal.setText(session.getData(Constant.currency) + totalAfterTax);
        tvPCAmount.setText("- " + session.getData(Constant.currency) + ApiConfig.StringFormat(order.getPromoDiscount()));
        tvWallet.setText("- " + session.getData(Constant.currency) + ApiConfig.StringFormat(order.getWalletBalance()));
        tvFinalTotal.setText(session.getData(Constant.currency) + ApiConfig.StringFormat(order.getFinal_total()));

        try {
            if (!order.getStatus().equalsIgnoreCase("delivered") && !order.getStatus().equalsIgnoreCase("cancelled") && !order.getStatus().equalsIgnoreCase("returned")) {
                btnCancel.setVisibility(View.VISIBLE);
            } else {
                btnCancel.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (order.getStatus().equalsIgnoreCase("cancelled") || order.getStatus().equalsIgnoreCase("awaiting_payment")) {
                lytTracker.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                if (order.getStatus().equalsIgnoreCase("awaiting_payment")) {
                    tvCancelDetail.setVisibility(View.GONE);
                } else {
                    tvCancelDetail.setVisibility(View.VISIBLE);
                    tvCancelDetail.setText(getString(R.string.canceled_on) + order.getStatusdate());

                }
                lytPriceDetail.setVisibility(View.GONE);
            } else {
                lytPriceDetail.setVisibility(View.VISIBLE);
                if (order.getStatus().equals("returned")) {
                    l4.setVisibility(View.VISIBLE);
                    returnLyt.setVisibility(View.VISIBLE);
                }
                lytTracker.setVisibility(View.VISIBLE);

                for (int i = 0; i < order.getItemsList().size(); i++) {
                    hashMap.put(order.getItemsList().get(i).getProduct_variant_id(), order.getItemsList().get(i).getQuantity());
                }

                for (int i = 0; i < order.getOrderStatusArrayList().size(); i++) {
                    int img = getResources().getIdentifier("img" + i, "id", activity.getPackageName());
                    int view = getResources().getIdentifier("l" + i, "id", activity.getPackageName());
                    int txt = getResources().getIdentifier("txt" + i, "id", activity.getPackageName());
                    int textview = getResources().getIdentifier("txt" + i + "" + i, "id", activity.getPackageName());


                    if (img != 0 && root.findViewById(img) != null) {
                        ImageView imageView = root.findViewById(img);
                        imageView.setColorFilter(ContextCompat.getColor(activity,R.color.colorPrimary));
                    }

                    if (view != 0 && root.findViewById(view) != null) {
                        View view1 = root.findViewById(view);
                        view1.setBackgroundColor(ContextCompat.getColor(activity,R.color.colorPrimary));
                    }

                    if (txt != 0 && root.findViewById(txt) != null) {
                        TextView view1 = root.findViewById(txt);
                        view1.setTextColor(ContextCompat.getColor(activity,R.color.black));
                    }

                    if (textview != 0 && root.findViewById(textview) != null) {
                        TextView view1 = root.findViewById(textview);
                        String str = order.getOrderStatusArrayList().get(i).getStatusdate();
                        String[] split = str.split("\\s+");
                        view1.setText(split[0] + "\n" + split[1]);
                    }
                }
            }

            scrollView.setVisibility(View.VISIBLE);
            mShimmerViewContainer.setVisibility(View.GONE);
            mShimmerViewContainer.stopShimmer();
        } catch (Exception e) {
            scrollView.setVisibility(View.VISIBLE);
            mShimmerViewContainer.setVisibility(View.GONE);
            mShimmerViewContainer.stopShimmer();
        }

        recyclerView.setAdapter(new ItemsAdapter(activity, order.getItemsList(), "detail"));
        recyclerView.setHasFixedSize(true);
        relativeLyt.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.order_track_detail);
        activity.invalidateOptionsMenu();
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.toolbar_layout).setVisible(false);
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(true);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(true);
    }
}