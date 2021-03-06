package com.example.chloe.bceo.view;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chloe.bceo.R;
import com.example.chloe.bceo.exception.ImageNotLoadedException;
import com.example.chloe.bceo.fragment.FragmentBottomMenu;
import com.example.chloe.bceo.model.Product;
import com.example.chloe.bceo.model.User;
import com.example.chloe.bceo.util.HTTPGet;
import com.example.chloe.bceo.util.HTTPPost;
import com.example.chloe.bceo.util.HTTPPut;
import com.example.chloe.bceo.util.Image64Base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SellActivity extends AppCompatActivity {
    private final static int CAMERA = 66 ;
    private final static int PHOTO = 99 ;
    private Button buttonComfirm;
    private DisplayMetrics mPhone;
    private int imagePos = 0;
    ImageView image_preview;

    public User user;
    public Product p;
    String cmd;

    private EditText et_name;
    private EditText et_price;
    private EditText et_description;
    private Spinner spinner_category;
    private Spinner spinner_status;

    private Integer[] imageId = {
            R.id.imageScroll1,
            R.id.imageScroll2,
            R.id.imageScroll3,
            R.id.imageScroll4,
            R.id.imageScroll5,
            R.id.imageScroll6,
            R.id.imageScroll7,
            R.id.imageScroll8,
            R.id.imageScroll9,
            R.id.imageScroll10,
            R.id.imageScroll11,
            R.id.imageScroll12,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        buttonComfirm = (Button) this.findViewById(R.id.button);
        Button buttonCamera = (Button) findViewById(R.id.button_camera);
        Button buttonPhoto = (Button) findViewById(R.id.button_upload);

        image_preview = (ImageView) findViewById(R.id.image_product_preview);

        et_name = (EditText) findViewById(R.id.editText_name);;
        et_price = (EditText) findViewById(R.id.editText_price);
        et_description = (EditText) findViewById(R.id.editText_description);
        spinner_category = (Spinner) findViewById(R.id.spinnerCategory);
        spinner_status = (Spinner) findViewById(R.id.spinnerStatus);

        //Get user and command
        user = FragmentBottomMenu.getUser();
        cmd = (String) getIntent().getSerializableExtra("cmd");

        Log.d("[SellPage]", "user received: "+ user.getUserID());

        //If for update purpose, get a product
        if (cmd.equals("update")){
            p = (Product) getIntent().getSerializableExtra("prod");
            Log.d("[SellPage]", "Product received: id = " + p.getpName() );

            fillDefaultTextAndImage(p);
        }


        //Read smartphone resolution
        mPhone = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mPhone);

        //camera button
        buttonCamera.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent_camera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent_camera, CAMERA);
            }

        });

        buttonPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PHOTO);
            }
        });

        //Product
        buttonComfirm.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (cmd.equals("upload")){
                            try {
                                retrieveDataAndUpload();
                            } catch (ImageNotLoadedException e) {
                                Toast.makeText(SellActivity.this, "Image not loaded yet. \nPlease load the picture by clicking one photo.", Toast.LENGTH_LONG).show();
                                Log.d("[SellPage]", "Image not loaded yet!");
                                return;
                            }
                        }else{
                            //Read the new configuration
                            Log.d("[SellPage]", "Begin Configuring");
                            configureProduct();

                            HTTPPut httpPut = new HTTPPut(p);
                            httpPut.execute(p.getpID());

                            Toast.makeText(SellActivity.this, "Update Product Successfully!", Toast.LENGTH_LONG).show();
                        }
                        //Start new activity
                        Intent intent = new Intent(v.getContext(), MypageActivity.class);
                        intent.putExtra("user", user);
                        startActivityForResult(intent, 0);
                    }

                }
        );


    }

    private void retrieveDataAndUpload() throws ImageNotLoadedException{

        if (image_preview.getDrawable() == null){
            ImageNotLoadedException imageException = new ImageNotLoadedException("This is a custom exception");
            throw imageException;
        }

        BitmapDrawable bd = (BitmapDrawable) image_preview.getDrawable();
        int image_id = saveImageOnServerSide(bd.getBitmap());

        String p_name = et_name.getText().toString();

        float p_price = Float.parseFloat(et_price.getText().toString());

        String p_description = et_description.getText().toString();

        String p_category = spinner_category.getSelectedItem().toString();

        String p_status = spinner_status.getSelectedItem().toString();

        //Upload product details
        int product_id = uploadProductOnServerSide(user.getUserID(), p_name, (float) p_price, p_description, 0, image_id, user.getGroupID(), p_category, p_status);

        Toast.makeText(SellActivity.this, "Upload Product Successfully!", Toast.LENGTH_LONG).show();
    }

    private void configureProduct() {
        String p_name = et_name.getText().toString();
        float p_price = Float.parseFloat(et_price.getText().toString());
        String p_description = et_description.getText().toString();
        String p_category = spinner_category.getSelectedItem().toString();
        String p_status = spinner_status.getSelectedItem().toString();

        p.setpName(p_name);
        p.setpPrice(p_price);
        p.setpDescription(p_description);
        p.setCategory(p_category);
        p.setStatus(p_status);
    }

    private void fillDefaultTextAndImage(Product p) {
//        BitmapDrawable bd = (BitmapDrawable) image_preview.getDrawable();
//        int image_id = saveImageOnServerSide(bd.getBitmap());

        //Set name
        String p_name = p.getpName();
        et_name.setText(p_name, TextView.BufferType.EDITABLE);

        //Set price
        String p_price = Float.toString(p.getpPrice());
        et_price.setText(p_price, TextView.BufferType.EDITABLE);

        //Set description
        String p_description = p.getpDescription();
        et_description.setText(p_description, TextView.BufferType.EDITABLE);

        //Set spinner
        spinner_status.setSelection(1);
        spinner_category.setSelection(2);

        //Set image
        String image_id = Integer.toString(p.getImageId());
        HTTPGet httpGet = new HTTPGet();
        String urlStr = httpGet.buildURL("images?id=" + image_id);
        String response = httpGet.getResponse(urlStr);
        Log.d("[HTTPGet]", urlStr);
        Log.d("[HTTPGet]", response);

        Bitmap bm = Image64Base.decodeBase64(response);

        //ImageView
        image_preview.setImageBitmap(bm);

    }

    public int uploadProductOnServerSide(int user_id, String pName, float pPrice, String pDescription, int pWaiting, int imageId, int groupId, String category, String status) {
        HTTPPost httpPost = new HTTPPost();
        httpPost.uploadProduct(user_id, pName, pPrice, pDescription, pWaiting, imageId, groupId, category, status);
        return httpPost.getImage_ID();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == PHOTO && data != null)
        {

            //Get picture uri
            Uri uri = data.getData();
            ContentResolver cr = this.getContentResolver();

            try
            {
                //read bitmap
                //Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                BitmapFactory.Options mOptions = new BitmapFactory.Options();
                //Resize the image to half of size
                mOptions.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri),null,mOptions);

                //判斷照片為橫向或者為直向，並進入ScalePic判斷圖片是否要進行縮放
                if(bitmap.getWidth()>bitmap.getHeight())
                    ScalePic(bitmap, mPhone.heightPixels);
                else ScalePic(bitmap,mPhone.widthPixels);
            }
            catch (FileNotFoundException e)
            {
            }

        } else if (resultCode == RESULT_OK) {
            ImageView iv = (ImageView)findViewById(imageId[imagePos++]);

            //Extract picture from data
            Bundle extras = data.getExtras();
            //Transform data to bitmap
            Bitmap bmp = (Bitmap) extras.get("data");

            //Resize pictures
            int width = bmp.getWidth();
            int height = bmp.getHeight();

            //scale factors
            float scaleWidth = (float) 1.7;
            float scaleHeight = (float) 1.7;

            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);

            Bitmap newbm = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix,true);
            iv.setImageBitmap(newbm);

            //Save to SD card
            saveImageSDCard(newbm);

            //set onclick
            iv.setOnClickListener(new imScrollListener(imagePos-1));

        }

        //overwrite activity
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void ScalePic(Bitmap bitmap,int phone)
    {
        ImageView mImg = (ImageView)findViewById(imageId[imagePos++]);

        //scaling factor
        float mScale = 1 ;

        //如果圖片寬度大於手機寬度則進行縮放，否則直接將圖片放入ImageView內
        if(bitmap.getWidth() > phone )
        {
            //Calculate scale
            mScale = (float)phone/(float)bitmap.getWidth();

            Matrix mMat = new Matrix() ;
            mMat.setScale(mScale, mScale);

            Bitmap mScaleBitmap = Bitmap.createBitmap(bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    mMat,
                    false);
            mImg.setImageBitmap(mScaleBitmap);
        }
        else mImg.setImageBitmap(bitmap);

        //Save image to SD card
        saveImageSDCard(bitmap);

        //set onclick
        mImg.setOnClickListener(new imScrollListener(imagePos - 1));
    }

    void saveImageSDCard(Bitmap bmImage){
        String fname = "myFile" + Integer.toString(imagePos-1) + ".PNG";

        String extStorage = Environment.getExternalStorageDirectory().toString();
        File file = new File(extStorage, fname);

        try {
            OutputStream outStream = new FileOutputStream(file);
            bmImage.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
            Toast.makeText(SellActivity.this,
                    extStorage + "/" + fname,
                    Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(SellActivity.this,
                    e.toString(),
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(SellActivity.this,
                    e.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    int saveImageOnServerSide(Bitmap bm) {

        if (bm == null) {
            Toast.makeText(SellActivity.this, "Bitmap not received", Toast.LENGTH_LONG).show();
            return -1;
        }

        String str64Base = Image64Base.encodeTobase64(bm);

        Log.d("[64Base]", str64Base);

        HTTPPost post = new HTTPPost();
        post.executeImageUpload(str64Base);

        while(post.getImage_ID() == 0){}

        return post.getImage_ID();
  }

    class imScrollListener implements View.OnClickListener{
        int pos;

        public imScrollListener(int pos){
            this.pos = pos;
        }

        @Override
        public void onClick(View v) {
            loadImageSDCard();
        }

        void loadImageSDCard(){
            String extStorage = Environment.getExternalStorageDirectory().toString();
            String file = new File(extStorage, "myFile"+ Integer.toString(pos) +".PNG").toString();
            Bitmap bm = BitmapFactory.decodeFile(file);
            image_preview.setImageBitmap(bm);

            Toast.makeText(SellActivity.this,
                    extStorage+"/myFile" + Integer.toString(pos) + ".PNG",
                    Toast.LENGTH_LONG).show();
        }
    }
}