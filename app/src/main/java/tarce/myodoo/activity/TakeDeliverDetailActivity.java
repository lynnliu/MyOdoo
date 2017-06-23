package tarce.myodoo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit2.Call;
import retrofit2.Response;
import tarce.api.MyCallback;
import tarce.api.RetrofitClient;
import tarce.api.api.InventoryApi;
import tarce.model.GetSaleResponse;
import tarce.model.inventory.TakeDelListBean;
import tarce.model.inventory.WaitingInBean;
import tarce.myodoo.R;
import tarce.myodoo.activity.salesout.SalesDetailActivity;
import tarce.myodoo.adapter.SalesDetailAdapter;
import tarce.myodoo.adapter.takedeliver.DetailTakedAdapter;
import tarce.myodoo.uiutil.FullyLinearLayoutManager;
import tarce.myodoo.uiutil.InsertNumDialog;
import tarce.myodoo.utils.StringUtils;
import tarce.myodoo.utils.UserManager;
import tarce.support.AlertAialogUtils;
import tarce.support.TimeUtils;
import tarce.support.ToastUtils;

/**
 * Created by zouzou on 2017/6/23.
 * 收货详情
 */

public class TakeDeliverDetailActivity extends BaseActivity {
    @InjectView(R.id.top_imageview)
    ImageView topImageview;
    @InjectView(R.id.partner)
    TextView partner;
    @InjectView(R.id.time)
    TextView time;
    @InjectView(R.id.states)
    TextView states;
    @InjectView(R.id.origin_documents)
    TextView originDocuments;
    @InjectView(R.id.sales_out)
    TextView salesOut;
    @InjectView(R.id.remarks)
    EditText remarks;
    @InjectView(R.id.framelayout)
    FrameLayout framelayout;
    @InjectView(R.id.camera_buttom_linearlayout)
    LinearLayout cameraButtomLinearlayout;
    @InjectView(R.id.recyclerview)
    RecyclerView recyclerview;
    @InjectView(R.id.buttom_button1)
    Button buttomButton1;
    @InjectView(R.id.buttom_button2)
    Button buttomButton2;
    @InjectView(R.id.buttom_button3)
    Button buttomButton3;
    @InjectView(R.id.buttom_button4)
    Button buttomButton4;
    @InjectView(R.id.linear_bottom)
    LinearLayout linearBottom;
    private TakeDelListBean.ResultBean.ResDataBean resDataBean;
    private InventoryApi inventoryApi;
    private DetailTakedAdapter  takedAdapter;
    private String type_code;
    private String state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_detial);
        ButterKnife.inject(this);

        Intent intent = getIntent();
        type_code = intent.getStringExtra("type_code");
        state = intent.getStringExtra("state");
        resDataBean = (TakeDelListBean.ResultBean.ResDataBean) intent.getSerializableExtra("dataBean");
        topImageview.setFocusableInTouchMode(true);
        topImageview.requestFocus();
        inventoryApi = RetrofitClient.getInstance(TakeDeliverDetailActivity.this).create(InventoryApi.class);
        recyclerview.setLayoutManager(new FullyLinearLayoutManager(TakeDeliverDetailActivity.this));
        recyclerview.addItemDecoration(new DividerItemDecoration(TakeDeliverDetailActivity.this,
                DividerItemDecoration.VERTICAL));
        recyclerview.setNestedScrollingEnabled(false);
        showView(resDataBean);
    }


    private void showView(TakeDelListBean.ResultBean.ResDataBean resDataBean) {
        partner.setText(resDataBean.getParnter_id());
        time.setText(TimeUtils.utc2Local(resDataBean.getMin_date()));
        states.setText(StringUtils.switchString(resDataBean.getState()));
        originDocuments.setText(resDataBean.getOrigin());
        if (resDataBean.getDelivery_rule() != null) {
            salesOut.setText(StringUtils.switchString((String) resDataBean.getDelivery_rule()));
        } else {
            salesOut.setText("");
        }
        remarks.setText(String.valueOf(resDataBean.getSale_note()));
        List<TakeDelListBean.ResultBean.ResDataBean.PackOperationProductIdsBean> pack_operation_product_ids = resDataBean.getPack_operation_product_ids();
        takedAdapter = new DetailTakedAdapter(R.layout.adapter_detaildeleive, pack_operation_product_ids);
        recyclerview.setAdapter(takedAdapter);
        refreshButtom(resDataBean.getState());
    }

    private void refreshButtom(final String state){
        switch (state){
            case "assigned":
                buttomButton1.setText("提交入库");
                showLinThreeCang();//根据权限判断
                initListenerAdapter();
                buttomButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertAialogUtils.getCommonDialog(TakeDeliverDetailActivity.this ,"是否确定提交")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        List<TakeDelListBean.ResultBean.ResDataBean.PackOperationProductIdsBean> data = takedAdapter.getData();
                                        ArrayList<Integer> doneNum = new ArrayList<>();
                                        for (int i = 0; i < data.size(); i++) {
                                            doneNum.add(StringUtils.doubleToInt(data.get(i).getQty_done()));
                                        }
                                        boolean isIntent = false;
                                        for (int i = 0; i < data.size(); i++) {
                                            if (data.get(i).getQty_done()>0){
                                                isIntent = true;
                                                break;
                                            }else {
                                                isIntent = false;
                                            }
                                        }
                                        if (isIntent){
                                            Intent intent = new Intent(TakeDeliverDetailActivity.this, TakeDeAreaActivity.class);
                                            intent.putExtra("bean", resDataBean);
                                            intent.putIntegerArrayListExtra("intArr", doneNum);
                                            intent.putExtra("type_code", type_code);
                                            intent.putExtra("state",state);
                                            startActivity(intent);
                                        }else {
                                            ToastUtils.showCommonToast(TakeDeliverDetailActivity.this, "请检查完成数量");
                                        }
                                    }
                                }).show();
                    }
                });
                break;
            case "qc_check":
                buttomButton1.setText("查看入库信息");
                buttomButton2.setText("填写品检信息");
                buttomButton2.setVisibility(View.VISIBLE);
                showLinThreePin();
                buttomButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TakeDeliverDetailActivity.this, AreaMessageActivity.class);
                        intent.putExtra("img_area", resDataBean.getQc_img());
                        intent.putExtra("string_area", (String) resDataBean.getPost_area_id().getArea_name());
                        startActivity(intent);
                    }
                });
                buttomButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TakeDeliverDetailActivity.this, WriteCheckMessaActivity.class);
                        intent.putExtra("bean", resDataBean);
                        startActivity(intent);
                    }
                });
                break;
            case "validate":
                buttomButton1.setText("查看品检信息");
                showLinThreePin();
                buttomButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                break;
            case "waiting_in":
                buttomButton1.setText("入库");
                showLinThreeCang();//根据权限判断
                buttomButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertAialogUtils.getCommonDialog(TakeDeliverDetailActivity.this, "是否确定入库")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        showDefultProgressDialog();
                                        HashMap<Object, Object> hashMap = new HashMap<>();
                                        hashMap.put("state","transfer");
                                        hashMap.put("picking_id",resDataBean.getPicking_id());
                                        int size = resDataBean.getPack_operation_product_ids().size();
                                        Map[] maps = new Map[size];
                                        for (int i = 0; i < size; i++) {
                                            Map<Object, Object> map = new HashMap<>();
                                            map.put("pack_id",resDataBean.getPack_operation_product_ids().get(i).getPack_id());
                                            map.put("qty_done", StringUtils.doubleToInt(resDataBean.getPack_operation_product_ids().get(i).getQty_done()));
                                            maps[i] = map;
                                        }
                                        hashMap.put("pack_operation_product_ids",maps);
                                        Call<TakeDelListBean> objectCall = inventoryApi.ruKu(hashMap);
                                        objectCall.enqueue(new MyCallback<TakeDelListBean>() {
                                            @Override
                                            public void onResponse(Call<TakeDelListBean> call, Response<TakeDelListBean> response) {
                                                dismissDefultProgressDialog();
                                                if (response.body() == null)return;
                                                if (response.body().getResult().getRes_data() != null && response.body().getResult().getRes_code() == 1){
                                                    ToastUtils.showCommonToast(TakeDeliverDetailActivity.this, "入库完成");
                                                    finish();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<TakeDelListBean> call, Throwable t) {
                                                dismissDefultProgressDialog();
                                                ToastUtils.showCommonToast(TakeDeliverDetailActivity.this, t.toString());
                                            }
                                        });
                                    }
                                }).show();
                    }
                });
                break;
            case "done":
                buttomButton1.setVisibility(View.GONE);
                break;
        }
    }

    private void initListenerAdapter() {
        takedAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                final TakeDelListBean.ResultBean.ResDataBean.PackOperationProductIdsBean bean
                        = takedAdapter.getData().get(position);
                final EditText editText = new EditText(TakeDeliverDetailActivity.this);
                final int qty_available = StringUtils.doubleToInt(bean.getProduct_id().getQty_available());
                int product_qty = StringUtils.doubleToInt(bean.getProduct_qty());
                final int qty = qty_available >= product_qty ? qty_available:product_qty;
                editText.setText(qty + "");
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setSelection(editText.getText().length());
                AlertDialog.Builder dialog = AlertAialogUtils.getCommonDialog(TakeDeliverDetailActivity.this, "请输入 " + bean.getProduct_id().getName() + " 完成数量");
                dialog.setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int anInt = Integer.parseInt(editText.getText().toString());
                                if (anInt > qty) {
                                    Toast.makeText(TakeDeliverDetailActivity.this, "库存不足", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                bean.setQty_done(anInt);
                                takedAdapter.notifyDataSetChanged();
                            }
                        }).show();
            }
        });
    }

    /**
     * 是否显示底部(仓库)
     */
    public void showLinThreeCang() {
        if (!UserManager.getSingleton().getGrops().contains("group_charge_warehouse")) {
            linearBottom.setVisibility(View.GONE);
        }
    }
    /**
     * 是否显示底部（品检）
     */
    public void showLinThreePin() {
        if (!UserManager.getSingleton().getGrops().contains("group_charge_inspection")) {
            linearBottom.setVisibility(View.GONE);
        }
    }
    /**
     * 是否显示底部（采购）
     */
    public void showLinThreeGou() {
        if (!UserManager.getSingleton().getGrops().contains("group_purchase_manager") && !UserManager.getSingleton().getGrops().contains("group_purchase_user")) {
            linearBottom.setVisibility(View.GONE);
        }
    }
}
