package com.app.mutliple.videos.compression.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.app.mutliple.videos.compression.R;

public class MessageDialog extends DialogFragment implements DialogInterface.OnCancelListener,
        DialogInterface.OnDismissListener {


    OnClick listener;
    OnClickLogout onClickLogout;

    public TextView tvMsg;
    public TextView tvMsgInfo;
    public ImageView imgClose;
    public Button btCancel;
    public Button btOk;
    public LinearLayout llMain;
    public CardView llMainCard;
    public ImageView imgIcon;
    String dialogType = "";
    String msgType = "";

    Boolean mShowCloseIcon = false;
    String msgTitle = "";

    public Integer iconDialogStatus = 0;
    public Integer backgroundColorDialog = 0;
    public MessageDialog() {
    }
    public MessageDialog(Integer mIconDialogStatus, Integer mBackgroundColorDialog) {
        iconDialogStatus = mIconDialogStatus;
        backgroundColorDialog = mBackgroundColorDialog;
    }
    public static MessageDialog getInstance(Integer mIconDialogStatus, Integer mBackgroundColorDialog) {
//        iconDialogStatus = 0;
//        iconDialogStatus = mIconDialogStatus;
//        backgroundColorDialog = mBackgroundColorDialog;
        MessageDialog msgDialog = new MessageDialog(mIconDialogStatus, mBackgroundColorDialog);
//        MessageDialog msgDialog = new MessageDialog();
//        if (msgDialog == null)
//            msgDialog = new MessageDialog();
        return msgDialog;
    }

    String tvMsgText = "", tvMsgInfoText = "", cancelTxt = "", okTxt = "";

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(0, R.style.myDialogThemePopup);

    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.TOP);
        dialog.getWindow().setWindowAnimations(R.style.DialogMessageAnimation);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_ok, container, false);

        btOk = view.findViewById(R.id.btOk);
        btCancel = view.findViewById(R.id.btCancel);
        tvMsg = view.findViewById(R.id.tvMsg);
        tvMsgInfo = view.findViewById(R.id.tvMsgInfo);
        imgClose = view.findViewById(R.id.imgClose);
        llMain = view.findViewById(R.id.llMain);
        llMainCard = view.findViewById(R.id.llMainCard);
        imgIcon = view.findViewById(R.id.imgIcon);
        btOk.setText("Ok");

        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.set(true);
                }

                if (onClickLogout != null) {
                    onClickLogout.logout(true);
                }

            }
        });

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btCancel.performClick();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.set(false);
                }
                if (onClickLogout != null) {
                    onClickLogout.cancelBtn(false);
                }

            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            tvMsgText = getArguments().getString("tvMsgText", null);
            tvMsgInfoText = getArguments().getString("tvMsgInfoText", null);

            cancelTxt = getArguments().getString("cancelTxt", "");
            okTxt = getArguments().getString("okTxt", "");
            dialogType = getArguments().getString("dialogType", "");
            msgType = getArguments().getString("msgType", "");
            mShowCloseIcon = getArguments().getBoolean("mShowCloseIcon",false);
//            iconDialogStatus = getArguments().getInt("iconDialogStatus");
//            backgroundColorDialog = getArguments().getInt("backgroundColorDialog");


            if (getArguments().containsKey("msgTitle")) {
                msgTitle = getArguments().getString("msgTitle", "");
                if (!TextUtils.isEmpty(msgTitle)) {
                    tvMsg.setVisibility(View.VISIBLE);
                }
            } else {
                tvMsg.setVisibility(View.GONE);
            }
            imgIcon.setVisibility(View.GONE);
            imgClose.setVisibility(View.GONE);

            tvMsgInfo.setTextColor(ContextCompat.getColor(getContext(),R.color.white));

            if (backgroundColorDialog != 0) {
                llMainCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), backgroundColorDialog));
            } else {
                llMainCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_black));
            }
            if (iconDialogStatus != 0) {
                imgIcon.setImageResource(iconDialogStatus);
            }
            if (mShowCloseIcon) {
                imgClose.setVisibility(View.VISIBLE);
            }
            switch (dialogType) {
                case Constants.SUCCESS:
//                llMainCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                    imgIcon.setVisibility(View.VISIBLE);
                    if (iconDialogStatus == 0) {
                        imgIcon.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                    break;


                default:
                    if (iconDialogStatus == 0) {
                        imgIcon.setImageResource(R.drawable.ic_seek_thumb_pressed);
                    }
                    imgIcon.setVisibility(View.VISIBLE);
//                llMainCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.alert_dialog_error));
                    //llMainCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent_black_banner));
                    break;
            }


            if (TextUtils.isEmpty(cancelTxt)) {
                btCancel.setVisibility(View.GONE);
            } else {
                btCancel.setVisibility(View.GONE);
            }


            btCancel.setText(cancelTxt);
            btOk.setText(okTxt);

            tvMsgInfo.setText(tvMsgText);

        }

        setLabel();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
    }


    private void setLabel() {

    }


    public void setListener(OnClick listener) {
        this.listener = listener;
    }

    public interface OnClick {
        public void set(boolean ok);
    }


    ///////////////////////////////////////////////////////////////

    public interface OnClickLogout {
        public void logout(boolean yes);

        public void cancelBtn(boolean cancel);
    }

    //    public void onDismiss(@NonNull DialogInterface.OnDismissListener dialog) {
//    public void onDismiss(@NonNull DialogInterface.OnDismissListener dialog) {
//        super.onDismiss((DialogInterface) dialog);
//        dismiss();
        // ((BaseActivity) getContext()).setStatusBar(ContextCompat.getColor(getContext(), R.color.colorPrimary), false);
//    }
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        dismissAllowingStateLoss();
        dismiss();
        listener=null;
        onClickLogout=null;
    }
}