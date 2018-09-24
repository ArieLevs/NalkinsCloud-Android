package cloud.nalkins.nalkinscloud.userAccount;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

import cloud.nalkins.nalkinscloud.R;
import cloud.nalkins.nalkinscloud.login.LogoActivity;

/**
 * Created by Arie on 1/16/2018.
 *
 */

public class ForgotPasswordCompleteActivity extends AppCompatActivity {

    // Create the ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.back_only_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_complete);

        //Set confirm button
        Button registerConfirmButton = (Button) findViewById(R.id.btnConfirmForgotPassComplete);

        registerConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),
                        LogoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}
