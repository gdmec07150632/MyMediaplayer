package cn.edu.gdmec.s07150632.mymediaplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Vector;

/**
 * Created by Administrator on 2016/11/17.
 */
public class MyFileActivity extends Activity{
    //支持的媒体格式
    private final String[]FILE_MapTable = {
            ".3gp",".mov",".avi",".rmvb",".wmv",".mp3",".mp4"};
    private Vector<String> items = null;        //items:存放显示的名称
    private Vector<String> paths = null;        //paths:存放文件路径
    private Vector<String> sizes = null;        //sizes:文件大小
    private String rootPath = "/mnt/sdcard";   //起始文件夹
    private EditText pathEditText;              //路径
    private Button queryButton;                 //查询按钮
    private ListView fileListView;              //文件列表

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.setTitle("多媒体文件浏览");
        setContentView(R.layout.activity_my_file);
        //从myfile.xml找到对应的元素
        pathEditText = (EditText) findViewById(R.id.path_edit);
        queryButton = (Button) findViewById(R.id.qry_button);
        fileListView = (ListView) findViewById(R.id.file_listview);
        //查询按钮事件
        queryButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                File file = new File(pathEditText.getText().toString());
                if (file.exists()){
                    if (file.isFile()){
                        //如果是媒体文件直接打开播放
                        openFile(pathEditText.getText().toString());
                    }else {
                        //如果是目录打开目录下的文件
                        getFileDir(pathEditText.getText().toString());
                    }
                }else {
                    Toast.makeText(MyFileActivity.this,"找不到该位置，请确定位置是否正确！",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        //设置ListITem被点击时要做的动作
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fileOrDir(paths.get(position));
            }
        });
        //打开默认文件夹
        getFileDir(rootPath);
    }

    //重写返回键功能；返回上一级文件夹
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //是否触发按键为back键
        if (keyCode==KeyEvent.KEYCODE_BACK){
            pathEditText = (EditText) findViewById(R.id.path_edit);
            File file = new File(pathEditText.getText().toString());
            if (rootPath.equals(pathEditText.getText().toString().trim())){
                return super.onKeyDown(keyCode, event);
            }else {
                getFileDir(file.getParent());
                return true;
            }
            //如果不是back键正常响应
        }else{
            return super.onKeyDown(keyCode, event);
        }

    }
    //处理文件或者目录的方法
    private void fileOrDir(String path){
        File file = new File(path);
        if (file.isDirectory()){
            getFileDir(file.getPath());
        }else {
            openFile(path);
        }
    }
    //取得文件结构的方法
    private void getFileDir(String filePath){
        //设置目前所在路径
        pathEditText.setText(filePath);
        items = new Vector<String>();
        paths = new Vector<String>();
        sizes = new Vector<String>();
        File f = new File(filePath);
        File[] files = f.listFiles();
        if (files != null){
            //将所有文件添加到ArrayList中
            for (int i=0;i < files.length;i++){
                if (files[i].isDirectory()){
                    items.add(files[i].getName());
                    paths.add(files[i].getPath());
                    sizes.add("");
                }
            }
            for (int i=0; i < files.length; i++){
                if (files[i].isFile()){
                    String fileName = files[i].getName();
                    int index = fileName.lastIndexOf(".");
                    if (index > 0){
                        String endName = fileName.substring(index,
                                fileName.length()).toLowerCase();
                        String type = null;
                        for (int x =0 ;x < FILE_MapTable.length; x++){
                            //支持的格式，才会在文件浏览器中显示
                            if (endName.equals(FILE_MapTable[x])){
                                type = FILE_MapTable[x];
                                break;
                            }
                        }
                        if (type != null){
                            items.add(files[i].getName());
                            paths.add(files[i].getPath());
                            sizes.add(files[i].length()+"");
                        }
                    }
                }
            }
        }
        //使用自定义的FileListAdapter将数据传入ListView
        fileListView.setAdapter(new FileListAdapter(this,items));
    }
    //打开媒体文件
    private void openFile(String path){
        //打开媒体播放器
        Intent intent = new Intent(MyFileActivity.this,MainActivity.class);
        intent.putExtra("path",path);
        startActivity(intent);
        finish();
    }
    //ListView列表适配器
    class FileListAdapter extends BaseAdapter{
        private Vector<String> items =null;  //items：存放显示的名称
        private MyFileActivity myFile;
        public FileListAdapter(MyFileActivity myFile,Vector<String> items){
            this.items = items;
            this.myFile = myFile;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.elementAt(position);
        }

        @Override
        public long getItemId(int position) {
            return items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null){
                //加载到列表项布局file_item.xml
                convertView = myFile.getLayoutInflater().inflate(R.layout.file_item,null);
            }
            //文件名称
            TextView name = (TextView) convertView.findViewById(R.id.name);
            //媒体文件类型
            ImageView music = (ImageView) convertView.findViewById(R.id.music);
            //文件夹类型
            ImageView folder = (ImageView) convertView.findViewById(R.id.folder);
            name.setText(items.elementAt(position));
            if (sizes.elementAt(position).equals("")){
                //隐藏媒体图标，显示文件夹图标
                music.setVisibility(View.GONE);
                folder.setVisibility(View.VISIBLE);
            }else {
                //隐藏文件夹图标，显示媒体图标
                folder.setVisibility(View.GONE);
                music.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }




}
