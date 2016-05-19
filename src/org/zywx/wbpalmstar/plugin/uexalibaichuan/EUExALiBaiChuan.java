package org.zywx.wbpalmstar.plugin.uexalibaichuan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.login.LoginService;
import com.alibaba.sdk.android.login.callback.LoginCallback;
import com.alibaba.sdk.android.login.callback.LogoutCallback;
import com.alibaba.sdk.android.session.model.Session;
import com.alibaba.sdk.android.trade.TradeConfigs;
import com.alibaba.sdk.android.trade.TradeConstants;
import com.alibaba.sdk.android.trade.TradeService;
import com.alibaba.sdk.android.trade.callback.TradeProcessCallback;
import com.alibaba.sdk.android.trade.model.TaokeParams;
import com.alibaba.sdk.android.trade.model.TradeResult;
import com.alibaba.sdk.android.trade.page.ItemDetailPage;
import com.alibaba.sdk.android.trade.page.MyCartsPage;
import com.alibaba.sdk.android.trade.page.MyOrdersPage;
import com.alibaba.sdk.android.trade.page.Page;
import com.taobao.tae.sdk.callback.CallbackContext;
import com.taobao.tae.sdk.callback.InitResultCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;

import java.util.HashMap;
import java.util.Map;

public class EUExALiBaiChuan extends EUExBase {
    private static final String TAG = "EUExALiBaiChuan";
    public static final String CALLBACK_INIT = "uexALiBaiChuan.cbInit";
    public static final String CALLBACK_LOGIN = "uexALiBaiChuan.cbLogin";
    public static final String CALLBACK_LOGOUT = "uexALiBaiChuan.cbLogout";
    public static final String CALLBACK_OPEN_ITEM_DETAIL_PAGE_BY_ID = "uexALiBaiChuan.cbOpenItemDetailPageById";
    public static final String CALLBACK_OPEN_ITEM_DETAIL_PAGE_BY_URL = "uexALiBaiChuan.cbOpenItemDetailPageByURL";
    public static final String TEXT_STATUS = "status";

    public EUExALiBaiChuan(Context context, EBrowserView view) {
        super(context, view);
    }

    /**
     * 初始化
     * @param params
     */
    public void init(String[] params) {
        AlibabaSDK.asyncInit(mContext, new InitResultCallback() {
            @Override
            public void onSuccess() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(TEXT_STATUS, 0);
                } catch (JSONException e) {
                    Log.i(TAG, "[init]" + e.getMessage());
                    e.printStackTrace();
                }
                callBackPluginJs(CALLBACK_INIT, jsonObject.toString());
            }

            @Override
            public void onFailure(int code, String message) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(TEXT_STATUS, 1);
                } catch (JSONException e) {
                    Log.i(TAG, "[init]" + e.getMessage());
                    e.printStackTrace();
                }
                callBackPluginJs(CALLBACK_INIT, jsonObject.toString());
            }
        });
    }

    /**
     * 打开手淘授权登陆
     * @param params
     */
    public void login(String params[]) {
        LoginService loginService = AlibabaSDK.getService(LoginService.class);
        registerActivityResult();
        loginService.showLogin((Activity) mContext, new LoginCallback() {

            @Override
            public void onSuccess(Session session) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("isLogin", 1);
                } catch (JSONException e) {
                    Log.i(TAG, "[login]" + e.getMessage());
                    e.printStackTrace();
                }
                callBackPluginJs(CALLBACK_LOGIN, jsonObject.toString());
            }

            @Override
            public void onFailure(int code, String message) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("isLogin", 0);
                } catch (JSONException e) {
                    Log.i(TAG, "[login]" + e.getMessage());
                    e.printStackTrace();
                }
                Log.i(TAG, "[login][onFailure]code:" + code + "    message:" + message);
                callBackPluginJs(CALLBACK_LOGIN, jsonObject.toString());
            }
        });
    }

    public String getUserInfo(String params[]) {
        LoginService loginService = AlibabaSDK.getService(LoginService.class);
        Session sessionCache = loginService.getSession();
        JSONObject jsonObject = new JSONObject();
        try {
            if(sessionCache == null) {
                jsonObject.put("isLogin", 1);
            } else {
                jsonObject.put("userId", sessionCache.getUserId());
                jsonObject.put("nick", sessionCache.getUser().nick);
                jsonObject.put("iconUrl", sessionCache.getUser().avatarUrl);
                jsonObject.put("authorizationCode", sessionCache.getAuthorizationCode());
                jsonObject.put("loginTime", sessionCache.getLoginTime());
                jsonObject.put("isLogin", 0);
            }
            return jsonObject.toString();
        } catch (JSONException e) {
            Log.i(TAG, "[getUserInfo]" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void logout(String params[]) {
        LoginService loginService = AlibabaSDK.getService(LoginService.class);
        loginService.logout((Activity) mContext, new LogoutCallback() {
            @Override
            public void onSuccess() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("isLogin", 1);
                } catch (JSONException e) {
                    Log.i(TAG, "[logout]" + e.getMessage());
                    e.printStackTrace();
                }
                callBackPluginJs(CALLBACK_LOGOUT, jsonObject.toString());
            }

            @Override
            public void onFailure(int code, String message) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("isLogin", 1);
                    Log.i(TAG, "[logout]code:" + code + "  message:" + message);
                } catch (JSONException e) {
                    Log.i(TAG, "[logout]" + e.getMessage());
                    e.printStackTrace();
                }
                callBackPluginJs(CALLBACK_LOGOUT, jsonObject.toString());
            }
        });
    }

    public void openMyCart(String params[]) {
        if (params != null && params.length == 1) {
            try {
                String json = params[0];
                JSONObject jsonObj = new JSONObject(json);
                String isvCode = jsonObj.optString("isvCode", "");
                if (!TextUtils.isEmpty(isvCode)) {
                    TradeConfigs.defaultISVCode = isvCode;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        TradeService tradeService = AlibabaSDK.getService(TradeService.class);
        MyCartsPage myCartsPage = new MyCartsPage();
        registerActivityResult();
        tradeService.show(myCartsPage, null, (Activity) mContext, null, new TradeProcessCallback() {
                    @Override
                    public void onPaySuccess(TradeResult tradeResult) {
                        tradeSuccessCallback();
                    }
                    @Override
                    public void onFailure(int code, String s) {
                        tradeFailedCallback(code, s);
                    }
                }
        );
    }

    public void openMyOrdersPage(String params[]) {
        TradeService tradeService = AlibabaSDK.getService(TradeService.class);
        MyOrdersPage myOrdersPage = new MyOrdersPage(0, false);
        registerActivityResult();
        tradeService.show(myOrdersPage, null, (Activity)mContext, null, new TradeProcessCallback(){

                    @Override
                    public void onPaySuccess(TradeResult tradeResult) {
                        tradeSuccessCallback();
                    }

                    @Override
                    public void onFailure(int code, String s) {
                        tradeFailedCallback(code, s);
                    }
                }
        );
    }

    public void openItemDetailPageById(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        String isvCode = "";
        String itemId = "";
        String mmpid = "";
        JSONObject result = new JSONObject();
        try {
            JSONObject jsonObj = new JSONObject(json);
            isvCode = jsonObj.optString("isvCode", "");
            itemId = jsonObj.optString("itemid");
            mmpid = jsonObj.optString("mmpid");
            if (TextUtils.isEmpty(itemId)) {
                result.put("error", "itemId can not be null");
                callBackPluginJs(CALLBACK_OPEN_ITEM_DETAIL_PAGE_BY_ID, result.toString());
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TradeService tradeService = AlibabaSDK.getService(TradeService.class);
        Map<String, String> exParams = new HashMap<String, String>();
        if (!TextUtils.isEmpty(isvCode)) {
            exParams.put(TradeConstants.ISV_CODE, isvCode);
        }
        ItemDetailPage itemDetailPage = new ItemDetailPage(itemId, exParams);
        TaokeParams taokeParams = null;
        if (!TextUtils.isEmpty(mmpid)) {
            taokeParams = new TaokeParams();
            taokeParams.pid = mmpid;
        }
        registerActivityResult();
        tradeService.show(itemDetailPage, taokeParams, (Activity)mContext, null, new TradeProcessCallback() {
            @Override
            public void onPaySuccess(TradeResult tradeResult) {
                tradeSuccessCallback();
            }

            @Override
            public void onFailure(int code, String s) {
                tradeFailedCallback(code, s);
            }
        });
    }



    public void openItemDetailPageByURL(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject result = new JSONObject();
        String url = "";
        String mmpid = "";
        try {
            JSONObject jsonObj = new JSONObject(json);

            url = jsonObj.optString("url");
            mmpid = jsonObj.optString("mmpid");
            if (TextUtils.isEmpty(url)) {
                result.put("error", "url can not be null");
                callBackPluginJs(CALLBACK_OPEN_ITEM_DETAIL_PAGE_BY_URL, result.toString());
                return;
            }
        } catch (JSONException e) {

        }
        Page page = new Page(url, null);
        TaokeParams taokeParams = null;
        if (!TextUtils.isEmpty(mmpid)) {
            taokeParams = new TaokeParams();
            taokeParams.pid = mmpid;
        }
        registerActivityResult();
        AlibabaSDK.getService(TradeService.class).show(page, taokeParams, (Activity) mContext, null, new TradeProcessCallback() {
            @Override
            public void onPaySuccess(TradeResult tradeResult) {
                tradeSuccessCallback();
            }

            @Override
            public void onFailure(int code, String message) {
                tradeFailedCallback(code, message);
            }
        });
    }

    private void tradeSuccessCallback() {
        Log.i(TAG, "[Trade Success]");
    }
    private void tradeFailedCallback(int code, String message) {
        Log.i(TAG, "[Trade Fail] code:" + code + "    msg:" + message);
    }

    private void callBackPluginJs(String methodName, String jsonData){
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        onCallback(js);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CallbackContext.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected boolean clean() {
        return false;
    }
}