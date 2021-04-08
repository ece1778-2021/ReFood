package com.refood.refood;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class ResourceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource);
        BackgroundMusic.getInstance(this).start();

        TextView link1 = (TextView) findViewById(R.id.textView10);
        String linkText1 = "Here's an <a href='https://food-guide.canada.ca/en/healthy-eating-recommendations/make-it-a-habit-to-eat-vegetables-fruit-whole-grains-and-protein-foods/choosing-foods-with-healthy-fats/'>article</a> that talks about healthy and unhealthy fats";
        link1.setText(Html.fromHtml(linkText1));
        link1.setMovementMethod(LinkMovementMethod.getInstance());

        TextView link2 = (TextView) findViewById(R.id.textView11);
        String linkText2 = "Here's an <a href='http://www.foodaddictsanonymous.org/'>food addiction program</a> that might help you recover from a food addiction";
        link2.setText(Html.fromHtml(linkText2));
        link2.setMovementMethod(LinkMovementMethod.getInstance());

        TextView link3 = (TextView) findViewById(R.id.textView12);
        String linkText3 = "Here are some <a href='https://thestayathomechef.com/healthy-lemon-garlic-salmon/'>healthy recipes</a>, give them a try!";
        link3.setText(Html.fromHtml(linkText3));
        link3.setMovementMethod(LinkMovementMethod.getInstance());


    }

    @Override
    public void onResume(){
        super.onResume();
        BackgroundMusic.getInstance(this).start();
    }

    @Override
    protected void onPause() {
        BackgroundMusic.getInstance(this).pause();
        super.onPause();
    }
}