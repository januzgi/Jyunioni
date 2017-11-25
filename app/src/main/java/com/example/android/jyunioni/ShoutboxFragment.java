package com.example.android.jyunioni;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * ShoutboxFragment displays a view in which the messages in the shoutbox are shown.
 *
 * @author Jani Suoranta 25.11.2017
 */
public class ShoutboxFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {

    /**
     * Initialize the different views for the messages
     */
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView senderTextView;
        CircleImageView senderImageView;

        private MessageViewHolder(View v) {
            super(v);
            messageTextView = itemView.findViewById(R.id.shoutboxMessageTextView);
            senderTextView = itemView.findViewById(R.id.shoutboxSenderTextView);
            senderImageView =
                    itemView.findViewById(R.id.shoutboxSenderImageView);
        }
    }


    /**
     * Declare a couple string variables
     */
    public static final String MESSAGES_CHILD = "messages";
    public static final String ANONYMOUS = "anonymous";

    /**
     * Declare username, profile picture URL and initialize Google API client
     */
    private String mUsername;
    private String mPhotoUrl;

    /**
     * Initialize send button, message recycler view, progress bar, edit text field and the layout manager.
     */
    private ImageButton mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private EditText mMessageEditText;

    /**
     * Firebase instance variables
     */
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<ShoutboxMessage, MessageViewHolder> mFirebaseAdapter;

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = SignInGoogleActivity.class.getSimpleName();

    /**
     * Required empty public constructor
     */
    public ShoutboxFragment() {
    }


    /**
     * Creates and returns the view hierarchy associated with the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.shoutbox_build, container, false);

        // Set default username is anonymous.
        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the sign in activity
            startActivity(new Intent(getContext(), SignInGoogleActivity.class));
            getActivity().finish();
            // return;
        } else {
            // If already signed in, initialize the user name and profile picture.
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        return rootView;
    }


    /**
     * Called when the fragments activity has completed its .onCreate().
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize RecyclerView and the LayoutManager
        mMessageRecyclerView = getView().findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLinearLayoutManager.setStackFromEnd(true);
        // Set the RecyclerView to use the LayoutManager
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);


        // Check for new child entries
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<ShoutboxMessage> parser = new SnapshotParser<ShoutboxMessage>() {

            @Override
            public ShoutboxMessage parseSnapshot(DataSnapshot dataSnapshot) {
                ShoutboxMessage shoutboxMessage = dataSnapshot.getValue(ShoutboxMessage.class);
                if (shoutboxMessage != null) {
                    shoutboxMessage.setId(dataSnapshot.getKey());
                }
                return shoutboxMessage;
            }

        };

        // Get a reference to the message in the database.
        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD);

        // Use recyclerView to reuse old views by binding new data to them.
        FirebaseRecyclerOptions<ShoutboxMessage> options =
                new FirebaseRecyclerOptions.Builder<ShoutboxMessage>().setQuery(messagesRef, parser).build();

        // Use a viewHolder to store each view inside the tag field without the need to look them up repeatedly.
        mFirebaseAdapter = new FirebaseRecyclerAdapter<ShoutboxMessage, MessageViewHolder>(options) {
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.message_item, viewGroup, false));
            }

            // Unused view holders get filled with data to be displayed.
            @Override
            protected void onBindViewHolder(final MessageViewHolder viewHolder, int position, ShoutboxMessage shoutboxMessage) {

                // Set the text message to the messageTextView's viewHolder
                if (shoutboxMessage.getText() != null) {
                    viewHolder.messageTextView.setText(shoutboxMessage.getText());
                    viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
                }

                // Set the profile picture to the senderImageView's viewHolder or use the default picture.
                viewHolder.senderTextView.setText(shoutboxMessage.getName());
                if (shoutboxMessage.getPhotoUrl() == null) {
                    viewHolder.senderImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_account_circle_black_36dp));
                } else {
                    // Use Glide to smoothen and fasten the scrolling of the messages list.
                    // https://github.com/bumptech/glide
                    Glide.with(getActivity()).load(shoutboxMessage.getPhotoUrl()).into(viewHolder.senderImageView);
                }

            }
        };


        // Watch the changes in the recyclerView
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int shoutboxMessageCount = mFirebaseAdapter.getItemCount();

                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

                // If the recyclerView is being loaded or the user is at the bottom of the list
                if (lastVisiblePosition == -1 || (positionStart >= (shoutboxMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    // Then scroll down to show the newly added message
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        // Set the adapter to the recyclerView
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);


        // Get the message editTextView instance
        mMessageEditText = getView().findViewById(R.id.messageEditText);

        // Create a listener for the edit text to dismiss the keyboard when the edit text loses focus
        class MyFocusChangeListener implements View.OnFocusChangeListener {

            public void onFocusChange(View v, boolean hasFocus) {
                if (v.getId() == R.id.messageEditText && !hasFocus) {

                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }

        // Set the focus listener into action
        View.OnFocusChangeListener ofcListener = new MyFocusChangeListener();
        mMessageEditText.setOnFocusChangeListener(ofcListener);


        // Add a listener if the editable textfield changes (user types something) to make the send button functioning
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            // If there is atleast one letter in the textfield, then enable sending the message.
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        // Initialize send button
        mSendButton = getView().findViewById(R.id.sendButton);
        // Set it disabled so the user can only push it when written something.
        // This changes to enabled in the listener.
        mSendButton.setEnabled(false);

        // Listen for pushing of the send button
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new ShoutboxMessage from the contents.
                ShoutboxMessage shoutboxMessage =
                        new ShoutboxMessage(mMessageEditText.getText().toString(), mUsername, mPhotoUrl);

                // Push the message to the database
                mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(shoutboxMessage);
                // Empty the message field
                mMessageEditText.setText("");
            }
        });

    }

    /**
     * When the activity is stopped, release possible resources
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * Pause from listening to database changes or new input.
     */
    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    /**
     * When resuming, start listening for new input or messages from database.
     */
    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    /**
     * Show a toast if connecting to Google APIs fails.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(getContext(), "Google sign in error.", Toast.LENGTH_SHORT).show();
    }

}