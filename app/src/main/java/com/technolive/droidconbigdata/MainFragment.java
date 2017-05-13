package com.technolive.droidconbigdata;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    // CallbackManager manages the callbacks into the FacebookSdk from an Activity's or Fragment's onActivityResult() method
    private CallbackManager mCallbackManager;
    TextView userName;
    ImageView userImage;
    ListView list;
    Button seeanalytics;

    String my_access_token = "";

    public MainFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(
                    "com.technolive.droidconbigdata",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        // Facebook SDK needs to be initialized after the Fragment or Class is created
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }

        // The factory class for the CallbackManager needs to be created
        mCallbackManager = CallbackManager.Factory.create();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        userName = (TextView) view.findViewById(R.id.userName);
        userImage = (ImageView) view.findViewById(R.id.userImage);
        list = (ListView) view.findViewById(R.id.list);
        seeanalytics = (Button) view.findViewById(R.id.seeanalytics);


        seeanalytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getContext(), AnalyticsActivity.class);
                in.putExtra("my_access_token", my_access_token);
                startActivity(in);
            }
        });

        loginButton.setReadPermissions(Arrays.asList("email", "user_birthday", "user_location", "user_posts", "user_photos"));

        loginButton.setFragment(this);

        // RegisterCallback is used to Callback the Registration for LoginResult
        loginButton.registerCallback(mCallbackManager, mCallback);
    }

    Bundle bFacebookData;
    private FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>() {
        // This is what happens if the Callback is successful
        @Override
        public void onSuccess(LoginResult loginResult) {
            AccessToken accessToken = loginResult.getAccessToken();
            my_access_token = accessToken.getToken();
            final Profile profile = Profile.getCurrentProfile();

            GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    Log.wtf("MURI", response.toString());
                    // Get facebook data from login
                    bFacebookData = getFacebookData(object);
                    Log.wtf("BFB", bFacebookData.toString());
                    displayWelcomeMessage(profile, bFacebookData);
                }
            });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender,birthday,location,posts{id}");
            request.setParameters(parameters);
            request.executeAsync();

        }
        // This is what happens if the Callback gets Cancelled for some reason
        @Override
        public void onCancel() {

        }
        // This is what happens if some Exception happens while running the Callback
        @Override
        public void onError(FacebookException error) {
            Log.wtf("ERROR", error.toString());
        }
    };

    private void displayWelcomeMessage(Profile profile, Bundle bFacebookData) {
        if (profile != null)
        {
            String email = bFacebookData.getString("email");
            String gender = bFacebookData.getString("gender");
            String location = bFacebookData.getString("location");
            String birthday = bFacebookData.getString("birthday");
            ArrayList<String> posts = bFacebookData.getStringArrayList("posts");

            final String[] user_posts = (String[]) posts.toArray(new String[posts.size()]);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    R.layout.activity_listview, user_posts);

            list.setAdapter(adapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    String url = "https://facebook.com/" + user_posts[position];

                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
                @SuppressWarnings("unused")
                public void onClick(View v){
                };
            });


            userName.setText("Hello, " + profile.getName() + "\n" +
                            "User email: " + email + "\n" +
                            "Gender: " + gender + "\n" +
                            "Location: " + location + "\n" +
                            "Birthday: " + birthday
                            );

            userImage.setVisibility(View.VISIBLE);

            Glide.with(getContext())
                    .load(profile.getProfilePictureUri(300,300))
                    .dontTransform()
                    .dontAnimate()
                    .into(userImage);

        }

    }

    // The method which handles the CallbackManager based on Result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private Bundle getFacebookData(JSONObject object) {
        Log.wtf("getFacebookData", object.toString());
        Bundle bundle = new Bundle();
        try {
            String id = object.getString("id");

            try {
                URL profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?width=200&height=150");
                Log.i("profile_pic", profile_pic + "");
                bundle.putString("profile_pic", profile_pic.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            bundle.putString("idFacebook", id);
            if (object.has("email")) {
                bundle.putString("email", object.getString("email"));
            }
            if (object.has("gender")) {
                bundle.putString("gender", object.getString("gender"));
            }
            if (object.has("birthday")) {
                bundle.putString("birthday", object.getString("birthday"));
            }
            if (object.has("location")) {
                bundle.putString("location", object.getJSONObject("location").getString("name"));
            }
            if (object.has("posts")) {
                ArrayList<String> abc = new ArrayList<>();
                ArrayList<String> final_abc = new ArrayList<>();

                JSONArray jarra = object.getJSONObject("posts").getJSONArray("data");

                for (int i = 0; i < jarra.length(); i++) {
                    abc.add(jarra.getString(i));
                }

                for (int i = 0; i < abc.size(); i++) {
                    JSONObject ppp = new JSONObject(abc.get(i));
                    final_abc.add(ppp.getString("id"));
                }

                bundle.putStringArrayList("posts", final_abc);
            }
        }

        catch (Exception ex)
        {
            Log.wtf("WTF Log:",ex);
        }

        return bundle;
    }
}
