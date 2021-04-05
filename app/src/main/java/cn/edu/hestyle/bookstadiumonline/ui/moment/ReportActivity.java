package cn.edu.hestyle.bookstadiumonline.ui.moment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.edu.hestyle.bookstadiumonline.BaseActivity;
import cn.edu.hestyle.bookstadiumonline.R;
import cn.edu.hestyle.bookstadiumonline.entity.Report;
import cn.edu.hestyle.bookstadiumonline.ui.my.setting.ServerSettingActivity;
import cn.edu.hestyle.bookstadiumonline.util.OkHttpUtil;
import cn.edu.hestyle.bookstadiumonline.util.ResponseResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class ReportActivity extends BaseActivity {
    protected static final Integer RESULT_CAMERA_IMAGE = 1;
    protected static final Integer RESULT_LOAD_IMAGE = 2;
    /** 举报title的最大长度 */
    private static final Integer REPORT_TITLE_MAX_LENGTH = 50;
    /** 举报description的最大长度 */
    private static final Integer REPORT_DESCRIPTION_MAX_LENGTH = 500;
    protected EditText titleEditText;
    protected EditText descriptionEditText;
    private ImageView oneImageView;
    private ImageView oneImageDeleteImageView;
    private ImageView twoImageView;
    private ImageView twoImageDeleteImageView;
    private ImageView threeImageView;
    private ImageView threeImageDeleteImageView;
    protected Button saveButton;

    protected String uploadingFilePath;
    protected List<String> reportImagePathList;

    private Integer reportContentType;
    private Integer reportContentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        this.navigationBarInit("举报");

        Intent intent = getIntent();
        reportContentType = intent.getIntExtra("reportContentType", -1);
        if (reportContentType == -1) {
            Toast.makeText(this, "程序内部发生数据传递错误！", Toast.LENGTH_SHORT).show();
            finish();
        }
        reportContentId = intent.getIntExtra("reportContentId", -1);
        if (reportContentId == -1) {
            Toast.makeText(this, "程序内部发生数据传递错误！", Toast.LENGTH_SHORT).show();
            finish();
        }

        this.uploadingFilePath = null;
        this.reportImagePathList = new ArrayList<>();

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);

        oneImageView = findViewById(R.id.oneImageView);
        oneImageDeleteImageView = findViewById(R.id.oneImageDeleteImageView);
        oneImageDeleteImageView.setVisibility(View.INVISIBLE);

        twoImageView = findViewById(R.id.twoImageView);
        twoImageView.setVisibility(View.INVISIBLE);
        twoImageDeleteImageView = findViewById(R.id.twoImageDeleteImageView);
        twoImageDeleteImageView.setVisibility(View.INVISIBLE);

        threeImageView = findViewById(R.id.threeImageView);
        threeImageView.setVisibility(View.INVISIBLE);
        threeImageDeleteImageView = findViewById(R.id.threeImageDeleteImageView);
        threeImageDeleteImageView.setVisibility(View.INVISIBLE);

        // 上传第一张图片
        oneImageView.setOnClickListener(v -> {
            ReportActivity.this.showUploadImagePopueWindow();
        });
        // 删除第一张图片
        oneImageDeleteImageView.setOnClickListener(v -> {
            ReportActivity.this.removeImage(0);
        });
        // 上传第二张图片
        twoImageView.setOnClickListener(v -> {
            ReportActivity.this.showUploadImagePopueWindow();
        });
        // 删除第二张图片
        twoImageDeleteImageView.setOnClickListener(v -> {
            ReportActivity.this.removeImage(1);
        });
        // 上传第三张图片
        threeImageView.setOnClickListener(v -> {
            ReportActivity.this.showUploadImagePopueWindow();
        });
        // 删除第三张图片
        threeImageDeleteImageView.setOnClickListener(v -> {
            ReportActivity.this.removeImage(2);
        });

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            FormBody formBody = ReportActivity.this.checkForm();
            if (formBody != null) {
                // 提交评论
                OkHttpUtil.post(ServerSettingActivity.getServerBaseUrl() + "/report/userReport.do", null, formBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        ReportActivity.this.runOnUiThread(()->{
                            Toast.makeText(ReportActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseString = response.body().string();
                        // 转json
                        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                        Type type =  new TypeToken<ResponseResult<Void>>(){}.getType();
                        final ResponseResult<Void> responseResult = gson.fromJson(responseString, type);
                        ReportActivity.this.runOnUiThread(()->{
                            Toast.makeText(ReportActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                        if (responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                            // 保存成功，则返回
                            ReportActivity.this.finish();
                        }
                    }
                });
            }
        });
        Toast.makeText(this, "请勿恶意举报，或者随意填写信息，否则系统将对您的账号进行处罚！", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK ) {
            if (requestCode == RESULT_LOAD_IMAGE && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                uploadingFilePath = cursor.getString(columnIndex);
                cursor.close();
                Log.i("Report", "已选中图片 imagePath = " + uploadingFilePath);
                // 上传图片
                File imageFile = new File(uploadingFilePath);
                uploadImageToServer(imageFile);
            } else if (requestCode == RESULT_CAMERA_IMAGE){
                Log.i("Report", "已选中(拍照)图片 imagePath = " + uploadingFilePath);
                // 上传图片
                File imageFile = new File(uploadingFilePath);
                uploadImageToServer(imageFile);
            }
        }
    }

    /**
     * 删除照片
     * @param index     reportImagePathList下标
     */
    protected void removeImage(int index) {
        if (reportImagePathList.size() > index) {
            reportImagePathList.remove(index);
        }
        // 第三张图片的位置
        if (reportImagePathList.size() > 2) {
            Glide.with(ReportActivity.this)
                    .load(ServerSettingActivity.getServerHostUrl() + reportImagePathList.get(2))
                    .into(threeImageView);
            threeImageView.setVisibility(View.VISIBLE);
            threeImageDeleteImageView.setVisibility(View.VISIBLE);
        } else if (reportImagePathList.size() == 2) {
            // 显示上传flag
            threeImageView.setImageResource(R.drawable.ic_upload_image);
            threeImageView.setVisibility(View.VISIBLE);
            threeImageDeleteImageView.setVisibility(View.INVISIBLE);
        } else {
            // 隐藏
            threeImageView.setVisibility(View.INVISIBLE);
            threeImageDeleteImageView.setVisibility(View.INVISIBLE);
        }
        // 第二张图片的位置
        if (reportImagePathList.size() > 1) {
            Glide.with(ReportActivity.this)
                    .load(ServerSettingActivity.getServerHostUrl() + reportImagePathList.get(1))
                    .into(twoImageView);
            twoImageView.setVisibility(View.VISIBLE);
            twoImageDeleteImageView.setVisibility(View.VISIBLE);
        } else if (reportImagePathList.size() == 1) {
            // 显示上传flag
            twoImageView.setImageResource(R.drawable.ic_upload_image);
            twoImageView.setVisibility(View.VISIBLE);
            twoImageDeleteImageView.setVisibility(View.INVISIBLE);
        } else {
            // 隐藏
            twoImageView.setVisibility(View.INVISIBLE);
            twoImageDeleteImageView.setVisibility(View.INVISIBLE);
        }
        // 第一张图片的位置
        if (reportImagePathList.size() > 0) {
            Glide.with(ReportActivity.this)
                    .load(ServerSettingActivity.getServerHostUrl() + reportImagePathList.get(0))
                    .into(oneImageView);
            oneImageDeleteImageView.setVisibility(View.VISIBLE);
        } else {
            // 显示上传flag
            oneImageView.setImageResource(R.drawable.ic_upload_image);
            oneImageView.setVisibility(View.VISIBLE);
            oneImageDeleteImageView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 成功上传完一张图片
     * @param imagePath     imagePath
     */
    protected void uploadImage(String imagePath) {
        if (reportImagePathList.size() == 2) {
            // 将上传的图片显示至threeImageView
            Glide.with(ReportActivity.this)
                    .load(ServerSettingActivity.getServerHostUrl() + imagePath)
                    .into(threeImageView);
            threeImageDeleteImageView.setVisibility(View.VISIBLE);
        } else if (reportImagePathList.size() == 1) {
            // 将上传的图片显示至twoImageView
            Glide.with(ReportActivity.this)
                    .load(ServerSettingActivity.getServerHostUrl() + imagePath)
                    .into(twoImageView);
            twoImageDeleteImageView.setVisibility(View.VISIBLE);
            // threeImageView显示上传按钮
            threeImageView.setImageResource(R.drawable.ic_upload_image);
            threeImageView.setVisibility(View.VISIBLE);
            threeImageDeleteImageView.setVisibility(View.INVISIBLE);
        } else if (reportImagePathList.size() == 0) {
            // 将上传的图片显示至oneImageView
            Glide.with(ReportActivity.this)
                    .load(ServerSettingActivity.getServerHostUrl() + imagePath)
                    .into(oneImageView);
            oneImageDeleteImageView.setVisibility(View.VISIBLE);
            // twoImageView显示上传按钮
            twoImageView.setImageResource(R.drawable.ic_upload_image);
            twoImageView.setVisibility(View.VISIBLE);
            twoImageDeleteImageView.setVisibility(View.INVISIBLE);
        }
        reportImagePathList.add(imagePath);
    }

    /**
     * 将图片上传至服务器
     * @param imageFile     待上传图片文件
     */
    protected void uploadImageToServer(File imageFile) {
        OkHttpUtil.uploadFile(ServerSettingActivity.getServerBaseUrl() + "/report/uploadImage.do", imageFile, OkHttpUtil.MEDIA_TYPE_JPG, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                ReportActivity.this.runOnUiThread(()->{
                    Toast.makeText(ReportActivity.this, "网络访问失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                // 转json
                Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
                Type type =  new TypeToken<ResponseResult<String>>(){}.getType();
                final ResponseResult<String> responseResult = gson.fromJson(responseString, type);
                if (!responseResult.getCode().equals(ResponseResult.SUCCESS)) {
                    ReportActivity.this.runOnUiThread(()->{
                        Toast.makeText(ReportActivity.this, responseResult.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                String imagePath = responseResult.getData();
                Log.i("ReportActivity", "文件上传成功！imagePath = " + responseResult.getData() + "");
                ReportActivity.this.runOnUiThread(()->{
                    ReportActivity.this.uploadImage(imagePath);
                });
            }
        });
    }

    /**
     * 检查表单
     * @return      FormBody
     */
    protected FormBody checkForm() {
        // 检查title
        String title = titleEditText.getText().toString();
        if (title.length() == 0) {
            Toast.makeText(this, "请输入倨傲标题！", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (title.length() > REPORT_TITLE_MAX_LENGTH) {
            Toast.makeText(this, "举报标题超过了 " + REPORT_TITLE_MAX_LENGTH + " 个字符！", Toast.LENGTH_SHORT).show();
            return null;
        }
        // 检查description
        String description = descriptionEditText.getText().toString();
        if (description.length() == 0) {
            Toast.makeText(this, "请输入举报描述！", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (description.length() > REPORT_DESCRIPTION_MAX_LENGTH) {
            Toast.makeText(this, "举报描述超过了 " + REPORT_DESCRIPTION_MAX_LENGTH + " 个字符！", Toast.LENGTH_SHORT).show();
            return null;
        }
        // 检查imagePaths
        StringBuilder imagePaths = new StringBuilder();
        for (String imagePath : reportImagePathList) {
            if (imagePaths.length() != 0) {
                imagePaths.append(",");
            }
            imagePaths.append(imagePath);
        }
        Report report = new Report();
        report.setReportContentType(reportContentType);
        report.setReportContentId(reportContentId);
        report.setTitle(title);
        report.setDescription(description);
        report.setImagePaths(imagePaths.toString());
        // 转json
        Gson gson = new GsonBuilder().setDateFormat(ResponseResult.DATETIME_FORMAT).create();
        String reportData = gson.toJson(report);
        return new FormBody.Builder()
                .add("reportData", reportData)
                .build();
    }

    /**
     * 选择上传图片弹窗
     */
    protected void showUploadImagePopueWindow() {
        View popView = View.inflate(this, R.layout.popue_window_upload_image,null);
        TextView bt_album = popView.findViewById(R.id.btn_pop_album);
        TextView bt_camera = popView.findViewById(R.id.btn_pop_camera);
        TextView bt_cancel = popView.findViewById(R.id.btn_pop_cancel);
        // 获取屏幕宽高
        int weight = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels / 3;

        final PopupWindow popupWindow = new PopupWindow(popView, weight, height);
        popupWindow.setAnimationStyle(R.style.MaterialAlertDialog_MaterialComponents_Title_Panel);
        popupWindow.setFocusable(true);
        // 点击外部popueWindow消失
        popupWindow.setOutsideTouchable(true);
        bt_camera.setOnClickListener(v -> {
            takeCamera(RESULT_CAMERA_IMAGE);
            popupWindow.dismiss();
        });
        bt_album.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
            popupWindow.dismiss();
        });
        bt_cancel.setOnClickListener(v -> popupWindow.dismiss());
        // popupWindow消失屏幕变为不透明
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = 1.0f;
            getWindow().setAttributes(lp);
        });
        // popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.BOTTOM,0,30);
    }

    /**
     * 调用相机，拍照
     * @param num
     */
    protected void takeCamera(int num) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            photoFile = createImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, num);//跳转界面传回拍照所得数据
            } else {
                Toast.makeText(getApplicationContext(), "发生未知错误！", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "相机不可用！", Toast.LENGTH_SHORT).show();
        }
    }

    protected File createImageFile() {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = null;
        try {
            image = File.createTempFile(
                    generateFileName(),  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        uploadingFilePath = image.getAbsolutePath();
        return image;
    }

    public static String generateFileName() {
        String timeStamp = new SimpleDateFormat(ResponseResult.DATETIME_FORMAT).format(new Date());
        return "JPEG_" + timeStamp + "_";
    }

    /**
     * 设置navigationBar
     */
    protected void navigationBarInit(String title) {
        // 设置title
        TextView titleTextView = this.findViewById(R.id.titleTextView);
        titleTextView.setText(String.format("%s", title));
        // 设置返回
        TextView backTitleTextView = this.findViewById(R.id.backTextView);
        backTitleTextView.setOnClickListener(v -> finish());
    }
}