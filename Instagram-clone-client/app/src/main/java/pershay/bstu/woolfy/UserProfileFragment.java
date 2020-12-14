package pershay.bstu.woolfy;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;

import pershay.bstu.woolfy.login.LoginActivity;
import pershay.bstu.woolfy.models.Post;
import pershay.bstu.woolfy.models.UserData;
import pershay.bstu.woolfy.profile.EditProfileActivity;
import pershay.bstu.woolfy.profile.ProfileActivity;
import pershay.bstu.woolfy.profile.ProfileFragment;
import pershay.bstu.woolfy.retrofit.APIClient;
import pershay.bstu.woolfy.retrofit.JsonPlaceHolderApi;
import pershay.bstu.woolfy.utils.BottomNavHelper;
import pershay.bstu.woolfy.utils.ProfileGridImageAdapter;
import pershay.bstu.woolfy.utils.SqliteHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileFragment extends Fragment {

    public interface OnGridImageSelectedListener{
        void onGridImageSelected(Post post, UserData userData, int activityNumber);
    }

    private UserProfileFragment.OnGridImageSelectedListener mOnGridImageSelectedListener;
    private JsonPlaceHolderApi jsonPlaceHolderApi;

    private static final int ACTIVITY_NUM = 4;
    private Context mContext;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private TextView tvUserName, tvDisplayName, tvDescription, tvAge, tvCity, tvFolowers, tvFollowing, tvPosts, tvEditProfile, tvLogout;

    private static String TAG = "ProfileActivity";
    private ImageView profilePhoto;
    private ProgressBar mProgressBar;
    private GridView gridView;

    String userId, username, firstName, lastName;

    private UserData userData;

    private SqliteHelper helper;

    private static final int NUM_GRID_COLUMNS = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        mContext = getActivity();

        if(helper == null)
            helper = new SqliteHelper(getContext());

        toolbar = view.findViewById(R.id.profileToolBar);
        gridView = view.findViewById(R.id.gridView);
        mProgressBar = view.findViewById(R.id.profileProgresBar);
        profilePhoto = view.findViewById(R.id.profileImage);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvAge = view.findViewById(R.id.tvAge);
        tvCity = view.findViewById(R.id.tvCity);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvDisplayName = view.findViewById(R.id.tvDisplayName);
        tvLogout = view.findViewById(R.id.tvLogout);
        tvLogout.setVisibility(View.INVISIBLE);
        jsonPlaceHolderApi = APIClient.getClient().create(JsonPlaceHolderApi.class);

        getUserId();

        tvUserName.setText(username);

        setUpToolbar();
        downloadImage();
        getUserData();

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.deleteData();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finishAffinity();
            }
        });

        return view;
    }

    public void getUserId(){
        userId = getActivity().getIntent().getExtras().getString("userId");
        username = getActivity().getIntent().getExtras().getString("userName");
    }

    private void downloadImage(){
        Call<List<Post>> call = jsonPlaceHolderApi.downloadImage(userId);
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                try{
                    setupGridView(response.body());
                    tvPosts.setText(String.valueOf(response.body().size()));
                }
                catch(Exception ex){
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
                Toast.makeText(mContext, "Response: "+response.code(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Toast.makeText(mContext, "error : "+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserData(){
        Call<UserData> call = jsonPlaceHolderApi.getUserDataById(userId);
        call.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(mContext, "Code: "+response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                userData = response.body();

                tvAge.setText(userData.getAge() + " y.o.");
                tvCity.setText(userData.getCity());
                tvDescription.setText(userData.getDescription());
                tvDisplayName.setText(userData.getFirstname() + " " + userData.getLastname());
                firstName = userData.getFirstname();
                lastName = userData.getLastname();
            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {
                Toast.makeText(mContext, "Error : "+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpToolbar(){
        ((UserProfileActivity)getActivity()).setSupportActionBar(toolbar);
    }

    private void setupGridView(final List<Post> posts){

        final ArrayList<String> photos = new ArrayList<>();

        for (Post url : posts) {
            photos.add(url.getImage());
        }

        profilePhoto.setImageBitmap(StringToImage(photos.get(photos.size() - 1)));
        mProgressBar.setVisibility(View.INVISIBLE);

        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        ProfileGridImageAdapter adapter = new ProfileGridImageAdapter(getActivity(),R.layout.layout_grid_imageview,
                "", photos);
        gridView.setAdapter(adapter);
    }

    private Bitmap StringToImage(String image){
        try{
            byte[] encodeByte = Base64.decode(image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }

}
