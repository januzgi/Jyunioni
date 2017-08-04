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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * {@link ShoutboxFragment} displays a view in which the messages in the shoutbox are shown.
 */
public class ShoutboxFragment extends Fragment {

    /** Required empty public constructor */
    public ShoutboxFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.shoutbox_build, container, false);

        // Create a list of events
        final ArrayList<ShoutboxMessage> events = new ArrayList<ShoutboxMessage>();
        events.add(new ShoutboxMessage("Jaajaa"));

        return rootView;
    }

    /** When the activity is stopped, release possible resources */
    @Override
    public void onStop() {
        super.onStop();

    }


}
