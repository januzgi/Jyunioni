/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.jyunioni;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class EventActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content of the activity to use the activity_main.xml layout file
        setContentView(R.layout.activity_main);

        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        CategoryAdapter adapter = new CategoryAdapter(this, getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Find the tab layout that shows the tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        // Connect the tab layout with the view pager. This will
        //   1. Update the tab layout when the view pager is swiped
        //   2. Update the view pager when a tab is selected
        //   3. Set the tab layout's tab names with the view pager's adapter's titles
        //      by calling onPageTitle()
        tabLayout.setupWithViewPager(viewPager);
    }
}

/*    // TODO: Create a duplicate arraylist of events just to see if it works for the eventsfragment.java
    public ArrayList<Event> createFakeList(Event event){
        ArrayList<Event> duplicateEvents = new ArrayList<>();

        for (int i = 0; i < 10; i++){
            duplicateEvents.add(event);
        }


        return duplicateEvents;
    }

    public ArrayList<Event> getFakeList(){
        return duplicateEvents;
    }*/


/*    // Set the according items to the right views.
        eventNameTextView.setText(event.getEventName());
        eventTimestampTextView.setText(event.getEventTimestamp());
        eventsGroupImageView.setBackgroundResource(event.getImageResourceId());

        // Set the theme color for the list item, find id first
        // Find the color that the resource ID maps to and
        // set the background color of the text container View
        textContainer.setBackgroundColor(ContextCompat.getColor(getActivity(), event.getGroupColorId()));*/