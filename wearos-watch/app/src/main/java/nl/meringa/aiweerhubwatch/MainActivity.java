package nl.meringa.aiweerhubwatch;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private final Handler handler = new Handler();
    private TextView temp, sky, rain, wind, advice, updated;
    private final String API = "https://api.open-meteo.com/v1/forecast?latitude=52.779&longitude=6.906&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m&hourly=precipitation_probability&timezone=Europe%2FAmsterdam&forecast_days=1";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();
        load();
        handler.postDelayed(new Runnable(){ public void run(){ load(); handler.postDelayed(this,300000); }},300000);
    }

    private TextView tv(String text, float size){
        TextView v=new TextView(this); v.setText(text); v.setTextColor(Color.WHITE); v.setTextSize(size);
        v.setGravity(Gravity.CENTER); v.setPadding(6,3,6,3); return v;
    }

    private void buildUi(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER);
        root.setPadding(10,8,10,8); root.setBackgroundColor(Color.rgb(5,11,20));
        TextView place=tv("📍 EMMEN",12); root.addView(place);
        temp=tv("--°",42); root.addView(temp);
        sky=tv("Weer laden…",15); root.addView(sky);
        TextView ai=tv("AI WeerHub",11); root.addView(ai);
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER);
        rain=tv("🌧 --%",14); wind=tv("💨 --",14);
        row.addView(rain,new LinearLayout.LayoutParams(0,55,1)); row.addView(wind,new LinearLayout.LayoutParams(0,55,1));
        root.addView(row);
        advice=tv("Analyse…",14); root.addView(advice);
        updated=tv("",9); root.addView(updated);
        setContentView(root);
    }

    private void load(){
        new Thread(() -> {
            try{
                HttpURLConnection c=(HttpURLConnection)new URL(API).openConnection();
                c.setConnectTimeout(7000); c.setReadTimeout(7000);
                BufferedReader r=new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder s=new StringBuilder(); String line; while((line=r.readLine())!=null)s.append(line);
                JSONObject d=new JSONObject(s.toString()), cur=d.getJSONObject("current"), hourly=d.getJSONObject("hourly");
                double t=cur.getDouble("temperature_2m"), w=cur.getDouble("wind_speed_10m");
                int code=cur.getInt("weather_code"), rp=hourly.getJSONArray("precipitation_probability").getInt(0);
                runOnUiThread(() -> {
                    temp.setText(String.format(Locale.US,"%.0f°",t));
                    sky.setText(weather(code)); rain.setText("🌧 "+rp+"%");
                    wind.setText("💨 "+String.format(Locale.US,"%.0f",w)+" km/u");
                    advice.setText(rp>=60?"🌧 Regenkans hoog":w>=35?"💨 Stevige wind":t<8?"🧥 Jas verstandig":"👍 Prima weer");
                    updated.setText("Bijgewerkt • V49");
                });
                c.disconnect();
            }catch(Exception e){ runOnUiThread(() -> updated.setText("⚠️ Geen verbinding")); }
        }).start();
    }

    private String weather(int c){
        if(c==0)return "☀️ Helder"; if(c<=3)return "🌤 Bewolkt"; if(c<=48)return "🌫 Mist";
        if(c<=67)return "🌧 Regen"; if(c<=77)return "🌨 Sneeuw"; if(c<=82)return "🌦 Buien"; if(c>=95)return "⛈ Onweer";
        return "🌦 Wisselvallig";
    }
}