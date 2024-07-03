package com.hci.ireye.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hci.ireye.R;

import java.math.BigInteger;
import java.security.MessageDigest;

@SuppressWarnings("all")

public class LoginActivity extends AppCompatActivity {

    private ImageView mIvInfo;
    private TextView mTvInfo, mTvError;
    private EditText mEtUsername, mEtPassword;
    private Button mBtnConfirm;
    private ProgressBar mPbProgress;
    private SharedPreferences mUserInfoPrefs, mUserPrefs;
    private CheckBox mCbRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mIvInfo = findViewById(R.id.iv_login_info);
        mTvInfo = findViewById(R.id.tv_login_info);
        mTvError = findViewById(R.id.tv_login_error);
        mEtUsername = findViewById(R.id.et_login_username);
        mEtPassword = findViewById(R.id.et_login_password);
        mBtnConfirm = findViewById(R.id.btn_login_confirm);
        mPbProgress = findViewById(R.id.pb_login_progress);
        mCbRemember = findViewById(R.id.cb_login_remember);

        mUserInfoPrefs = getSharedPreferences(getString(R.string.user_info_prefs), MODE_PRIVATE);
        mUserPrefs = getSharedPreferences(getString(R.string.user_prefs), MODE_PRIVATE);

        mCbRemember.setChecked(mUserPrefs.getBoolean(getString(R.string.sp_remember), false));
        mCbRemember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUserPrefs.edit().putBoolean(getString(R.string.sp_remember), isChecked).apply();
            }
        });

        mIvInfo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mTvInfo.setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        mTvInfo.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });

        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCbRemember.isChecked()) {
                    mUserInfoPrefs.edit()
                            .putString(getString(R.string.sp_password), mEtPassword.getText().toString())
                            .putString(getString(R.string.sp_username), mEtUsername.getText().toString())
                            .apply();
                } else {
                    mUserInfoPrefs.edit()
                            .putString(getString(R.string.sp_password), "")
                            .putString(getString(R.string.sp_username), "")
                            .apply();
                }

                mTvError.setVisibility(View.INVISIBLE);
                mPbProgress.setVisibility(View.VISIBLE);
                mBtnConfirm.setClickable(false);

                final String username = mEtUsername.getText().toString();
                final String password = mEtPassword.getText().toString();

//                ApacheFtpUtil.ftpsClient = new FTPClient();
//                ApacheFtpUtil.info = new FtpInfo(getString(R.string.server_name), getResources().getInteger(R.integer.port_number), username, password);

                final Handler handler = new Handler() {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        super.handleMessage(msg);
                        switch (msg.what) {
                            case 1: // wrong
                                mTvError.setVisibility(View.VISIBLE);
                                mPbProgress.setVisibility(View.INVISIBLE);
                                mBtnConfirm.setClickable(true);
                                break;
                            case 2: // correct
                                mTvError.setVisibility(View.INVISIBLE);
                                mPbProgress.setVisibility(View.INVISIBLE);

                                Intent intent = new Intent(LoginActivity.this, MainInterfaceActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                        }
                    }
                };

                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] passwordHash = digest.digest(password.getBytes("UTF_8"));
                    String passwordHashHex = String.format("%064x", new BigInteger(1, passwordHash));
                    Log.d("Cao", passwordHashHex + " " + username);
                    handler.sendEmptyMessage(username.equals("admin") && passwordHashHex.equals("b5247ae6a54089966ad4717b7c0b040fe4c4eebb33cc4458b0744cf943e291b6") ? 2 : 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                ThreadUtil.runOnThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        boolean success = ApacheFtpUtil.connect(ApacheFtpUtil.ftpsClient, ApacheFtpUtil.info);
//                        Message msg = new Message();
//                        msg.what = success ? 2 : 1;
//                        handler.sendMessage(msg);
//                    }
//                });
            }
        });

        mEtUsername.setText(mUserInfoPrefs.getString(getString(R.string.sp_username), null));
        mEtPassword.setText(mUserInfoPrefs.getString(getString(R.string.sp_password), null));

    }
}

// adb install -r "C:\Users\30847\Desktop\UltraCount_2021\IReye-2.0-App\app\build\outputs\apk\debug\app-debug.apk"