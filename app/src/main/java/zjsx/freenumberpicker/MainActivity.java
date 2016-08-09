package zjsx.freenumberpicker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements FreeNumberPicker.OnNumberChangeListener {
    FreeNumberPicker numperPicker1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        numperPicker1 = (FreeNumberPicker) findViewById(R.id.numperPicker1);
        numperPicker1.setOnNumberChangeListener(this);
    }

    Toast toast;

    @Override
    public void onNumberChanged(int value) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, "change:" + value, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onNumberClick() {
        
    }
}
